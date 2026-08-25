# Product Catalog Tasks

**Status:** Draft — requires plan approval before execution  
**Traces to:** `docs/plans/product-catalog-implementation-plan.md`

Each task cites requirements, acceptance criteria, likely files, verification
command, and suggested commit message.

---

## TASK-001: Info endpoint (Exercise 1)

| Field | Value |
| --- | --- |
| **Requirements** | REQ-003, REQ-004 |
| **Acceptance** | AC-001, AC-002, AC-003 |
| **Branch** | `exercise-1-setup` |

**Steps (TDD):**

1. Write failing `@WebMvcTest` for `GET /api/info` → 200 + JSON fields.
2. Implement `InfoController` injecting `@Value` or `@ConfigurationProperties`.
3. Ensure values come from `application.yml`.

**Likely files:**

- `src/main/java/.../controller/InfoController.java`
- `src/test/java/.../controller/InfoControllerTest.java`
- `src/main/resources/application.yml`

**Verify:** `./mvnw verify`

**Commit:** `feat(api): add GET /api/info endpoint`

---

## TASK-002: Product model and DTOs (Exercise 2)

| Field | Value |
| --- | --- |
| **Requirements** | REQ-010 |
| **Acceptance** | AC-015 |
| **Branch** | `exercise-2-layers` |

**Steps:**

1. Add `Product`, `ProductRequest`, `ProductResponse`.
2. Add mapper with private or dedicated mapping methods.

**Likely files:**

- `model/Product.java`
- `dto/ProductRequest.java`, `dto/ProductResponse.java`

**Verify:** compile + existing tests green

**Commit:** `feat(product): add product model and DTOs`

---

## TASK-003: In-memory repository (Exercise 2)

| Field | Value |
| --- | --- |
| **Requirements** | REQ-011 |
| **Acceptance** | AC-010 (partial) |
| **Branch** | `exercise-2-layers` |

**Steps (TDD):**

1. Failing repository tests: save/find/findAll/delete/existsBySkuIgnoreCase/count.
2. Implement `InMemoryProductRepository`.

**Likely files:**

- `repository/ProductRepository.java`
- `repository/InMemoryProductRepository.java`
- `src/test/.../repository/InMemoryProductRepositoryTest.java`

**Verify:** `./mvnw -Dtest=InMemoryProductRepositoryTest test`

**Commit:** `feat(repository): add concurrent in-memory product store`

---

## TASK-004: Product service CRUD (Exercise 2)

| Field | Value |
| --- | --- |
| **Requirements** | REQ-012–REQ-016 |
| **Acceptance** | AC-010–AC-014 |
| **Branch** | `exercise-2-layers` |

**Steps (TDD):**

1. Service tests with mocked repository.
2. Implement create, list, get, update, delete.

**Likely files:**

- `service/ProductService.java`
- `src/test/.../service/ProductServiceTest.java`

**Verify:** `./mvnw -Dtest=ProductServiceTest test`

**Commit:** `feat(service): implement product CRUD operations`

---

## TASK-005: Product REST controller (Exercise 2)

| Field | Value |
| --- | --- |
| **Requirements** | REQ-012–REQ-017 |
| **Acceptance** | AC-010–AC-015 |
| **Branch** | `exercise-2-layers` |

**Steps (TDD):**

1. MockMvc tests for POST 201 + Location, GET, PUT, DELETE.
2. Implement `ProductController` with `ResponseEntity`.

**Likely files:**

- `controller/ProductController.java`
- `src/test/.../controller/ProductControllerTest.java`

**Verify:** `./mvnw verify`

**Commit:** `feat(api): add product CRUD REST endpoints`

---

## TASK-006: Bean Validation (Exercise 3)

| Field | Value |
| --- | --- |
| **Requirements** | REQ-030 |
| **Acceptance** | AC-030 |
| **Branch** | `exercise-3-validation` |

**Steps (TDD):**

1. MockMvc test: invalid body → validation failure.
2. Add annotations to `ProductRequest`; `@Valid` on controller.

**Verify:** `./mvnw -Dtest=ProductControllerTest test`

**Commit:** `feat(validation): add bean validation on product requests`

---

## TASK-007: Business rules and exceptions (Exercise 3)

| Field | Value |
| --- | --- |
| **Requirements** | REQ-031–REQ-035 |
| **Acceptance** | AC-031–AC-034 |
| **Branch** | `exercise-3-validation` |

**Steps (TDD):**

1. Service tests: duplicate SKU, not found.
2. Add `ProductNotFoundException`, `DuplicateSkuException`.

**Likely files:**

- `exception/ProductNotFoundException.java`
- `exception/DuplicateSkuException.java`
- `service/ProductService.java` (updates)

**Verify:** `./mvnw verify`

**Commit:** `feat(service): enforce SKU uniqueness and not-found rules`

---

## TASK-008: Global exception handler (Exercise 4)

| Field | Value |
| --- | --- |
| **Requirements** | REQ-050–REQ-054 |
| **Acceptance** | AC-050–AC-055 |
| **Branch** | `exercise-4-errors` |

**Steps (TDD):**

1. Tests per exception type → HTTP status + error JSON shape.
2. Implement `GlobalExceptionHandler` + `ErrorResponse`.

**Likely files:**

- `dto/ErrorResponse.java`
- `exception/GlobalExceptionHandler.java`
- `src/test/.../exception/GlobalExceptionHandlerTest.java`

**Verify:** `./mvnw verify`

**Commit:** `feat(exception): add centralized REST error handling`

---

## TASK-009: Catalog configuration (Exercise 5)

| Field | Value |
| --- | --- |
| **Requirements** | REQ-070–REQ-075 |
| **Acceptance** | AC-070–AC-074 |
| **Branch** | `exercise-5-config` |

**Steps (TDD):**

1. `CatalogProperties` + yaml profiles + env override.
2. Low-stock endpoint + max-products guard tests.

**Likely files:**

- `config/CatalogProperties.java`
- `application-dev.yml`, `application-test.yml`
- `controller/ProductController.java` (low-stock route)
- `service/ProductService.java` (limit + filter)

**Verify:** `./mvnw verify` with `@ActiveProfiles("test")`

**Commit:** `feat(config): add catalog properties and low-stock endpoint`

---

## TASK-010: Test suite completion (Exercise 6)

| Field | Value |
| --- | --- |
| **Requirements** | REQ-090–REQ-092 |
| **Acceptance** | AC-090, AC-092 |
| **Branch** | `exercise-6-tests` |

**Steps:**

1. Fill gaps in service, controller, and integration test matrices per spec.
2. Actuator config: expose health + info only.

**Likely files:**

- `src/test/**` (expand existing)
- `application.yml` (management endpoints)

**Verify:** `./mvnw clean verify`

**Commit:** `test(catalog): complete unit web and integration test coverage`

---

## TASK-011: Documentation and deliverables (Exercise 6)

| Field | Value |
| --- | --- |
| **Requirements** | REQ-093–REQ-095 |
| **Acceptance** | AC-091, AC-093 |
| **Branch** | `exercise-6-tests` |

**Steps:**

1. Complete README with endpoint table and samples.
2. Add `docs/curl-commands.sh`, test evidence, `SELF_REVIEW.md`.

**Verify:** manual README walkthrough + `./mvnw verify`

**Commit:** `docs(readme): add setup endpoints and submission deliverables`

---

## Coverage matrix

| Requirement | Task(s) |
| --- | --- |
| REQ-001–005 | TASK-001 |
| REQ-010–017 | TASK-002–005 |
| REQ-030–035 | TASK-006–007 |
| REQ-050–054 | TASK-008 |
| REQ-070–075 | TASK-009 |
| REQ-090–095 | TASK-010–011 |

Every acceptance criterion AC-001 through AC-095 maps to at least one task above.
