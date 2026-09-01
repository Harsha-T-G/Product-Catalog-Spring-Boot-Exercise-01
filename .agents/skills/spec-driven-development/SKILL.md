---
name: spec-driven-development
description: Bootstrap for addyosmani spec-driven-development. Use when starting a feature or change and no approved spec exists. Fetches the canonical skill from the web if not installed locally.
upstream-repo: https://github.com/addyosmani/agent-skills
upstream-skill: spec-driven-development
---

# Spec-Driven Development (Product Catalog bootstrap)

This file is a **project-local pointer**. The full skill lives in the
[addyosmani/agent-skills](https://github.com/addyosmani/agent-skills) repository.
Do not implement from this stub alone.

## Step 1 — Load the canonical skill (required)

Before Specify / Plan / Tasks / Implement, load the upstream instructions using
**one** of these methods (prefer A):

### A. Fetch skill text for this session (no install)

```bash
npx skills use "https://github.com/addyosmani/agent-skills" --skill "spec-driven-development"
```

Read the complete `SKILL.md` output and treat it as binding for this task.

### B. Install globally (recommended for regular work)

```bash
npx skills add "https://github.com/addyosmani/agent-skills" -g -y \
  -s spec-driven-development -s test-driven-development -a cursor
```

Then read `~/.agents/skills/spec-driven-development/SKILL.md`.

### C. Install for this project only

From the repository root:

```bash
npx skills add "https://github.com/addyosmani/agent-skills" -y \
  -s spec-driven-development -s test-driven-development -a cursor
```

Verify with `npx skills list`.

## Step 2 — Apply Product Catalog overrides

After loading the upstream skill, override these paths and commands:

| Upstream default | This repository |
| --- | --- |
| Generic spec location | `SPEC.md` + `docs/specs/product-catalog/` |
| `tasks/plan.md` | `docs/plans/product-catalog-implementation-plan.md` |
| `tasks/todo.md` | `docs/plans/product-catalog-tasks.md` |
| Generic build/test | `./mvnw clean verify`, `./mvnw test` (see `AGENTS.md`) |

**Companion skills** (fetch from the same upstream repo when SDD references them):

- `planning-and-task-breakdown` — Phase 2–3 task breakdown
- `incremental-implementation` — Phase 4 incremental delivery
- `test-driven-development` — see `.agents/skills/test-driven-development/SKILL.md`

## Step 3 — Gate

Stop for human approval after Specify, Plan, and Tasks. Do not implement
behavior that is not in the approved spec (`SPEC.md` and linked chunks).
