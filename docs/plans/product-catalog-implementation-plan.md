# Product Catalog Implementation Plan

**Status:** PLAN-W7-01 through PLAN-W7-07 implemented and verified on Java 21; Git publication authorized
**Active initiative:** Week 7 Spring Security, logging, debugging, and error handling
**Branch:** `week7-security-logging`
**Baseline:** `week6-exercise-6-docs-delivery`
**Traces to:** `WEEK7_CAPABILITY_MAP.md`, `SPEC.md`, `docs/security-spec.md`, and `docs/specs/product-catalog/07-database-authentication.md` through `11-security-verification-delivery.md`

## Outcome

Extend the approved Week 6 PostgreSQL catalog with database-backed HTTP Basic
authentication, the approved VIEWER/EDITOR/ADMIN access matrix, ADMIN-only user
management, trace-aware safe logging, and Testcontainers-backed security
evidence. Preserve all product behavior after authorization succeeds.

The work remains on one Week 7 branch and one final focused pull request. It is
delivered as small vertical slices so each security rule has a recorded failing
test before the minimum production change.

## Planning constraints

- The approved `security-contract` owns the shared role, endpoint, status, and
  sensitive-data rules.
- Each dependent capability receives its own approved specification before its
  implementation tasks are executed.
- Spring Security and `spring-security-test` are the only new dependencies
  currently approved by the Week 7 contract.
- Flyway owns database changes; Hibernate remains `ddl-auto=validate`.
- PostgreSQL Testcontainers is used for database-backed authentication tests.
- No JWT, database alternative, public endpoint, or API path is added without a
  specification change and approval.
- Existing tests are updated with explicit test principals where protection is
  the only behavior change; they are not weakened or disabled.

## Dependency graph

```text
PLAN-W7-01 security baseline and first RED test
        ↓
PLAN-W7-02 database authentication
        ↓
PLAN-W7-03 role authorization
        ↓
PLAN-W7-04 admin user management
        ↓
PLAN-W7-05 request observability
        ↓
PLAN-W7-06 verification and delivery
        ↓
PLAN-W7-07 BCrypt boundary hardening
```

The sequence is intentionally linear at the capability level. Documentation and
additional test cases inside a slice may be prepared in parallel, but production
configuration, migrations, and shared error changes are integrated one slice at
a time to keep failures attributable.

## PLAN-W7-01: Security baseline and first TDD cycle

**Requirements:** REQ-110, REQ-111, REQ-115, REQ-116, REQ-120
**Acceptance:** AC-110, AC-111, AC-116, AC-117, AC-120

1. Run the Week 6 regression suite and record the environment result.
2. Add `SecurityAccessIntegrationTest` first. Its initial assertion requires an
   unauthenticated `GET /api/products` to return 401; run it against the unchanged
   baseline and record the expected RED result in `docs/tdd-evidence.md`.
3. Add Spring Security runtime and test dependencies.
4. Add the minimum stateless HTTP Basic `SecurityFilterChain`: CSRF disabled,
   `GET /api/info` and `GET /actuator/health` public, every other request
   authenticated by default.
5. Prevent Spring Boot from creating or logging a generated default password.
   The temporary authentication boundary must deny unknown credentials and must
   not embed a development password; database identity replaces it in
   PLAN-W7-02.
6. Add explicit test principals to existing full-stack product tests so their
   business assertions continue past the new authentication boundary.
7. Re-run the focused test for GREEN, then public/default-deny checks and the
   full regression suite. Record actual RED and GREEN commands and outcomes.

**Checkpoint:** Public health/info remain 200, unauthenticated product access is
401 with a Basic challenge, no generated password appears in captured startup
logs, and existing product behavior remains green for an authorized test
principal.

## PLAN-W7-02: Database-backed authentication

**Module:** `database-authentication`
**Depends on:** PLAN-W7-01
**Contract:** `docs/specs/product-catalog/07-database-authentication.md`
**Requirements:** REQ-121–REQ-129
**Acceptance:** AC-121–AC-127
**Gate:** approved under standing user approval on 2026-09-09

1. RED: extend the PostgreSQL Flyway test to require `app_users`, `roles`,
   `app_user_roles`, their constraints/indexes, and the three canonical role
   rows. GREEN: add forward-only `V2__create_security_tables.sql`.
2. RED: add repository integration tests for case-insensitive lookup, role
   loading, persisted enabled state, and lower-case duplicate rejection. GREEN:
   add `ApplicationRole`, user/role entities, and repositories matching V2.
3. RED: add focused `UserDetailsService` unit tests for principal, authority,
   enabled-state, and unknown-user mapping. GREEN: implement the database
   adapter and central `ROLE_` conversion.
4. RED: add Testcontainers-backed HTTP Basic tests for valid mixed-case login,
   invalid password, unknown username, disabled user, and stored BCrypt form.
   GREEN: configure `BCryptPasswordEncoder`, let Spring wire the database
   `UserDetailsService`, and remove the temporary deny-all identity bean.
5. RED: add dev-seeder tests proving it creates nothing without all passwords
   and creates one BCrypt user per role when configured. GREEN: add typed seed
   properties and a `dev`-only idempotent seeder with environment-only secrets.
6. Remove committed database-password defaults, document required environment
   variables in configuration comments/docs, then run focused suites, the full
   Maven gate, a generated-password check, credential scan, and diff review.

**Checkpoint:** A database user authenticates with BCrypt; unknown, invalid, and
disabled users receive indistinguishable 401 responses; no generated or hardcoded
development credential is present.

## PLAN-W7-03: Role authorization

**Module:** `role-authorization`
**Depends on:** PLAN-W7-02
**Contract:** `docs/specs/product-catalog/08-role-authorization.md`
**Requirements:** REQ-130–REQ-137
**Acceptance:** AC-130–AC-136
**Gate:** approved under standing user approval on 2026-09-09

1. VIEWER RED→GREEN: prove create currently reaches the controller, then add
   VIEWER product reads and EDITOR-only product-write matchers. Expand VIEWER
   denials only after the first GREEN.
2. EDITOR RED→GREEN: prove delete currently reaches business routing, then add
   the ADMIN-only delete and `/api/admin/**` matchers. Expand EDITOR allowed and
   denied scenarios after the first GREEN.
3. ADMIN RED→GREEN: show the interim EDITOR-only write matcher rejects ADMIN,
   then explicitly add ADMIN to product read/write and operational matchers.
   Verify ADMIN passes the admin boundary to the current routing 404.
4. Method-security RED→GREEN: invoke the proxied product service as EDITOR and
   prove deletion reaches business logic; enable method security and annotate
   `ProductService.delete` for ADMIN.
5. Add public, operational/documentation, and restrictive-fallback coverage;
   run the focused authorization suite, full regression gate, controller role
   scan, credential scan, and complete diff review.

### PLAN-W7-03 approval

- [x] VIEWER, EDITOR, and ADMIN incremental TDD cycles approved
- [x] Product deletion method-security cycle approved
- [x] Focused/full verification approved
- [x] **PLAN-W7-03 approved under standing user approval on 2026-09-09**

**Checkpoint:** Every row of the endpoint matrix is covered, matcher order is
restrictive by default, and controllers contain no security decisions.

## PLAN-W7-04: ADMIN user management

**Module:** `admin-user-management`
**Depends on:** PLAN-W7-03
**Contract:** `docs/specs/product-catalog/09-admin-user-management.md`
**Requirements:** REQ-140–REQ-149
**Acceptance:** AC-140–AC-147
**Gate:** approved under standing user approval on 2026-09-09

1. Add service tests first for safe creation, username trimming, BCrypt input,
   role resolution, case-insensitive duplicate detection, and response mapping;
   implement the DTOs, exceptions, and transactional service.
2. Add service tests first for enable/disable, missing user, and case-insensitive
   self-disable prevention; implement the minimum state transition.
3. Add HTTP tests first for ADMIN 201, validation 400, duplicate 409, missing
   404, self-disable 409, and non-ADMIN denial; implement the controller and
   exception mappings.
4. Add a PostgreSQL/HTTP flow proving a created user's assigned role authenticates
   immediately and disabling that user makes the next request return 401.
5. Run focused/full verification and scan response DTOs, JSON, logs, and source
   for password/hash disclosure.

### PLAN-W7-04 approval

- [x] Service and HTTP TDD slices approved
- [x] PostgreSQL immediate-authentication flow approved
- [x] Error mapping and sensitive-data checks approved
- [x] **PLAN-W7-04 approved under standing user approval on 2026-09-09**

**Checkpoint:** Only ADMIN can manage users, persisted passwords are BCrypt
hashes, responses never include password fields, and disabled users immediately
fail their next authentication attempt.

**Result:** Implemented and verified on 2026-09-09. The focused security/admin
suite passed 35 tests and the full Maven gate passed 148 tests. Java 21 runtime
verification remains pending because the local machine currently provides Java
26 while compilation targets `--release 21`.

## PLAN-W7-05: Request observability and security errors

**Module:** `request-observability`
**Depends on:** PLAN-W7-02, PLAN-W7-03, PLAN-W7-04
**Contract:** `docs/specs/product-catalog/10-request-observability.md`
**Requirements:** REQ-150–REQ-163
**Acceptance:** AC-150–AC-160
**Gate:** approved under standing user approval on 2026-09-09

1. RED: add focused filter tests for exact UUID reuse, invalid/missing
   replacement, response-header propagation, MDC availability, exception-safe
   cleanup, and consecutive-request isolation. GREEN: implement the outer
   once-per-request trace and request-completion filter.
2. RED: extend public HTTP security tests to require a common trace-aware 401
   body and Basic challenge. GREEN: add a JSON authentication entry point backed
   by the shared error-response factory.
3. RED: extend role and product error flows to require matching trace header/body
   for 403 and application errors. GREEN: add the access-denied handler and
   migrate the global handler from `errorReferenceId` to request `traceId`.
4. RED: capture request/security logs and require safe request completion,
   authentication-failure, and access-denied events without headers,
   credentials, bodies, or normal-4xx stack traces. GREEN: add safe identity
   capture and bounded control-character sanitization.
5. RED: capture successful product, stock, and user event output plus unexpected
   failure output. GREEN: add minimal service-layer events and remove generic
   exception message/stack logging.
6. Document the three required debugging scenarios with repeatable trace-aware
   test evidence, then run focused/full tests, credential/body/log scans, MDC
   checks, and complete diff review.

### PLAN-W7-05 approval

- [x] Trace filter and MDC lifecycle TDD slice approved
- [x] Shared 401/403/application-error migration approved
- [x] Safe request, security, domain, and unexpected-error logging approved
- [x] Debugging notes and final log scans approved
- [x] **PLAN-W7-05 approved under standing user approval on 2026-09-09**

**Checkpoint:** Every response has a valid trace ID in its header and error body
where applicable; MDC never leaks; 401/403 use the common envelope; sensitive
values are absent from captured logs.

**Result:** Implemented and verified on 2026-09-10. Focused observability tests
passed, the full gate passed 155 tests, exception paths now log an effective 500,
and captured PostgreSQL constraint failures no longer expose constraint or row
details. Java 21 runtime verification remains pending; compilation uses
`--release 21` on the available Java 26 runtime.

## PLAN-W7-06: Verification and delivery

**Module:** `security-verification-delivery`
**Depends on:** PLAN-W7-01 through PLAN-W7-05
**Contract:** `docs/specs/product-catalog/11-security-verification-delivery.md`
**Requirements:** REQ-170–REQ-178
**Acceptance:** AC-170–AC-175
**Gate:** approved under standing user approval on 2026-09-10

1. Audit the existing PostgreSQL/HTTP suites against the complete Exercise 6
   matrix. Add tests only for uncovered public behavior; do not duplicate
   already-proven role or authentication flows.
2. Complete `docs/tdd-evidence.md` with authentic Week 7 RED→GREEN results,
   including observability and security findings from the final full-suite run.
3. Update README, authenticated curl examples, environment-variable setup,
   trace behavior, error examples, debugging notes, and Week 7 self-review.
4. Run focused security/observability suites followed by `./mvnw clean verify`.
   Record test count, JAR output, runtime JDK, and Java 21 release target.
5. Scan the worktree and test reports for secrets, generated passwords,
   passwords/hashes in JSON, Authorization values, SQL failure details, unsafe
   request bodies, controller role logic, and formatting defects.
6. Inspect the complete scoped diff and prepare an outcome summary. Leave
   commit, push, and pull-request actions pending explicit user authorization.

### PLAN-W7-06 approval

- [x] Integration-matrix audit and non-duplicative testing approved
- [x] TDD evidence, README, examples, and self-review updates approved
- [x] Full build, sensitive-output scans, and complete diff review approved
- [x] Git publication boundary preserved
- [x] **PLAN-W7-06 approved under standing user approval on 2026-09-10**

**Result:** Implemented on 2026-09-10. The Exercise 6 matrix maps to existing
PostgreSQL-backed HTTP tests, the approved optimistic-lock response/precondition
contract was restored, secured usage and self-review documentation are current,
and the final Maven gate passed 156 tests on OpenJDK 21.0.12.1. Git publication
remains an explicit pending delivery boundary.

**Checkpoint:** Clean Java 21 verification, documented role examples, no secrets
or sensitive logs, complete evidence, and traceability from every Week 7
requirement to tests and implementation.

## PLAN-W7-07: BCrypt boundary hardening

**Module:** `admin-user-management`
**Depends on:** PLAN-W7-06
**Contract:** `docs/specs/product-catalog/09-admin-user-management.md`
**Requirements:** REQ-141
**Acceptance:** AC-143
**Gate:** approved by the user on 2026-09-10

1. RED: add an ADMIN API test proving that a password within the character cap
   but beyond BCrypt's 72-byte UTF-8 boundary returns 400 with a `password`
   field error.
2. GREEN: add the minimum reusable Bean Validation constraint and apply it to
   user-creation passwords without changing the existing character limits.
3. REFACTOR: keep encoding details out of the controller and service, run the
   focused test, then rerun the full quality gate under Java 21.
4. Inspect the complete scoped diff, commit the Week 7 deliverable, push the
   branch, and perform a post-push comparison review.

**Checkpoint:** Distinct passwords cannot authenticate as one another merely
because their UTF-8 encodings share the first 72 bytes.

**Result:** Implemented and verified on 2026-09-10. The focused ADMIN suite
passed 10 tests and the full Java 21 gate passed 157 tests. Password validation
now preserves the existing character rules while rejecting UTF-8 input beyond
BCrypt's 72-byte boundary.

## Verification commands

```bash
# First required RED/GREEN behavior
./mvnw -Dtest=SecurityAccessIntegrationTest#givenNoCredentials_whenGetProducts_thenReturns401 test

# Module-focused suites (class names finalized in approved tasks)
./mvnw -Dtest='*Security*Test,*User*Test,*Trace*Test' test

# Full quality gate
./mvnw clean verify
```

Testcontainers-backed commands require a working Docker daemon. The final full
quality gate must use a Java 21 JDK, matching the project contract.

## Risks and mitigations

| Risk | Mitigation |
| --- | --- |
| Security auto-configuration changes every integration test to 401 | Introduce the first boundary in one slice and add explicit test principals only where existing tests must exercise product behavior |
| A temporary default user leaks a generated password | Supply an explicit deny-all authentication boundary in PLAN-W7-01 and replace it with database authentication in PLAN-W7-02 |
| Matcher order accidentally exposes Actuator or documentation endpoints | Keep only two exact public matchers and finish with `anyRequest().authenticated()`; test unmapped and documentation paths |
| Database roles and Spring authorities diverge | Store canonical role names and centralize the `ROLE_` mapping in the authentication adapter |
| Disabled-user state is stale | Load account state for each stateless Basic authentication attempt and cover immediate post-disable behavior in an HTTP integration test |
| Security failures bypass the application error handler | Use dedicated authentication-entry-point/access-denied adapters backed by the shared error representation |
| MDC data leaks between reused request threads | Clear the MDC in `finally` and prove isolation with consecutive-request tests |
| Tests pass only with the wrong local JDK or unavailable Docker | Record toolchain checks early; use Java 21 and verify Docker before final Testcontainers runs |
| One large security commit hides regressions | Complete and verify one approved task at a time; inspect the diff between slices |

## Approval gate

- [x] Dependency order and vertical slices approved
- [x] Temporary deny-all authentication boundary approved
- [x] Existing-test authentication strategy approved
- [x] Risks and verification checkpoints approved
- [x] **Week 7 implementation plan approved on 2026-09-09 — task breakdown may begin**

### PLAN-W7-02 approval

- [x] Migration-first identity schema slice approved
- [x] Repository, user-loading, and HTTP Basic TDD slices approved
- [x] Environment-only development seeding slice approved
- [x] Verification and credential-removal work approved
- [x] **PLAN-W7-02 approved under standing user approval on 2026-09-09**

## Historical note

The Week 5/6 implementation plan is complete on the approved Week 6 baseline.
Its detailed plan remains available in Git history; this file now tracks the
active Week 7 initiative.
