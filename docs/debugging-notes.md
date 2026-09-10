# Week 7 Debugging Notes

These observations use focused PostgreSQL-backed MockMvc tests and trace-aware
logs so the evidence is repeatable. For an IDE demonstration, place breakpoints
at the named security filter or service lines, run the named test in debug mode,
and inspect the same values described below. No credential or complete request
body is required at a breakpoint or in a log.

## VIEWER attempts product creation

- **Observed result:** HTTP 403 with `Access is denied`; response header and
  JSON both contain trace ID `f4c39b16-41d0-42e9-a964-8f87e05dd322`.
- **Root cause:** The authenticated principal is `viewer-user` with
  `ROLE_VIEWER`. `SecurityConfig` requires `ROLE_EDITOR` or `ROLE_ADMIN` for
  `POST /api/products`, so authorization stops the request before the
  controller and service.
- **Debugging method:** Run
  `RoleAuthorizationIntegrationTest#givenViewer_whenCreatingProduct_thenReturns403`
  and inspect the security context immediately before `AuthorizationFilter` or
  the `RestAccessDeniedHandler` event. The WARN log records only trace ID, path,
  safe username, and `outcome=forbidden`.
- **Why the response is correct:** Authentication succeeded but authority is
  insufficient, which is 403 rather than 401.
- **Sensitive-data evidence:** The captured security-log test proves the
  denied request body and `AccessDeniedException` stack are absent.

## Duplicate SKU during product creation

- **Observed result:** HTTP 409 with a safe duplicate-SKU message; response
  header/body share trace ID `bfe4cd2c-0aac-45bb-8aca-e6176a646705`.
- **Root cause:** `ProductService.create` performs a case-insensitive repository
  existence check before persistence. The database unique index remains the
  race-condition backstop, and `ProductPersistenceSupport` maps only the named
  SKU constraint to the same domain conflict.
- **Debugging method:** Run
  `ProductControllerTest#givenDuplicateSku_whenCreateProduct_thenReturns409ErrorEnvelope`.
  Step from `ProductController.create` to `ProductService.create`; the duplicate
  branch throws `DuplicateSkuException`, and `GlobalExceptionHandler` builds the
  409 envelope with the request trace.
- **Why the response is correct:** The client requested a state conflicting
  with the case-insensitive unique SKU contract.
- **Sensitive-data evidence:** The response contains no SQL/constraint detail,
  and normal 4xx handling logs no exception stack.

## Stock reduction would become negative

- **Observed result:** HTTP 400 with trace ID
  `c852163d-c4a7-447b-8617-9b8da9434839`; a follow-up GET still returns stock
  quantity 10.
- **Root cause:** `ProductService.adjustStock` calculates the proposed quantity
  before mutating or flushing the entity. A negative result throws
  `InsufficientStockException` inside the transaction.
- **Debugging method:** Run
  `ProductControllerTest#givenInsufficientStock_whenAdjustStock_thenReturns400AndPreservesQuantity`
  and inspect `newQuantity` before the guard. The service integration test
  independently reloads the entity after the exception and proves quantity 10.
- **Why the response is correct:** The input is syntactically valid but violates
  the non-negative stock business rule, so 400 is appropriate and no state is
  committed.
- **Sensitive-data evidence:** Only the safe WARN request-completion fields are
  logged; neither the request body nor a normal-4xx stack trace is emitted.

## Log-level interpretation

- DEBUG is optional local diagnostic detail and is disabled by default.
- INFO records successful requests and state-changing domain events.
- WARN records expected client and security failures without stack traces.
- ERROR records unexpected 5xx outcomes using trace ID, path, and exception type
  while omitting potentially sensitive exception messages and stacks.

Trace IDs connect the response, request summary, security decision, and domain
event. MDC supplies that value to every log event on the request thread and is
always cleared in `finally` so a reused server thread cannot misattribute the
next request. Complete bodies are not logged because user-creation bodies carry
plaintext passwords and other payloads may later gain sensitive fields.
