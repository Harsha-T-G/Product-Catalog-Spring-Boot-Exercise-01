# Project Setup Contract

**Status:** Implemented — governed by `SPEC.md`  
**Covers:** `REQ-001`–`REQ-005`, `AC-001`–`AC-003`

Read `SPEC.md` first for assumptions, scope, and approval status.

## Requirements

### REQ-001: Maven Spring Boot project

The repository shall be a Maven-based Spring Boot 3.x application with
dependencies: Spring Web, Validation, Actuator, and Spring Boot Test. Coordinates:
`com.codewalnut:product-catalog`. Base package: `com.codewalnut.productcatalog`.
Java 21.

### REQ-002: Application entry point

A `@SpringBootApplication` main class shall bootstrap the application via
`SpringApplication.run`.

### REQ-003: Info endpoint

`GET /api/info` shall return JSON:

```json
{
  "application": "<from configuration>",
  "version": "<from configuration>",
  "status": "UP"
}
```

Application name and version must come from configuration (`application.yml`), not
hardcoded in the controller.

### REQ-004: Runnable and verifiable

The application shall start via IDE and `./mvnw spring-boot:run`. A basic
`@SpringBootTest` context-load test shall pass.

### REQ-005: Package structure readiness

The repository shall document the target package layout
(`controller`, `service`, `repository`, `model`, `dto`, `exception`, `config`)
without implementing business endpoints beyond `/api/info`.

## Acceptance criteria

### AC-001: Info endpoint success

**Given** the application is running with default configuration, **when**
`GET /api/info` is called, **then** the response is HTTP 200 with
`application`, `version`, and `status: UP"` populated from configuration.

### AC-002: Context loads

**Given** the test classpath, **when** `./mvnw test` runs
`ProductCatalogApplicationTest`, **then** the Spring context loads without error.

### AC-003: No hardcoded metadata

**Given** `application.yml` changes the app name or version, **when**
`GET /api/info` is called, **then** the response reflects the configured values.

## Testing focus

- `@SpringBootTest` context load
- Optional `@WebMvcTest` for `/api/info` once controller exists

## Out of scope for this chunk

- Product CRUD, validation, error handler, profiles beyond defaults, Actuator
  exposure configuration (Actuator dependency may be present; full config in
  chunk 06).
