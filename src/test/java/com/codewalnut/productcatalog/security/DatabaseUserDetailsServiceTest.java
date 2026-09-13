package com.codewalnut.productcatalog.security;

import com.codewalnut.productcatalog.entity.AppUserEntity;
import com.codewalnut.productcatalog.entity.RoleEntity;
import com.codewalnut.productcatalog.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseUserDetailsServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private DatabaseUserDetailsService userDetailsService;

    @Test
    void givenEnabledDatabaseUser_whenLoaded_thenMapsUsernamePasswordAndAuthorities() {
        // Arrange
        AppUserEntity user = new AppUserEntity(
                UUID.randomUUID(),
                "catalog-admin",
                "$2a$10$test-hash-value-not-used-for-comparison123456789",
                true,
                Set.of(new RoleEntity(ApplicationRole.VIEWER), new RoleEntity(ApplicationRole.ADMIN)));
        when(appUserRepository.findByUsernameIgnoreCase("catalog-admin")).thenReturn(Optional.of(user));

        // Act
        UserDetails details = userDetailsService.loadUserByUsername("  catalog-admin  ");

        // Assert
        assertEquals("catalog-admin", details.getUsername());
        assertEquals(user.getPasswordHash(), details.getPassword());
        assertTrue(details.isEnabled());
        assertEquals(
                Set.of("ROLE_VIEWER", "ROLE_ADMIN"),
                details.getAuthorities().stream()
                        .map(authority -> authority.getAuthority())
                        .collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void givenDisabledDatabaseUser_whenLoaded_thenPrincipalIsDisabled() {
        // Arrange
        AppUserEntity user = new AppUserEntity(
                UUID.randomUUID(),
                "disabled-user",
                "$2a$10$test-hash-value-not-used-for-comparison123456789",
                false,
                Set.of(new RoleEntity(ApplicationRole.VIEWER)));
        when(appUserRepository.findByUsernameIgnoreCase("disabled-user")).thenReturn(Optional.of(user));

        // Act
        UserDetails details = userDetailsService.loadUserByUsername("disabled-user");

        // Assert
        assertFalse(details.isEnabled());
    }

    @Test
    void givenUnknownUsername_whenLoaded_thenThrowsGenericUsernameNotFoundException() {
        // Arrange
        when(appUserRepository.findByUsernameIgnoreCase("missing-user")).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("missing-user"));
        assertEquals("Invalid credentials", exception.getMessage());
    }
}
