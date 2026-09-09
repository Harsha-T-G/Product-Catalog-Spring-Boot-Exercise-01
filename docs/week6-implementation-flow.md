# Week 6 — Implementation Flow

This document explains **what was built**, **in what order**, and **how the pieces connect** after migrating from Week 5 in-memory storage to PostgreSQL.

**Related docs**

| Doc | Purpose |
|-----|---------|
| [week6-architecture.md](./diagrams/week6-architecture.md) | Mermaid component, ER, and sequence diagrams |
| [../SPEC.md](../SPEC.md) | Approved product contract |
| [curl-commands.sh](./curl-commands.sh) | Runnable API examples |
| [test-evidence.txt](./test-evidence.txt) | Test run record (93 tests) |
| [../SELF_REVIEW.md](../SELF_REVIEW.md) | Decisions, limitations, verification |

---

## 1. Big picture: Week 5 → Week 6

```text
Week 5 (in-memory)                    Week 6 (PostgreSQL)
─────────────────────                 ────────────────────
ConcurrentHashMap repository    →     Spring Data JPA + ProductRepository
Product model class             →     ProductEntity (@Entity)
No migrations                   →     Flyway V1__create_products_table.sql
GET /api/products → JSON array  →     GET /api/products → page envelope
No stock PATCH                  →     PATCH /api/products/{id}/stock
Unit/MockMvc tests only         →     + Testcontainers integration tests
```

**Runtime stack**

```text
HTTP Client
    ↓
ProductController          (@RestController, validation, status codes)
    ↓
ProductService             (@Transactional business rules)
    ↓
ProductRepository          (Spring Data JPA)
    ↓
PostgreSQL                 (schema owned by Flyway)
```

---

## 2. Branch progression (how work landed)

Each exercise is a branch merged forward into the next. Final delivery is on `week6-exercise-6-docs-delivery`.

```text
task14-main
  └── week6-exercise-1-postgresql-config    Exercises 1–3
        └── week6-exercise-4-api-features   Exercise 4
              └── week6-exercise-5-database-tests   Exercise 5
                    └── week6-exercise-6-docs-delivery   Exercise 6 + review fixes
```

| Exercise | Branch | What you get |
|----------|--------|--------------|
| **1** | `week6-exercise-1-postgresql-config` | Docker Compose, env datasource, app starts against Postgres |
| **2** | *(same branch)* | Flyway migration, `ProductEntity`, mapper |
| **3** | *(same branch)* | JPA repository, transactional service, in-memory store removed |
| **4** | `week6-exercise-4-api-features` | Pagination, filter, sort, low-stock, stock PATCH, optimistic lock |
| **5** | `week6-exercise-5-database-tests` | Repository, service, Flyway, and HTTP integration tests |
| **6** | `week6-exercise-6-docs-delivery` | README, diagrams, Postman/curl, test evidence, contract doc alignment |

---

## 3. Exercise-by-exercise implementation

### Exercise 1 — PostgreSQL configuration

**Goal:** Application connects to PostgreSQL using environment variables, not hard-coded credentials.

**Implemented**

- `compose.yml` — PostgreSQL 16 service, health check, volume, bound to `127.0.0.1:5432`
- `.env.example` — placeholders for `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `POSTGRES_PASSWORD`
- `application.yml` — datasource from `${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}`
- `application-dev.yml` — local dev defaults pointing at Compose Postgres

**Flow**

```text
1. cp .env.example .env  (set passwords)
2. docker compose --env-file .env up -d
3. export DB_* from .env
4. ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
5. Flyway runs V1 on first start → empty DB gets products table
```

**Key files:** `compose.yml`, `.env.example`, `src/main/resources/application*.yml`

---

### Exercise 2 — Flyway schema + JPA entity

**Goal:** Database schema is version-controlled; Hibernate validates but never creates tables.

**Implemented**

- `V1__create_products_table.sql` — `products` table, CHECK constraints, case-insensitive SKU index
- `ProductEntity` — JPA mapping, `@PrePersist` / `@PreUpdate` timestamps, `@Version`
- `ProductEntityMapper` — maps between `ProductRequest`, entity, and `ProductResponse`
- `spring.jpa.hibernate.ddl-auto=validate`

**Schema highlights**

| Column | Notes |
|--------|--------|
| `price` | `NUMERIC(19,2)`, CHECK `> 0` |
| `stock_quantity` | CHECK `>= 0` |
| `version` | Optimistic lock (JPA `@Version`) |
| SKU | Unique index on `LOWER(sku)` |

**Key files:** `db/migration/V1__*.sql`, `entity/ProductEntity.java`, `mapper/ProductEntityMapper.java`

---

### Exercise 3 — Repository and transactional service

**Goal:** Replace in-memory store with Spring Data JPA; keep business logic in the service layer.

**Implemented**

- `ProductRepository` — extends `JpaRepository`, custom queries (SKU check, low stock)
- `ProductSpecifications` — dynamic filters for category (case-insensitive) and active flag
- `ProductService` — `@Transactional` create/update/delete/stock; `readOnly = true` on queries
- `ProductPersistenceSupport` — `saveAndFlush` + maps duplicate SKU constraint to `DuplicateSkuException`
- Removed `InMemoryProductRepository` and internal `Product` model

**Create flow (service layer)**

```text
create(request)
  → check count() < maximumProducts
  → check !existsBySkuIgnoreCase(sku)
  → mapper.toNewEntity(uuid, request)
  → persistenceSupport.saveAndFlush(entity, sku)
  → mapper.toResponse(saved)
```

**Key files:** `repository/*`, `service/ProductService.java`, `service/ProductPersistenceSupport.java`

---

### Exercise 4 — API features (pagination, filter, stock)

**Goal:** List products with server-side pagination/filtering; adjust stock via PATCH.

**Implemented**

| Feature | Endpoint | Implementation |
|---------|----------|----------------|
| Paginated list | `GET /api/products?page&size&sort&category&active` | `ProductSearchCriteria` → `ProductPageRequestFactory` → `Pageable` + `Specifications` |
| Page envelope | Response body | `ProductPageResponse { content, page, size, totalElements, totalPages }` |
| Stable sort | Any sort | Appends `id ASC` tie-breaker |
| Low stock | `GET /api/products/low-stock` | Repository query + threshold from `CatalogProperties` |
| Stock delta | `PATCH /api/products/{id}/stock` | `StockAdjustmentRequest.adjustment` (+ optional `version`) |
| Optimistic lock | Conflicting writes | `@Version` → HTTP **409** via `GlobalExceptionHandler` |

**List flow**

```text
GET /api/products?category=Electronics&page=0&size=10&sort=name,asc
  → ProductController builds ProductSearchCriteria
  → ProductService.findProducts(criteria)
  → ProductPageRequestFactory.createPageable(...)
  → ProductSpecifications.withFilters(category, active)
  → productRepository.findAll(spec, pageable)
  → map entities → ProductPageResponse
```

**Stock PATCH flow**

```text
PATCH /api/products/{id}/stock  { "adjustment": -3, "version": 2 }
  → validate adjustment != 0
  → load entity; optional version check → 409 if mismatch
  → newQty = stock + adjustment; if newQty < 0 → 400 InsufficientStock
  → entity.adjustStockBy(adjustment)
  → persistenceSupport.saveAndFlush(entity)
  → ProductResponse
```

**Key files:** `controller/ProductController.java`, `dto/ProductSearchCriteria.java`, `service/ProductPageRequestFactory.java`, `dto/StockAdjustmentRequest.java`

---

### Exercise 5 — Database-backed tests

**Goal:** Prove persistence, migrations, and API behaviour against a real PostgreSQL instance in tests.

**Implemented**

| Test class | Scope |
|------------|--------|
| `FlywayMigrationTest` | V1 migration applied; columns, constraints, index exist |
| `ProductRepositoryTest` | `@DataJpaTest` + Testcontainers — save, SKU uniqueness, checks |
| `ProductServiceIntegrationTest` | Full service + DB — CRUD, limits, stock, optimistic lock |
| `ProductPersistenceSupportIntegrationTest` | Duplicate SKU and constraint rethrow paths |
| `ProductIntegrationTest` | End-to-end HTTP + DB — CRUD, pagination, concurrent PATCH 409 |
| `ProductControllerTest` | `@SpringBootTest` + MockMvc + shared Postgres container |
| `ProductControllerWebMvcTest` | Standalone MockMvc — 201 Location, 409 limit/conflict (mocked service) |

**Test infrastructure**

```text
SharedPostgreSqlContainer (singleton Postgres 16)
    ↓
PostgreSqlTestSupport (@DynamicPropertySource → JDBC URL)
    ↓
All @SpringBootTest / @DataJpaTest integration classes
```

**Key files:** `src/test/.../support/SharedPostgreSqlContainer.java`, `PostgreSqlTestSupport.java`

---

### Exercise 6 — Documentation and delivery

**Goal:** Make the project runnable, verifiable, and understandable without reading every source file.

**Implemented**

- `README.md` — setup, profiles, endpoints, troubleshooting Docker/Testcontainers
- `docs/week6-architecture.md` — diagrams
- `docs/week6-implementation-flow.md` — this document
- `docs/curl-commands.sh`, `docs/product-catalog.postman_collection.json`
- `docs/test-evidence.txt` — `./mvnw clean test` → 93 tests
- `SELF_REVIEW.md` — decisions and known limitations
- `SPEC.md`, `AGENTS.md`, `CONTEXT.md` — aligned with Week 6 PostgreSQL reality
- `reviews/pr-9/AZ-REVIEW.md` — PR review tracker

---

## 4. Package map (where to look in code)

```text
com.codewalnut.productcatalog/
├── controller/
│   ├── InfoController.java          GET /api/info
│   └── ProductController.java       Product REST API
├── service/
│   ├── ProductService.java          Business rules, transactions
│   ├── ProductPageRequestFactory.java   Pageable + sort validation
│   └── ProductPersistenceSupport.java   saveAndFlush + SKU error mapping
├── repository/
│   ├── ProductRepository.java       Spring Data JPA
│   └── ProductSpecifications.java   Filter specifications
├── entity/
│   └── ProductEntity.java           JPA entity + @Version
├── mapper/
│   └── ProductEntityMapper.java     DTO ↔ entity
├── dto/                             Request/response/error payloads
├── exception/                       Domain exceptions + GlobalExceptionHandler
└── config/
    └── CatalogProperties.java       catalog.* settings (@Validated)
```

---

## 5. HTTP surface (quick reference)

| Method | Path | Success | Notes |
|--------|------|---------|-------|
| POST | `/api/products` | 201 | Creates product; `Location` header |
| GET | `/api/products` | 200 | Page envelope; filter/sort query params |
| GET | `/api/products/{id}` | 200 | Single product |
| PUT | `/api/products/{id}` | 200 | Full update; optional `version` → 409 |
| PATCH | `/api/products/{id}/stock` | 200 | `{ "adjustment": ±n }`; optional `version` |
| DELETE | `/api/products/{id}` | 204 | Remove product |
| GET | `/api/products/low-stock` | 200 | JSON array of active low-stock items |
| GET | `/actuator/health` | 200 | Includes `db` component status |

**Common error codes:** 400 validation/business rule, 404 not found, 409 duplicate SKU / optimistic lock / catalog full, 500 unexpected (includes `errorReferenceId` in body).

---

## 6. Configuration flow

```text
application.yml          Base: datasource env vars, Flyway, catalog defaults, actuator
application-dev.yml      Dev profile: local Postgres URL, higher limits
application-test.yml     Test profile: Testcontainers overrides, small page sizes
.env (local, gitignored) POSTGRES_PASSWORD, DB_* for Compose and app
CatalogProperties        Binds catalog.* ; validated at startup
```

| Profile | max products | low-stock threshold | default page size |
|---------|-------------|---------------------|-------------------|
| default | 500 | 5 | 20 |
| dev | 1000 | 10 | 20 |
| test | 20 | 2 | 5 |

---

## 7. Error handling flow

All API errors use the same JSON envelope (`ErrorResponse`):

```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Bad Request",
  "message": "...",
  "path": "/api/products",
  "fieldErrors": [],
  "errorReferenceId": null
}
```

`GlobalExceptionHandler` maps:

- Bean Validation → 400 + `fieldErrors`
- `ProductNotFoundException` → 404
- `DuplicateSkuException`, optimistic lock, catalog full → 409
- Malformed JSON → 400
- Unhandled → 500 + generic message + `errorReferenceId` (stack logged server-side)

---

## 8. How to verify the full implementation

```bash
cd Task14-Product-Catalog   # or repo root on standalone clone
docker info                 # Docker required
./mvnw clean test           # 93 tests — see docs/test-evidence.txt
```

Optional manual smoke test:

```bash
cp .env.example .env        # edit passwords
docker compose --env-file .env up -d
set -a && source .env && set +a
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

curl -X POST http://localhost:8080/api/products \
  -H 'Content-Type: application/json' \
  -d '{"sku":"DEMO-1","name":"Demo","category":"General","price":9.99,"stockQuantity":5,"active":true}'

curl 'http://localhost:8080/api/products?page=0&size=10&sort=name,asc'
curl http://localhost:8080/actuator/health
```

---

## 9. Known design trade-offs

Documented honestly in `SELF_REVIEW.md` and `reviews/pr-9/AZ-REVIEW.md`:

- **Catalog max products** — `count()` guard is best-effort under concurrent creates (no SERIALIZABLE).
- **PUT without version** — last-write-wins; pass `version` in body to get 409 on stale updates.
- **No `/api/v1`, no Spring Security** — out of Week 6 exercise scope.
- **Week 5 breaking change** — list endpoint returns a page envelope, not a bare array.

---

## 10. Reading order for new contributors

1. This doc — overall flow  
2. [week6-architecture.md](./diagrams/week6-architecture.md) — visual diagrams  
3. [SPEC.md](../SPEC.md) + [02-layered-api.md](./specs/product-catalog/02-layered-api.md) — contract  
4. `ProductService.java` — core business logic  
5. `ProductIntegrationTest.java` — end-to-end behaviour examples  
6. [curl-commands.sh](./curl-commands.sh) — hands-on API exploration  
