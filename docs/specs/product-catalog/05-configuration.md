# Configuration and Profiles Contract

**Status:** Draft — governed by `SPEC.md`  
**Covers:** `REQ-070`–`REQ-080`, `AC-070`–`AC-075`

## Requirements

### REQ-070: CatalogProperties

Type-safe configuration with prefix `catalog`:

- `lowStockThreshold` (maps from `low-stock-threshold`)
- `maximumProducts` (maps from `maximum-products`)
- `defaultCategory` (maps from `default-category`)

Enable via `@ConfigurationProperties` and `@EnableConfigurationProperties`.

### REQ-071: Profile configuration

| Profile | low-stock-threshold | maximum-products |
| --- | --- | --- |
| dev (`application-dev.yml`) | 10 | 1000 |
| test (`application-test.yml`) | 2 | 20 |

Base `application.yml` provides sensible defaults.

### REQ-072: Environment override

`maximum-products` shall be overridable via environment variable
(`CATALOG_MAXIMUM_PRODUCTS` or `${CATALOG_MAXIMUM_PRODUCTS:default}` pattern).

### REQ-073: Low-stock endpoint

`GET /api/products/low-stock` returns active products where
`stockQuantity <= catalog.lowStockThreshold`. HTTP 200 with JSON array.

### REQ-074: Maximum products guard

Product creation shall be rejected when the repository count reaches
`catalog.maximumProducts`.

### REQ-075: Safe configuration exposure

Do not expose raw configuration values through public API endpoints unless safe
for clients. Threshold and limit values stay internal.

## Acceptance criteria

### AC-070: Low-stock filtering

**Given** active products with stock 0, 2, and 5 and test profile threshold 2,
**when** `GET /api/products/low-stock` is called, **then** only active products
with stock <= 2 are returned.

### AC-071: Inactive excluded from low-stock

**Given** an inactive product below threshold, **when** low-stock is queried,
**then** that product is excluded.

### AC-072: Max products enforced

**Given** repository at maximum count for the active profile, **when** create is
attempted, **then** creation is rejected with HTTP 409.

### AC-073: Env override wins

**Given** `CATALOG_MAXIMUM_PRODUCTS=5`, **when** creating a sixth product,
**then** creation is rejected regardless of yaml default.

### AC-074: Profile activation

**Given** `--spring.profiles.active=dev`, **when** low-stock threshold applies,
**then** dev values from `application-dev.yml` are used.

## Testing focus

- Service tests with injected `CatalogProperties` for threshold and limit
- Integration tests with `@ActiveProfiles("test")`
