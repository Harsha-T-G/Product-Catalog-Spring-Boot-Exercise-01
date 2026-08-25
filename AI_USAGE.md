# AI Usage Record — Product Catalog

Track material AI-assisted work for exercise submission and self-review.

## Session log

| Date | Phase | Prompt / intent | Outcome | Verified |
| --- | --- | --- | --- | --- |
| 2026-08-26 | Specify | Agentic boilerplate + draft spec/plan for Week 5 Spring Boot exercise | Created Task14-Product-Catalog scaffold, SPEC, plans, skills, guidelines | `./mvnw verify` pending |

## TDD evidence

Record RED → GREEN cycles per task:

```text
TASK-xxx:
  RED:   ./mvnw -Dtest=... test — exit 1 — <reason>
  GREEN: ./mvnw -Dtest=... test — exit 0
```

## Accepted / rejected suggestions

- **Accepted:** Single repo with exercise branch checkpoints; Conventional Commits
  `feat(scope): subject` for commits and PR titles.
- **Rejected:** Spring Initializr (requires Boot 4.x online); manual Boot 3.4.2 pom instead.

## Agent errors and corrections

- Initializr returned 400 for Spring Boot 3.x — created pom manually with 3.4.2.

## Open items

- [ ] Human approval of SPEC.md
- [ ] Resolve open questions in SPEC.md
- [ ] Begin TASK-001 after approval
