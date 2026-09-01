---
name: test-driven-development
description: Bootstrap for addyosmani test-driven-development. Use when implementing behavior or fixing bugs. Fetches the canonical skill from the web if not installed locally.
upstream-repo: https://github.com/addyosmani/agent-skills
upstream-skill: test-driven-development
---

# Test-Driven Development (Product Catalog bootstrap)

This file is a **project-local pointer**. The full skill lives in the
[addyosmani/agent-skills](https://github.com/addyosmani/agent-skills) repository.
Do not implement from this stub alone.

## Step 1 — Load the canonical skill (required)

Before writing production code, load the upstream TDD instructions using
**one** of these methods (prefer A):

### A. Fetch skill text for this session (no install)

```bash
npx skills use "https://github.com/addyosmani/agent-skills" --skill "test-driven-development"
```

Read the complete `SKILL.md` output and follow RED → GREEN → REFACTOR.

### B. Install globally (recommended for regular work)

```bash
npx skills add "https://github.com/addyosmani/agent-skills" -g -y \
  -s spec-driven-development -s test-driven-development -a cursor
```

Then read `~/.agents/skills/test-driven-development/SKILL.md`.

### C. Install for this project only

From the repository root:

```bash
npx skills add "https://github.com/addyosmani/agent-skills" -y \
  -s spec-driven-development -s test-driven-development -a cursor
```

Verify with `npx skills list`.

## Step 2 — Apply Product Catalog test conventions

After loading the upstream skill, also read:

- `AGENTS.md` — commit conventions, boundaries
- `.guidelines/java.md` — package rules, naming
- `.guidelines/spring-boot.md` — test levels for this stack

| Concern | This repository |
| --- | --- |
| Full suite | `./mvnw clean verify` |
| Single test class | `./mvnw -Dtest=ClassName test` |
| Service unit tests | `@ExtendWith(MockitoExtension.class)`, mock `ProductRepository` |
| HTTP tests | `@SpringBootTest` + `MockMvc` (see existing controller tests) |
| Integration + test profile | `@ActiveProfiles("test")` on integration tests |
| Test naming | Given-When-Then; `// Arrange`, `// Act`, `// Assert` |

Use real `CatalogProperties` objects in unit tests when Mockito cannot mock
configuration classes on newer JDKs.

Never weaken, skip, or delete a failing test to obtain a green build.
