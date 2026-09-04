# Spring Boot Guidelines — Product Catalog

These conventions apply in addition to `.guidelines/java.md`.

## Application structure

- Single `@SpringBootApplication` class at package root.
- Enable `@ConfigurationProperties` via `@EnableConfigurationProperties` on the
  application class or a dedicated `@Configuration` class.
- Keep `@SpringBootApplication` scan scope at `com.codewalnut.productcatalog`.

## Dependency injection

- One constructor per bean; inject dependencies as `final` fields.
- Prefer `@Service`, `@Repository`, `@RestController` over bare `@Component`.
- Define repository as interface; inject interface into service.

## Configuration

- Use `application.yml` as primary config; profile files for dev and test.
- Bind grouped settings with `@ConfigurationProperties(prefix = "catalog")`.
- Prefer `@ConfigurationProperties` over repeated `@Value` for related settings.
- Override sensitive or environment-specific values via env vars, not committed
  secrets.

Example property binding:

```yaml
catalog:
  low-stock-threshold: 5
  maximum-products: ${CATALOG_MAXIMUM_PRODUCTS:500}
  default-category: General
```

## Actuator

- Include `spring-boot-starter-actuator` dependency.
- Expose only required endpoints:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

- Do not expose env, beans, or shutdown endpoints in this exercise.

## Testing with Spring

| Annotation | Use when |
| --- | --- |
| `@SpringBootTest` | Full context integration flows |
| `@WebMvcTest(Controller.class)` | Controller HTTP contract in isolation |
| `@ExtendWith(MockitoExtension.class)` | Pure service unit tests |
| `@ActiveProfiles("test")` | Integration tests using test profile limits |
| `@MockBean` | Replace a Spring bean in slice/full tests |
| `@Autowired MockMvc` | HTTP assertions in web tests |

Import `GlobalExceptionHandler` in `@WebMvcTest` when testing error responses.

## Running locally

```bash
./mvnw spring-boot:run
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
./mvnw clean verify
```

Default server port: 8080 (override with `server.port` in yaml if needed).

## What not to add

- Spring Data JPA, `@Entity`, or datasource configuration
- Prefer `@Getter` / `@Setter` or `@Value` on DTOs; use `@Getter` `@Setter` on entities (not `@Data`).
- Spring Security (unless exercise scope expands)
- Custom embedded server configuration beyond exercise requirements
