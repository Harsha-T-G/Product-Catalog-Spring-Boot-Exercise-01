package com.codewalnut.productcatalog.security;

import com.codewalnut.productcatalog.entity.AppUserEntity;
import com.codewalnut.productcatalog.repository.AppUserRepository;
import com.codewalnut.productcatalog.repository.ProductRepository;
import com.codewalnut.productcatalog.support.PostgreSqlTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminUserManagementIntegrationTest extends PostgreSqlTestSupport {

    private static final String USER_PASSWORD = "test-only-password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanData() {
        productRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void givenAdminAndValidRequest_whenCreatingUser_thenReturns201AndSafeResponse() throws Exception {
        mockMvc.perform(post("/api/admin/users")
                        .with(user("catalog-admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserJson("  catalog-editor  ", "EDITOR")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.username").value("catalog-editor"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.roles[0]").value("EDITOR"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        AppUserEntity stored = appUserRepository.findByUsernameIgnoreCase("CATALOG-EDITOR").orElseThrow();
        assertNotEquals(USER_PASSWORD, stored.getPasswordHash());
        assertTrue(passwordEncoder.matches(USER_PASSWORD, stored.getPasswordHash()));
    }

    @Test
    void givenInvalidFields_whenCreatingUser_thenReturns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/admin/users")
                        .with(user("catalog-admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":" x ","password":"short","roles":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field", containsInAnyOrder(
                        "username", "password", "roles")));
    }

    @Test
    void givenPasswordBeyondBcryptByteLimit_whenCreatingUser_thenReturns400PasswordError() throws Exception {
        // Arrange
        String password = "€".repeat(24) + "A";

        // Act and Assert
        mockMvc.perform(post("/api/admin/users")
                        .with(user("catalog-admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserJson("multibyte-password-user", password, "VIEWER")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field", containsInAnyOrder("password")));
    }

    @Test
    void givenCaseVariantDuplicate_whenCreatingUser_thenReturns409WithoutDatabaseDetail() throws Exception {
        createAsAdmin("Catalog-Viewer", "VIEWER");

        mockMvc.perform(post("/api/admin/users")
                        .with(user("catalog-admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserJson("catalog-viewer", "VIEWER")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username already exists: catalog-viewer"))
                .andExpect(content().string(not(containsString("constraint"))));
    }

    @Test
    void givenAdmin_whenDisablingAnotherUser_thenReturns200AndSafeResponse() throws Exception {
        createAsAdmin("catalog-viewer", "VIEWER");

        mockMvc.perform(patch("/api/admin/users/{username}/enabled", "CATALOG-VIEWER")
                        .with(user("catalog-admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("catalog-viewer"))
                .andExpect(jsonPath("$.enabled").value(false))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void givenMissingUser_whenChangingEnabledState_thenReturns404() throws Exception {
        String traceId = "6fa2033e-1f24-4c9f-98ca-b27a95f75fe1";

        mockMvc.perform(patch("/api/admin/users/{username}/enabled", "missing-user")
                        .with(user("catalog-admin").roles("ADMIN"))
                        .header(RequestTraceFilter.TRACE_HEADER, traceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":false}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found: missing-user"))
                .andExpect(jsonPath("$.traceId").value(traceId));
    }

    @Test
    void givenAdmin_whenDisablingSelfIgnoringCase_thenReturns409() throws Exception {
        createAsAdmin("catalog-admin", "ADMIN");

        mockMvc.perform(patch("/api/admin/users/{username}/enabled", "CATALOG-ADMIN")
                        .with(httpBasic("catalog-admin", USER_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":false}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Administrators cannot disable their own account"));

        assertTrue(appUserRepository.findByUsernameIgnoreCase("catalog-admin").orElseThrow().isEnabled());
    }

    @Test
    void givenNonAdminOrAnonymous_whenCreatingUser_thenAccessIsDenied() throws Exception {
        String request = createUserJson("denied-user", "VIEWER");
        mockMvc.perform(post("/api/admin/users")
                        .with(user("viewer-user").roles("VIEWER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/admin/users")
                        .with(user("editor-user").roles("EDITOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenNewEditor_whenAuthenticating_thenAssignedRoleCanCreateProduct() throws Exception {
        createAsAdmin("new-editor", "EDITOR");

        mockMvc.perform(post("/api/products")
                        .with(httpBasic("NEW-EDITOR", USER_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson()))
                .andExpect(status().isCreated());
    }

    @Test
    void givenDisabledViewer_whenAuthenticatingAgain_thenReturns401() throws Exception {
        createAsAdmin("disable-me", "VIEWER");
        mockMvc.perform(get("/api/products").with(httpBasic("disable-me", USER_PASSWORD)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/admin/users/{username}/enabled", "disable-me")
                        .with(user("catalog-admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":false}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/products").with(httpBasic("disable-me", USER_PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

    private void createAsAdmin(String username, String role) throws Exception {
        mockMvc.perform(post("/api/admin/users")
                        .with(user("catalog-admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserJson(username, role)))
                .andExpect(status().isCreated());
    }

    private String createUserJson(String username, String role) {
        return createUserJson(username, USER_PASSWORD, role);
    }

    private String createUserJson(String username, String password, String role) {
        return """
                {
                  "username": "%s",
                  "password": "%s",
                  "roles": ["%s"]
                }
                """.formatted(username, password, role);
    }

    private String validProductJson() {
        return """
                {
                  "sku": "ADMIN-CREATED-EDITOR",
                  "name": "Created Editor Product",
                  "category": "Security",
                  "price": 19.99,
                  "stockQuantity": 5,
                  "active": true
                }
                """;
    }
}
