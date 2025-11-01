package com.app.login.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Response DTO for successful login
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String tokenType = "Bearer";
    private Long userId;
    private String username;
    private String email;
    private String mobileNumber;
    private Set<String> roles;
    private String preferredLanguage;
    private String preferredCurrency;
    private LocalDateTime loginTime;
    private Long expiresIn; // in milliseconds
}
