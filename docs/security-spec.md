# Security Contract Specification

**Status:** Approved on 2026-09-09
**Module id:** `security-contract`
**Capability map:** [`WEEK7_CAPABILITY_MAP.md`](../WEEK7_CAPABILITY_MAP.md)
**Baseline:** Week 6 PostgreSQL Product Catalog
**Requirements source:** Week 7 Spring Security, logging, debugging, and error-handling exercise brief

## Source and precedence

The Week 7 brief is requirements input. This approved document is the
authoritative security contract and remains subordinate to current user
instructions. Existing Week 6 product behavior must be preserved unless an
approved Week 7 requirement explicitly changes access to it.

## Objective

Define an explicit, testable security boundary for the Product Catalog before
security code is introduced. Success means every endpoint has an unambiguous
authentication and authorization rule, 401 and 403 behavior is distinguishable,
disabled users cannot authenticate, sensitive data is protected, and the first
implementation slice can begin with a recorded failing HTTP test.

## Assumptions

1. Week 7 starts from `week6-exercise-6-docs-delivery` and retains Java 21,
   Spring Boot 3.4.x, PostgreSQL, Flyway, JPA, and Testcontainers.
2. Required authentication is HTTP Basic. JWT is excluded until all required
   Week 7 work is complete and verified.
3. The API is a stateless, non-browser learning API. No authenticated HTTP
   session is retained between requests.
4. CSRF protection is disabled for the stateless HTTP Basic API. If a browser
   client or cookie/session authentication is introduced, this decision must be
   revisited before implementation.
5. Database role names are `VIEWER`, `EDITOR`, and `ADMIN`; Spring Security maps
   them to `ROLE_VIEWER`, `ROLE_EDITOR`, and `ROLE_ADMIN` authorities.
6. `/actuator/info`, OpenAPI documentation, and Swagger UI are not public. Any
   authenticated role may access them unless a later approved contract narrows
   that access.
7. Usernames are looked up case-insensitively. Unknown users and invalid
   passwords produce the same client-visible 401 response.

## Technology and commands

Planned additions are Spring Security 6.x through
`spring-boot-starter-security` and `spring-security-test`. They are authorized
only after this specification and its plan/tasks are approved.

```bash
# Focused security contract test during implementation
./mvnw -Dtest=SecurityAccessIntegrationTest test

# Full regression suite
./mvnw clean verify

# Local PostgreSQL-backed application
docker compose up -d
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Project structure

```text
src/main/java/com/codewalnut/productcatalog/
  config/          SecurityFilterChain and method-security activation
  security/        authentication, entry points, denied handlers, principals
  controller/      HTTP adapters only; no manual role checks
  service/         business operations and selected defense-in-depth annotations
  dto/             request/response and shared error payloads
src/test/java/com/codewalnut/productcatalog/security/
                   focused HTTP security tests
docs/security-spec.md
                   canonical Week 7 role and endpoint policy
docs/tdd-evidence.md
                   actual RED → GREEN evidence captured during implementation
```

Packages are created only when their approved implementation task begins.

## Roles

| Role | Purpose |
| --- | --- |
| `VIEWER` | Read product information only |
| `EDITOR` | Read and modify products except deletion |
| `ADMIN` | Full product access and user administration |

Roles are cumulative by policy: ADMIN receives every listed permission, EDITOR
receives product read/write permissions, and VIEWER receives product read
permissions. This does not require a Java role hierarchy; each endpoint may list
its accepted roles explicitly.

## Endpoint access matrix

| Method | Path | Public | VIEWER | EDITOR | ADMIN |
| --- | --- | ---: | ---: | ---: | ---: |
| GET | `/api/info` | Allow | Allow | Allow | Allow |
| GET | `/actuator/health` | Allow | Allow | Allow | Allow |
| GET | `/api/products` | 401 | Allow | Allow | Allow |
| GET | `/api/products/{id}` | 401 | Allow | Allow | Allow |
| GET | `/api/products/low-stock` | 401 | Allow | Allow | Allow |
| POST | `/api/products` | 401 | 403 | Allow | Allow |
| PUT | `/api/products/{id}` | 401 | 403 | Allow | Allow |
| PATCH | `/api/products/{id}/stock` | 401 | 403 | Allow | Allow |
| DELETE | `/api/products/{id}` | 401 | 403 | 403 | Allow |
| POST | `/api/admin/users` | 401 | 403 | 403 | Allow |
| PATCH | `/api/admin/users/{username}/enabled` | 401 | 403 | 403 | Allow |

Only `/api/info` and `/actuator/health` are public. Every other request is
authenticated by default. An unauthenticated request to a protected but
otherwise unknown endpoint therefore returns 401; an authenticated request may
continue to normal routing and return 404.

## Requirements

### REQ-110: Security roles

The application shall recognize exactly the application roles VIEWER, EDITOR,
and ADMIN for the required exercise. Controllers shall not inspect roles or the
security context manually.

### REQ-111: Public endpoints

`GET /api/info` and `GET /actuator/health` shall be accessible without
authentication. No other endpoint is public unless a later approved contract
adds it explicitly.

### REQ-112: Product read access

VIEWER, EDITOR, and ADMIN may list, retrieve, and query low-stock products.
Missing or invalid authentication receives 401.

### REQ-113: Product write access

EDITOR and ADMIN may create, replace, and adjust stock. VIEWER receives 403 for
these operations.

### REQ-114: Administrative access

Only ADMIN may delete products or create, enable, or disable application users.
VIEWER and EDITOR receive 403.

### REQ-115: Restrictive default

Any request not explicitly declared public shall require authentication.
Actuator endpoints other than health, OpenAPI endpoints, and Swagger UI must
never become public through a broad matcher.

### REQ-116: Authentication and authorization status

Missing, invalid, unknown-user, and disabled-user authentication shall return
401. A successfully authenticated principal lacking authority shall receive
403. Authentication failures shall not reveal whether a username exists.

### REQ-117: Disabled users

A disabled user shall fail every subsequent HTTP Basic authentication attempt
with 401, including immediately after an administrator disables the account.

### REQ-118: Password storage and handling

Only BCrypt password hashes may be persisted. Plain-text passwords exist only
as transient request input and must never be returned, logged, or committed.
Development sample-user passwords come from environment variables or other
uncommitted development configuration.

### REQ-119: Sensitive-data boundary

Responses and logs shall never expose passwords, password hashes,
`Authorization` headers, session values, database credentials, SQL details, or
complete user-creation request bodies. Normal 4xx responses shall not log stack
traces.

### REQ-120: Test-first security delivery

Security behavior shall be implemented one access rule at a time. The first
production change must be preceded by a failing HTTP test proving that an
unauthenticated request to `GET /api/products` receives 401. Actual RED and
GREEN commands and outputs must be recorded in `docs/tdd-evidence.md`.

## Error contract

Security-generated 401 and 403 responses shall use the same JSON envelope as
application errors:

```json
{
  "timestamp": "2026-09-09T00:00:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication is required",
  "path": "/api/products",
  "traceId": "<request trace identifier>",
  "fieldErrors": []
}
```

The 401 response includes the normal HTTP Basic challenge header. Messages are
generic and do not distinguish missing credentials, invalid passwords, unknown
usernames, or disabled accounts. The 403 message states that access is denied
without disclosing internal matcher or authority details.

## Code conventions

Use the Spring Security 6 lambda DSL and a `SecurityFilterChain`. Do not use the
removed `WebSecurityConfigurerAdapter` and do not encode authorization as
controller `if` statements.

```java
http.authorizeHttpRequests(authorize -> authorize
        .requestMatchers(publicEndpoints).permitAll()
        .requestMatchers(HttpMethod.GET, productEndpoints).hasAnyRole("VIEWER", "EDITOR", "ADMIN")
        .anyRequest().authenticated());
```

The final implementation may split matchers into clearly named constants or
methods, but the restrictive `anyRequest().authenticated()` rule remains last.

## Testing strategy and checklist

Security rules are observed through MockMvc at the public HTTP boundary.
Database-backed identity tests use PostgreSQL Testcontainers. Full integration
tests do not mock controllers, services, repositories, or security components.

| Scenario | Expected evidence |
| --- | --- |
| Public success | `/api/info` and `/actuator/health` return 200 without credentials |
| Missing authentication | Protected product and admin endpoints return 401 |
| Invalid credentials | Wrong password and unknown username return indistinguishable 401 envelopes |
| VIEWER success/denial | Product reads succeed; product writes, delete, and admin operations return 403 |
| EDITOR success/denial | Product reads/writes succeed; delete and admin operations return 403 |
| ADMIN success | Product and user-management operations pass authorization |
| Disabled user | Correct credentials for a disabled user return 401 |
| Restrictive default | A newly introduced or otherwise unlisted endpoint is not public |
| Sensitive output | Responses and captured logs omit passwords, hashes, credentials, and SQL details |

Tests use Given-When-Then names and Arrange/Act/Assert structure. At least one
focused failing access test is added and run before each role's minimum
configuration change.

## Acceptance criteria

### AC-110: Public access

**Given** no credentials, **when** `/api/info` or `/actuator/health` is requested,
**then** the response is successful and no generated development password is
written to application logs.

### AC-111: Missing authentication

**Given** no credentials, **when** `GET /api/products` is requested, **then** the
response is 401 with a generic security error envelope and HTTP Basic challenge.

### AC-112: Invalid authentication

**Given** a wrong password, unknown username, or disabled account, **when** a
protected endpoint is requested, **then** each response is the same client-
visible 401 shape and does not reveal account existence.

### AC-113: VIEWER permissions

**Given** an authenticated VIEWER, **when** product reads are requested, **then**
they pass authorization; **when** create, update, stock adjustment, delete, or
user-management is requested, **then** each is rejected with 403.

### AC-114: EDITOR permissions

**Given** an authenticated EDITOR, **when** product read, create, update, or stock
adjustment is requested, **then** authorization succeeds; **when** product delete
or user management is requested, **then** it is rejected with 403.

### AC-115: ADMIN permissions

**Given** an authenticated ADMIN, **when** any required product or user-
management endpoint is requested, **then** authorization permits the request to
continue to application validation and business behavior.

### AC-116: Restrictive default

**Given** no credentials, **when** `/actuator/info`, Swagger UI, OpenAPI JSON, or
an unlisted endpoint is requested, **then** it is not publicly accessible.

### AC-117: Authentication versus authorization

**Given** two otherwise equivalent requests, **when** one has no valid principal
and the other has an authenticated principal without permission, **then** the
first returns 401 and the second returns 403.

### AC-118: Password non-disclosure

**Given** any authentication or user-management outcome, **when** its response
and captured logs are inspected, **then** neither the submitted password nor a
stored password hash appears.

### AC-119: Controller boundary

**Given** the implemented access rules, **when** controllers are inspected,
**then** they contain no manual authentication, authorization, logging, or
database logic.

### AC-120: First recorded TDD cycle

**Given** the Week 6 baseline without Spring Security, **when** the focused
unauthenticated product-list test is run, **then** it fails because the endpoint
is still public; **when** the minimum approved security configuration is added,
**then** the same test passes and both outputs are recorded.

## Boundaries

Always:

- Preserve existing product behavior after a request passes authorization.
- Use HTTP-boundary tests for authentication and authorization behavior.
- Keep secure defaults and record real RED → GREEN evidence.
- Use constructor injection and the existing shared error envelope.

Ask first:

- Change authentication away from HTTP Basic.
- Make any additional endpoint public.
- Change the approved role matrix or 401/403 semantics.
- Add dependencies not listed in the Week 7 exercise.

Never:

- Commit credentials or plain-text sample passwords.
- Log passwords, password hashes, authorization headers, database credentials,
  session values, or complete sensitive request bodies.
- Put role-checking `if` statements in controllers.
- Add JWT before all required Week 7 acceptance criteria pass.

## Out of scope for this module

- Flyway table definitions and JPA user entities (`database-authentication`)
- Concrete role matcher implementation (`role-authorization`)
- Admin request/response schemas (`admin-user-management`)
- Trace-ID validity rules and logging format (`request-observability`)
- Final debugging notes, complete security integration suite, and PR evidence
  (`security-verification-delivery`)

## Approved decisions

1. Use stateless HTTP Basic and disable CSRF for this non-browser learning API.
2. Store role names without `ROLE_` and map them to Spring's role authorities.
3. Permit any authenticated role to use `/actuator/info`, Swagger UI, and
   OpenAPI endpoints while keeping them unavailable anonymously.
4. Return the shared JSON error envelope for 401 and 403, including a trace ID;
   retain the HTTP Basic challenge header on 401.

## Approval

- [x] Assumptions reviewed
- [x] Endpoint access matrix approved
- [x] Requirements and acceptance criteria approved
- [x] Proposed decisions resolved
- [x] **`security-contract` approved on 2026-09-09 — planning may begin**
