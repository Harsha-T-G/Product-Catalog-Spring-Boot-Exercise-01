# Agent skills

Skills are **vendored in this repository** under `.agents/skills/`. Each folder
contains a complete `SKILL.md` — agents should read the repo copy only, not fetch
external URLs or depend on `~/.agents/skills/`.

## Available skills

| Skill | Path | When to use |
| --- | --- | --- |
| `spec-driven-development` | `.agents/skills/spec-driven-development/SKILL.md` | New feature or unclear requirements |
| `test-driven-development` | `.agents/skills/test-driven-development/SKILL.md` | Implementing behavior or fixing bugs |
| `create-pr` | `.agents/skills/create-pr/SKILL.md` | Opening or updating a pull request |
| `write-pr-description` | `.agents/skills/write-pr-description/SKILL.md` | Drafting PR body from template |

`write-pr-description` includes supporting references under
`.agents/skills/write-pr-description/references/`.

## Product Catalog path overrides

Built into the vendored SDD/TDD skills:

| Generic | This repository |
| --- | --- |
| Spec output | `docs/specs/product-catalog/` indexed by `SPEC.md` |
| Plan | `docs/plans/product-catalog-implementation-plan.md` |
| Tasks | `docs/plans/product-catalog-tasks.md` |
| Verify | `./mvnw clean verify` |

## Optional: upstream sync

To refresh from [addyosmani/agent-skills](https://github.com/addyosmani/agent-skills)
or your global `~/.agents/skills/` copies, copy files into `.agents/skills/` and
re-apply the **Product Catalog overrides** sections at the top of each skill.

`scripts/setup-agent-skills.sh` is optional legacy install for global copies — the
repo skills are authoritative for agents working in this project.

See `AGENTS.md` for routing and boundaries.
