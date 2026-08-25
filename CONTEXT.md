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

- **Product** — internal domain entity stored in the in-memory repository. Holds
  UUID id, SKU, name, category, price, stock quantity, and active flag. Not
  exposed directly through the REST API.
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
- **Repository** — persistence boundary abstraction. The exercise uses an
  in-memory `ConcurrentHashMap` implementation; callers depend on the interface.
- **Service** — owns business rules (SKU uniqueness, product limits, mapping).
  Controllers delegate here; repositories do not enforce business rules.
- **Controller** — HTTP adapter only. Validates request shape via Bean
  Validation, maps HTTP status and headers, and delegates to the service.
- **Error response** — consistent JSON envelope for all API failures: timestamp,
  status, error, message, path, and optional field-level validation errors.

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

## Exercise branch checkpoints

Work proceeds on incremental branches (one per exercise), merged forward:

```text
exercise-1-setup → exercise-2-layers → exercise-3-validation →
exercise-4-errors → exercise-5-config → exercise-6-tests
```

Each branch must keep `./mvnw verify` green before advancing.
