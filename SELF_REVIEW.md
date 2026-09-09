# Self Review — Product Catalog API (Week 6)

## Completed

- [x] Exercise 1: PostgreSQL, Docker Compose, env-based datasource config
- [x] Exercise 2: Flyway `V1__create_products_table.sql`, `ProductEntity`, mapper
- [x] Exercise 3: Spring Data JPA repository, `@Transactional` service layer, removed in-memory store
- [x] Exercise 4: Pagination, filtering, sorting, low-stock, stock PATCH, optimistic-lock 409
- [x] Exercise 5: `@DataJpaTest`, service integration, Flyway verification, API integration tests
- [x] Exercise 6: README, diagrams, curl/Postman updates, test evidence

## Database and transaction decisions

- **Flyway owns schema** — Hibernate `ddl-auto=validate` only; no auto DDL.
- **Case-insensitive SKU** enforced by PostgreSQL unique index on `LOWER(sku)` plus service pre-checks.
- **`@Transactional`** on write paths; `readOnly = true` on queries.
- **Stock adjustment** validates quantity in service before save; insufficient stock throws without persisting a partial change.
- **`@Version`** on `ProductEntity` maps optimistic conflicts to HTTP 409 via `GlobalExceptionHandler`.
- **Filtering and pagination** use JPA Specifications and `Pageable` so work stays in PostgreSQL.

## Problems encountered and resolutions

- **Testcontainers + Docker 29:** Added `src/test/resources/docker-java.properties` with `api.version=1.44`.
- **Multiple containers timing out:** Shared singleton `SharedPostgreSqlContainer` across test classes.
- **Global `~/.testcontainers.properties`:** Commented out incompatible `UnixSocketClientProviderStrategy`.
- **Mockito on Java 26:** Kept HTTP tests as `@SpringBootTest` with real beans where mocking concrete classes fails.

## Week 5 feedback applied

- Consistent error envelope and status codes preserved after persistence migration.
- Constructor injection and DTO boundary unchanged.
- Given-When-Then test naming and Arrange/Act/Assert structure maintained.

## Known limitations

- Single-table catalog; no categories table or full-text search.
- Product limit checked via `count()` — acceptable at configured max (500 default).
- Tests share one PostgreSQL Testcontainer per JVM run (faster, but not fully isolated containers per class).
- Optimistic-lock coverage includes concurrent stock PATCH integration test (HTTP 409 path).
- Maximum-product limit check is best-effort under concurrent creates (no SERIALIZABLE — avoids PostgreSQL serialization failures).

## Verification

```text
./mvnw clean test — 93 tests, BUILD SUCCESS
docker compose up -d && ./mvnw spring-boot:run — application starts against empty DB
```
