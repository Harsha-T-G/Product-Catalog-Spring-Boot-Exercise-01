# Product Catalog Tasks

**Status:** Week 7 tasks complete
**Branch:** `week7-security-logging`
**Contract:** `docs/security-spec.md` and the Week 7 exercise brief

## Completed task groups

- [x] **TASK-W7-001–TASK-W7-004 — Security boundary:** prove protected product
  access returns 401, configure HTTP Basic and restrictive defaults, then update
  authenticated regression tests.
- [x] **TASK-W7-005–TASK-W7-010 — Database authentication:** migrate the user and
  role schema, map repositories/entities, load users through `UserDetailsService`,
  configure BCrypt, and seed development users from environment variables.
- [x] **TASK-W7-011–TASK-W7-015 — Role authorization:** implement and verify
  VIEWER, EDITOR, and ADMIN access including method-level delete protection.
- [x] **TASK-W7-016–TASK-W7-020 — ADMIN user management:** create users, change
  enabled state, prevent self-disable, and verify immediate authentication
  behavior.
- [x] **TASK-W7-021–TASK-W7-026 — Logging and errors:** propagate trace IDs,
  return consistent 401/403 errors, emit safe logs, and document the required
  debugging scenarios.
- [x] **TASK-W7-027–TASK-W7-030 — Integration and delivery:** cover the exercise
  matrix through PostgreSQL-backed HTTP tests, update required documentation,
  scan for sensitive data, and run the Java 21 quality gate.
- [x] **TASK-W7-031 — BCrypt byte boundary:** reject passwords over 72 UTF-8
  bytes using reusable field validation.

## Required documentation

- `docs/security-spec.md` — role and endpoint contract
- `docs/debugging-notes.md` — three required debugging scenarios
- `docs/tdd-evidence.md` — authentic RED → GREEN evidence
- `README.md` — setup, security behavior, examples, and test commands
- `SELF_REVIEW.md` — completion, decisions, feedback, and limitations

## Verification

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./mvnw clean verify
```

Result: 158 tests passed with no failures or errors on OpenJDK 21.0.12.1.
