---
name: verify-feature-readiness
description: Use when preparing to report a Product Catalog feature ready, commit, open a pull request, or verify spec, tests, and repository hygiene.
---

# Verify Feature Readiness

1. Read `AGENTS.md`, the approved `SPEC.md`, the affected
   `docs/specs/product-catalog/` chunk, and routed guidelines.
2. Inspect Git status and the complete diff, including untracked files. Exclude
   unrelated, generated, IDE, secret, and environment files.
3. Map every changed behavior to its requirement ID, test, and evidence.
4. Run focused tests, then `./mvnw clean verify` with JDK 21.
5. For REST changes, smoke-test endpoints with curl or MockMvc as applicable.
6. Confirm DTO boundaries, validation placement, error envelope consistency,
   configuration profile behavior, and Actuator exposure limits.
7. Verify commit messages and PR title follow `feat(scope): subject` convention.
8. Report exact commands, exit codes, skipped checks with reasons, unresolved
   assumptions, and residual risk. Update `AI_USAGE.md` and PR evidence.

Never weaken a check or claim readiness while required verification is failing
or unavailable. Distinguish product defects from environment blockers.

PR title must match the deliverable, for example:
`feat(exercise-1): project setup and info endpoint`.
