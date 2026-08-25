# Layered API Contract

**Status:** Implemented — governed by `SPEC.md`  
**Covers:** `REQ-010`–`REQ-020`, `AC-010`–`AC-015`

## Requirements

### REQ-010: Layered components

Implement `ProductController`, `ProductService`, `ProductRepository` (interface),
`InMemoryProductRepository`, `Product` model, `ProductRequest` DTO, and
`ProductResponse` DTO with constructor injection between layers.

Stereotypes: `@RestController`, `@Service`, `@Repository`. Use `@Component` only
when no more specific stereotype applies.

### REQ-011: In-memory repository

Store products in `ConcurrentHashMap<UUID, Product>`. Provide save, find by id,
find all, case-insensitive SKU existence check, delete by id, and count. Do not
expose the internal map or mutable collections to callers.

### REQ-012: Create product

`POST /api/products` shall generate UUID, persist the product, return HTTP 201
with `ProductResponse` body and `Location: /api/products/{id}` header.

### REQ-013: List products

`GET /api/products` shall return HTTP 200 with all products; empty list when none
exist.

### REQ-014: Get product by id

`GET /api/products/{id}` shall return HTTP 200 with the matching product when
found. Missing product handling is defined in chunk 03/04.

### REQ-015: Update product

`PUT /api/products/{id}` shall replace editable fields, preserve id from path,
return HTTP 200 with updated `ProductResponse`.

### REQ-016: Delete product

`DELETE /api/products/{id}` shall remove the product and return HTTP 204 with no
body.

### REQ-017: Separation of concerns

Controllers shall not access repositories or encode business rules. No static
mutable collections. Use `ResponseEntity` when status or headers require control.
Map between DTOs and model via dedicated mapper or clearly named private methods.

## Acceptance criteria

### AC-010: Create with Location

**Given** valid product input, **when** `POST /api/products` is called, **then**
response is 201, body includes generated id, and `Location` header points to the
new resource.

### AC-011: List empty and populated

**Given** zero or more products, **when** `GET /api/products` is called, **then**
response is 200 with a JSON array matching stored products.

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
`Product` type is not exposed; only `ProductResponse` fields appear.

## Testing focus

- Repository unit tests for save/find/delete/defensive copies
- Service tests with mocked repository (expanded in chunk 06)
- Controller MockMvc tests for status codes and Location header
