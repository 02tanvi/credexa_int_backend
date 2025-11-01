package com.app.customer.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Client for calling login-service APIs
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LoginServiceClient {

    private final RestTemplate restTemplate;

    @Value("${login-service.url:http://localhost:8082/api/auth}")
    private String loginServiceUrl;

    /**
     * Get user ID by username from login-service
     */
    public Long getUserIdByUsername(String username) {
        try {
            String url = loginServiceUrl + "/user/" + username;
            log.debug("Calling login-service to get userId for username: {}", username);
            
            ApiResponse response = restTemplate.getForObject(url, ApiResponse.class);
            
            if (response != null && response.isSuccess() && response.getData() != null && response.getData().getId() != null) {
                log.debug("Retrieved userId: {} for username: {}", response.getData().getId(), username);
                return response.getData().getId();
            }
            
            log.error("Failed to retrieve userId for username: {}", username);
            throw new RuntimeException("Unable to retrieve user information from login-service");
            
        } catch (Exception e) {
            log.error("Error calling login-service for username: {}", username, e);
            throw new RuntimeException("Error communicating with login-service: " + e.getMessage());
        }
    }

    /**
     * Inner class for API response wrapper from login-service
     */
    @lombok.Data
    public static class ApiResponse {
        private boolean success;
        private String message;
        private UserData data;
        private String timestamp;
    }

    /**
     * Inner class for user data from login-service
     */
    @lombok.Data
    public static class UserData {
        private Long id;  // This is the userId
        private String username;
        private String email;
        private String mobileNumber;
    }
}
