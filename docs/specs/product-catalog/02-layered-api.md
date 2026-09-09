# Layered API Contract

**Status:** Implemented — Week 6 PostgreSQL + pagination (governed by `SPEC.md`)  
**Covers:** `REQ-010`–`REQ-020`, `AC-010`–`AC-015`

## Requirements

### REQ-010: Layered components

Implement `ProductController`, `ProductService`, `ProductRepository` (Spring Data JPA),
`ProductEntity`, `ProductEntityMapper`, `ProductRequest` DTO, and
`ProductResponse` DTO with constructor injection between layers.

Stereotypes: `@RestController`, `@Service`, `@Repository`. Use `@Component` only
when no more specific stereotype applies.

### REQ-011: JPA repository

Persist products in PostgreSQL via Spring Data JPA. Provide save, find by id,
paginated find with Specifications, case-insensitive SKU existence check, delete
by id, count, and low-stock query. Flyway migration `V1__create_products_table.sql`
owns the schema.

### REQ-012: Create product

`POST /api/products` shall generate UUID, persist the product, return HTTP 201
with `ProductResponse` body and `Location: /api/products/{id}` header.

### REQ-013: List products (paginated)

`GET /api/products` shall return HTTP 200 with a **page envelope**:

```json
{
  "content": [ /* ProductResponse[] */ ],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0
}
```

Supports query parameters: `page`, `size`, `sort`, `category`, `active`.

> **Week 6 breaking change:** Week 5 returned a bare JSON array. Week 6 clients
> must parse the envelope above.

### REQ-014: Get product by id

`GET /api/products/{id}` shall return HTTP 200 with the matching product when
found. Missing product handling is defined in chunk 03/04.

### REQ-015: Update product

`PUT /api/products/{id}` shall replace editable fields, preserve id from path,
return HTTP 200 with updated `ProductResponse`. When the request body includes
`version`, it must match the persisted row or the API returns HTTP 409.

### REQ-016: Delete product

`DELETE /api/products/{id}` shall remove the product and return HTTP 204 with no
body.

### REQ-017: Separation of concerns

Controllers shall not access repositories or encode business rules. No static
mutable collections. Use `ResponseEntity` when status or headers require control.
Map between DTOs and entity via `ProductEntityMapper`.

### REQ-018: Stock adjustment

`PATCH /api/products/{id}/stock` with body `{ "adjustment": <int>, "version": <optional> }`
shall adjust stock by delta. Zero adjustment → 400. Resulting quantity below zero → 400.
Optional `version` mismatch → 409.

### REQ-019: Low-stock list

`GET /api/products/low-stock` returns active products at or below the configured
threshold as a JSON array.

## Acceptance criteria

### AC-010: Create with Location

**Given** valid product input, **when** `POST /api/products` is called, **then**
response is 201, body includes generated id, and `Location` header points to the
new resource.

### AC-011: List empty and populated (page envelope)

**Given** zero or more products, **when** `GET /api/products` is called, **then**
response is 200 with a page envelope whose `content` array matches the requested
page of stored products.

### AC-012: Get by id

**Given** an existing product id, **when** `GET /api/products/{id}` is called,
**then** response is 200 with the correct product.

### AC-013: Update preserves id

**Given** an existing product, **when** `PUT /api/products/{id}` is called with
new field values, **then** response is 200, id unchanged, fields updated.

### AC-014: Delete no content

**Given** an existing product, **when** `DELETE /api/products/{id}` is called,
**then** response is 204 and subsequent get returns not found (after chunk 04).

### AC-015: DTO boundary

**Given** any successful API response, **when** inspecting JSON, **then** internal
JPA entity type is not exposed; only `ProductResponse` fields appear.

## Testing focus

- Repository tests with Testcontainers for persistence and constraints
- Service tests with mocked repository; service integration tests with real DB
- `@WebMvcTest` for controller HTTP contract with mocked service
- `@SpringBootTest` integration tests for end-to-end persistence paths
