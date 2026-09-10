# ADMIN User Management Contract

**Status:** Implemented on 2026-09-09; byte-limit hardening verified on 2026-09-10
**Module:** `admin-user-management`
**Covers:** `REQ-140`–`REQ-149`, `AC-140`–`AC-147`
**Depends on:** Implemented PLAN-W7-02 and PLAN-W7-03

## API contract

### Create user

`POST /api/admin/users`

```json
{
  "username": "catalog-viewer",
  "password": "environment-value",
  "roles": ["VIEWER"]
}
```

Success is HTTP 201 with a user response containing only `id`, `username`,
`enabled`, `roles`, and `createdAt`. Password and password hash are never part
of a response DTO.

### Change enabled state

`PATCH /api/admin/users/{username}/enabled`

```json
{ "enabled": false }
```

Success is HTTP 200 with the same safe user response. Username lookup is
case-insensitive.

## Requirements

### REQ-140: ADMIN-only boundary

Only ADMIN may pass request and method authorization for user creation or
enabled-state changes. VIEWER and EDITOR receive 403; anonymous callers receive
401. Controllers contain no manual role checks.

### REQ-141: Create validation

The trimmed username length shall be 3–50 characters. Password length shall be
10–72 characters and its UTF-8 representation shall not exceed 72 bytes,
preventing BCrypt's input-limit ambiguity for both ASCII and multibyte input.
At least one non-null role from VIEWER, EDITOR, or ADMIN is required. Invalid or
malformed input receives 400 and field validation details where available.

### REQ-142: Safe password persistence

The service shall BCrypt-encode the transient request password before saving.
Neither plaintext nor hash shall be returned or logged.

### REQ-143: Duplicate usernames

Application pre-check and the database lower-username unique index shall enforce
case-insensitive uniqueness. Duplicate creation returns 409 with no SQL detail.

### REQ-144: Role assignment

Every requested canonical role shall resolve from the `roles` table and be
connected through `app_user_roles`. The response lists assigned roles in enum
order. Missing canonical migration data is an unexpected server failure.

### REQ-145: Enabled-state change

Changing another user's enabled state is transactional and returns the updated
safe response. A missing target returns 404. A disabled user fails the next
stateless HTTP Basic attempt.

### REQ-146: Self-disable prevention

An ADMIN may not set their own case-insensitive username to `enabled=false`.
The operation returns 409. Self-enable is not blocked, although a disabled user
cannot authenticate to request it.

### REQ-147: Safe response model

User REST responses shall never serialize `password`, `passwordHash`, JPA
entities, or internal authority objects.

### REQ-148: Application error mapping

User validation uses 400, missing user uses 404, duplicate username and
self-disable use 409, and unexpected failures use the existing generic 500
contract. Trace-aware 401/403 and unified `traceId` fields are completed in the
dependent `request-observability` module.

### REQ-149: Immediate authentication consistency

Because HTTP Basic is stateless and database-backed, creation and enabled-state
changes shall be visible to the very next authentication request.

## Acceptance criteria

- **AC-140:** ADMIN creates a user and receives 201 with safe fields only.
- **AC-141:** VIEWER/EDITOR receive 403 and anonymous receives 401 for creation.
- **AC-142:** The created user's stored password is BCrypt and the user can
  authenticate with the assigned role.
- **AC-143:** Invalid username/password/roles return 400 with field errors,
  including a `password` error for multibyte input exceeding 72 UTF-8 bytes.
- **AC-144:** A case-variant duplicate username returns 409 without SQL detail.
- **AC-145:** ADMIN disables another user and receives 200; missing target is 404.
- **AC-146:** ADMIN self-disable returns 409 and leaves the account enabled.
- **AC-147:** The disabled user's next correct-password request returns 401.

## TDD sequence

1. Service RED→GREEN for create, trim, encode-before-save, roles, duplicate, and
   safe response mapping.
2. Service RED→GREEN for enable/disable, missing user, and self-disable.
3. HTTP RED→GREEN for ADMIN creation plus validation/status contracts,
   including the 72-byte BCrypt boundary.
4. PostgreSQL flow proving created-role authentication and immediate disable.
5. Full security regression and password-field scans.

## Approval

- [x] DTO fields, validation, and status codes approved
- [x] Case-insensitive duplicate and self-disable policies approved
- [x] Safe response and immediate authentication behavior approved
- [x] Trace-aware security error completion remains in request observability
- [x] **Approved under the user's standing approval on 2026-09-09**
- [x] **UTF-8 byte-limit clarification approved by the user on 2026-09-10**
