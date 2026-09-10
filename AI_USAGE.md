# AI Usage Record — Product Catalog

Track material AI-assisted work for exercise submission and self-review.

## Session log

| Date | Phase | Prompt / intent | Outcome | Verified |
| --- | --- | --- | --- | --- |
| 2026-08-26 | Specify | Agentic boilerplate + draft spec/plan for Week 5 Spring Boot exercise | Created Task14-Product-Catalog scaffold, SPEC, plans, skills, guidelines | `./mvnw verify` pending |
| 2026-09-09 | Scope check | Decompose the Week 7 Spring Security and logging brief | Created a six-module capability map on `week7-security-logging` from the Week 6 delivery baseline | Human approved module boundaries and build order |
| 2026-09-09 | Specify | Define the `security-contract` module before adding Spring Security | Approved `docs/security-spec.md` and linked REQ-110–REQ-120 from `SPEC.md` | Human approved the contract and four security decisions |
| 2026-09-09 | Plan | Design the Week 7 implementation sequence without mixing all capabilities into one change | Approved the dependency-ordered Week 7 initiative plan with TDD and regression checkpoints | Human approved the plan and implementation strategy |
| 2026-09-09 | Tasks | Break the first security boundary into reviewable TDD slices | Approved TASK-W7-001 through TASK-W7-004 for the first RED, minimum security configuration, regression authentication, and verification | Human approved implementation start |
| 2026-09-09 | Implement | Establish the first restrictive Spring Security boundary with authentic TDD evidence | Added Spring Security, stateless HTTP Basic, exact public matchers, deny-all temporary identity, focused access tests, and authenticated regression principals | RED 200 vs 401 captured; focused security and regression suites passed |
| 2026-09-09 | Verify | Run the PLAN-W7-01 full quality gate and inspect sensitive output | Corrected the info-controller web slice to import production security; final suite and diff/security scans passed | 100 tests passed on Java 26 using `--release 21`; Java 21 runtime rerun pending |
| 2026-09-09 | Specify/Plan | Define database-backed authentication under the user's standing approval | Added the user/role schema, BCrypt, disabled-user, non-revealing failure, and dev-seeding contract plus six implementation tasks | Contract, plan, and tasks approved before source changes |
| 2026-09-09 | Implement | Build PostgreSQL-backed HTTP Basic authentication test-first | Added Flyway V2, user/role entities and repositories, database `UserDetailsService`, BCrypt, and environment-gated dev users | Four RED→GREEN cycles recorded; 26 focused tests passed |
| 2026-09-09 | Verify | Close PLAN-W7-02 | Removed committed password defaults, updated setup docs, scanned credentials/logs, and ran the full suite | 116 tests passed on Java 26 using `--release 21`; Java 21 runtime rerun pending |
| 2026-09-09 | Specify/Plan | Define role authorization under standing approval | Added explicit matcher precedence, role behavior, restrictive fallback, and product-delete method-security contract | Contract, plan, and five tasks approved before behavior changes |
| 2026-09-09 | Implement/Verify | Implement roles one RED→GREEN cycle at a time | Added explicit VIEWER/EDITOR/ADMIN matchers and ADMIN-only service deletion | Four authorization RED→GREEN cycles recorded; 131 tests passed |
| 2026-09-09 | Specify/Plan | Define ADMIN user management under standing approval | Added safe DTOs, validation, duplicate/self-disable policy, immediate-authentication requirements, and five executable tasks | Contract, plan, and tasks approved before source changes |
| 2026-09-09 | Implement/Verify | Implement ADMIN user management test-first | Added ADMIN-only create/enable APIs, BCrypt persistence, safe responses, domain errors, service defense in depth, and database-backed auth/disable flows | Two RED→GREEN cycles recorded; 35 focused and 148 full-suite tests passed |
| 2026-09-10 | Specify/Plan | Define request observability and common security errors | Approved trace lifecycle, shared error envelope, safe logging policy, debugging evidence, and six executable tasks | Contract and tasks approved before behavior changes |
| 2026-09-10 | Implement/Verify | Implement trace-aware errors and safe logging test-first | Added trace/MDC filter, JSON 401/403 adapters, safe request/security/domain logs, debugging notes, exception-status correction, and SQL-detail suppression | Focused cycles recorded; 155-test gate passed |
| 2026-09-10 | Specify/Deliver | Complete Exercise 6 verification and delivery | Approved delivery contract, audited the integration matrix, restored the approved optimistic-lock API contract, and updated secured examples/self-review | 156 tests passed on OpenJDK 21.0.12.1; scans clean; Git publication pending |
| 2026-09-10 | Verify | Close the required Java 21 runtime gate | Installed versioned Homebrew OpenJDK 21 without changing the global Java selection and pinned Maven to that JDK | Full Docker-backed suite passed: 156 tests and packaged JAR |
| 2026-09-10 | Review/Implement | Correct the pre-push BCrypt password-boundary finding | Clarified REQ-141, captured an HTTP RED at 201 instead of 400, then added reusable UTF-8 byte-length validation | Focused ADMIN suite passed 10 tests; full Java 21 gate passed 157 tests |

## TDD evidence

Record RED → GREEN cycles per task:

```text
TASK-xxx:
  RED:   ./mvnw -Dtest=... test — exit 1 — <reason>
  GREEN: ./mvnw -Dtest=... test — exit 0
```

## Accepted / rejected suggestions

- **Accepted:** Single repo with exercise branch checkpoints; Conventional Commits
  `feat(scope): subject` for commits and PR titles.
- **Rejected:** Spring Initializr (requires Boot 4.x online); manual Boot 3.4.2 pom instead.

## Agent errors and corrections

- Initializr returned 400 for Spring Boot 3.x — created pom manually with 3.4.2.

## Open items

- [x] Week 7 capability map approved
- [x] Human approval of the `security-contract` specification
- [x] Approve the Week 7 implementation plan
- [x] Prepare and approve session-sized tasks
- [x] Complete the first recorded RED → GREEN security behavior
- [x] Re-run the full suite on a Java 21 runtime
- [x] Specify, approve, implement, and verify `database-authentication`
- [x] Specify, approve, implement, and verify `role-authorization`
- [x] Specify, approve, implement, and verify `admin-user-management`
- [x] Specify, approve, implement, and verify `request-observability`
- [x] Specify, approve, implement, and verify `security-verification-delivery`
- [ ] Commit, push, and open the focused PR after explicit user authorization
