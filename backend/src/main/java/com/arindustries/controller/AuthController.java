package com.arindustries.controller;

import com.arindustries.dto.request.LoginRequest;
import com.arindustries.dto.response.AuthResponse;
import com.arindustries.entity.User;
import com.arindustries.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication and User Session APIs")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and receive JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user info")
    public ResponseEntity<?> getCurrentUser(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Unauthorized", "message", "User session is not authenticated"));
        }
        User user = authService.getCurrentUser(principal.getName());
        return ResponseEntity.ok(Map.of(
            "username", user.getUsername(),
            "role", user.getRole().name(),
            "fullName", user.getFullName(),
            "email", user.getEmail()
        ));
    }
}
