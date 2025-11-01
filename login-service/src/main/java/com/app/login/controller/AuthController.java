package com.app.login.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.common.dto.ApiResponse;
import com.app.login.dto.BankConfigResponse;
import com.app.login.dto.LoginRequest;
import com.app.login.dto.LoginResponse;
import com.app.login.dto.RegisterRequest;
import com.app.login.dto.TokenValidationResponse;
import com.app.login.entity.User;
import com.app.login.service.AuthService;
import com.app.login.service.BankConfigService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for Authentication endpoints
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication and Authorization APIs")
public class AuthController {

    private final AuthService authService;
    private final BankConfigService bankConfigService;

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new user account with username/email/mobile")
    public ResponseEntity<ApiResponse<User>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request);
            // Remove password from response
            user.setPassword(null);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("User registered successfully", user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Registration failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and generate JWT token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        try {
            LoginResponse response = authService.login(request, httpRequest);
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (Exception e) {
            log.error("Login failed for user: {}", request.getUsernameOrEmailOrMobile(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user and invalidate session")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Missing or invalid Authorization header"));
            }
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            authService.logout(token);
            return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
        } catch (Exception e) {
            log.error("Logout failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Logout failed: " + e.getMessage()));
        }
    }

    @PostMapping("/validate-token")
    @Operation(summary = "Validate JWT token", description = "Validate if JWT token is valid and active")
    public ResponseEntity<ApiResponse<TokenValidationResponse>> validateToken(@RequestBody String token) {
        try {
            // Remove quotes if present
            token = token.replace("\"", "");
            TokenValidationResponse response = authService.validateToken(token);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token validation failed"));
        }
    }

    @GetMapping("/bank-config")
    @Operation(summary = "Get bank configuration", description = "Get bank name, logo, language, and currency settings")
    public ResponseEntity<ApiResponse<BankConfigResponse>> getBankConfig() {
        try {
            BankConfigResponse config = bankConfigService.getBankConfiguration();
            return ResponseEntity.ok(ApiResponse.success(config));
        } catch (Exception e) {
            log.error("Failed to get bank configuration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to get bank configuration"));
        }
    }

    @GetMapping("/user/{username}")
    @Operation(summary = "Get user info by username", description = "Retrieve user information by username (for internal microservice use)")
    public ResponseEntity<ApiResponse<User>> getUserByUsername(@org.springframework.web.bind.annotation.PathVariable String username) {
        try {
            User user = authService.getUserByUsername(username);
            // Remove sensitive information
            user.setPassword(null);
            return ResponseEntity.ok(ApiResponse.success("User found", user));
        } catch (Exception e) {
            log.error("Failed to get user by username: {}", username, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found with username: " + username));
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the service is running")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Login Service is running"));
    }
}
