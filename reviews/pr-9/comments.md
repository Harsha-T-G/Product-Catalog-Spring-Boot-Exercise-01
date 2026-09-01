# PR #9 review comments — honest status

**Do not merge “Fixed” here with GitHub thread resolution without checking code.**

Full file-by-file list: [AZ-REVIEW.md](./AZ-REVIEW.md)  
Round-2 detail: [rereview-faa4fd8.md](./rereview-faa4fd8.md)

## Major correctness items

| # | Topic | Status | Notes |
|---|--------|--------|-------|
| 1 | Price vs PostgreSQL `NUMERIC(19,2)` | **Fixed** | `@Digits(17,2)` on `ProductRequest.price` |
| 2 | Malformed JSON → 500 | **Fixed** | `HttpMessageNotReadableException` → 400 |
| 3 | Stale `updatedAt`/`version` on writes | **Fixed** | `saveAndFlush` before `toResponse` |
| 4 | Pagination non-deterministic sort | **Fixed** | `id ASC` tie-breaker + tests |
| 5 | Health `db` component in dev/default | **Fixed** | `show-components: always` |
| 6 | Health details exposure | **Fixed** | `show-details: never` |
| 7 | SKU constraint → wrong 409 | **Fixed** | `ProductPersistenceSupport` constraint name chain |
| 8 | Integration test cleanup | **Fixed** | `ProductIntegrationTest` uses `deleteAll()` |
| 9 | Compose / `.env` exposure | **Fixed** | localhost + placeholders |
| 10 | SERIALIZABLE on create | **Fixed (revert)** | Was added then removed — caused concurrent 500 |

## Still open (worth fixing in Task14)

| # | Topic | Action |
|---|--------|--------|
| 11 | Max-products concurrent race | Document best-effort; optional DB counter later |
| 12 | `CatalogProperties` validation | Add `@Validated`, `@Min`, page size invariant |
| 13 | README / SELF_REVIEW test count | Align to actual `./mvnw test` count |
| 14 | `ProductControllerTest` cleanup | Use `deleteAll()` like integration test |
| 15 | Validation test for `@Digits` message | Assert field error text in controller test |
| 16 | Optimistic lock via HTTP | Concurrent PATCH test (local, verify before push) |

## Deferred — reply on PR, do not block Week 6 merge

- `/api/v1` versioning (exercise uses `/api/products`)
- Spring Security and rate limiting (not in brief)
- `If-Match` preconditions (409 on conflict is implemented)
- SPEC.md / AGENTS.md draft drift (separate docs commit)
- Low-stock unbounded list at exercise scale
- `@WebMvcTest` slice vs full SpringBootTest
- Pinning agent setup scripts / postgres image digests

## What the reviewer actually reported

- **Round 1:** 1 critical + 29 major + 34 nitpicks — “Do not merge as-is”
- **Round 2:** 10 major + 19 nitpicks — “Approve with suggestions” after first fixes
- **Not:** 20 compile errors in every class — the bot comments on almost every file in the diff
