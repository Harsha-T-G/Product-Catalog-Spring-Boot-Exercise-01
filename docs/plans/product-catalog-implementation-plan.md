# Product Catalog Implementation Plan

**Status:** Week 7 implementation complete and verified on Java 21
**Branch:** `week7-security-logging`
**Contract:** `SPEC.md`, `docs/security-spec.md`, and the Week 7 exercise brief

## Outcome

Extend the Week 6 PostgreSQL catalog with database-backed HTTP Basic
authentication, VIEWER/EDITOR/ADMIN authorization, ADMIN user management,
consistent security errors, safe structured logging, request trace IDs, and
Testcontainers-backed tests. Preserve existing product behavior after a request
passes authorization.

## Implementation sequence

1. **PLAN-W7-01 — Security baseline:** add the first failing unauthenticated
   access test, then configure a restrictive `SecurityFilterChain`.
2. **PLAN-W7-02 — Database authentication:** add Flyway user/role tables,
   repositories, BCrypt, `UserDetailsService`, and environment-only dev users.
3. **PLAN-W7-03 — Role authorization:** implement VIEWER, EDITOR, and ADMIN
   permissions plus ADMIN method protection for product deletion.
4. **PLAN-W7-04 — User management:** add ADMIN-only user creation and
   enable/disable endpoints with safe validation and responses.
5. **PLAN-W7-05 — Observability:** add trace IDs, consistent 401/403 envelopes,
   safe request/application logs, and the required debugging notes.
6. **PLAN-W7-06 — Delivery:** complete HTTP integration coverage, README/API
   examples, TDD evidence, self-review, and the Java 21 quality gate.
7. **PLAN-W7-07 — BCrypt boundary:** reject passwords exceeding BCrypt's
   72-byte UTF-8 input limit before encoding.

Each behavior-changing slice follows RED → GREEN → REFACTOR. Detailed results
are retained only in the exercise-required `docs/tdd-evidence.md`.

## Verification

```bash
./mvnw -Dtest='*Security*Test,*User*Test,*Trace*Test' test
./mvnw clean verify
```

The final quality gate passed 158 tests using OpenJDK 21.0.12.1 and produced
`target/product-catalog-1.0.0-SNAPSHOT.jar`.

## Boundaries

- HTTP Basic is the required authentication mechanism; JWT remains optional.
- Only `/api/info` and `/actuator/health` are public.
- Credentials and sensitive request data must never be committed or logged.
- PostgreSQL and Flyway remain the persistence and schema boundaries.
