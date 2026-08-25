# Product Catalog Agent Guidelines

## Scope and precedence

This file is the repository-wide source of truth for coding agents. Platform
instructions and the current user request apply first. Within this repository:

1. An approved `SPEC.md` and its linked `docs/specs/product-catalog/` chunks
   define product behavior. `SPEC.md` owns their shared approval status.
2. `CONTEXT.md` defines stable domain vocabulary.
3. `.guidelines/java.md` and `.guidelines/spring-boot.md` define stack
   conventions.
4. Existing code and tests describe current behavior but do not override an
   approved contract.

Surface conflicts instead of silently selecting a source.

## Repository purpose

Product Catalog is a Spring Boot 3.x learning application that exposes an
in-memory REST API for product management. It demonstrates layered design,
dependency injection, validation, centralized error handling, configuration
profiles, and automated testing. It deliberately has no database, JPA, or Lombok.

## Gated workflow

Use **Specify → Plan → Tasks → Implement**. Stop for human approval after each
phase. Do not implement behavior that is not in the approved spec.

| Phase | Artifact | Gate |
| --- | --- | --- |
| Specify | `SPEC.md`, `docs/specs/product-catalog/` | Human approves spec |
| Plan | `docs/plans/product-catalog-implementation-plan.md` | Human approves plan |
| Tasks | `docs/plans/product-catalog-tasks.md` | Human approves tasks |
| Implement | Source and tests per task | TDD + verify-feature-readiness |

**Current status:** Draft spec and plan prepared. **Implementation is forbidden
until the spec is approved.**

## Required routing

- New features or material behavior changes: read and follow
  `.agents/skills/spec-driven-development/SKILL.md`.
- API, validation, configuration, or error-handling work: read the matching
  `docs/specs/product-catalog/` chunk listed in `SPEC.md`.
- Java or Spring Boot source, tests, Maven: read `.guidelines/java.md` and
  `.guidelines/spring-boot.md`.
- Implementation or bug fixes: follow
  `.agents/skills/test-driven-development/SKILL.md`.
- Before reporting work ready, committing, or opening a PR: follow
  `.agents/skills/verify-feature-readiness/SKILL.md`.
- Domain terminology: read `CONTEXT.md`.

## Ownership and layout

```text
src/main/java/com/codewalnut/productcatalog/
  controller/     HTTP adapters only
  service/        business rules and orchestration
  repository/     in-memory persistence boundary
  model/          internal domain entities
  dto/            request/response and error payloads
  exception/      domain exceptions and global handler
  config/         @ConfigurationProperties and profile wiring
src/test/java/    mirrors production packages
SPEC.md           product contract index and approval status
docs/specs/product-catalog/   capability contract chunks
docs/plans/       implementation plan and tasks (after spec approval)
CONTEXT.md        stable domain glossary
.agents/skills/   reusable agent procedures
.guidelines/      stable stack conventions
```

Create feature packages only when implementing that capability. Do not add empty
placeholder classes.

## Commands

Use the checked-in Maven Wrapper:

```bash
./mvnw clean verify
./mvnw test
./mvnw spring-boot:run
./mvnw -Dtest=ClassName test
```

The project targets Java 21 and Spring Boot 3.4.x. A matching JDK is required.

## Git and pull request conventions

### Commit messages

Use [Conventional Commits](https://www.conventionalcommits.org/) with this shape:

```text
feat(scope): short imperative subject
fix(scope): short imperative subject
docs(scope): short imperative subject
test(scope): short imperative subject
chore(scope): short imperative subject
```

Examples:

- `feat(api): add GET /api/info endpoint`
- `feat(product): implement in-memory product repository`
- `fix(validation): reject duplicate SKU case-insensitively`
- `test(service): cover low-stock filtering with test profile`
- `docs(readme): document available profiles and endpoints`
- `chore(setup): scaffold agentic boilerplate and draft spec`

Rules:

- Use lowercase type and scope.
- Subject is imperative, present tense, no trailing period.
- Body is optional; reference requirement IDs when useful (`REQ-020`).
- One logical change per commit when possible.

### Pull request titles

Match commit style:

```text
feat(scope): short description of the deliverable
```

Examples:

- `feat(setup): agentic boilerplate and draft product catalog spec`
- `feat(exercise-1): project setup and info endpoint`
- `feat(exercise-6): automated tests and actuator configuration`

Branch naming (recommended):

```text
exercise-N-short-description
feat/short-description
```

Use the PR template at `.github/pull_request_template.md`.

## Working boundaries

Always:

- Keep changes scoped to an approved requirement and task.
- Write a failing behavior test before production behavior (TDD).
- Use constructor injection only; never field `@Autowired`.
- Use DTOs at REST boundaries; never expose internal model objects.
- Use `BigDecimal` for price; validate all external inputs.
- Preserve unrelated work and inspect the complete diff before handoff.

Ask first:

- Changing the approved spec or Java/Spring Boot version.
- Adding dependencies beyond the exercise list (web, validation, actuator, test).
- Adding database, JPA, Lombok, or CI workflows.
- Changing public API contracts or package boundaries.

Never:

- Add secrets, credentials, or personal environment data to the repo.
- Log passwords, tokens, or complete sensitive request bodies.
- Use Lombok, Spring Data JPA, or a database for this exercise.
- Weaken, skip, or delete a failing test to obtain a green build.
- Implement behavior that is not in the approved spec.
- Commit, push, or open a pull request without explicit user authorization.
