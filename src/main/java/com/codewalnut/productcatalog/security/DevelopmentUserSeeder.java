package com.codewalnut.productcatalog.security;

import com.codewalnut.productcatalog.config.SecuritySeedProperties;
import com.codewalnut.productcatalog.entity.AppUserEntity;
import com.codewalnut.productcatalog.entity.RoleEntity;
import com.codewalnut.productcatalog.repository.AppUserRepository;
import com.codewalnut.productcatalog.repository.RoleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Component
@Profile("dev")
public class DevelopmentUserSeeder implements ApplicationRunner {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecuritySeedProperties properties;

    public DevelopmentUserSeeder(
            AppUserRepository appUserRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            SecuritySeedProperties properties) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        run();
    }

    void run() {
        if (!properties.hasCompleteConfiguration()) {
            return;
        }

        seed(properties.viewer(), ApplicationRole.VIEWER);
        seed(properties.editor(), ApplicationRole.EDITOR);
        seed(properties.admin(), ApplicationRole.ADMIN);
    }

    private void seed(SecuritySeedProperties.SeedUser configuredUser, ApplicationRole applicationRole) {
        String username = configuredUser.username().trim();
        if (appUserRepository.existsByUsernameIgnoreCase(username)) {
            return;
        }

        RoleEntity role = roleRepository.findByName(applicationRole)
                .orElseThrow(() -> new IllegalStateException("Required application role is missing: " + applicationRole));
        appUserRepository.save(new AppUserEntity(
                UUID.randomUUID(),
                username,
                passwordEncoder.encode(configuredUser.password()),
                true,
                Set.of(role)));
    }
}
