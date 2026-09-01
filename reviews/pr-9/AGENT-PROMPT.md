# PR #9 — Agent fix task

Address mergemitra review comments on `week6-exercise-6-docs-delivery` → `task14-main`.

## Scope

Fix correctness and test-quality items from `comments.md` without breaking Week 5/6 exercise API paths (`/api/products`, not `/api/v1`).

## Verification

```bash
cd Task14-Product-Catalog
docker info
./mvnw clean test
```

## Out of scope (document / dismiss)

- Spring Security and rate limiting (no security stack in exercise brief)
- `/api/v1` versioning (exercise spec uses `/api/products`)
- `If-Match` client preconditions (future enhancement)
- `ProductSearchRequest` refactor (nice-to-have)
- Pinning `npx skills` in setup script (agent bootstrap only)

## After fixes

Push to `week6-exercise-6-docs-delivery` and reply on resolved PR threads.
