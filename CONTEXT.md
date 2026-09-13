# Product Catalog Domain Context

This glossary gives each term one stable meaning. Product behavior belongs in
`SPEC.md`; HTTP and implementation details belong in specs and code.

## Source ranking

1. Current user instructions.
2. Human-approved `SPEC.md` and linked `docs/specs/product-catalog/` chunks.
3. Week 5 exercise brief (`harsha_week5_spring_boot_fundamentals_exercises.txt`).
4. This glossary.
5. Tests and implementation as evidence of current behavior.
6. AI notes and prior-session claims, which are untrusted until verified.

The exercise brief is historical input. `SPEC.md` is the live product authority
once approved.

## Terms

- **Product** — JPA entity (`ProductEntity`) persisted in PostgreSQL. Holds UUID id,
  SKU, name, category, price, stock quantity, active flag, optimistic-lock version,
  and timestamps. Not exposed directly through the REST API.
- **SKU** — business identifier string for a product. Required, unique across
  the catalog with case-insensitive comparison. Immutable identity for business
  deduplication; distinct from the UUID primary key.
- **Product request** — caller input DTO for create and update operations.
  Contains editable fields only (no server-generated id).
- **Product response** — API output DTO including the product id and all fields
  clients need to display or follow links.
- **Active product** — a product with `active=true`. Inactive products are stored
  and retrieved normally but are excluded from low-stock reporting.
- **Low-stock product** — an active product whose stock quantity is less than or
  equal to the configured catalog low-stock threshold.
- **Catalog properties** — type-safe configuration (`catalog.*`) for
  low-stock threshold, maximum products, and default category. Values may vary
  by Spring profile or environment variable override.
- **Repository** — Spring Data JPA persistence boundary (`ProductRepository`) with
  Specifications for filtered queries. Flyway owns schema; Hibernate validates only.
- **Service** — owns business rules (SKU uniqueness, product limits, mapping).
  Controllers delegate here; repositories do not enforce business rules.
- **Controller** — HTTP adapter only. Validates request shape via Bean
  Validation, maps HTTP status and headers, and delegates to the service.
- **Error response** — consistent JSON envelope for all API failures: timestamp,
  status, error, message, path, trace ID, and a field-level validation error list.
- **Application user** — database identity used by HTTP Basic authentication.
  A user has a UUID, case-insensitive username identity, BCrypt password hash,
  enabled flag, creation timestamp, and one or more roles. It is not a REST
  response model.
- **Role** — one canonical database permission group: `VIEWER`, `EDITOR`, or
  `ADMIN`. Spring Security exposes these as `ROLE_VIEWER`, `ROLE_EDITOR`, and
  `ROLE_ADMIN` authorities.
- **Password hash** — one-way BCrypt output stored for an application user.
  Authentication compares a submitted password to this value; it is never
  decoded, returned, or logged.
- **Disabled user** — an application user whose persisted `enabled` flag is
  false. The identity remains stored but cannot authenticate.

## Important non-equivalences

- Request validation (Bean Validation on DTOs) is not business-rule validation
  (duplicate SKU, product limit). The former runs in the web layer; the latter
  runs in the service.
- SKU uniqueness is case-insensitive; UUID identity is exact match.
- Product id (UUID) is server-generated and never changes on update.
- Configuration properties are not domain entities; do not expose raw config
  values through public API endpoints unless safe for clients.
- Actuator health reflects application readiness; `/api/info` status is
  application metadata for the exercise, not a substitute for Actuator health.
- Username identity is case-insensitive; retained username spelling is display
  data, not a separate account.
- A role is an authorization assignment, not an authenticated user and not a
  password-storage record.

## Exercise branch checkpoints

Work proceeds on incremental branches (one per exercise), merged forward:

```text
exercise-1-setup → exercise-2-layers → exercise-3-validation →
exercise-4-errors → exercise-5-config → exercise-6-tests
```

Each branch must keep `./mvnw verify` green before advancing.
