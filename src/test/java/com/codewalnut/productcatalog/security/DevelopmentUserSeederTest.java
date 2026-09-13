package com.codewalnut.productcatalog.security;

import com.codewalnut.productcatalog.config.SecuritySeedProperties;
import com.codewalnut.productcatalog.entity.AppUserEntity;
import com.codewalnut.productcatalog.entity.RoleEntity;
import com.codewalnut.productcatalog.repository.AppUserRepository;
import com.codewalnut.productcatalog.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DevelopmentUserSeederTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private RoleRepository roleRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void givenAnyMissingPassword_whenSeederRuns_thenCreatesNoUsers() {
        // Arrange
        SecuritySeedProperties properties = properties("viewer-secret", "", "admin-secret");
        DevelopmentUserSeeder seeder = new DevelopmentUserSeeder(
                appUserRepository, roleRepository, passwordEncoder, properties);

        // Act
        seeder.run();

        // Assert
        verifyNoInteractions(appUserRepository, roleRepository);
    }

    @Test
    void givenAllPasswords_whenSeederRuns_thenCreatesOneBcryptUserPerRole() {
        // Arrange
        SecuritySeedProperties properties = properties("viewer-secret", "editor-secret", "admin-secret");
        for (ApplicationRole role : ApplicationRole.values()) {
            when(roleRepository.findByName(role)).thenReturn(Optional.of(new RoleEntity(role)));
        }
        DevelopmentUserSeeder seeder = new DevelopmentUserSeeder(
                appUserRepository, roleRepository, passwordEncoder, properties);

        // Act
        seeder.run();

        // Assert
        ArgumentCaptor<AppUserEntity> captor = ArgumentCaptor.forClass(AppUserEntity.class);
        verify(appUserRepository, times(3)).save(captor.capture());
        List<AppUserEntity> users = captor.getAllValues();
        assertEquals(List.of("viewer", "editor", "admin"),
                users.stream().map(AppUserEntity::getUsername).toList());
        assertTrue(users.stream().allMatch(AppUserEntity::isEnabled));
        assertTrue(passwordEncoder.matches("viewer-secret", users.get(0).getPasswordHash()));
        assertTrue(passwordEncoder.matches("editor-secret", users.get(1).getPasswordHash()));
        assertTrue(passwordEncoder.matches("admin-secret", users.get(2).getPasswordHash()));
        assertEquals(List.of(ApplicationRole.VIEWER, ApplicationRole.EDITOR, ApplicationRole.ADMIN),
                users.stream().map(user -> user.getRoles().iterator().next().getName()).toList());
    }

    @Test
    void givenUsersAlreadyExist_whenSeederRunsAgain_thenDoesNotCreateDuplicates() {
        // Arrange
        SecuritySeedProperties properties = properties("viewer-secret", "editor-secret", "admin-secret");
        when(appUserRepository.existsByUsernameIgnoreCase(any())).thenReturn(true);
        DevelopmentUserSeeder seeder = new DevelopmentUserSeeder(
                appUserRepository, roleRepository, passwordEncoder, properties);

        // Act
        seeder.run();

        // Assert
        verify(appUserRepository, never()).save(any());
        verifyNoInteractions(roleRepository);
    }

    private SecuritySeedProperties properties(
            String viewerPassword,
            String editorPassword,
            String adminPassword) {
        return new SecuritySeedProperties(
                new SecuritySeedProperties.SeedUser("viewer", viewerPassword),
                new SecuritySeedProperties.SeedUser("editor", editorPassword),
                new SecuritySeedProperties.SeedUser("admin", adminPassword));
    }
}
