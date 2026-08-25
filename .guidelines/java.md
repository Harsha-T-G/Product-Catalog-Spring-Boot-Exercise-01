# Product Catalog Java Guidelines

## Baseline

- Target Java 21 and Spring Boot 3.4.x. Use the checked-in Maven Wrapper.
- Package root: `com.codewalnut.productcatalog`.
- Do not add Lombok, JPA, or database dependencies without approval.
- Constructor injection only. No field `@Autowired`.

## Package boundaries

| Package | Allowed dependencies |
| --- | --- |
| `controller` | `service`, `dto`, Spring Web |
| `service` | `repository`, `model`, `dto`, `config`, `exception` |
| `repository` | `model` |
| `model` | JDK types only |
| `dto` | Validation annotations, JDK types |
| `exception` | Spring Web (advice only), `dto` |
| `config` | Spring Boot configuration |

Controllers must not depend on `repository` or `model` directly.

## Domain and DTOs

- `Product` is an internal entity; never return it from controllers.
- Use `ProductRequest` for create/update bodies; `ProductResponse` for success
  responses; `ErrorResponse` for failures.
- Price is `BigDecimal`; never use `double` or `float` for money.
- Product id is `UUID`, generated on create, immutable on update.

## REST conventions

- Base path: `/api/products` for product resources; `/api/info` for metadata.
- Use correct HTTP methods and status codes (201 + Location, 204 no body, etc.).
- Register static path segments (e.g. `low-stock`) before `{id}` path variables.
- Use `ResponseEntity` when status codes or headers need explicit control.

## Validation

- Bean Validation on `ProductRequest` at the controller boundary with `@Valid`.
- Business rules (duplicate SKU, product limit) in the service layer.
- Do not catch domain exceptions in controllers; use `@RestControllerAdvice`.

## Exceptions

- Use small unchecked domain exceptions with clear messages.
- Global handler maps exceptions to consistent `ErrorResponse` JSON.
- Never expose stack traces or internal details in API responses.

## Testing

- Name tests in Given-When-Then form.
- Structure with `// Arrange`, `// Act`, `// Assert` (omit only absent phases).
- One behavior per test; assert observable outcomes.
- Unit tests: Mockito for repository boundary in service tests.
- Web tests: `@WebMvcTest` + MockMvc; import `GlobalExceptionHandler`.
- Integration tests: `@SpringBootTest` + `@ActiveProfiles("test")` sparingly.
- Never weaken or delete failing tests to obtain green builds.

## Security and logging

- No secrets in source or committed yaml.
- Do not log passwords, tokens, or complete sensitive request bodies.
- Read overrides from environment variables where specified.

## Git conventions

See `AGENTS.md` for commit and PR title format (`feat(scope): subject`).
