# Self Review — Product Catalog API (Week 7)

## Completed

- [x] Security contract and endpoint/role matrix before implementation
- [x] PostgreSQL users, roles, mapping table, and case-insensitive identity lookup
- [x] BCrypt-only password persistence and environment-only development seeding
- [x] Stateless HTTP Basic authentication with generic 401 behavior
- [x] VIEWER, EDITOR, and ADMIN request authorization with restrictive fallback
- [x] Method security on product deletion and ADMIN user-management operations
- [x] ADMIN user creation and enable/disable endpoints with self-disable protection
- [x] Common trace-aware application, authentication, and authorization errors
- [x] Request trace propagation, MDC cleanup, safe completion logs, and domain events
- [x] Debugging notes and authentic RED→GREEN evidence
- [x] PostgreSQL-backed security/API integration coverage and secured usage examples

## Important security decisions

- Only `GET /api/info` and `GET /actuator/health` are public. Every unlisted
  route requires authentication.
- Canonical database roles are converted to Spring `ROLE_` authorities in one
  adapter, keeping persistence vocabulary separate from framework conventions.
- Authentication failures do not reveal whether a username exists or an account
  is disabled.
- Passwords are encoded before persistence and excluded from every response DTO.
  Development sample passwords exist only in process environment variables.
- ADMIN rules are enforced at the request boundary; sensitive service methods
  add method security for defense in depth.
- Trace IDs are canonical UUIDs, placed in MDC only for the request lifetime, and
  returned in response headers and error bodies.
- Normal failures log bounded identifiers and outcomes. Authorization headers,
  request credentials/bodies, password hashes, SQL failure details, and normal
  4xx stack traces are excluded.

## Problems found and diagnosis

- The initial Spring Security dependency made old product tests return 401.
  Existing behavior tests were given explicit principals while dedicated
  security tests retained anonymous boundaries.
- A temporary deny-all identity bean remained beside the database adapter and
  prevented the valid authentication provider from wiring. Removing the
  temporary seam made enabled database users authenticate.
- The public-info MVC slice did not import the application security/error
  components. The full suite exposed the missing bean chain; the slice now runs
  the real public route contract.
- The concurrent stock test supplied no version, making its 409 race timing
  dependent. It now sends the persisted starting version, so either overlapping
  or serialized requests deterministically reject one stale update.
- The completion filter originally logged downstream exceptions as status 200.
  A focused RED test proved it; the filter now records an effective 500 while
  preserving exception propagation and MDC cleanup.
- Hibernate printed constraint names and a complete failing database row.
  Captured-output RED tests exposed this and the narrow SQL-exception logger is
  disabled, without suppressing application failure events.
- Docker Desktop stopped between runs. The first full attempt failed only during
  Testcontainers setup; after Docker restarted, the same suite reached behavior
  assertions and then passed after the corrections above.

## Feedback and process applied

- Work stayed on the dedicated `week7-security-logging` branch.
- Each capability received an approved spec, ordered plan, and independently
  verifiable task before production behavior changed.
- Behavior changes followed focused RED→GREEN cycles; no failing assertion was
  deleted or weakened to obtain a green build.
- Tests use public HTTP and PostgreSQL boundaries for security behavior, with
  focused unit tests retained for small adapters and MDC lifecycle rules.
- No dependency, Java/Spring version, commit, push, or pull request was added
  outside the user authorization boundary.

## Verification

```text
./mvnw clean verify
BUILD SUCCESS
157 tests, 0 failures, 0 errors, 0 skipped
Executable JAR: target/product-catalog-1.0.0-SNAPSHOT.jar
Compiler: javac --release 21
Runtime: OpenJDK 21.0.12.1 (Homebrew)
```

Focused security, authorization, ADMIN API, observability, rollback, and
log-safety suites also passed. Final source/report scans and complete diff review
are recorded in the Week 7 handoff.

## Known limitations and future improvements

- HTTP Basic requires TLS in deployment; TLS is not configured by this PoC.
- Rate limiting, account lockout, password rotation/reset, MFA, persistent audit
  storage, and external secret management are not implemented.
- JWT is intentionally deferred until the required Week 7 HTTP Basic exercises
  are accepted; an external identity provider is preferable for production.
- Commit, push, and pull-request creation have not been performed and still
  require explicit user authorization.
