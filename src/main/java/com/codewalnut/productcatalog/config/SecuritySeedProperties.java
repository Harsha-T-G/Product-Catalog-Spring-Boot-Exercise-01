package com.codewalnut.productcatalog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.stream.Stream;

@ConfigurationProperties(prefix = "catalog.security.dev-users")
public record SecuritySeedProperties(SeedUser viewer, SeedUser editor, SeedUser admin) {

    public boolean hasCompleteConfiguration() {
        return Stream.of(viewer, editor, admin)
                .allMatch(user -> user != null && user.isComplete());
    }

    public record SeedUser(String username, String password) {

        boolean isComplete() {
            return username != null && !username.isBlank()
                    && password != null && !password.isBlank();
        }
    }
}
