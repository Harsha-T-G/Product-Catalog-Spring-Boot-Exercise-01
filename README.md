# Product Catalog API

Spring Boot REST API for product management with PostgreSQL persistence,
Flyway migrations, pagination, filtering, and stock adjustment.

**Status:** Week 6 complete (Exercises 1–6).

## Prerequisites

- JDK 21
- Git
- Docker and Docker Compose (required for `./mvnw verify` — tests use PostgreSQL Testcontainers)

## Quick start (tests)

```bash
cd Task14-Product-Catalog
docker info   # Docker must be running
./mvnw clean verify
```

Tests spin up a shared PostgreSQL 16 container automatically via Testcontainers.
**93 tests** must pass — see [docs/test-evidence.txt](docs/test-evidence.txt).

### Troubleshooting: Docker / Testcontainers errors

If tests fail with `Could not find a valid Docker environment` or
`ContainerFetch Can't get Docker image`:

1. **Start Docker Desktop** and confirm it is healthy:
   ```bash
   docker info
   docker pull postgres:16-alpine
   ```
2. **Fix global Testcontainers config** at `~/.testcontainers.properties`. Remove or comment out:
   ```properties
   docker.client.strategy=org.testcontainers.dockerclient.UnixSocketClientProviderStrategy
   ```
3. **Docker 29+ API version:** This project includes `src/test/resources/docker-java.properties`
   with `api.version=1.44` (required for Docker Desktop 29.x).
4. Run tests via the helper script:
   ```bash
   ./scripts/verify-tests.sh
   ```

### Startup error: `jdbcUrl, ${DB_URL}`

Spring is using the literal placeholder because **`DB_PASSWORD` (and optionally `DB_URL`) are not in the environment**. Spring Boot does not read `.env` automatically.

**Fix (reset DB — required after changing username/password):**

```bash
./scripts/reset-local-db.sh
./scripts/run-dev.sh
```

This runs `docker compose down -v` and recreates Postgres so `root` / `root@123` from `.env` are applied.

**Fix (IntelliJ / IDE):** In the run configuration, add environment variables from `.env`:

- `DB_URL=jdbc:postgresql://localhost:5432/product_catalog`
- `DB_USERNAME=root`
- `DB_PASSWORD=root@123`

Active profile: **`dev`**

## Quick start (application with PostgreSQL)

1. Copy environment template and set a local password:

```bash
cp .env.example .env
# Local defaults: user root, password root@123 (already in .env.example)
```

2. Start PostgreSQL:

```bash
docker compose --env-file .env up -d
docker compose ps   # wait until postgres is healthy
```

3. Export database credentials and run the application:

```bash
set -a && source .env && set +a
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
curl http://localhost:8080/api/info
curl http://localhost:8080/actuator/health
```

Swagger UI is available for interactive API testing (disabled only in the `test` profile):

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

Or one command (Compose + `.env` + app):

```bash
./scripts/run-dev.sh
```

4. Stop PostgreSQL when finished:

```bash
docker compose --env-file .env down
```

To remove persisted data as well:

```bash
docker compose --env-file .env down -v
```

Default port: **8080**. PostgreSQL: **5432**.

### Required environment variables

| Variable | Purpose |
|----------|---------|
| `DB_URL` | JDBC URL (default in dev profile: `jdbc:postgresql://localhost:5432/product_catalog`) |
| `DB_USERNAME` | Application database user (local default: `root`) |
| `DB_PASSWORD` | Application password (local default in `.env.example`: `root@123`) |
| `POSTGRES_PASSWORD` | Used by Docker Compose to initialize the container user (must match `DB_PASSWORD`) |
| `CATALOG_MAXIMUM_PRODUCTS` | Optional override for catalog size limit (default 500) |

If the database is unavailable, the application fails at startup with a clear
connection error. No credentials belong in Git — use `.env` (gitignored) or your
shell environment.

### Hibernate and Flyway

- `spring.jpa.hibernate.ddl-auto=validate` — Hibernate never creates or alters tables.
- Flyway owns schema changes under `src/main/resources/db/migration/`.
- **V1__create_products_table.sql** — creates `products` with constraints and case-insensitive SKU index.
- On first start against an empty database, Flyway applies V1; subsequent starts skip already-applied migrations.

## Profiles

| Profile | Command | Database | low-stock threshold | max products | default page size |
|---------|---------|----------|---------------------|--------------|-------------------|
| default | Requires `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | PostgreSQL | 5 | 500 (or `CATALOG_MAXIMUM_PRODUCTS`) | 20 |
| dev | `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` + `.env` | PostgreSQL (Docker Compose) | 10 | 1000 | 20 |
| test | `./mvnw test` (automatic) | PostgreSQL Testcontainers | 2 | 20 | 5 |

## Endpoint table

| Method | Path | Status | Purpose |
|--------|------|--------|---------|
| GET | `/api/info` | 200 | Application metadata |
| GET | `/actuator/health` | 200 | Health check (includes `db` component) |
| GET | `/actuator/info` | 200 | Build info |
| POST | `/api/products` | 201 | Create product |
| GET | `/api/products` | 200 | Paginated, filterable product list |
| GET | `/api/products/low-stock` | 200 | Active low-stock products |
| GET | `/api/products/{id}` | 200 | Get one product |
| PUT | `/api/products/{id}` | 200 | Update product |
| PATCH | `/api/products/{id}/stock` | 200 | Adjust stock by delta |
| DELETE | `/api/products/{id}` | 204 | Delete product |

Error responses: **400** validation / business rule, **404** not found, **405** method not allowed, **409** conflict (duplicate SKU or optimistic lock), **500** unexpected.

Only **health** and **info** actuator endpoints are exposed. Health reports database availability without leaking credentials.

## Sample requests

**Create product** — `price` must be greater than zero with at most **17 integer digits and 2 decimal places** (matches PostgreSQL `NUMERIC(19,2)`).

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"sku":"SKU-001","name":"Sample","category":"General","price":19.99,"stockQuantity":10,"active":true}'
```

**Paginated list with sorting**

```bash
curl "http://localhost:8080/api/products?page=0&size=10&sort=name,asc"
```

Response shape:

```json
{
  "content": [ /* ProductResponse[] */ ],
  "page": 0,
  "size": 10,
  "totalElements": 42,
  "totalPages": 5
}
```

**Filter + paginate**

```bash
curl "http://localhost:8080/api/products?category=Electronics&active=true&page=0&size=5&sort=price,desc"
```

Allowed sort fields: `name`, `price`, `category`, `createdAt`, `stockQuantity`.

**Adjust stock**

```bash
curl -X PATCH http://localhost:8080/api/products/{id}/stock \
  -H "Content-Type: application/json" \
  -d '{"adjustment":-3}'
```

**Validation error (400)**

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"sku":"","name":"X","category":"General","price":-1,"stockQuantity":0}'
```

See [docs/curl-commands.sh](docs/curl-commands.sh) for a runnable script and
[docs/product-catalog.postman_collection.json](docs/product-catalog.postman_collection.json) for Postman.

## Architecture diagrams

See [docs/diagrams/week6-architecture.md](docs/diagrams/week6-architecture.md):

- Component diagram (client → controller → service → repository → PostgreSQL)
- ER diagram for the `products` table
- Sequence diagram for creating a product

**Implementation walkthrough:** [docs/week6-implementation-flow.md](docs/week6-implementation-flow.md) — exercise-by-exercise flow, branch progression, request paths, and package map.

## Package structure

```text
com.codewalnut.productcatalog/
├── controller/   InfoController, ProductController
├── service/      ProductService, ProductPageRequestFactory
├── repository/   ProductRepository, ProductSpecifications
├── entity/       ProductEntity
├── dto/          Request/response and error payloads
├── mapper/       ProductEntityMapper
├── exception/    Domain exceptions, GlobalExceptionHandler
└── config/       CatalogProperties
```

## Week 6 branches

```text
task14-main
  └── week6-exercise-1-postgresql-config   (Ex 1–3: PostgreSQL, Flyway, JPA)
        └── week6-exercise-4-api-features  (Ex 4: pagination, filter, stock PATCH)
              └── week6-exercise-5-database-tests  (Ex 5: repository & integration tests)
                    └── week6-exercise-6-docs-delivery  (Ex 6: docs & evidence)
```

## Agentic workflow

| Artifact | Path |
|----------|------|
| Spec | [SPEC.md](SPEC.md) |
| Specs | [docs/specs/product-catalog/](docs/specs/product-catalog/) |
| Plans | [docs/plans/](docs/plans/) |
| Self review | [SELF_REVIEW.md](SELF_REVIEW.md) |
| Week 6 implementation flow | [docs/week6-implementation-flow.md](docs/week6-implementation-flow.md) |
| Test evidence | [docs/test-evidence.txt](docs/test-evidence.txt) |

## Tests

```bash
./mvnw clean verify
./mvnw -Dtest=ProductRepositoryTest test
./mvnw -Dtest=ProductServiceIntegrationTest test
./mvnw -Dtest=ProductIntegrationTest test
```
