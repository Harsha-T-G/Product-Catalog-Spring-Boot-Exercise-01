package com.codewalnut.productcatalog.dto;

import com.codewalnut.productcatalog.security.ApplicationRole;
import lombok.Getter;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public final class UserResponse {

    private final UUID id;
    private final String username;
    private final boolean enabled;
    private final Set<ApplicationRole> roles;
    private final Instant createdAt;

    public UserResponse(
            UUID id,
            String username,
            boolean enabled,
            Set<ApplicationRole> roles,
            Instant createdAt) {
        this.id = id;
        this.username = username;
        this.enabled = enabled;
        this.roles = Collections.unmodifiableSet(new LinkedHashSet<>(roles));
        this.createdAt = createdAt;
    }
}
