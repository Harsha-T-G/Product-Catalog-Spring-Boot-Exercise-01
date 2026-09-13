package com.codewalnut.productcatalog.security;

import com.codewalnut.productcatalog.entity.AppUserEntity;
import com.codewalnut.productcatalog.entity.RoleEntity;
import com.codewalnut.productcatalog.repository.AppUserRepository;
import com.codewalnut.productcatalog.repository.RoleRepository;
import com.codewalnut.productcatalog.support.PostgreSqlTestSupport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DatabaseAuthenticationIntegrationTest extends PostgreSqlTestSupport {

    private static final String TEST_PASSWORD = "test-only-password";
    private static final BCryptPasswordEncoder TEST_ENCODER = new BCryptPasswordEncoder();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void cleanUsers() {
        appUserRepository.deleteAll();
    }

    @Test
    void givenEnabledDatabaseUser_whenCredentialsUseDifferentUsernameCase_thenAuthenticates() throws Exception {
        // Arrange
        saveUser("catalog-viewer", TEST_PASSWORD, true);

        // Act & Assert
        mockMvc.perform(get("/api/products").with(httpBasic("CATALOG-VIEWER", TEST_PASSWORD)))
                .andExpect(status().isOk());
    }

    @Test
    void givenExistingUser_whenPasswordIsInvalid_thenReturns401WithBasicChallenge() throws Exception {
        // Arrange
        saveUser("known-user", TEST_PASSWORD, true);

        // Act & Assert
        mockMvc.perform(get("/api/products").with(httpBasic("known-user", "wrong-test-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("Basic")));
    }

    @Test
    void givenUnknownUsername_whenAuthenticating_thenReturnsSameResponseAsInvalidPassword() throws Exception {
        // Arrange
        saveUser("known-user", TEST_PASSWORD, true);

        // Act
        MvcResult invalidPassword = mockMvc.perform(
                        get("/api/products").with(httpBasic("known-user", "wrong-test-password")))
                .andReturn();
        MvcResult unknownUser = mockMvc.perform(
                        get("/api/products").with(httpBasic("missing-user", TEST_PASSWORD)))
                .andReturn();

        // Assert
        assertEquals(401, invalidPassword.getResponse().getStatus());
        assertEquals(invalidPassword.getResponse().getStatus(), unknownUser.getResponse().getStatus());
        assertEquals(invalidPassword.getResponse().getHeader(HttpHeaders.WWW_AUTHENTICATE),
                unknownUser.getResponse().getHeader(HttpHeaders.WWW_AUTHENTICATE));
        JsonNode invalidPasswordBody = objectMapper.readTree(invalidPassword.getResponse().getContentAsString());
        JsonNode unknownUserBody = objectMapper.readTree(unknownUser.getResponse().getContentAsString());
        assertEquals(invalidPasswordBody.get("status"), unknownUserBody.get("status"));
        assertEquals(invalidPasswordBody.get("error"), unknownUserBody.get("error"));
        assertEquals(invalidPasswordBody.get("message"), unknownUserBody.get("message"));
        assertEquals(invalidPasswordBody.get("path"), unknownUserBody.get("path"));
        assertEquals(invalidPasswordBody.get("fieldErrors"), unknownUserBody.get("fieldErrors"));
        assertTrue(invalidPasswordBody.get("traceId").isTextual());
        assertTrue(unknownUserBody.get("traceId").isTextual());
    }

    @Test
    void givenDisabledUser_whenPasswordIsCorrect_thenReturns401() throws Exception {
        // Arrange
        saveUser("disabled-user", TEST_PASSWORD, false);

        // Act & Assert
        mockMvc.perform(get("/api/products").with(httpBasic("disabled-user", TEST_PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("Basic")));
    }

    @Test
    void givenPlaintextPassword_whenUserIsPersisted_thenStoredValueIsBCryptHash() {
        // Act
        AppUserEntity saved = saveUser("hashed-user", TEST_PASSWORD, true);
        String storedHash = appUserRepository.findById(saved.getId()).orElseThrow().getPasswordHash();

        // Assert
        assertNotEquals(TEST_PASSWORD, storedHash);
        assertTrue(storedHash.matches("^\\$2[aby]\\$.*"));
        assertTrue(TEST_ENCODER.matches(TEST_PASSWORD, storedHash));
    }

    private AppUserEntity saveUser(String username, String password, boolean enabled) {
        RoleEntity viewer = roleRepository.findByName(ApplicationRole.VIEWER).orElseThrow();
        return appUserRepository.saveAndFlush(new AppUserEntity(
                UUID.randomUUID(), username, TEST_ENCODER.encode(password), enabled, Set.of(viewer)));
    }
}
