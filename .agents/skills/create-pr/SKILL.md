---
name: create-pr
description: Create or update a pull request for Product Catalog. Self-contained vendored skill — no external fetch required.
---

# create-pr (Product Catalog)

## Pre-PR checklist

### 1. Sync with base branch

```bash
git fetch origin
git merge origin/<base-branch>   # e.g. task14-main or previous exercise branch
```

Resolve merge conflicts before opening the PR.

### 2. Run tests

```bash
./mvnw clean verify
```

Docker must be running (Testcontainers). Optional runtime check:

```bash
./scripts/run-dev.sh
```

Do not open a PR with failing tests unless the description states what failed and why.

### 3. Review the diff

```bash
git --no-pager log origin/<base>..HEAD --oneline
git --no-pager diff origin/<base>...HEAD --stat
git --no-pager diff origin/<base>...HEAD
```

### 4. Write the PR body

Use `.agents/skills/write-pr-description/SKILL.md` and fill `.github/pull_request_template.md`.

Do not commit or push without explicit user authorization (see `AGENTS.md`).

### 5. Open or update the PR

```bash
gh pr view --json number,url
gh pr create --title "feat(scope): short subject" --body-file /tmp/pr-body.md
gh pr edit --body-file /tmp/pr-body.md
```

Title: Conventional Commits — `feat(scope):`, `fix(scope):`, `docs(scope):` (see `AGENTS.md`).

## Base branch guide

| Work type | Typical base |
| --- | --- |
| Week 6 exercise branches | Previous exercise branch or `task14-main` |
| Week 5 exercise branches | Previous exercise branch or `task14-main` |
| Standalone delivery | `task14-main` or `main` |

## Testing expectations

- Bug fixes: include a regression test (Prove-It pattern — see TDD skill).
- New behavior: failing test first, then implementation.
- API changes: extend `ProductIntegrationTest` or controller tests as appropriate.

## Related paths

| Resource | Path |
| --- | --- |
| PR template | `.github/pull_request_template.md` |
| PR description skill | `.agents/skills/write-pr-description/SKILL.md` |
| Spec index | `SPEC.md` |
| Plans / tasks | `docs/plans/` |
