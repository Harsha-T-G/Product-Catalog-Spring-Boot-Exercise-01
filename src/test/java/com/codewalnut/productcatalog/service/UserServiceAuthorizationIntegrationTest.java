package com.codewalnut.productcatalog.service;

import com.codewalnut.productcatalog.dto.CreateUserRequest;
import com.codewalnut.productcatalog.exception.AppUserNotFoundException;
import com.codewalnut.productcatalog.repository.AppUserRepository;
import com.codewalnut.productcatalog.security.ApplicationRole;
import com.codewalnut.productcatalog.support.PostgreSqlTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceAuthorizationIntegrationTest extends PostgreSqlTestSupport {

    @Autowired
    private UserService userService;

    @Autowired
    private AppUserRepository appUserRepository;

    @BeforeEach
    void cleanUsers() {
        appUserRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "editor-user", roles = "EDITOR")
    void givenEditor_whenCallingCreateDirectly_thenDeniedBeforePersistence() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("direct-user");
        request.setPassword("test-only-password");
        request.setRoles(Set.of(ApplicationRole.VIEWER));

        assertThrows(AccessDeniedException.class, () -> userService.create(request));
    }

    @Test
    @WithMockUser(username = "editor-user", roles = "EDITOR")
    void givenEditor_whenCallingSetEnabledDirectly_thenDeniedBeforeLookup() {
        assertThrows(
                AccessDeniedException.class,
                () -> userService.setEnabled("missing-user", false, "editor-user"));
    }

    @Test
    @WithMockUser(username = "admin-user", roles = "ADMIN")
    void givenAdmin_whenCallingSetEnabledDirectly_thenReachesBusinessLogic() {
        assertThrows(
                AppUserNotFoundException.class,
                () -> userService.setEnabled("missing-user", false, "admin-user"));
    }
}
