# Agent skills

Skills are **not duplicated** in this repo. Each skill folder contains a
**bootstrap `SKILL.md`** that tells the agent how to fetch the canonical skill
from the web.

## After cloning — human setup (one time)

From the repository root, either:

```bash
# Project-scoped install (good for this repo only)
./scripts/setup-agent-skills.sh

# Or global install (all projects on this machine)
./scripts/setup-agent-skills.sh --global
```

Or manually:

```bash
npx skills add "https://github.com/addyosmani/agent-skills" -y \
  -s spec-driven-development -s test-driven-development -a cursor
```

## After cloning — agent behavior

Agents should read the bootstrap file first, then fetch upstream instructions:

| Skill | Bootstrap (in repo — always present) | Canonical source |
| --- | --- | --- |
| `spec-driven-development` | `.agents/skills/spec-driven-development/SKILL.md` | [addyosmani/agent-skills](https://github.com/addyosmani/agent-skills) |
| `test-driven-development` | `.agents/skills/test-driven-development/SKILL.md` | same repo |

If skills are not installed locally, run:

```bash
npx skills use "https://github.com/addyosmani/agent-skills" --skill "<skill-name>"
```

and follow the printed `SKILL.md` before coding.

## Optional global copy

If you previously ran a global install, skills may also exist at:

```text
~/.agents/skills/spec-driven-development/SKILL.md
~/.agents/skills/test-driven-development/SKILL.md
```

Bootstrap files in this repo still apply **Product Catalog path overrides**
(`docs/specs/product-catalog/`, `docs/plans/`, `./mvnw` commands).

## Routing

See `AGENTS.md` for when to invoke each skill and project boundaries.
