# Self Review — Product Catalog API (Week 5)

## Completed

- [x] Exercise 1: Spring Boot setup, `GET /api/info`, context and web tests
- [x] Exercise 2: Layered CRUD API with DTOs, mapper, in-memory repository
- [x] Exercise 3: Bean Validation, SKU rules, domain exceptions
- [x] Exercise 4: `@RestControllerAdvice`, consistent `ErrorResponse` JSON
- [x] Exercise 5: `CatalogProperties`, dev/test profiles, low-stock endpoint, product limit
- [x] Exercise 6: Service/controller/integration/actuator tests, README, curl samples

## Design decisions

- **Constructor injection only** across controller, service, repository, and handler layers.
- **DTO boundary** — `Product` never exposed via REST; errors use dedicated `ErrorResponse`.
- **Case-insensitive SKU** enforced in the service layer, not Bean Validation.
- **`ConcurrentHashMap`** for thread-safe in-memory storage with defensive copies on read.
- **`/api/products/low-stock`** registered before `/{id}` to avoid route conflicts.
- **Integration-style controller tests** used where Java 26 blocks Mockito mocking of concrete `ProductService`.

## Problems resolved

- Mockito could not mock concrete `ProductService` on Java 26 — used `@SpringBootTest` + real beans for HTTP tests.
- Unhandled exceptions before Exercise 4 returned servlet errors — fixed with global handler.
- `/actuator/env` returned 500 via catch-all handler — added `NoResourceFoundException` mapping to 404.
- Actuator `info` needed `management.info.env.enabled` for custom `info.app.*` properties.

## Known limitations

- In-memory store resets on restart; no persistence.
- No pagination on list endpoints.
- Product limit and low-stock thresholds are configuration-only (not exposed via public API).
- Exercise repos use branch-per-exercise PRs for incremental review.

## Verification

```text
./mvnw clean verify — exit 0 (45 tests)
```
