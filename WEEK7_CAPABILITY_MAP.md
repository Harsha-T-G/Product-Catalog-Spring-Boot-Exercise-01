# Week 7 Security and Logging Capability Map

**Status:** Approved on 2026-09-09
**Baseline:** `week6-exercise-6-docs-delivery`
**Implementation branch:** `week7-security-logging`
**Requirements source:** Week 7 Spring Security, logging, debugging, and error-handling exercise brief

## Scope decision

Week 7 is an initiative containing multiple independently testable capabilities,
not one implementation task. The approved Week 6 product API remains the
baseline. JWT authentication is excluded until every required Week 7 capability
and test is complete.

## Capability modules

| Module id | Responsibility | Depends on |
| --- | --- | --- |
| `security-contract` | Canonical role/endpoint matrix, 401/403 semantics, disabled-user behavior, sensitive-data rules, and test checklist in `docs/security-spec.md` | Week 6 baseline |
| `database-authentication` | User/role Flyway schema, JPA persistence, BCrypt password encoding, case-insensitive user loading, disabled-user rejection, HTTP Basic, and safe development users | `security-contract` |
| `role-authorization` | Request-level VIEWER/EDITOR/ADMIN permissions, restrictive default rule, Actuator protection, and defense-in-depth method security | `database-authentication` |
| `admin-user-management` | ADMIN-only user creation and enable/disable APIs, validation, duplicate handling, password non-disclosure, and self-disable prevention | `role-authorization` |
| `request-observability` | Trace-ID validation/generation, MDC lifecycle, response header, safe request and security-event logging, and trace-aware application/security errors | `database-authentication`, `role-authorization`, `admin-user-management` |
| `security-verification-delivery` | PostgreSQL Testcontainers security flows, debugging notes, four or more recorded RED→GREEN cycles, README/API examples, self-review, and final verification evidence | all preceding modules |

## Dependency direction

```text
security-contract
        ↓
database-authentication
        ↓
role-authorization
        ↓
admin-user-management
        └──→ request-observability
                  ↓
security-verification-delivery
```

`request-observability` also consumes the authenticated principal supplied by
`database-authentication` and authorization outcomes supplied by
`role-authorization`. No earlier module depends on logging or delivery code.

## Build order

1. `security-contract`
2. `database-authentication`
3. `role-authorization`
4. `admin-user-management`
5. `request-observability`
6. `security-verification-delivery`

Each module will run its own Specify → Plan → Tasks → Implement gates and
will be implemented through small behavior-first RED → GREEN slices on the
single Week 7 branch.

## Approval

- [x] Module boundaries approved
- [x] Dependency direction approved
- [x] Build order approved
- [x] Single Week 7 branch and focused final PR approved
