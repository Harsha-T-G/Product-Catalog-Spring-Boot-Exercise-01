# Product Catalog API

Week 5 Spring Boot fundamentals — in-memory REST API for product management.

**Status:** Exercises 1–6 implemented. `./mvnw verify` passes (45 tests).

## Prerequisites

- JDK 21
- Git

## Quick start

```bash
cd Task14-Product-Catalog   # or repo root in Product-Catalog exercise repo
./mvnw clean verify
./mvnw spring-boot:run
curl http://localhost:8080/api/info
```

Default port: **8080**

## Profiles

| Profile | Command | low-stock threshold | max products |
|---------|---------|---------------------|--------------|
| default | `./mvnw spring-boot:run` | 5 | 500 (or `CATALOG_MAXIMUM_PRODUCTS`) |
| dev | `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` | 10 | 1000 |
| test | `./mvnw test -Dspring.profiles.active=test` | 2 | 20 |

Override max products:

```bash
CATALOG_MAXIMUM_PRODUCTS=5 ./mvnw spring-boot:run
```

## Endpoint table

| Method | Path | Status | Purpose |
|--------|------|--------|---------|
| GET | `/api/info` | 200 | Application metadata |
| GET | `/actuator/health` | 200 | Health check |
| GET | `/actuator/info` | 200 | Build info |
| POST | `/api/products` | 201 | Create product |
| GET | `/api/products` | 200 | List all products |
| GET | `/api/products/low-stock` | 200 | Active low-stock products |
| GET | `/api/products/{id}` | 200 | Get one product |
| PUT | `/api/products/{id}` | 200 | Update product |
| DELETE | `/api/products/{id}` | 204 | Delete product |

Error responses: **400** validation, **404** not found, **405** method not allowed, **409** conflict, **500** unexpected.

## Sample requests

**Create product**

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"sku":"SKU-001","name":"Sample","category":"General","price":19.99,"stockQuantity":10,"active":true}'
```

**Validation error (400)**

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"sku":"","name":"X","category":"General","price":-1,"stockQuantity":0}'
```

See [docs/curl-commands.sh](docs/curl-commands.sh) for more samples.

## Package structure

```text
com.codewalnut.productcatalog/
├── controller/   InfoController, ProductController
├── service/      ProductService
├── repository/   ProductRepository, InMemoryProductRepository
├── model/        Product
├── dto/          Request/response and error payloads
├── mapper/       ProductMapper
├── exception/    Domain exceptions, GlobalExceptionHandler
└── config/       CatalogProperties
```

## Exercise branches

```text
task14-main → exercise-1-setup → exercise-2-layers → exercise-3-validation →
exercise-4-errors → exercise-5-config → exercise-6-tests
```

## Agentic workflow

| Artifact | Path |
|----------|------|
| Spec | [SPEC.md](SPEC.md) |
| Specs | [docs/specs/product-catalog/](docs/specs/product-catalog/) |
| Plans | [docs/plans/](docs/plans/) |
| Self review | [SELF_REVIEW.md](SELF_REVIEW.md) |
| Test evidence | [docs/test-evidence.txt](docs/test-evidence.txt) |

## Tests

```bash
./mvnw clean verify
./mvnw -Dtest=ProductServiceTest test
./mvnw -Dtest=ProductIntegrationTest test
```
