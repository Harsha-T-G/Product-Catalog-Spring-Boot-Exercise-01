# Product Catalog API

Spring Boot REST API for product management with PostgreSQL persistence,
Flyway migrations, database-backed HTTP Basic authentication, pagination,
filtering, and stock adjustment.

**Status:** Week 7 implementation and Java 21 verification complete; PR publication remains pending.

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
**157 tests** must pass. Week 7 RED→GREEN results are recorded in
[docs/tdd-evidence.md](docs/tdd-evidence.md).

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

This runs `docker compose down -v` and recreates Postgres using the credentials
you placed in `.env`.

**Fix (IntelliJ / IDE):** In the run configuration, add environment variables from `.env`:

- `DB_URL=jdbc:postgresql://localhost:5432/product_catalog`
- `DB_USERNAME=root`
- `DB_PASSWORD=<your local database password>`

Active profile: **`dev`**

## Quick start (application with PostgreSQL)

1. Copy environment template and set a local password:

```bash
cp .env.example .env
# Fill in POSTGRES_PASSWORD and DB_PASSWORD; keep them equal for local Compose.
# Optionally fill all three CATALOG_*_PASSWORD values to seed dev users.
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
| `DB_USERNAME` | Required application database user (`root` for the supplied Compose service) |
| `DB_PASSWORD` | Required application database password; no committed default |
| `POSTGRES_PASSWORD` | Used by Docker Compose to initialize the container user (must match `DB_PASSWORD`) |
| `CATALOG_MAXIMUM_PRODUCTS` | Optional override for catalog size limit (default 500) |
| `CATALOG_VIEWER_PASSWORD` | Dev-only VIEWER password; seeding requires all three role passwords |
| `CATALOG_EDITOR_PASSWORD` | Dev-only EDITOR password; seeding requires all three role passwords |
| `CATALOG_ADMIN_PASSWORD` | Dev-only ADMIN password; seeding requires all three role passwords |

The corresponding usernames default to `viewer`, `editor`, and `admin` and may
be overridden with `CATALOG_VIEWER_USERNAME`, `CATALOG_EDITOR_USERNAME`, and
`CATALOG_ADMIN_USERNAME`. If any sample-user password is absent, no sample users
are created.

If the database is unavailable, the application fails at startup with a clear
connection error. No credentials belong in Git — use `.env` (gitignored) or your
shell environment.

### Hibernate and Flyway

- `spring.jpa.hibernate.ddl-auto=validate` — Hibernate never creates or alters tables.
- Flyway owns schema changes under `src/main/resources/db/migration/`.
- **V1__create_products_table.sql** — creates `products` with constraints and case-insensitive SKU index.
- **V2__create_security_tables.sql** — creates users, roles, mappings, and the case-insensitive username index.
- On first start against an empty database, Flyway applies both migrations;
  subsequent starts skip already-applied migrations.

## Profiles

| Profile | Command | Database | low-stock threshold | max products | default page size |
|---------|---------|----------|---------------------|--------------|-------------------|
| default | Requires `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | PostgreSQL | 5 | 500 (or `CATALOG_MAXIMUM_PRODUCTS`) | 20 |
| dev | `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` + `.env` | PostgreSQL (Docker Compose) | 10 | 1000 | 20 |
| test | `./mvnw test` (automatic) | PostgreSQL Testcontainers | 2 | 20 | 5 |

## Security design and permissions

The API uses stateless HTTP Basic authentication. Users, BCrypt password hashes,
and canonical roles are stored in PostgreSQL; Spring Security loads current
account state on each request. Request rules are restrictive by default, and
product deletion plus ADMIN user operations also have service-level method
security as defense in depth.

| Method and path | Public | VIEWER | EDITOR | ADMIN |
| --- | ---: | ---: | ---: | ---: |
| `GET /api/info`, `GET /actuator/health` | ✓ | ✓ | ✓ | ✓ |
| Product GET endpoints | — | ✓ | ✓ | ✓ |
| `POST /api/products`, product `PUT`, stock `PATCH` | — | — | ✓ | ✓ |
| `DELETE /api/products/{id}` | — | — | — | ✓ |
| `POST /api/admin/users` | — | — | — | ✓ |
| `PATCH /api/admin/users/{username}/enabled` | — | — | — | ✓ |
| `/actuator/info`, OpenAPI, Swagger UI | — | ✓ | ✓ | ✓ |
| Any unlisted route | — | Authenticated, then normal routing | Authenticated, then normal routing | Authenticated, then normal routing |

Missing or invalid credentials return **401** with a Basic challenge.
Authenticated users without permission receive **403**. Application and
security failures share `timestamp`, `status`, `error`, `message`, `path`,
`traceId`, and `fieldErrors`. Error responses never include stack traces, SQL
details, entity internals, passwords, or password hashes.

Only **health** and **info** actuator endpoints are exposed. Health reports database availability without leaking credentials.

## Sample requests

Set local credentials without writing their values into tracked files:

```bash
export CATALOG_ADMIN_USERNAME=admin
read -s CATALOG_ADMIN_PASSWORD && export CATALOG_ADMIN_PASSWORD
```

**Create product as ADMIN** — `price` must be greater than zero with at most
**17 integer digits and 2 decimal places** (matches PostgreSQL `NUMERIC(19,2)`).

```bash
curl -X POST http://localhost:8080/api/products \
  -u "${CATALOG_ADMIN_USERNAME}:${CATALOG_ADMIN_PASSWORD}" \
  -H "Content-Type: application/json" \
  -d '{"sku":"SKU-001","name":"Sample","category":"General","price":19.99,"stockQuantity":10,"active":true}'
```

**Paginated list with sorting**

```bash
curl -u "${CATALOG_ADMIN_USERNAME}:${CATALOG_ADMIN_PASSWORD}" \
  "http://localhost:8080/api/products?page=0&size=10&sort=name,asc"
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
curl -u "${CATALOG_ADMIN_USERNAME}:${CATALOG_ADMIN_PASSWORD}" \
  "http://localhost:8080/api/products?category=Electronics&active=true&page=0&size=5&sort=price,desc"
```

Allowed sort fields: `name`, `price`, `category`, `createdAt`, `stockQuantity`.

**Adjust stock**

```bash
curl -X PATCH http://localhost:8080/api/products/{id}/stock \
  -u "${CATALOG_ADMIN_USERNAME}:${CATALOG_ADMIN_PASSWORD}" \
  -H "Content-Type: application/json" \
  -d '{"adjustment":-3}'
```

**Validation error (400)**

```bash
curl -X POST http://localhost:8080/api/products \
  -u "${CATALOG_ADMIN_USERNAME}:${CATALOG_ADMIN_PASSWORD}" \
  -H "Content-Type: application/json" \
  -d '{"sku":"","name":"X","category":"General","price":-1,"stockQuantity":0}'
```

**Create a VIEWER as ADMIN**

```bash
read -s NEW_USER_PASSWORD && export NEW_USER_PASSWORD
jq -n --arg password "$NEW_USER_PASSWORD" \
  '{username:"catalog-reader",password:$password,roles:["VIEWER"]}' | \
curl -X POST http://localhost:8080/api/admin/users \
  -u "${CATALOG_ADMIN_USERNAME}:${CATALOG_ADMIN_PASSWORD}" \
  -H "Content-Type: application/json" --data-binary @-
```

**401 and 403 examples**

```bash
# 401: no credentials
curl -i http://localhost:8080/api/products

# 403: authenticated VIEWER attempts a write
export CATALOG_VIEWER_USERNAME="${CATALOG_VIEWER_USERNAME:-viewer}"
read -s CATALOG_VIEWER_PASSWORD && export CATALOG_VIEWER_PASSWORD
curl -i -X POST http://localhost:8080/api/products \
  -u "${CATALOG_VIEWER_USERNAME}:${CATALOG_VIEWER_PASSWORD}" \
  -H "Content-Type: application/json" \
  -d '{"sku":"DENIED-1","name":"Denied","category":"General","price":1.00,"stockQuantity":0,"active":true}'
```

Both responses are JSON error envelopes. The 401 also returns
`WWW-Authenticate: Basic ...`.

### Trace IDs and safe logging

Every response contains `X-Trace-Id`. A canonical UUID supplied by the client is
reused; a missing or invalid value is replaced with a generated UUID. The same
value appears in error JSON and request-completion logs.

```bash
TRACE_ID=7046bd93-568f-49ae-ac1d-9d5be793f720
curl -i http://localhost:8080/api/products \
  -u "${CATALOG_ADMIN_USERNAME}:${CATALOG_ADMIN_PASSWORD}" \
  -H "X-Trace-Id: ${TRACE_ID}"
```

Logs contain method, safe path, status, duration, trace ID, and authenticated
username when available. Authorization values, passwords/hashes, database
credentials, complete sensitive request bodies, SQL failure details, and normal
4xx stack traces are excluded. See
[docs/debugging-notes.md](docs/debugging-notes.md) for the required scenarios.

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
├── controller/   Product, info, and ADMIN user HTTP adapters
├── service/      Product and user business rules
├── repository/   Product, user, and role persistence
├── entity/       Product, user, and role JPA models
├── dto/          Request/response and error payloads
├── mapper/       ProductEntityMapper
├── exception/    Domain exceptions, GlobalExceptionHandler
├── config/       Catalog and security configuration
└── security/     Database identity, filters, and security error adapters
```

## Active branch

```text
week7-security-logging
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
| Week 7 security contract | [docs/security-spec.md](docs/security-spec.md) |
| Week 7 debugging notes | [docs/debugging-notes.md](docs/debugging-notes.md) |
| Week 7 TDD evidence | [docs/tdd-evidence.md](docs/tdd-evidence.md) |
| Week 7 verification matrix | [docs/week7-verification.md](docs/week7-verification.md) |

## Tests

```bash
./mvnw clean verify
./mvnw -Dtest=ProductRepositoryTest test
./mvnw -Dtest=ProductServiceIntegrationTest test
./mvnw -Dtest=ProductIntegrationTest test
```

The final local gate passed all 157 tests on OpenJDK 21.0.12.1 while compiling
with `javac --release 21`.

## Security limitations and future improvements

- HTTP Basic should be used only behind TLS; this PoC does not configure TLS.
- There is no rate limiting, account lockout, password reset/rotation workflow,
  MFA, or persistent audit-event store.
- Dev users are seeded only when all three role passwords are supplied; production
  identity provisioning needs an external secret manager and controlled workflow.
- JWT or an external identity provider is intentionally deferred until all
  required HTTP Basic behavior is complete.
