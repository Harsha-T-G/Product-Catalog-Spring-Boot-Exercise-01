package com.codewalnut.productcatalog.controller;

import com.codewalnut.productcatalog.dto.CreateUserRequest;
import com.codewalnut.productcatalog.dto.UserEnabledRequest;
import com.codewalnut.productcatalog.dto.UserResponse;
import com.codewalnut.productcatalog.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @PatchMapping("/{username}/enabled")
    public ResponseEntity<UserResponse> setEnabled(
            @PathVariable String username,
            @Valid @RequestBody UserEnabledRequest request,
            Principal principal) {
        return ResponseEntity.ok(userService.setEnabled(username, request.getEnabled(), principal.getName()));
    }
}
