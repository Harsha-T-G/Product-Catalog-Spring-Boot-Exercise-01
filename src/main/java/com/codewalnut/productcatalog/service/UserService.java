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
import com.codewalnut.productcatalog.security.SafeLogValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            AppUserRepository appUserRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String username = request.getUsername().trim();
        if (appUserRepository.existsByUsernameIgnoreCase(username)) {
            throw new DuplicateUsernameException(username);
        }

        Set<RoleEntity> roles = resolveRoles(request.getRoles());
        AppUserEntity user = new AppUserEntity(
                UUID.randomUUID(),
                username,
                passwordEncoder.encode(request.getPassword()),
                true,
                roles);
        try {
            UserResponse response = toResponse(appUserRepository.saveAndFlush(user));
            log.info(
                    "event=user_created username={} roles={} enabled=true outcome=success",
                    SafeLogValue.of(response.getUsername()),
                    response.getRoles());
            return response;
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateUsernameException(username);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse setEnabled(String username, boolean enabled, String actorUsername) {
        if (!enabled && username.equalsIgnoreCase(actorUsername)) {
            throw new SelfDisableNotAllowedException();
        }
        AppUserEntity user = appUserRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new AppUserNotFoundException(username));
        user.setEnabled(enabled);
        UserResponse response = toResponse(appUserRepository.saveAndFlush(user));
        log.info(
                "event=user_enabled_changed username={} enabled={} outcome=success",
                SafeLogValue.of(response.getUsername()),
                enabled);
        return response;
    }

    private Set<RoleEntity> resolveRoles(Set<ApplicationRole> requestedRoles) {
        Set<RoleEntity> roles = new LinkedHashSet<>();
        Arrays.stream(ApplicationRole.values())
                .filter(requestedRoles::contains)
                .map(role -> roleRepository.findByName(role)
                        .orElseThrow(() -> new IllegalStateException("Canonical role is missing: " + role)))
                .forEach(roles::add);
        return roles;
    }

    private UserResponse toResponse(AppUserEntity user) {
        Set<ApplicationRole> roles = new LinkedHashSet<>();
        Arrays.stream(ApplicationRole.values())
                .filter(role -> user.getRoles().stream().anyMatch(entity -> entity.getName() == role))
                .forEach(roles::add);
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.isEnabled(),
                roles,
                user.getCreatedAt());
    }
}
