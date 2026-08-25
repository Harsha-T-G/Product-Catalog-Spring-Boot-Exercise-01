# Validation and Business Rules Contract

**Status:** Implemented — governed by `SPEC.md`  
**Covers:** `REQ-030`–`REQ-040`, `AC-030`–`AC-035`

## Requirements

### REQ-030: Bean Validation on ProductRequest

| Field | Constraints |
| --- | --- |
| sku | `@NotBlank`, `@Size(min=3, max=30)` |
| name | `@NotBlank`, `@Size(min=2, max=100)` |
| category | `@NotBlank`, `@Size(max=50)` |
| price | `@NotNull`, `@Positive` or `@DecimalMin` > 0 |
| stockQuantity | `@Min(0)` or `@PositiveOrZero` |

Controllers shall use `@Valid` on request bodies and reject invalid input before
calling the service.

### REQ-031: SKU uniqueness

Two products must not share the same SKU. Comparison is case-insensitive. On
update, the current product's SKU is excluded from the duplicate check.

### REQ-032: Update id immutability

Update operations must never change product id; path id is authoritative.

### REQ-033: Inactive products

Products with `active=false` are stored and retrieved normally.

### REQ-034: Domain exceptions

Define `ProductNotFoundException` and `DuplicateSkuException` as unchecked
exceptions. Do not catch them in controllers (handled globally in chunk 04).

### REQ-035: Missing product behavior

Find, update, and delete by unknown id shall throw `ProductNotFoundException`.

## Acceptance criteria

### AC-030: Invalid request rejected

**Given** a request with blank sku or non-positive price, **when**
`POST /api/products` is called, **then** validation fails before service
invocation (400 after chunk 04).

### AC-031: Duplicate SKU rejected

**Given** a product with sku `ABC-001`, **when** creating another with sku
`abc-001`, **then** `DuplicateSkuException` is thrown.

### AC-032: Update duplicate SKU

**Given** two products, **when** updating one to the other's SKU (any casing),
**then** `DuplicateSkuException` is thrown.

### AC-033: Inactive product round-trip

**Given** a product with `active=false`, **when** created and retrieved,
**then** `active` remains false in the response.

### AC-034: Not found on update/delete

**Given** a random UUID not in the repository, **when** update or delete is
called, **then** `ProductNotFoundException` is thrown.

## Testing focus

- Service tests for duplicate SKU (case variants) and not-found paths
- Controller tests for 400 validation responses (after chunk 04)
