package com.codewalnut.productcatalog.dto;

import com.codewalnut.productcatalog.security.ApplicationRole;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

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

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Set<ApplicationRole> getRoles() {
        return roles;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
