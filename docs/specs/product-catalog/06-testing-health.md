# Testing, Actuator, and Deliverables Contract

**Status:** Implemented — governed by `SPEC.md`  
**Covers:** `REQ-090`–`REQ-100`, `AC-090`–`AC-095`

## Requirements

### REQ-090: Service unit tests

JUnit 5 + Mockito tests for `ProductService` covering at minimum:

- Valid create succeeds
- Duplicate SKU rejected (case variants)
- Lookup succeeds; missing throws `ProductNotFoundException`
- Update preserves id; delete succeeds
- Low-stock uses configured threshold
- Create rejected at maximum products

### REQ-091: Controller web tests

`@WebMvcTest` + MockMvc covering at minimum:

- POST 201 with Location header
- GET by id 200
- Invalid request 400 with field errors
- Missing product 404
- Duplicate SKU 409
- DELETE 204

### REQ-092: Integration tests

`@SpringBootTest` with `@ActiveProfiles("test")` covering:

- Create then retrieve via API
- Update then verify response
- Delete then later lookup returns 404

### REQ-093: Actuator exposure

Expose only `health` and `info` Actuator endpoints. Include application name and
version in info. Verify `/actuator/health` returns UP. Do not expose all
Actuator endpoints.

### REQ-094: Test discipline

Tests use Given-When-Then naming, Arrange/Act/Assert structure, one behavior per
test, independence, and observable outcomes. Full suite via `./mvnw test`.

### REQ-095: Final deliverables

README (setup, profiles, endpoint table, samples), curl or Postman collection,
test run evidence, and self-review document listing completed work, design
decisions, problems resolved, and known limitations.

## Acceptance criteria

### AC-090: Full suite green

**Given** all exercises implemented, **when** `./mvnw verify` runs,
**then** all tests pass with exit code 0.

### AC-091: Actuator limited

**Given** running application, **when** `/actuator/health` and `/actuator/info`
are accessed, **then** they respond successfully; other actuator endpoints are
not exposed.

### AC-092: Integration flow

**Given** test profile, **when** create-get-update-delete flow runs through HTTP,
**then** each step matches expected status and body.

### AC-093: README complete

**Given** the final README, **when** a new developer follows setup steps,
**then** they can run the app and tests without undocumented steps.

## Testing focus

- Clear separation: unit (Mockito), slice (`@WebMvcTest`), integration
  (`@SpringBootTest`)
- Not every test uses `@SpringBootTest`

## Deliverable artifacts

| Artifact | Location |
| --- | --- |
| README | `README.md` |
| curl samples | `docs/curl-commands.sh` or equivalent |
| Test evidence | `docs/test-evidence.txt` or README section |
| Self-review | `SELF_REVIEW.md` |
