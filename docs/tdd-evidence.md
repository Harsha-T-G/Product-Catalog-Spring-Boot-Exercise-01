# Week 7 TDD Evidence

Commands below were run on `week7-security-logging`. Routine framework output
is omitted. Tests use PostgreSQL 16 through Testcontainers.

## Cycle 1 — Protect product access

**Behavior:** An unauthenticated product request returns HTTP 401.

```text
RED command: ./mvnw -Dtest=SecurityAccessIntegrationTest#givenNoCredentials_whenGetProducts_thenReturns401 test
RED result: expected 401 but received 200; 1 failure

GREEN command: ./mvnw -Dtest=SecurityAccessIntegrationTest#givenNoCredentials_whenGetProducts_thenReturns401 test
GREEN result: BUILD SUCCESS; 1 test passed
```

**Change:** Added Spring Security, stateless HTTP Basic, exact public matchers,
and a restrictive authenticated default.

**Learned:** Security must be tested at the HTTP boundary because the filter
chain runs before controllers.

## Cycle 2 — Authenticate database users

**Behavior:** An enabled PostgreSQL user authenticates using BCrypt credentials.

```text
RED command: ./mvnw -Dtest=DatabaseAuthenticationIntegrationTest test
RED result: valid user expected 200 but received 401; 1 of 5 tests failed

GREEN command: ./mvnw -Dtest=DatabaseAuthenticationIntegrationTest test
GREEN result: BUILD SUCCESS; 5 tests passed
```

**Change:** Made the database `UserDetailsService` the authentication source and
configured `BCryptPasswordEncoder`.

**Learned:** Competing `UserDetailsService` beans prevented Spring from selecting
the intended username/password authentication provider.

## Cycle 3 — Enforce VIEWER authorization

**Behavior:** A VIEWER cannot create a product.

```text
RED command: ./mvnw -Dtest=RoleAuthorizationIntegrationTest#givenViewer_whenCreatingProduct_thenReturns403 test
RED result: expected 403 but the controller returned 201; 1 failure

GREEN command: ./mvnw -Dtest=RoleAuthorizationIntegrationTest test
GREEN result: BUILD SUCCESS; role access matrix passed
```

**Change:** Added method/path authorization rules for VIEWER, EDITOR, and ADMIN,
with ADMIN-only deletion and user management.

**Learned:** Authentication establishes identity; authorization separately
decides whether that identity may perform an operation.

## Cycle 4 — Create users through the ADMIN API

**Behavior:** ADMIN can create a user without exposing password information.

```text
RED command: ./mvnw -Dtest=AdminUserManagementIntegrationTest#givenAdminAndValidRequest_whenCreatingUser_thenReturns201AndSafeResponse test
RED result: expected 201 but received 404; 1 failure

GREEN command: ./mvnw -Dtest=AdminUserManagementIntegrationTest test
GREEN result: BUILD SUCCESS; 9 tests passed
```

**Change:** Added the ADMIN controller, validated DTOs, transactional service,
BCrypt persistence, duplicate/self-disable errors, and safe response mapping.

**Learned:** Authorization can succeed while routing still returns 404; the
public test distinguishes the missing endpoint from a security failure.

## Cycle 5 — Propagate trace IDs safely

**Behavior:** Requests receive a trace ID in MDC and the response, and MDC is
cleared after completion.

```text
RED command: ./mvnw -Dtest=RequestTraceFilterTest test
RED result: compilation failed because RequestTraceFilter did not exist

GREEN command: ./mvnw -Dtest=RequestTraceFilterTest test
GREEN result: BUILD SUCCESS; 4 tests passed
```

**Change:** Added an outer once-per-request filter that accepts or generates a
UUID, populates request/header/MDC state, logs safe completion data, and clears
MDC in `finally`.

**Learned:** Trace ownership must begin outside Spring Security so 401 and 403
responses receive the same correlation behavior.

## Cycle 6 — Enforce BCrypt's UTF-8 boundary

**Behavior:** User passwords exceeding 72 UTF-8 bytes are rejected before
encoding, even when they contain no more than 72 characters.

```text
RED command: JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./mvnw -Dtest=AdminUserManagementIntegrationTest#givenPasswordBeyondBcryptByteLimit_whenCreatingUser_thenReturns400PasswordError test
RED result: expected 400 but received 201; 1 failure

GREEN command: JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./mvnw -Dtest=AdminUserManagementIntegrationTest test
GREEN result: BUILD SUCCESS; 10 tests passed
```

**Change:** Added reusable field-level `Utf8ByteLength` validation to the user
creation password.

**Learned:** Character-count validation alone does not enforce BCrypt's byte
limit for multi-byte Unicode input.

## Final verification

```text
Command: JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./mvnw clean verify
Result: BUILD SUCCESS
Tests: 158 run, 0 failures, 0 errors, 0 skipped
Runtime: OpenJDK 21.0.12.1
Compilation: javac --release 21
Artifact: target/product-catalog-1.0.0-SNAPSHOT.jar
```
