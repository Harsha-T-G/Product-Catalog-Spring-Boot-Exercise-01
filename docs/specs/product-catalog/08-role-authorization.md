# Role-Based Authorization Contract

**Status:** Implemented on 2026-09-09; final Java 21 gate verified on 2026-09-10
**Module:** `role-authorization`
**Covers:** `REQ-130`–`REQ-137`, `AC-130`–`AC-136`
**Depends on:** `docs/security-spec.md` and implemented PLAN-W7-02

## Objective

Apply the approved VIEWER/EDITOR/ADMIN endpoint matrix through Spring Security's
request authorization and protect product deletion again at the service layer.
Authentication remains database-backed and stateless; controllers contain no
manual role checks.

## Matcher policy and order

Matchers are evaluated from most specific to least specific:

1. Exact public GET endpoints: `/api/info`, `/actuator/health`.
2. Product reads (`GET /api/products` and `/api/products/**`): VIEWER, EDITOR,
   ADMIN.
3. Product create (`POST /api/products`), update (`PUT /api/products/*`), and
   stock change (`PATCH /api/products/*/stock`): EDITOR, ADMIN.
4. Product delete (`DELETE /api/products/*`): ADMIN.
5. User administration (`/api/admin/**`): ADMIN.
6. Actuator info, OpenAPI, and Swagger UI: any recognized application role.
7. Restrictive default: any other request must be authenticated.

The default allows an authenticated request to reach normal routing, so an
unknown endpoint returns 404 rather than exposing it publicly. It does not grant
access to any explicitly role-protected path.

## Requirements

### REQ-130: Request-level authorization

The `SecurityFilterChain` shall implement the approved endpoint matrix with
ordered request matchers. Controllers shall delegate business work without
reading roles, authorities, or `SecurityContext` manually.

### REQ-131: VIEWER access

VIEWER may read products and receives 403 for product create, update, stock
change, delete, and all user-management paths.

### REQ-132: EDITOR access

EDITOR may read, create, update, and change product stock. EDITOR receives 403
for product deletion and all user-management paths.

### REQ-133: ADMIN access

ADMIN may pass the authorization boundary for every product and user-management
operation. Until PLAN-W7-04 supplies the user controller, an ADMIN request to
`/api/admin/**` may receive normal routing 404; VIEWER and EDITOR must receive
403 at the security boundary.

### REQ-134: Protected operational and documentation endpoints

`/actuator/info`, `/v3/api-docs/**`, `/swagger-ui.html`, and `/swagger-ui/**`
shall require VIEWER, EDITOR, or ADMIN. Actuator health remains public. Other
Actuator endpoints remain unexposed by Actuator configuration and are never
made public by security matchers.

### REQ-135: Restrictive default

Any request not explicitly public shall require authentication. Anonymous
requests receive 401. Authenticated requests to otherwise unknown routes may
continue to 404.

### REQ-136: Method-security defense in depth

Method security shall be enabled. `ProductService.delete(UUID)` shall require
ADMIN with `@PreAuthorize`. This protects the destructive operation when it is
called from another adapter or scheduled/component path that does not traverse
the HTTP matcher. Request and method checks intentionally enforce the same rule.

### REQ-137: Explicit roles, no implicit hierarchy

Roles map to `ROLE_*` authorities. No role hierarchy is configured: each
request matcher explicitly lists all allowed roles, making the effect of a new
role visible in the contract and tests.

## Acceptance criteria

### AC-130: Public and anonymous behavior

The two public GET endpoints return 200 without authentication; an anonymous
product listing and any other protected path return 401.

### AC-131: VIEWER matrix

A VIEWER product read succeeds; create, update, stock change, delete, and admin
requests each return 403.

### AC-132: EDITOR matrix

An EDITOR product read/create/update/stock request passes authorization; delete
and admin requests return 403.

### AC-133: ADMIN matrix

An ADMIN passes every product authorization rule and the `/api/admin/**`
boundary. Business or routing outcomes after that boundary remain unchanged.

### AC-134: Operational endpoints

Actuator info and enabled documentation endpoints reject anonymous access and
allow every recognized role. Unexposed Actuator endpoints remain unavailable.

### AC-135: Restrictive fallback

An anonymous request to a newly introduced or unmapped endpoint receives 401;
an authenticated recognized user reaches normal routing and receives 404.

### AC-136: Service deletion boundary

Direct proxied invocation of `ProductService.delete` by EDITOR is denied before
business execution; ADMIN invocation is allowed to reach the normal service
outcome.

## TDD sequence

1. VIEWER RED: product create is expected 403 but currently reaches the API.
2. EDITOR RED: product delete is expected 403 but currently reaches the API.
3. ADMIN RED: after the EDITOR-only write matcher exists, ADMIN create is
   expected to pass but receives 403; expand the matcher explicitly.
4. Method-security RED: direct EDITOR service deletion reaches business logic;
   enable method security and protect deletion.
5. Expand role matrices and run focused/full regression suites after each GREEN.

## Approval

- [x] Matcher order and explicit role lists approved
- [x] ADMIN routing behavior before the admin controller exists approved
- [x] Product deletion selected for method-security defense in depth
- [x] Role-by-role TDD sequence approved
- [x] **Approved under the user's standing approval on 2026-09-09**
