# Product Catalog Tasks

**Status:** PLAN-W7-01 through PLAN-W7-07 implemented
**Active slice:** TASK-W7-031 complete; commit and push authorized
**Branch:** `week7-security-logging`
**Traces to:** `docs/plans/product-catalog-implementation-plan.md`, `docs/security-spec.md`

These are the Week 7 tasks. TASK-W7-031 includes the user's explicit
authorization to commit and push after its quality gates pass; it does not
authorize opening a pull request.

## Execution order

```text
TASK-W7-001 → … → TASK-W7-015 → TASK-W7-016 → TASK-W7-017 →
TASK-W7-018 → … → TASK-W7-026 → TASK-W7-027 → … → TASK-W7-030 → TASK-W7-031
```

## TASK-W7-001: Capture the unauthenticated-access RED

**Status:** Complete

| Field | Value |
| --- | --- |
| **Requirements** | REQ-120 |
| **Acceptance** | AC-120 RED half |
| **Depends on** | Approved Week 6 baseline |

**Steps:**

1. Confirm the active Java and Docker/Testcontainers environment and run the
   unchanged Week 6 regression suite.
2. Add `SecurityAccessIntegrationTest` with one behavior: no credentials on
   `GET /api/products` must return 401.
3. Run only that test before adding Spring Security. Confirm it fails because
   the unchanged endpoint returns 200, not because of compilation, database, or
   environment failure.
4. Create `docs/tdd-evidence.md` and record the exact command, non-zero exit
   status, expected-versus-actual status, and relevant output excerpt.

**Likely files:**

- `src/test/java/com/codewalnut/productcatalog/security/SecurityAccessIntegrationTest.java`
- `docs/tdd-evidence.md`

**Verify:**

```bash
./mvnw -Dtest=SecurityAccessIntegrationTest#givenNoCredentials_whenGetProducts_thenReturns401 test
```

**Acceptance:** The focused test compiles and fails only because the response is
200 instead of 401; authentic RED evidence is recorded before production code or
security dependencies change.

## TASK-W7-002: Establish the minimum secure boundary

**Status:** Complete

| Field | Value |
| --- | --- |
| **Requirements** | REQ-111, REQ-115, REQ-116, REQ-120 |
| **Acceptance** | AC-110, AC-111 status/challenge, AC-116, AC-120 GREEN half |
| **Depends on** | TASK-W7-001 |

**Steps:**

1. Add `spring-boot-starter-security` and test-scoped
   `spring-security-test` through Spring Boot dependency management.
2. Add a Spring Security 6 lambda-DSL `SecurityFilterChain` with stateless HTTP
   Basic, CSRF disabled, exact public GET matchers for `/api/info` and
   `/actuator/health`, and `anyRequest().authenticated()` last.
3. Add an explicit temporary `UserDetailsService` that rejects every username,
   preventing Spring Boot from generating or logging a default password. It must
   contain no hardcoded credential and is removed in PLAN-W7-02.
4. Re-run the focused test and record the passing command and exit status.
5. Expand the focused class to verify the Basic challenge and that public,
   Actuator-info, OpenAPI, Swagger, and unknown-path behavior follows the
   approved public/default boundary.

**Likely files:**

- `pom.xml`
- `src/main/java/com/codewalnut/productcatalog/config/SecurityConfig.java`
- `src/test/java/com/codewalnut/productcatalog/security/SecurityAccessIntegrationTest.java`
- `docs/tdd-evidence.md`

**Verify:**

```bash
./mvnw -Dtest=SecurityAccessIntegrationTest test
```

**Acceptance:** The original test is GREEN; `/api/info` and
`/actuator/health` remain public; other checked paths return 401 without
credentials; 401 retains `WWW-Authenticate`; no generated password is emitted.
The shared trace-aware JSON envelope remains deferred to PLAN-W7-05.

## TASK-W7-003: Preserve authenticated product regressions

**Status:** Complete

| Field | Value |
| --- | --- |
| **Requirements** | REQ-120 and Week 6 regression contract |
| **Acceptance** | Authorized requests preserve existing Week 6 outcomes |
| **Depends on** | TASK-W7-002 |

**Steps:**

1. Add an explicit ADMIN test principal to full-stack product/controller tests
   whose purpose is product behavior rather than security behavior.
2. Ensure executor-thread MockMvc calls in the optimistic-lock test attach their
   own ADMIN request post-processor instead of relying on thread-local security
   context inheritance.
3. Keep the health test anonymous to prove public access; authenticate existing
   Actuator info/env assertions where they need to pass the security filter.
4. Do not modify standalone MockMvc tests that do not install Spring Security.

**Likely files:**

- `src/test/java/com/codewalnut/productcatalog/ProductIntegrationTest.java`
- `src/test/java/com/codewalnut/productcatalog/controller/ProductControllerTest.java`
- `src/test/java/com/codewalnut/productcatalog/ActuatorEndpointTest.java`

**Verify:**

```bash
./mvnw -Dtest=ProductIntegrationTest,ProductControllerTest,ActuatorEndpointTest test
```

**Acceptance:** Existing authenticated product and Actuator assertions retain
their Week 6 status/body behavior, the anonymous health assertion remains 200,
and no test is skipped, deleted, or weakened.

## TASK-W7-004: Verify and close the first security slice

**Status:** Complete; final suite verified on OpenJDK 21.0.12.1

| Field | Value |
| --- | --- |
| **Requirements** | REQ-111, REQ-115, REQ-116, REQ-120 |
| **Acceptance** | AC-110, AC-111 status/challenge, AC-116, AC-120 |
| **Depends on** | TASK-W7-003 |

**Steps:**

1. Run the focused security and regression commands followed by the full Maven
   quality gate under Java 21 with Docker available.
2. Inspect startup/test logs for a generated password and scan the changed files
   for credentials, authorization headers, password values, and hashes.
3. Inspect the complete project diff and confirm only the approved first slice
   changed production behavior.
4. Finalize the PLAN-W7-01 evidence/status records without marking later modules
   complete.

**Likely files:**

- `docs/tdd-evidence.md`
- `AI_USAGE.md`
- `docs/plans/product-catalog-tasks.md`
- `docs/plans/product-catalog-implementation-plan.md`

**Verify:**

```bash
./mvnw clean verify
git diff --check
```

**Acceptance:** Focused and full suites pass under the required toolchain,
authentic RED→GREEN evidence is complete, no sensitive value is present, and
PLAN-W7-01 is ready for review.

## TASK-W7-005: Migrate the identity schema

**Status:** Complete

| Field | Value |
| --- | --- |
| **Requirements** | REQ-121, REQ-123 |
| **Acceptance** | AC-121, database half of AC-126 |
| **Depends on** | TASK-W7-004 |

Add failing migration assertions first, then add V2 with the user, role, and
mapping tables, foreign keys, canonical role data, and a unique index on
`LOWER(username)`. Verify with `FlywayMigrationTest` against PostgreSQL.

**Files:** `FlywayMigrationTest.java`, `V2__create_security_tables.sql`,
`docs/tdd-evidence.md`.

## TASK-W7-006: Map and query users and roles

**Status:** Complete

| Field | Value |
| --- | --- |
| **Requirements** | REQ-122, REQ-123 |
| **Acceptance** | AC-121, AC-126 |
| **Depends on** | TASK-W7-005 |

Add repository integration tests first for case-insensitive lookup, eager role
availability, enabled persistence, and case-variant duplicate rejection. Then
add the role enum, JPA entities, and repositories with no REST exposure.

**Files:** `AppUserRepositoryTest.java`, `ApplicationRole.java`,
`AppUserEntity.java`, `RoleEntity.java`, `AppUserRepository.java`,
`RoleRepository.java`, `docs/tdd-evidence.md`.

## TASK-W7-007: Load Spring Security principals from PostgreSQL

**Status:** Complete

| Field | Value |
| --- | --- |
| **Requirements** | REQ-124, REQ-126 |
| **Acceptance** | AC-122, AC-124 service boundary |
| **Depends on** | TASK-W7-006 |

Add focused user-details tests first. Then map a database user to Spring's
principal with `ROLE_` authorities and its persisted enabled state, while
mapping an absent account to `UsernameNotFoundException`.

**Files:** `DatabaseUserDetailsServiceTest.java`,
`DatabaseUserDetailsService.java`, `docs/tdd-evidence.md`.

## TASK-W7-008: Authenticate real HTTP Basic requests

**Status:** Complete

| Field | Value |
| --- | --- |
| **Requirements** | REQ-124–REQ-127 |
| **Acceptance** | AC-122–AC-125 |
| **Depends on** | TASK-W7-007 |

Add Testcontainers HTTP tests before configuration changes. Prove mixed-case
valid login, wrong-password, unknown-user, and disabled-user outcomes, plus the
persisted BCrypt form. Then add the encoder bean and remove the temporary
deny-all user loader.

**Files:** `DatabaseAuthenticationIntegrationTest.java`,
`SecurityConfig.java`, `docs/tdd-evidence.md`.

## TASK-W7-009: Seed development users safely

**Status:** Complete

| Field | Value |
| --- | --- |
| **Requirements** | REQ-128, REQ-129 |
| **Acceptance** | AC-127 |
| **Depends on** | TASK-W7-008 |

Add tests proving incomplete secret configuration creates no users and complete
configuration creates one idempotent BCrypt user per role. Then add typed
properties and a `dev`-only seeder. Passwords and hashes must never be logged.

**Files:** `DevelopmentUserSeederTest.java`, `SecuritySeedProperties.java`,
`DevelopmentUserSeeder.java`, `ProductCatalogApplication.java`,
`application-dev.yml`, `docs/tdd-evidence.md`.

## TASK-W7-010: Verify the database-authentication slice

**Status:** Complete; final suite verified on OpenJDK 21.0.12.1

| Field | Value |
| --- | --- |
| **Requirements** | REQ-121–REQ-129 |
| **Acceptance** | AC-121–AC-127 |
| **Depends on** | TASK-W7-009 |

Remove committed datasource-password fallbacks, run focused and full tests,
scan changed files/logs for secrets and generated passwords, inspect the full
diff, and update evidence/status. The final Java 21 runtime result is recorded
under TASK-W7-030.

**Files:** `application.yml`, `application-dev.yml`, `docs/tdd-evidence.md`,
plan/status documents.

## TASK-W7-011: Establish VIEWER read-only access

**Status:** Complete

| Field | Value |
| --- | --- |
| **Requirements** | REQ-130, REQ-131 |
| **Acceptance** | AC-131 |
| **Depends on** | TASK-W7-010 |

Add a VIEWER create-denial HTTP test first and prove the authenticated request
currently reaches the product API. Add only the product read and interim
EDITOR-only create/update/stock matchers needed for GREEN, then expand the
VIEWER matrix without changing production behavior again.

## TASK-W7-012: Establish EDITOR write-without-delete access

**Status:** Complete

| Field | Value |
| --- | --- |
| **Requirements** | REQ-130, REQ-132 |
| **Acceptance** | AC-132 |
| **Depends on** | TASK-W7-011 |

Add an EDITOR delete-denial test first and prove it currently reaches routing.
Add ADMIN-only product-delete and `/api/admin/**` matchers, then expand EDITOR
read/write success and denial coverage.

## TASK-W7-013: Complete ADMIN and operational access

**Status:** Complete

| Field | Value |
| --- | --- |
| **Requirements** | REQ-133–REQ-135, REQ-137 |
| **Acceptance** | AC-130, AC-133–AC-135 |
| **Depends on** | TASK-W7-012 |

Add an ADMIN product-create success test first; it must fail against the
interim EDITOR-only matcher. Explicitly add ADMIN to product read/write and
recognized-role operational matchers. Verify ADMIN deletion and admin-boundary
pass-through, public behavior, protected documentation, and authenticated 404
fallback.

## TASK-W7-014: Add deletion defense in depth

**Status:** Complete

| Field | Value |
| --- | --- |
| **Requirements** | REQ-136 |
| **Acceptance** | AC-136 |
| **Depends on** | TASK-W7-013 |

Add a proxied service integration test proving an EDITOR can currently invoke
deletion directly. Enable method security and protect `ProductService.delete`
for ADMIN. Prove EDITOR is denied before business logic and ADMIN reaches the
normal service outcome.

## TASK-W7-015: Verify role authorization

**Status:** Complete; final suite verified on OpenJDK 21.0.12.1

| Field | Value |
| --- | --- |
| **Requirements** | REQ-130–REQ-137 |
| **Acceptance** | AC-130–AC-136 |
| **Depends on** | TASK-W7-014 |

Run the focused role suite and full Maven gate. Scan controllers for manual
security checks, verify matcher order and generated-password absence, inspect
credentials and the complete diff, and update evidence/status documents.

## TASK-W7-016: Create users safely

**Status:** Complete

| Field | Value |
| --- | --- |
| **Requirements** | REQ-140–REQ-144, REQ-147 |
| **Acceptance** | AC-140, AC-142–AC-144 service behavior |
| **Depends on** | TASK-W7-015 |

Add focused service tests first for trimmed username validation boundaries,
case-insensitive duplicate detection, BCrypt encoding before persistence,
canonical role resolution, and safe response mapping. Then add the request and
response DTOs, user-management service, and duplicate-user domain exception.

## TASK-W7-017: Change enabled state safely

**Status:** Complete

| Field | Value |
| --- | --- |
| **Requirements** | REQ-140, REQ-145–REQ-147 |
| **Acceptance** | AC-145–AC-146 service behavior |
| **Depends on** | TASK-W7-016 |

Add focused service tests before behavior for disabling another user, missing
case-insensitive lookup, and case-insensitive self-disable prevention. Add the
enabled-state request, domain exceptions, transactional update, and method-level
ADMIN defense in depth.

## TASK-W7-018: Expose the ADMIN user API

**Status:** Complete

| Field | Value |
| --- | --- |
| **Requirements** | REQ-140–REQ-148 |
| **Acceptance** | AC-140–AC-146 HTTP contracts |
| **Depends on** | TASK-W7-017 |

Add MockMvc tests first for 201/200 success responses, validation failures,
duplicate 409, missing 404, self-disable 409, ADMIN access, and anonymous,
VIEWER, and EDITOR denial. Then add the thin ADMIN controller and safe global
exception mappings without manual role checks.

## TASK-W7-019: Prove immediate authentication consistency

**Status:** Complete

| Field | Value |
| --- | --- |
| **Requirements** | REQ-142, REQ-144, REQ-145, REQ-149 |
| **Acceptance** | AC-142, AC-147 |
| **Depends on** | TASK-W7-018 |

Add PostgreSQL-backed HTTP flows proving a newly created user authenticates
with the assigned role, its stored password is BCrypt, and a disabled user's
next correct-password request returns 401.

## TASK-W7-020: Verify ADMIN user management

**Status:** Complete; final suite verified on OpenJDK 21.0.12.1

| Field | Value |
| --- | --- |
| **Requirements** | REQ-140–REQ-149 |
| **Acceptance** | AC-140–AC-147 |
| **Depends on** | TASK-W7-019 |

Run focused security and ADMIN API tests followed by the full Maven gate. Scan
responses, logs, and changed files for plaintext passwords, hashes, SQL detail,
or generated credentials; confirm no controller contains role logic; inspect
the complete diff; and update the evidence and status documents.

## TASK-W7-021: Propagate trace IDs without MDC leakage

**Status:** Complete

| Field | Value |
| --- | --- |
| **Requirements** | REQ-150–REQ-153, REQ-161 |
| **Acceptance** | AC-150–AC-153 |
| **Depends on** | TASK-W7-020 |

Add filter tests first for valid UUID reuse, generated replacement, response
headers, downstream MDC, `finally` cleanup, and consecutive-request isolation.
Then implement the outer trace/request-completion filter and safe log-value
sanitization.

## TASK-W7-022: Return trace-aware 401 responses

**Status:** Complete

| Field | Value |
| --- | --- |
| **Requirements** | REQ-154–REQ-155, REQ-157 |
| **Acceptance** | AC-154, authentication half of AC-158 |
| **Depends on** | TASK-W7-021 |

Add a failing HTTP assertion for the common JSON 401 envelope, matching header
and body trace, generic message, and Basic challenge. Then add the shared error
factory and authentication entry point with a credential-free WARN event.

## TASK-W7-023: Unify 403 and application errors

**Status:** Complete

| Field | Value |
| --- | --- |
| **Requirements** | REQ-154, REQ-156–REQ-157 |
| **Acceptance** | AC-155–AC-156, denial half of AC-158 |
| **Depends on** | TASK-W7-022 |

Add failing HTTP assertions for 403 and application errors with matching trace
header/body. Then configure the JSON access-denied handler and migrate
`GlobalExceptionHandler`/`ErrorResponse` from `errorReferenceId` to `traceId`.

## TASK-W7-024: Log safe request and security outcomes

**Status:** Complete

| Field | Value |
| --- | --- |
| **Requirements** | REQ-152–REQ-153, REQ-157, REQ-160–REQ-162 |
| **Acceptance** | AC-153, AC-158 |
| **Depends on** | TASK-W7-023 |

Add captured-log tests first, then add successful-principal capture and safe
request completion at INFO/WARN/ERROR. Prove failed login and access denial omit
submitted credentials, Authorization data, bodies, and stack traces.

## TASK-W7-025: Log safe application events and failures

**Status:** Complete

| Field | Value |
| --- | --- |
| **Requirements** | REQ-158–REQ-160 |
| **Acceptance** | AC-157, AC-159 |
| **Depends on** | TASK-W7-024 |

Add captured-log assertions before event changes. Then emit minimal successful
product-create, stock-adjust, user-create, and user-enabled events, and log
unexpected failures by trace/path/type without exception message or stack.

## TASK-W7-026: Document and verify observability

**Status:** Complete; final suite verified on OpenJDK 21.0.12.1

| Field | Value |
| --- | --- |
| **Requirements** | REQ-163 and REQ-150–REQ-162 regression |
| **Acceptance** | AC-160 and AC-150–AC-159 regression |
| **Depends on** | TASK-W7-025 |

Create `docs/debugging-notes.md` for the three required scenarios using
repeatable trace-aware evidence. Run focused/full verification, scan logs/source
for secrets and complete request bodies, prove no generated password or MDC
leak, inspect the full diff, and update evidence/status.

## TASK-W7-027: Audit the complete integration matrix

**Status:** Complete

| Field | Value |
| --- | --- |
| **Requirements** | REQ-170–REQ-171 |
| **Acceptance** | AC-170–AC-171 |
| **Depends on** | TASK-W7-026 |

Map every required Exercise 6 behavior to named PostgreSQL-backed HTTP tests.
Add only missing public-boundary coverage and make concurrency preconditions
deterministic without weakening expected outcomes.

## TASK-W7-028: Complete delivery evidence

**Status:** Complete

| Field | Value |
| --- | --- |
| **Requirements** | REQ-172, REQ-175, REQ-177 |
| **Acceptance** | AC-172, AC-175 |
| **Depends on** | TASK-W7-027 |

Record authentic observability RED→GREEN cycles, the full-suite regressions and
their fixes, toolchain facts, and the short Week 7 self-review.

## TASK-W7-029: Update secured usage documentation

**Status:** Complete

| Field | Value |
| --- | --- |
| **Requirements** | REQ-173–REQ-174 |
| **Acceptance** | AC-173 |
| **Depends on** | TASK-W7-027 |

Update README and curl examples with the role matrix, environment-only dev user
setup, authenticated product and ADMIN flows, 401/403 envelopes, trace reuse,
test commands, limitations, and future improvements.

## TASK-W7-030: Run final quality and security gates

**Status:** Complete on OpenJDK 21.0.12.1; Git publication pending

| Field | Value |
| --- | --- |
| **Requirements** | REQ-176–REQ-178 |
| **Acceptance** | AC-170, AC-174–AC-175 |
| **Depends on** | TASK-W7-028, TASK-W7-029 |

Run focused and complete Maven verification, scan source/reports/configuration,
inspect the complete scoped diff, and prepare the handoff without committing,
pushing, or opening a pull request.

## TASK-W7-031: Enforce the BCrypt UTF-8 byte boundary

**Status:** Complete; focused suite 10 tests and Java 21 gate 157 tests

| Field | Value |
| --- | --- |
| **Requirements** | REQ-141 |
| **Acceptance** | AC-143 |
| **Depends on** | TASK-W7-030 |

Add a failing ADMIN API test for a password that is at most 72 characters but
exceeds 72 UTF-8 bytes. Implement a reusable Bean Validation constraint that
reports the violation against `password`, rerun the focused test and full Java
21 quality gate, inspect the scoped diff, then commit and push under the user's
2026-09-10 authorization. Do not open a pull request without a separate request.

## Traceability

| Requirement / acceptance | Task |
| --- | --- |
| REQ-111 / AC-110 | TASK-W7-002, TASK-W7-003 |
| REQ-115 / AC-116 | TASK-W7-002, TASK-W7-004 |
| REQ-116 / AC-111 status and challenge | TASK-W7-002, TASK-W7-004 |
| REQ-120 / AC-120 | TASK-W7-001 through TASK-W7-004 |
| Week 6 authorized behavior | TASK-W7-003, TASK-W7-004 |
| REQ-121 / AC-121 | TASK-W7-005, TASK-W7-006 |
| REQ-122–REQ-123 / AC-126 | TASK-W7-006 |
| REQ-124–REQ-127 / AC-122–AC-125 | TASK-W7-007, TASK-W7-008 |
| REQ-128–REQ-129 / AC-127 | TASK-W7-009, TASK-W7-010 |
| REQ-130–REQ-131 / AC-131 | TASK-W7-011 |
| REQ-132 / AC-132 | TASK-W7-012 |
| REQ-133–REQ-135, REQ-137 / AC-130, AC-133–AC-135 | TASK-W7-013 |
| REQ-136 / AC-136 | TASK-W7-014 |
| REQ-140–REQ-144 / AC-140–AC-144 | TASK-W7-016, TASK-W7-018, TASK-W7-019, TASK-W7-031 |
| REQ-145–REQ-147 / AC-145–AC-147 | TASK-W7-017–TASK-W7-019 |
| REQ-148–REQ-149 | TASK-W7-018–TASK-W7-020 |
| REQ-150–REQ-153 / AC-150–AC-153 | TASK-W7-021, TASK-W7-024 |
| REQ-154–REQ-157 / AC-154–AC-156, AC-158 | TASK-W7-022–TASK-W7-024 |
| REQ-158–REQ-162 / AC-157–AC-159 | TASK-W7-024–TASK-W7-026 |
| REQ-163 / AC-160 | TASK-W7-026 |
| REQ-170–REQ-171 / AC-170–AC-171 | TASK-W7-027, TASK-W7-030 |
| REQ-172 / AC-172 | TASK-W7-028 |
| REQ-173–REQ-175 / AC-173 | TASK-W7-028–TASK-W7-029 |
| REQ-176–REQ-178 / AC-174–AC-175 | TASK-W7-030 |

## Approval gate

- [x] Task order and dependencies approved
- [x] RED evidence task approved
- [x] Temporary deny-all implementation task approved
- [x] Regression authentication changes approved
- [x] Verification and sensitive-data checks approved
- [x] **TASK-W7-001 through TASK-W7-004 approved on 2026-09-09 — implementation may begin**

### PLAN-W7-02 task approval

- [x] TASK-W7-005 through TASK-W7-010 are independently verifiable
- [x] Each behavior slice begins with a focused failing test
- [x] Secret handling and final credential scan are explicit tasks
- [x] **TASK-W7-005 through TASK-W7-010 approved under standing user approval on 2026-09-09**

### PLAN-W7-03 task approval

- [x] Role-by-role RED→GREEN order preserved
- [x] Method-security behavior has a separate focused cycle
- [x] Restrictive fallback and operational endpoint checks included
- [x] **TASK-W7-011 through TASK-W7-015 approved under standing user approval on 2026-09-09**

### PLAN-W7-04 task approval

- [x] Service and HTTP behavior are split into independently verifiable tasks
- [x] Each behavior slice begins with a focused failing test
- [x] End-to-end authentication consistency and credential scans are explicit
- [x] **TASK-W7-016 through TASK-W7-020 approved under standing user approval on 2026-09-09**

### PLAN-W7-05 task approval

- [x] Trace, error-envelope, logging, and documentation slices are independently verifiable
- [x] Each behavior-changing slice begins with a focused failing test
- [x] Sensitive-output and MDC cleanup checks are explicit
- [x] **TASK-W7-021 through TASK-W7-026 approved under standing user approval on 2026-09-09**

### PLAN-W7-06 task approval

- [x] Integration audit, documentation, evidence, and final gate are independently verifiable
- [x] No duplicate integration suite is required where public behavior is already covered
- [x] Runtime and publication limitations remain explicit
- [x] **TASK-W7-027 through TASK-W7-030 approved under standing user approval on 2026-09-10**

### PLAN-W7-07 task approval

- [x] BCrypt byte-boundary reproduction test precedes production validation
- [x] Full Java 21 verification and pre/post-push review are required
- [x] **TASK-W7-031 and its commit/push boundary approved by the user on 2026-09-10**
