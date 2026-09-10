package com.codewalnut.productcatalog.repository;

import com.codewalnut.productcatalog.entity.AppUserEntity;
import com.codewalnut.productcatalog.entity.RoleEntity;
import com.codewalnut.productcatalog.security.ApplicationRole;
import com.codewalnut.productcatalog.support.PostgreSqlTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AppUserRepositoryTest extends PostgreSqlTestSupport {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void cleanUsers() {
        appUserRepository.deleteAll();
    }

    @Test
    void givenStoredUser_whenLookupUsesDifferentCase_thenReturnsUserWithRoles() {
        // Arrange
        RoleEntity viewer = roleRepository.findByName(ApplicationRole.VIEWER).orElseThrow();
        AppUserEntity user = new AppUserEntity(
                UUID.randomUUID(), "catalog-user", "stored-hash", true, Set.of(viewer));
        appUserRepository.saveAndFlush(user);

        // Act
        AppUserEntity found = appUserRepository.findByUsernameIgnoreCase("CATALOG-USER").orElseThrow();

        // Assert
        assertEquals("catalog-user", found.getUsername());
        assertEquals(Set.of(ApplicationRole.VIEWER),
                found.getRoles().stream().map(RoleEntity::getName).collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void givenDisabledUser_whenPersisted_thenEnabledStateRemainsFalse() {
        // Arrange
        RoleEntity viewer = roleRepository.findByName(ApplicationRole.VIEWER).orElseThrow();
        AppUserEntity user = new AppUserEntity(
                UUID.randomUUID(), "disabled-user", "stored-hash", false, Set.of(viewer));

        // Act
        appUserRepository.saveAndFlush(user);
        AppUserEntity found = appUserRepository.findByUsernameIgnoreCase("disabled-user").orElseThrow();

        // Assert
        assertFalse(found.isEnabled());
    }

    @Test
    void givenExistingUsername_whenSavingCaseVariant_thenDatabaseRejectsDuplicate() {
        // Arrange
        RoleEntity viewer = roleRepository.findByName(ApplicationRole.VIEWER).orElseThrow();
        appUserRepository.saveAndFlush(new AppUserEntity(
                UUID.randomUUID(), "unique-user", "first-hash", true, Set.of(viewer)));

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> appUserRepository.saveAndFlush(new AppUserEntity(
                UUID.randomUUID(), "UNIQUE-USER", "second-hash", true, Set.of(viewer))));
    }

    @Test
    void givenStoredUsername_whenCheckingCaseVariant_thenReportsExisting() {
        // Arrange
        RoleEntity admin = roleRepository.findByName(ApplicationRole.ADMIN).orElseThrow();
        appUserRepository.saveAndFlush(new AppUserEntity(
                UUID.randomUUID(), "Admin-One", "stored-hash", true, Set.of(admin)));

        // Act & Assert
        assertTrue(appUserRepository.existsByUsernameIgnoreCase("admin-one"));
    }
}
