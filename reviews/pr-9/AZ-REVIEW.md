# Fresh A–Z review of PR #9 at faa4fd8

Nothing was posted on GitHub.

**Verdict:** Comment. No crash-class defect remains. Do not approve until the contract docs match the code. SERIALIZABLE, SKU mapping, flush-before-response, @Digits, malformed JSON → 400, and localhost compose bind are closed and were not re-filed.

## A–Z (what holds)

| | Finding |
|---|---------|
| **A** Architecture | Controller → service → JPA is sound. `entity/` vs guidelines `model/`. |
| **B** Config | Env-based datasource, profiles, unused default-category. No @Validated on CatalogProperties. |
| **C** HTTP | CRUD + stock PATCH + page envelope. Unversioned `/api` is the exercise contract. |
| **D** DB | Flyway V1, CHECKs, LOWER(sku) unique index, ddl-auto=validate. |
| **E** Entity | Mapper + @Version + timestamps. Bean Validation tighter than VARCHAR widths. |
| **F** Filter/sort | id ASC tie-break is good. Default-locale toLowerCase() on category. Bad direction blamed on the field. |
| **G** Errors | Consistent envelope. 500 logs type-only, no stack, correlation id not returned. |
| **H** HTTP tests | ProductControllerTest is @SpringBootTest, not @WebMvcTest. No MockMvc 409. |
| **I** Testcontainers | Shared Postgres 16. Evidence file: 86 tests. Docker not re-run in this environment. |
| **J** Tx | Default isolation. Catalog-max count() is best-effort (documented). |
| **K** Accepted | No /api/v1, no Spring Security, no rate limits. |
| **L** Logs | 500s are un-debuggable after dropping the throwable. |
| **M** SKU | Cause-chain mapping is proven by IT. |
| **N** Guidelines | AGENTS.md / .guidelines still forbid JPA. |
| **O** Optimistic lock | version on GET only. Sequential PUT is last-write-wins. |
| **P** Persistence | Single saveAndFlush helper. Stock path correctly omits SKU remap. |
| **Q** Tests | Tree count 86. README/SELF_REVIEW still say 64. |
| **R** REST | REQ-013 still a JSON array; handler returns a page. |
| **S** Spec | SPEC.md still Draft Week 5 in-memory. |
| **T** Counts | 86 vs 64 vs PR body 64. |
| **U** Dead API | findBySkuIgnoreCase / findByCategoryIgnoreCase unused on the request path. |
| **V** Validation | @Digits, @Valid, zero-adjustment 400. |
| **W** Stock | Check then flush. Concurrent PATCH can 409 via @Version. |
| **X** Extra | reviews/pr-9/ inside the catalog tree is review notes, not product code. |
| **Y** Compose | Localhost bind + placeholder .env are good. Surefire pins /var/run/docker.sock. |
| **Z** Crash | None remaining. |

## Fresh comments (F01–F12)

**side:** RIGHT, **commit_id:** faa4fd8ba50bebf933e0373852b008674add9d7e

### F01 — must-fix — SPEC.md:13

SPEC.md is still the Week 5 contract: Status Draft, assumption 3 “in memory only”, pagination and JPA listed as out of scope, approval boxes unchecked.

This PR implements PostgreSQL, Flyway, ProductEntity, and a page envelope. AGENTS.md says SPEC.md is the behaviour authority and not to implement an unapproved spec.

Update SPEC.md (and the six chunks) for Week 6: persistence, pagination/filter/sort, stock PATCH, optimistic lock, Testcontainers. Resolve the open questions and mark approval honestly, or stop claiming the old spec governs this code.

### F02 — must-fix — AGENTS.md:23

“It deliberately has no database, JPA, or Lombok” is false for this branch. The Never list still forbids Spring Data JPA and a database.

Agents following AGENTS.md will treat this PR as a policy violation. Align the purpose, package layout (entity vs model), allowed dependencies, and Ask-first rules with Week 6. Same drift in CONTEXT.md (Product still defined as ConcurrentHashMap).

### F03 — must-fix — ProductController.java:43

GET /api/products returns ProductPageResponse { content, page, size, totalElements, totalPages }.

docs/specs/product-catalog/02-layered-api.md REQ-013 / AC-011 still require HTTP 200 with a JSON array. Existing Week 5 clients that json-parse a list will break.

Week 6 pagination can keep the envelope, but then change REQ-013/AC-011, the PR body (“preserving the existing REST API behaviour”), and any remaining array examples. Do not leave both contracts in the repo.

### F04 — must-fix — README.md:23

README says 64 tests must pass and points at docs/test-evidence.txt. That file says Tests run: 86. Count of @Test methods in src/test/java is 86.

Set the number to 86 in README (and the PR How to Test).

### F05 — must-fix — SELF_REVIEW.md:45

Verification still claims ./mvnw clean test — 64 tests, BUILD SUCCESS. docs/test-evidence.txt on this commit is 86 / 0 / 0.

Replace 64 with 86.

### F06 — should-fix — GlobalExceptionHandler.java:172

handleUnexpected allocates errorReferenceId, then logs only exception.getClass().getName() with no throwable, and does not put the id in ErrorResponse.

Keep the generic body (REQ-053). Pass exception as the last log.error argument so the stack stays in logs. Put errorReferenceId in the JSON so a 500 can be matched to a log line.

### F07 — should-fix — ProductController.java:63

ProductResponse.version is serialized. PUT and PATCH accept no version and no If-Match.

Sequential lost update: GET (v=1, name=A) → other writer commits name=B (v=2) → PUT of the stale body. update() does findById (loads v=2), applyUpdate, save. HTTP 200, B is gone. @Version only conflicts when two persistence contexts save the same loaded version.

Either accept version / If-Match on PUT and PATCH and return 409 on mismatch, or drop version from the public JSON and describe @Version as an internal concurrent-write guard.

### F08 — should-fix — ProductServiceIntegrationTest.java:135

givenStaleVersion_whenSecondSaveCommits_thenThrowsOptimisticLockingFailureException calls productRepository.saveAndFlush on two detached entities. It never goes through ProductService or MockMvc.

Add MockMvc: overlapping write (or a stubbed ObjectOptimisticLockingFailureException through the controller) returns 409 with the standard ErrorResponse. Add MockMvc for ProductLimitReachedException as well (AC-072).

### F09 — should-fix — ProductSpecifications.java:20

criteriaBuilder.lower(root.get("category")) is compared to category.toLowerCase() with the JVM default locale.

Use category.toLowerCase(Locale.ROOT).

### F10 — should-fix — ProductControllerTest.java:31

REQ-091 and .guidelines/java.md require @WebMvcTest + MockMvc for the controller HTTP contract. This class is @SpringBootTest + Testcontainers, same as ProductIntegrationTest.

Move status/Location/error-envelope cases to a slice test with a mocked ProductService. Also split givenProduct_whenAdjustStock_thenReturnsUpdatedQuantity: it PATCHes +5 and −3 in one method.

### F11 — should-fix — ProductPageRequestFactory.java:66

sort=name,sideways throws InvalidSortFieldException(field). Clients see “Sorting is not allowed by field 'name'”.

Throw a dedicated invalid-direction exception.

### F12 — should-fix — pom.xml:83

Surefire sets DOCKER_HOST=unix:///var/run/docker.sock. testcontainers.properties pins the same socket.

Remove the hardcoded host. Keep docker-java.properties api.version=1.44 if Desktop 29 still needs it.
