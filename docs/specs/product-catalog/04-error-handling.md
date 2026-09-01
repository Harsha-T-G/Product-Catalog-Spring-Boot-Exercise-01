# Error Handling Contract

**Status:** Implemented — governed by `SPEC.md`  
**Covers:** `REQ-050`–`REQ-060`, `AC-050`–`AC-055`

## Requirements

### REQ-050: Global handler

A `@RestControllerAdvice` class shall map exceptions to consistent JSON error
responses.

### REQ-051: Error response shape

Every error response shall include: `timestamp`, `status`, `error`, `message`,
`path`. Validation failures (`MethodArgumentNotValidException`) shall additionally
include `fieldErrors` (field name + message per invalid field).

### REQ-052: Exception mapping

| Condition | HTTP status |
| --- | --- |
| `ProductNotFoundException` | 404 Not Found |
| `DuplicateSkuException` | 409 Conflict |
| `MethodArgumentNotValidException` | 400 Bad Request |
| Invalid UUID in path | 400 Bad Request |
| Unsupported HTTP method | 405 Method Not Allowed |
| Unexpected exception | 500 Internal Server Error |

### REQ-053: Safe 500 responses

Unexpected exceptions must not expose stack traces, secrets, or internal
implementation details in the API response.

### REQ-054: Consistent structure

All error responses must follow the same JSON structure regardless of failure
type.

## Acceptance criteria

### AC-050: Not found envelope

**Given** a missing product id, **when** `GET /api/products/{id}` is called,
**then** response is 404 with the standard error shape including `path`.

### AC-051: Validation field errors

**Given** invalid request body, **when** `POST /api/products` is called,
**then** response is 400 with `fieldErrors` listing each invalid field.

### AC-052: Invalid UUID

**Given** path id `not-a-uuid`, **when** a product endpoint is called,
**then** response is 400 with a clear message.

### AC-053: Method not allowed

**Given** an unsupported HTTP method on a mapped path, **when** the request is
made, **then** response is 405 with consistent error shape.

### AC-054: Duplicate SKU conflict

**Given** duplicate SKU on create, **when** the request is processed,
**then** response is 409 with consistent error shape.

### AC-055: Internal error safety

**Given** an unexpected server error, **when** the client receives 500,
**then** the body contains a generic message without stack trace.

## Testing focus

- `@WebMvcTest` with `@Import(GlobalExceptionHandler.class)` for each status
- Integration tests verifying error JSON shape
