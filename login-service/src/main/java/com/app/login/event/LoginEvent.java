package com.app.login.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Login event to be published to Kafka
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginEvent {
    
    private String eventId;
    private String username;
    private String email;
    private Long userId;
    private String eventType; // LOGIN_SUCCESS, LOGIN_FAILURE, LOGOUT, AUTO_LOGOUT
    private LocalDateTime eventTime;
    private String ipAddress;
    private String userAgent;
}
