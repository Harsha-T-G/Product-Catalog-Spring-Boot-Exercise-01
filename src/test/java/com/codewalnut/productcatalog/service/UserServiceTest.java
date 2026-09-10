package com.codewalnut.productcatalog.service;

import com.codewalnut.productcatalog.dto.CreateUserRequest;
import com.codewalnut.productcatalog.dto.UserResponse;
import com.codewalnut.productcatalog.entity.AppUserEntity;
import com.codewalnut.productcatalog.entity.RoleEntity;
import com.codewalnut.productcatalog.exception.AppUserNotFoundException;
import com.codewalnut.productcatalog.exception.DuplicateUsernameException;
import com.codewalnut.productcatalog.exception.SelfDisableNotAllowedException;
import com.codewalnut.productcatalog.repository.AppUserRepository;
import com.codewalnut.productcatalog.repository.RoleRepository;
import com.codewalnut.productcatalog.security.ApplicationRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void givenValidRequest_whenCreate_thenTrimsUsernameEncodesPasswordAndReturnsSafeUser() {
        UserService userService = new UserService(appUserRepository, roleRepository, passwordEncoder);
        CreateUserRequest request = createRequest(
                "  catalog-editor  ", "long-enough-password", Set.of(ApplicationRole.EDITOR));
        RoleEntity editorRole = new RoleEntity(ApplicationRole.EDITOR);
        when(appUserRepository.existsByUsernameIgnoreCase("catalog-editor")).thenReturn(false);
        when(roleRepository.findByName(ApplicationRole.EDITOR)).thenReturn(Optional.of(editorRole));
        when(passwordEncoder.encode("long-enough-password")).thenReturn("encoded-password-hash");
        when(appUserRepository.saveAndFlush(any(AppUserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.create(request);

        ArgumentCaptor<AppUserEntity> savedUser = ArgumentCaptor.forClass(AppUserEntity.class);
        verify(appUserRepository).saveAndFlush(savedUser.capture());
        assertEquals("catalog-editor", savedUser.getValue().getUsername());
        assertEquals("encoded-password-hash", savedUser.getValue().getPasswordHash());
        assertEquals(Set.of(editorRole), savedUser.getValue().getRoles());
        assertEquals("catalog-editor", response.getUsername());
        assertEquals(Set.of(ApplicationRole.EDITOR), response.getRoles());
    }

    @Test
    void givenCaseVariantDuplicate_whenCreate_thenRejectsBeforeEncodingOrSaving() {
        UserService userService = new UserService(appUserRepository, roleRepository, passwordEncoder);
        CreateUserRequest request = createRequest(
                "Catalog-Viewer", "long-enough-password", Set.of(ApplicationRole.VIEWER));
        when(appUserRepository.existsByUsernameIgnoreCase("Catalog-Viewer")).thenReturn(true);

        assertThrows(DuplicateUsernameException.class, () -> userService.create(request));

        verifyNoInteractions(roleRepository, passwordEncoder);
        verify(appUserRepository, never()).saveAndFlush(any());
    }

    @Test
    void givenAnotherUser_whenDisable_thenPersistsDisabledState() {
        UserService userService = new UserService(appUserRepository, roleRepository, passwordEncoder);
        AppUserEntity target = user("catalog-viewer", true, ApplicationRole.VIEWER);
        when(appUserRepository.findByUsernameIgnoreCase("CATALOG-VIEWER")).thenReturn(Optional.of(target));
        when(appUserRepository.saveAndFlush(target)).thenReturn(target);

        UserResponse response = userService.setEnabled("CATALOG-VIEWER", false, "catalog-admin");

        assertFalse(target.isEnabled());
        assertFalse(response.isEnabled());
        verify(appUserRepository).saveAndFlush(target);
    }

    @Test
    void givenMissingUser_whenChangingEnabledState_thenReturnsNotFound() {
        UserService userService = new UserService(appUserRepository, roleRepository, passwordEncoder);
        when(appUserRepository.findByUsernameIgnoreCase("missing-user")).thenReturn(Optional.empty());

        assertThrows(
                AppUserNotFoundException.class,
                () -> userService.setEnabled("missing-user", false, "catalog-admin"));
    }

    @Test
    void givenSameUsernameIgnoringCase_whenDisabling_thenRejectsWithoutLoadingUser() {
        UserService userService = new UserService(appUserRepository, roleRepository, passwordEncoder);

        assertThrows(
                SelfDisableNotAllowedException.class,
                () -> userService.setEnabled("Catalog-Admin", false, "catalog-admin"));

        verifyNoInteractions(appUserRepository);
    }

    private CreateUserRequest createRequest(String username, String password, Set<ApplicationRole> roles) {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername(username);
        request.setPassword(password);
        request.setRoles(roles);
        return request;
    }

    private AppUserEntity user(String username, boolean enabled, ApplicationRole role) {
        return new AppUserEntity(
                UUID.randomUUID(), username, "encoded-password-hash", enabled, Set.of(new RoleEntity(role)));
    }
}
