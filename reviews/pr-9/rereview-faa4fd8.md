# PR #9 re-review — commit `faa4fd8` (round 2 follow-up)

**Branch:** `week6-exercise-6-docs-delivery`  
**PR:** https://github.com/Harsha-T-G/Java-Exercises/pull/9  
**Baseline addressed:** mergemitra comments at head `fa73cdfc12cb83ed5372845a19860476066ce105`  
**This pass:** additional fixes on top of `faa4fd8` before next push

## Summary

Round 1 (`77e11fd`, `fa73cdf`, `faa4fd8`) closed the highest-risk correctness items (price precision, malformed JSON, flush-before-response, pagination tie-breaker, SKU constraint mapping, SERIALIZABLE regression). This document tracks round-2 follow-ups from the `fa73cdf` re-review and what was done in the current working tree.

## Fixed in this pass

| # | Issue | Resolution |
|---|--------|------------|
| 1 | `show-details: always` exposes full health to unauthenticated callers | Base `application.yml`: `show-components: always`, `show-details: never` — DB component visible without leaking connection details |
| 2 | Invalid sort direction reported as invalid field | New `InvalidSortDirectionException`; dedicated handler; factory unit test updated |
| 3 | Five-arg pagination on service | `ProductSearchCriteria` record; controller builds criteria object |
| 4 | Duplicate Java defaults in `CatalogProperties` | Removed field initializers; YAML remains single source |
| 5 | Category filter locale sensitivity | `ProductSpecifications` uses `Locale.ROOT` for lowercasing |
| 6 | `@Digits` message/docs mismatch | Validation message covers integer + fraction; README updated |
| 7 | `fieldErrors` null in error envelope | `ErrorResponse` normalizes null to empty list |
| 8 | JDBC pool timeouts | Hikari `connection-timeout` / `validation-timeout` in base config |
| 9 | Tie-breaker not exercised end-to-end | `ProductIntegrationTest` — equal `name` sort with page size 1 |
| 10 | Optimistic lock only at repository layer | `ProductIntegrationTest` — concurrent stock PATCH returns 200 + 409 |
| 11 | Shared test payloads | `ProductTestFixtures`; used in controller + integration tests |

## Already fixed in `faa4fd8` (unchanged)

| Issue | Status |
|--------|--------|
| SERIALIZABLE concurrent create → 500 | Reverted to default isolation |
| SKU constraint message fallback | Cause-chain walk only in `ProductPersistenceSupport` |
| Sensitive data in 500 logs | Log exception type + reference id only |
| Pagination `id ASC` tie-breaker | `ProductPageRequestFactory` + unit tests |
| `saveAndFlush` before mapping writes | All create/update/stock paths |
| Integration cleanup | `productRepository.deleteAll()` |
| Compose localhost bind + placeholder `.env` | Done |
| `ProductPersistenceSupportIntegrationTest` | Real duplicate SKU + rethrow paths |

## Partially addressed / accepted deferrals

| Issue | Notes |
|--------|--------|
| Split read vs write repository usage | Writes route through `ProductPersistenceSupport`; reads still use repository directly — acceptable for exercise scope |
| `If-Match` / client version preconditions | Response includes `version`; clients can retry on 409; no ETag contract yet |
| `/api/v1` versioning | Week 6 exercise uses `/api/products`; breaking list→page envelope documented in README |
| Spring Security / rate limiting | Out of Week 6 brief |
| `@WebMvcTest` slice tests | Full `@SpringBootTest` + Testcontainers chosen for DB-backed controller tests |
| Low-stock unbounded list | Acceptable at configured catalog scale |
| `SPEC.md` / `AGENTS.md` drift | Docs still describe pre-PostgreSQL state; reconcile in separate docs PR |
| Docker socket hardcoding | Required for Docker 29 + local Testcontainers; noted in SELF_REVIEW |

## Verification

```bash
cd Task14-Product-Catalog
./mvnw clean test
```

Expected: BUILD SUCCESS; test count recorded in `docs/test-evidence.txt` after run.

## Suggested PR reply (mergemitra threads)

- **Health exposure:** Switched to `show-components: always` + `show-details: never`.
- **Sort direction:** Dedicated exception and handler; no longer masquerades as invalid field.
- **Optimistic lock:** Concurrent stock PATCH integration test asserts HTTP 409 path.
- **Pagination criteria:** Introduced `ProductSearchCriteria` record.
- **Defer:** API versioning, Spring Security, `If-Match` — follow-up enhancements outside Week 6 delivery.
