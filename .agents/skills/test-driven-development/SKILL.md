---
name: test-driven-development
description: Use when implementing behavior, fixing a defect, changing logic, or adding REST endpoints in Product Catalog.
---

# Test-Driven Development

For each approved behavior, use **RED → GREEN → REFACTOR**.

1. Read the requirement, acceptance criterion, and current code.
2. Write one focused JUnit 5 behavior test. For defects, reproduce the failure.
3. Run the narrowest Maven test command and confirm it fails for the expected
   reason. A test that passes immediately does not prove the new behavior.
4. Write the smallest production change that satisfies the test.
5. Re-run the focused test, then affected tests. Refactor only while green.
6. Before task completion, run `./mvnw clean verify`.

Name tests in Given-When-Then form, for example
`givenDuplicateSku_whenCreateProduct_thenThrowsDuplicateSkuException`.
Structure each test with `// Arrange`, `// Act`, and `// Assert` comments.

Test level selection:

- **Service:** `@ExtendWith(MockitoExtension.class)` + mocked repository.
- **Controller:** `@WebMvcTest` + MockMvc + mocked service; import exception handler.
- **Integration:** `@SpringBootTest` + `@ActiveProfiles("test")` for full HTTP flows.

Prefer real in-memory objects in repository tests; mock only external or slow
boundaries. Never weaken, skip, or delete a failing test to obtain green output.
Record RED and GREEN commands in `AI_USAGE.md` or task evidence.

Suggested commit after each green cycle: `test(scope): ...` or `feat(scope): ...`
per `AGENTS.md`.
