# Week 7 Security Verification

**Date:** 2026-09-10
**Branch:** `week7-security-logging`
**Compiler target:** Java 21 (`javac --release 21`)
**Runtime used:** OpenJDK 21.0.12.1 (Homebrew)

## Exercise 6 integration matrix

| Required behavior | Public-boundary evidence |
| --- | --- |
| Public endpoints | `SecurityAccessIntegrationTest.givenNoCredentials_whenGetApiInfo_thenReturns200`, `givenNoCredentials_whenGetActuatorHealth_thenReturns200` |
| Successful authentication | `DatabaseAuthenticationIntegrationTest.givenEnabledDatabaseUser_whenCredentialsUseDifferentUsernameCase_thenAuthenticates` |
| Invalid authentication | `givenExistingUser_whenPasswordIsInvalid_thenReturns401WithBasicChallenge`, `givenUnknownUsername_whenAuthenticating_thenReturnsSameResponseAsInvalidPassword` |
| Disabled-user authentication | `givenDisabledUser_whenPasswordIsCorrect_thenReturns401` |
| VIEWER permissions | `RoleAuthorizationIntegrationTest` VIEWER read and four write-denial tests |
| EDITOR permissions | `RoleAuthorizationIntegrationTest.givenEditor_whenReadingCreatingUpdatingAndChangingStock_thenRequestsSucceed` plus delete/user-management denials |
| ADMIN permissions | `RoleAuthorizationIntegrationTest` ADMIN product and user-boundary tests |
| User creation and login | `AdminUserManagementIntegrationTest.givenNewEditor_whenAuthenticating_thenAssignedRoleCanCreateProduct` |
| User disable and rejected login | `AdminUserManagementIntegrationTest.givenDisabledViewer_whenAuthenticatingAgain_thenReturns401` |
| Authenticated product validation | `ProductControllerTest.givenInvalidRequest_whenCreateProduct_thenReturns400WithFieldErrors` |
| Duplicate SKU | `ProductControllerTest.givenDuplicateSku_whenCreateProduct_thenReturns409ErrorEnvelope` |
| Stock rollback | `ProductControllerTest.givenInsufficientStock_whenAdjustStock_thenReturns400AndPreservesQuantity` |
| 401 structure | `SecurityAccessIntegrationTest.givenNoCredentials_whenGetProducts_thenReturns401` |
| 403 structure | `RoleAuthorizationIntegrationTest.givenViewer_whenCreatingProduct_thenReturns403` |
| Generated response trace | `SecurityAccessIntegrationTest.givenNoCredentials_whenGetApiInfo_thenReturns200` |
| Valid incoming trace reuse | `SecurityAccessIntegrationTest.givenNoCredentials_whenGetProducts_thenReturns401` |
| Stale product update | `ProductControllerTest.givenStaleVersion_whenUpdatingProduct_thenReturns409` |

These are `@SpringBootTest`/MockMvc tests backed by the shared PostgreSQL 16
Testcontainer. They use application beans rather than mocked controllers,
services, or repositories. Focused unit tests separately cover the trace filter's
MDC lifecycle and database identity adapter.

## Final build

```text
Command: JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home ./mvnw clean verify
Exit: 0
Result: BUILD SUCCESS
Tests: 157 run, 0 failures, 0 errors, 0 skipped
Artifact: target/product-catalog-1.0.0-SNAPSHOT.jar
Artifact size: 64 MB
```

The final count includes the PLAN-W7-07 regression proving ADMIN user creation
rejects a password whose UTF-8 representation exceeds BCrypt's 72-byte input
boundary.

## Final scans

| Check | Result |
| --- | --- |
| `git diff --check -- .` | Passed |
| Postman JSON and shell syntax | Passed (`jq empty`, `bash -n`) |
| Generated Spring password in reports | No match |
| Test credential/Authorization marker in reports | No match |
| SQL constraint/failing-row detail in reports | No match |
| Password/Authorization logging calls in production | No match |
| Password/hash fields in response DTOs | No match |
| Security or logging decisions in controllers | No match |
| Field `@Autowired` in production | No match |
| Committed configuration credentials | Only required/empty environment placeholders |

The complete scoped status and production diff were reviewed. An unrelated
parent-repository `.DS_Store` change was not modified or included in the scoped
Product Catalog review.

## Open delivery boundary

- No commit, push, or pull request has been created. Those actions require
  explicit user authorization.
