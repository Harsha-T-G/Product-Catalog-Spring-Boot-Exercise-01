package com.codewalnut.productcatalog.dto;

import com.codewalnut.productcatalog.security.ApplicationRole;
import com.codewalnut.productcatalog.validation.Utf8ByteLength;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.LinkedHashSet;
import java.util.Set;

public class CreateUserRequest {

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Size(min = 10, max = 72)
    @Utf8ByteLength(max = 72)
    private String password;

    @NotEmpty
    private Set<@Valid @NotNull ApplicationRole> roles = new LinkedHashSet<>();

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username == null ? null : username.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<ApplicationRole> getRoles() {
        return Set.copyOf(roles);
    }

    public void setRoles(Set<ApplicationRole> roles) {
        this.roles = roles == null ? null : new LinkedHashSet<>(roles);
    }
}
