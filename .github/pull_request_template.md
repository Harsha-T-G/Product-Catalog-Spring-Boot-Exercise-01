## Description

Briefly describe the bug or feature delivered in this PR.

Fixes # (issue number, or N/A)

## Why are these changes needed?

Business or technical context. Why this path vs alternatives.

## Key Changes

- Concrete bullets of what changed

## Specification

- Requirement(s): REQ-xxx
- Acceptance criteria: AC-xxx
- Approved plan/task: TASK-xxx / PLAN-xxx

## How to Test

1. Checkout the branch
2. `./mvnw clean verify`
3. `./mvnw spring-boot:run` (if runtime verification needed)
4. Describe curl or MockMvc checks that prove the change

## Verification

- [ ] Focused RED test captured (TDD)
- [ ] Focused GREEN test captured
- [ ] `./mvnw clean verify` — exit 0
- [ ] Diff reviewed for unrelated/generated/secret files
- [ ] Commit messages follow `feat(scope): subject`
- [ ] README/docs updated when behavior or endpoints changed

Exact commands and outcomes:

```text
<command> — exit <code>
```

## Remaining risk / follow-up

- 

## Media

No UI — link `docs/curl-commands.sh` or spec/plan paths when relevant.
