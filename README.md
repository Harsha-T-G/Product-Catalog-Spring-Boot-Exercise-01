# Product Catalog API

Week 5 Spring Boot fundamentals exercise — in-memory REST API for product
management.

**Status:** Agentic boilerplate ready. Exercises 1–3 implemented; `./mvnw verify` passes (28 tests).

## Prerequisites

- JDK 21
- Git

Maven Wrapper is included; a global Maven install is optional.

## Quick start

```bash
cd Task14-Product-Catalog
./mvnw clean verify
./mvnw spring-boot:run
```

The application starts on port 8080. Only the Spring context load test passes
until Exercise 1 is implemented.

## Agentic workflow

This project follows **Specify → Plan → Tasks → Implement**:

| Artifact | Path |
| --- | --- |
| Agent routing | [AGENTS.md](AGENTS.md) |
| Domain glossary | [CONTEXT.md](CONTEXT.md) |
| Product contract | [SPEC.md](SPEC.md) (Draft) |
| Capability specs | [docs/specs/product-catalog/](docs/specs/product-catalog/) |
| Implementation plan | [docs/plans/product-catalog-implementation-plan.md](docs/plans/product-catalog-implementation-plan.md) |
| Tasks | [docs/plans/product-catalog-tasks.md](docs/plans/product-catalog-tasks.md) |
| Java conventions | [.guidelines/java.md](.guidelines/java.md) |
| Spring Boot conventions | [.guidelines/spring-boot.md](.guidelines/spring-boot.md) |

Skills: `.agents/skills/spec-driven-development`, `test-driven-development`,
`verify-feature-readiness`.

## Exercise branches (planned)

```text
exercise-1-setup → exercise-2-layers → exercise-3-validation →
exercise-4-errors → exercise-5-config → exercise-6-tests
```

## Git conventions

**Commits:** `feat(scope): short imperative subject`

**Pull requests:** same format, e.g. `feat(exercise-1): project setup and info endpoint`

See [AGENTS.md](AGENTS.md) for full commit/PR rules.

## Endpoints (planned — not yet implemented)

| Method | Path | Status | Purpose |
| --- | --- | --- | --- |
| GET | `/api/info` | Exercise 1 | Application metadata |
| POST | `/api/products` | Exercise 2 | Create product |
| GET | `/api/products` | Exercise 2 | List products |
| GET | `/api/products/{id}` | Exercise 2 | Get product |
| PUT | `/api/products/{id}` | Exercise 2 | Update product |
| DELETE | `/api/products/{id}` | Exercise 2 | Delete product |
| GET | `/actuator/health` | Exercise 6 | Health check |
| GET | `/actuator/info` | Exercise 6 | Build info |

Detailed contracts are in `docs/specs/product-catalog/`.

## Next step

Review exercises 1–3 on branch `exercise-3-validation`, then continue with
Exercise 4 (global error handling) on branch `exercise-4-errors`.
