# Security Verification and Delivery Contract

**Module:** `security-verification-delivery`
**Status:** Implemented and verified on 2026-09-10
**Depends on:** REQ-110–REQ-163

## Purpose

Close Week 7 with public-boundary evidence that the security, persistence,
authorization, error, and observability contracts work together. Delivery
documentation must be runnable without committing credentials and must state
any toolchain limitation honestly.

## Requirements

### REQ-170: Complete HTTP security verification

PostgreSQL Testcontainers-backed HTTP tests shall cover public access, enabled
and invalid authentication, disabled users, VIEWER/EDITOR/ADMIN permissions,
user creation and subsequent login, user disable and rejected login, authenticated
product validation, duplicate SKU, stock rollback, structured 401/403 responses,
trace generation, and valid incoming trace reuse.

### REQ-171: Public-boundary test quality

Full integration tests shall use behavior-focused names and public HTTP
boundaries. They shall not mock application controllers, services, or
repositories, assert private methods, or verify internal call counts. Tests
shall be repeatable and use known values from approved contracts.

### REQ-172: TDD evidence

`docs/tdd-evidence.md` shall contain at least four authentic RED→GREEN cycles.
Each cycle records the behavior, failing test and output, minimum implementation
change, passing output, and learning used for the next slice.

### REQ-173: Operator documentation

The README shall document the security design, role-permission matrix,
development-user setup, required environment variables, authenticated examples,
401 and 403 examples, trace behavior, full verification commands, limitations,
and future improvements.

### REQ-174: Safe runnable examples

Command examples shall accept credentials through environment variables or
local interactive tooling. No plaintext credential value, password hash,
Authorization header value, or database credential may be committed.

### REQ-175: Self-review

The Week 7 self-review shall summarize completed work, important security
decisions, diagnosed problems, feedback applied, verification results, and known
limitations.

### REQ-176: Final quality gate

The Maven wrapper shall compile with Java 21 release compatibility and the full
test suite shall pass with Docker-backed PostgreSQL. Verification shall also
include whitespace, credential, generated-password, sensitive-log, controller
authorization-logic, and complete-diff checks.

### REQ-177: Toolchain evidence

Final evidence shall identify both the compiler release target and runtime JDK.
If a Java 21 runtime is unavailable, the Java 26/`--release 21` result may be
recorded as implementation evidence, but Java 21 runtime verification remains an
explicit delivery limitation rather than being reported as complete.

### REQ-178: Pull-request boundary

The branch may be made ready for a focused pull request, but committing, pushing,
and creating or updating the pull request require separate explicit user
authorization. PR evidence shall not be fabricated before those actions occur.

## Acceptance criteria

### AC-170: Integrated behavior is green

**Given** Docker is available, **when** `./mvnw clean verify` runs, **then** all
tests pass and the executable JAR is produced.

### AC-171: Security matrix is traceable

**Given** the Week 7 requirements, **when** delivery evidence is reviewed,
**then** every integration behavior in REQ-170 maps to one or more named tests.

### AC-172: Evidence is authentic and complete

**Given** the implementation history, **when** `docs/tdd-evidence.md` is
reviewed, **then** at least four cycles contain real failure and success results
from this branch.

### AC-173: A developer can run secured examples safely

**Given** a developer has configured local environment variables, **when** the
README and curl samples are followed, **then** public, authenticated, rejected,
admin-user, and trace-ID behavior can be exercised without editing tracked
credentials into the repository.

### AC-174: Security scans are clean

**Given** the final worktree and test reports, **when** the approved scans run,
**then** no committed secret, generated Spring password, request credential,
password/hash response field, SQL failure detail, or complete sensitive request
body is found.

### AC-175: Delivery state is truthful

**Given** the available local JDK and Git authorization boundary, **when** the
handoff is written, **then** runtime and PR limitations are reported explicitly
and no unperformed action is claimed.

## Out of scope

- JWT or another authentication mechanism
- Rate limiting, account lockout, password reset, and audit-log persistence
- Production secret management, TLS termination, and an external identity provider
- Commit, push, or pull-request creation without explicit authorization
