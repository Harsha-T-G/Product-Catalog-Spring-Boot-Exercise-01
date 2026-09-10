# Week 7 TDD Evidence

Evidence in this file records commands actually run on the
`week7-security-logging` branch. Output excerpts omit routine framework logs.

## Environment

- Initial date: 2026-09-09
- Required release target: Java 21
- Initial development runtime: Java 26.0.1 (`javac --release 21` via Maven)
- Final verification date/runtime: 2026-09-10, OpenJDK 21.0.12.1 (Homebrew)
- Docker Desktop: 29.7.2
- Test database: PostgreSQL 16 via Testcontainers 1.21.3

## Week 6 baseline

```text
Command: ./mvnw clean verify
Exit: 0
Result: BUILD SUCCESS
Tests: 93 run, 0 failures, 0 errors, 0 skipped
```

## TASK-W7-001 — Unauthenticated product access

### RED

The test was added before Spring Security dependencies or production
configuration.

```text
Command: ./mvnw -Dtest=SecurityAccessIntegrationTest#givenNoCredentials_whenGetProducts_thenReturns401 test
Exit: 1
Test: SecurityAccessIntegrationTest.givenNoCredentials_whenGetProducts_thenReturns401
Observed: Status expected:<401> but was:<200>
Tests: 1 run, 1 failure, 0 errors, 0 skipped
```

### Minimum GREEN

```text
Command: ./mvnw -Dtest=RoleAuthorizationIntegrationTest#givenAdmin_whenCreatingProduct_thenReturns201 test
Exit: 0
Tests: 1 run, 0 failures, 0 errors, 0 skipped
```

### Expanded matrix RED

```text
Command: ./mvnw -Dtest=RoleAuthorizationIntegrationTest test
Exit: 1
Observed: ADMIN product read expected HTTP 200 but received 403
Tests: 13 run, 1 failure, 0 errors, 0 skipped
```

The other 12 VIEWER, EDITOR, ADMIN-route, operational, and fallback checks
passed against the interim policy.

### GREEN

```text
Command: ./mvnw -Dtest=RoleAuthorizationIntegrationTest#givenEditor_whenDeletingProduct_thenReturns403 test
Exit: 0
Tests: 1 run, 0 failures, 0 errors, 0 skipped
```

## TASK-W7-013 — ADMIN and operational authorization

### RED

```text
Command: ./mvnw -Dtest=RoleAuthorizationIntegrationTest#givenAdmin_whenCreatingProduct_thenReturns201 test
Exit: 1
Observed: interim EDITOR-only create matcher returned HTTP 403 to ADMIN
Tests: 1 run, 1 failure, 0 errors, 0 skipped
```

### GREEN

```text
Minimum command: ./mvnw -Dtest=RoleAuthorizationIntegrationTest#givenAdmin_whenCreatingProduct_thenReturns201 test
Exit: 0
Tests: 1 run, 0 failures, 0 errors, 0 skipped

Expanded RED command: ./mvnw -Dtest=RoleAuthorizationIntegrationTest test
Exit: 1
Observed: 12 scenarios passed; ADMIN product read expected 200 but received 403

Expanded GREEN command: ./mvnw -Dtest=RoleAuthorizationIntegrationTest test
Exit: 0
Tests: 13 run, 0 failures, 0 errors, 0 skipped
```

## TASK-W7-014 — Product deletion method security

### RED

```text
Command: ./mvnw -Dtest=ProductServiceAuthorizationIntegrationTest test
Exit: 1
Observed: EDITOR expected AccessDeniedException but ProductService.delete ran
          and threw ProductNotFoundException
Tests: 2 run, 1 failure, 0 errors, 0 skipped
```

### GREEN

```text
Command: ./mvnw -Dtest=ProductServiceAuthorizationIntegrationTest test
Exit: 0
Tests: 2 run, 0 failures, 0 errors, 0 skipped
```

## PLAN-W7-03 verification

```text
Focused command: ./mvnw -Dtest=RoleAuthorizationIntegrationTest,ProductServiceAuthorizationIntegrationTest,ProductServiceIntegrationTest,SecurityAccessIntegrationTest,DatabaseAuthenticationIntegrationTest test
Exit: 0
Tests: 35 run, 0 failures, 0 errors, 0 skipped

Full command: ./mvnw clean verify
Exit: 0
Result: BUILD SUCCESS
Tests: 131 run, 0 failures, 0 errors, 0 skipped
Compilation: javac --release 21
Runtime: Java 26.0.1
```

No controller contains a manual role or `SecurityContext` check. The final
reports contain no generated-security-password warning, no committed BCrypt
value or former database password is present, and `git diff --check` passed.

### GREEN

```text
Command: ./mvnw -Dtest=RoleAuthorizationIntegrationTest#givenViewer_whenCreatingProduct_thenReturns403 test
Exit: 0
Tests: 1 run, 0 failures, 0 errors, 0 skipped
```

## TASK-W7-012 — EDITOR write-without-delete authorization

### RED

```text
Command: ./mvnw -Dtest=RoleAuthorizationIntegrationTest#givenEditor_whenDeletingProduct_thenReturns403 test
Exit: 1
Observed: expected HTTP 403 but deletion reached ProductService and returned 404
Tests: 1 run, 1 failure, 0 errors, 0 skipped
```

The failure is the intended behavioral mismatch: the unchanged Week 6 endpoint
is publicly accessible. Compilation, Spring context startup, PostgreSQL, and
Testcontainers all succeeded before the assertion failed.

### GREEN

```text
Command: ./mvnw -Dtest=SecurityAccessIntegrationTest#givenNoCredentials_whenGetProducts_thenReturns401 test
Exit: 0
Result: BUILD SUCCESS
Tests: 1 run, 0 failures, 0 errors, 0 skipped
```

The minimal production change added Spring Security, stateless HTTP Basic, two
exact public endpoint matchers, a restrictive authenticated default, and an
explicit credential-rejecting `UserDetailsService`. Startup output contained no
generated security password.

## PLAN-W7-01 focused verification

```text
Command: ./mvnw -Dtest=SecurityAccessIntegrationTest test
Exit: 0
Result: BUILD SUCCESS
Tests: 7 run, 0 failures, 0 errors, 0 skipped
```

The suite verifies the public endpoints, unauthenticated product denial, Basic
challenge header, protected Actuator info, OpenAPI, Swagger UI, and the
restrictive default for an unmapped path.

```text
Command: ./mvnw -Dtest=ProductIntegrationTest,ProductControllerTest,ActuatorEndpointTest test
Exit: 0
Result: BUILD SUCCESS
Tests: 33 run, 0 failures, 0 errors, 0 skipped
```

Existing product and Actuator behavior remains green with explicit test
principals. The concurrent stock requests attach an ADMIN principal to each
executor-thread request.

## Regression correction

The first post-security full-suite run produced one useful regression failure:

```text
Command: ./mvnw clean verify
Exit: 1
Observed: InfoControllerTest expected 200 but received 401
Tests: 100 run, 1 failure, 0 errors, 0 skipped
```

Cause: the `@WebMvcTest` slice loaded Spring Security auto-configuration but did
not import the application's public-endpoint security chain. That isolated test
also created Spring Boot's generated development user. Importing
`SecurityConfig` made the slice use the same public contract and explicit
credential-rejecting user service as production.

A restricted sandbox rerun then hit Mockito/Byte Buddy self-attachment limits
under JDK 26 before executing the assertion. The unchanged test passed when run
with the same permitted execution context used by the integration suite:

```text
Command: ./mvnw -Dtest=InfoControllerTest test
Exit: 0
Tests: 1 run, 0 failures, 0 errors, 0 skipped
```

## PLAN-W7-01 full quality gate

```text
Command: ./mvnw clean verify
Exit: 0
Result: BUILD SUCCESS
Tests: 100 run, 0 failures, 0 errors, 0 skipped
Compilation: javac --release 21
Runtime: Java 26.0.1
```

The final reports contain no `Using generated security password` warning. A
changed-code scan found no credential assignment, authorization-header value, or
BCrypt hash. `git diff --check` also passed.

## TASK-W7-005 — Identity schema migration

### RED

The identity-schema assertion was added before the V2 migration.

```text
Command: ./mvnw -Dtest=FlywayMigrationTest#givenEmptyDatabase_whenFlywayRuns_thenCreatesIdentitySchemaAndCanonicalRoles test
Exit: 1
Observed: app_users table count expected:<1> but was:<0>
Tests: 1 run, 1 failure, 0 errors, 0 skipped
```

The application, Flyway V1, PostgreSQL 16 Testcontainer, and Hibernate all
started successfully. The failure was the intended missing-schema behavior.

### GREEN

```text
Command: ./mvnw -Dtest=FlywayMigrationTest test
Exit: 0
Result: BUILD SUCCESS
Tests: 3 run, 0 failures, 0 errors, 0 skipped
Flyway: 2 migrations applied; schema at version v2
```

## TASK-W7-006 — User and role persistence

### RED

```text
Command: ./mvnw -Dtest=AppUserRepositoryTest test
Exit: 1
Observed: test compilation failed because AppUserEntity, RoleEntity,
          ApplicationRole, AppUserRepository, and RoleRepository did not exist
```

This is the initial type-introduction RED. No persistence production type
existed before the repository contract test was added.

### GREEN

```text
Command: ./mvnw -Dtest=AppUserRepositoryTest test
Exit: 0
Result: BUILD SUCCESS
Tests: 4 run, 0 failures, 0 errors, 0 skipped
```

## TASK-W7-007 — Database user-details adapter

### RED

```text
Command: ./mvnw -Dtest=DatabaseUserDetailsServiceTest test
Exit: 1
Observed: test compilation failed because DatabaseUserDetailsService did not exist
```

### GREEN

```text
Command: ./mvnw -Dtest=DatabaseUserDetailsServiceTest test
Exit: 0
Result: BUILD SUCCESS
Tests: 3 run, 0 failures, 0 errors, 0 skipped
```

## TASK-W7-021 — Trace propagation and MDC cleanup

### RED

```text
Command: ./mvnw -Dtest=RequestTraceFilterTest test
Exit: 1
Observed: test compilation failed because RequestTraceFilter did not exist
```

### GREEN

```text
Command: ./mvnw -Dtest=RequestTraceFilterTest test
Exit: 0
Tests: 4 run, 0 failures, 0 errors, 0 skipped
```

Minimum change: an outer once-per-request filter selected or generated a UUID,
populated request/header/MDC state, and removed MDC in `finally`. Learning: trace
ownership must begin outside Spring Security so 401 and 403 paths share it.

## TASK-W7-022 and TASK-W7-023 — Shared security error envelope

### RED

```text
Authentication test: expected application/json 401 body; response body was empty
Authorization test: expected trace-aware JSON 403 body; response body was empty
```

### GREEN

```text
Focused classes: SecurityAccessIntegrationTest, RoleAuthorizationIntegrationTest,
                 AdminUserManagementIntegrationTest, GlobalExceptionHandlerTest
Result: BUILD SUCCESS
```

Minimum change: a shared `ErrorResponseFactory`, JSON authentication entry point,
JSON access-denied handler, and migration from `errorReferenceId` to request
`traceId`. Learning: MVC controller advice does not own failures produced inside
the security filter chain.

## TASK-W7-024 and TASK-W7-025 — Safe request and application events

### RED

```text
Request completion capture expected one event but captured zero.
Product/stock/user event assertions found no matching application events.
```

### GREEN

```text
Command: ./mvnw -Dtest=RequestTraceFilterTest,ApplicationEventLoggingIntegrationTest test
Exit: 0
Result: BUILD SUCCESS
```

Minimum change: bounded control-character sanitization, authenticated-principal
capture, one completion event per request, and successful domain events after
persistence. Unexpected failures log trace/path/type only. Learning: log tests
must assert both useful fields and forbidden sensitive values.

## TASK-W7-026 — Exception completion status

### RED

```text
Command: ./mvnw -Dtest=RequestTraceFilterTest test
Exit: 1
Observed: givenDownstreamFailure... expected ERROR but was INFO; log said status=200
Tests: 5 run, 1 failure
```

### GREEN

```text
Command: ./mvnw -Dtest=RequestTraceFilterTest,InfoControllerTest,ProductIntegrationTest test
Exit: 0
Tests: 14 run, 0 failures, 0 errors, 0 skipped
```

Minimum change: preserve downstream exception propagation while recording an
effective 500 completion status when the response had not yet been marked as an
error. Learning: servlet response status can remain 200 while an exception is
still unwinding through an outer filter.

## TASK-W7-026 — SQL failure-detail suppression

### RED

```text
Command: ./mvnw -Dtest=ProductPersistenceSupportIntegrationTest test
Exit: 1
Observed: captured output contained products_sku_unique_lower and
          "Failing row contains (...)"
Tests: 2 run, 2 failures
```

### GREEN

```text
Command: ./mvnw -Dtest=ProductPersistenceSupportIntegrationTest test
Exit: 0
Tests: 2 run, 0 failures, 0 errors, 0 skipped
```

Minimum change: disable only
`org.hibernate.engine.jdbc.spi.SqlExceptionHelper` while retaining safe typed
application events. Learning: avoiding explicit exception logging is insufficient
when an infrastructure library logs raw database diagnostics first.

## PLAN-W7-05 full verification

```text
Command: ./mvnw clean verify
Exit: 0
Result: BUILD SUCCESS
Tests: 155 run, 0 failures, 0 errors, 0 skipped
Artifact: target/product-catalog-1.0.0-SNAPSHOT.jar
Compilation: javac --release 21
Runtime: Java 26.0.1
```

The first attempt in this final cycle failed during setup because Docker Desktop
was not running. After Docker was restored, the suite exposed the INFO/200
exception completion, MVC-slice dependency, timing-dependent optimistic-lock
fixture, and SQL-detail output described above. None was hidden by test removal.
Java 21 runtime verification remains pending.

## PLAN-W7-02 verification

```text
Focused command: ./mvnw -Dtest=FlywayMigrationTest,AppUserRepositoryTest,DatabaseUserDetailsServiceTest,DatabaseAuthenticationIntegrationTest,DevelopmentUserSeederTest,SecurityAccessIntegrationTest,InfoControllerTest test
Exit: 0
Tests: 26 run, 0 failures, 0 errors, 0 skipped
```

```text
Full command: ./mvnw clean verify
Exit: 0
Result: BUILD SUCCESS
Tests: 116 run, 0 failures, 0 errors, 0 skipped
Compilation: javac --release 21
Runtime: Java 26.0.1
```

The final reports contain no generated-security-password warning. Committed
datasource and sample-user password defaults were removed. The credential scan
found only environment-variable placeholders and documentation examples with no
secret values. `git diff --check` passed. A Java 21 runtime remains unavailable
locally, so that final toolchain rerun is still pending.

## TASK-W7-016 and TASK-W7-017 — ADMIN user service

### RED

```text
Command: ./mvnw -Dtest=UserServiceTest test
Exit: 1
Observed: test compilation failed because CreateUserRequest, UserResponse,
          the user-management exceptions, and UserService did not exist
```

### GREEN

```text
Command: ./mvnw -Dtest=UserServiceTest test
Exit: 0
Result: BUILD SUCCESS
Tests: 5 run, 0 failures, 0 errors, 0 skipped
```

The first in-sandbox GREEN attempt was disregarded because JDK 26 prevented
Mockito from attaching before any assertion ran. The identical unsandboxed
command above ran every assertion successfully.

## TASK-W7-018 and TASK-W7-019 — ADMIN user HTTP API

### RED

```text
Command: ./mvnw -Dtest=AdminUserManagementIntegrationTest#givenAdminAndValidRequest_whenCreatingUser_thenReturns201AndSafeResponse test
Exit: 1
Observed: expected HTTP 201 but received 404 because the authorized route had
          no controller yet
Tests: 1 run, 1 failure, 0 errors, 0 skipped
```

### GREEN

```text
Command: ./mvnw -Dtest=AdminUserManagementIntegrationTest test
Exit: 0
Result: BUILD SUCCESS
Tests: 9 run, 0 failures, 0 errors, 0 skipped
```

## PLAN-W7-04 verification

```text
Focused command: ./mvnw -Dtest=UserServiceTest,UserServiceAuthorizationIntegrationTest,AdminUserManagementIntegrationTest,RoleAuthorizationIntegrationTest,DatabaseAuthenticationIntegrationTest test
Exit: 0
Tests: 35 run, 0 failures, 0 errors, 0 skipped
```

```text
Full command: ./mvnw clean verify
Exit: 0
Result: BUILD SUCCESS
Tests: 148 run, 0 failures, 0 errors, 0 skipped
Compilation: javac --release 21
Runtime: Java 26.0.1
```

The response tests prove password and hash fields are absent, the source scan
found no controller role checks or committed credentials, the reports contain
no generated-security-password warning, and `git diff --check` passed. Java 21
runtime verification remains pending.

## TASK-W7-011 — VIEWER read-only authorization

### RED

```text
Command: ./mvnw -Dtest=RoleAuthorizationIntegrationTest#givenViewer_whenCreatingProduct_thenReturns403 test
Exit: 1
Observed: expected HTTP 403 but ProductController#create ran and returned 201
Tests: 1 run, 1 failure, 0 errors, 0 skipped
```

## TASK-W7-008 — PostgreSQL-backed HTTP Basic authentication

### RED

```text
Command: ./mvnw -Dtest=DatabaseAuthenticationIntegrationTest test
Exit: 1
Observed: valid enabled database user expected HTTP 200 but received 401
Tests: 5 run, 1 failure, 0 errors, 0 skipped
Startup: two UserDetailsService beans prevented username/password provider wiring
```

The invalid-password, unknown-user, disabled-user, and BCrypt-storage assertions
already passed. The valid path failed because the PLAN-W7-01 deny-all identity
bean still existed alongside the new database adapter.

### GREEN

```text
Command: ./mvnw -Dtest=DatabaseAuthenticationIntegrationTest test
Exit: 0
Result: BUILD SUCCESS
Tests: 5 run, 0 failures, 0 errors, 0 skipped
Authentication provider: sole databaseUserDetailsService + BCryptPasswordEncoder
```

## TASK-W7-009 — Development-only sample-user seeding

### RED

```text
Command: ./mvnw -Dtest=DevelopmentUserSeederTest test
Exit: 1
Observed: test compilation failed because SecuritySeedProperties and
          DevelopmentUserSeeder did not exist
```

### GREEN

```text
Command: ./mvnw -Dtest=DevelopmentUserSeederTest test
Exit: 0
Result: BUILD SUCCESS
Tests: 3 run, 0 failures, 0 errors, 0 skipped
```

## TASK-W7-027 — Restore the approved optimistic-lock API contract

### RED 1: expose the version

```text
Command: ./mvnw -Dtest=ProductControllerTest#givenValidRequest_whenCreateProduct_thenReturns201WithLocationHeader test
Exit: 1
Observed: No value at JSON path "$.version"
Tests: 1 run, 1 failure
```

### GREEN 1

```text
Same focused command
Exit: 0
Tests: 1 run, 0 failures, 0 errors, 0 skipped
```

Minimum change: add the persisted entity version to `ProductResponse` and the
entity-to-response mapper.

### RED 2: enforce a stale PUT precondition

```text
Command: ./mvnw -Dtest=ProductControllerTest#givenStaleVersion_whenUpdatingProduct_thenReturns409 test
Exit: 1
Observed: expected HTTP 409 but received 200
Tests: 1 run, 1 failure
```

### GREEN 2

```text
Command: ./mvnw -Dtest=ProductControllerTest,ProductIntegrationTest test
Exit: 0
Tests: 31 run, 0 failures, 0 errors, 0 skipped
```

Minimum change: accept optional `version` in `ProductRequest` and apply the same
service precondition already used by stock adjustment. The concurrency test now
obtains its starting version over HTTP. Learning: the approved API exposed the
optimistic-lock contract, but the DTO and service had drifted from it.

## PLAN-W7-06 final quality gate

```text
Command: JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./mvnw clean verify
Exit: 0
Result: BUILD SUCCESS
Tests: 156 run, 0 failures, 0 errors, 0 skipped
Artifact: target/product-catalog-1.0.0-SNAPSHOT.jar
Compilation: javac --release 21
Runtime: OpenJDK 21.0.12.1 (Homebrew)
```

## TASK-W7-031 — BCrypt UTF-8 byte boundary

### RED

```text
Command: JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./mvnw -Dtest=AdminUserManagementIntegrationTest#givenPasswordBeyondBcryptByteLimit_whenCreatingUser_thenReturns400PasswordError test
Exit: 1
Observed: expected HTTP 400 but received 201
Tests: 1 run, 1 failure, 0 errors, 0 skipped
```

The 25-character reproduction input encoded to 73 UTF-8 bytes and passed the
existing `@Size(max = 72)` character check. Direct Java 21 BCrypt probing also
proved that changing the byte after the shared first 72 bytes still matched the
same hash.

### GREEN

```text
Focused command: JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./mvnw -Dtest=AdminUserManagementIntegrationTest test
Focused result: BUILD SUCCESS; 10 tests, 0 failures, 0 errors, 0 skipped

Full command: JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./mvnw clean verify
Full result: BUILD SUCCESS; 157 tests, 0 failures, 0 errors, 0 skipped
Artifact: target/product-catalog-1.0.0-SNAPSHOT.jar
Runtime: OpenJDK 21.0.12.1 (Homebrew)
Compilation: javac --release 21
```

Minimum change: add a field-level `Utf8ByteLength` Bean Validation constraint
and apply it alongside the existing password character constraints. Validation
now returns a normal `password` field error before password encoding or
persistence.
