package com.app.login.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Client for Email Service integration
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${email-service.url:http://localhost:8085/api/email}")
    private String emailServiceUrl;

    @Value("${email-service.enabled:true}")
    private boolean emailServiceEnabled;

    /**
     * Send login notification email
     */
    public void sendLoginNotificationEmail(String recipientEmail, String username, String loginTime,
            String ipAddress, String deviceInfo, String location) {
        if (!emailServiceEnabled) {
            log.info("Email service is disabled. Skipping login notification to: {}", recipientEmail);
            return;
        }

        log.info("Sending login notification email to: {}", recipientEmail);

        Map<String, String> emailRequest = new HashMap<>();
        emailRequest.put("to", recipientEmail);
        emailRequest.put("username", username);
        emailRequest.put("loginTime", loginTime);
        emailRequest.put("ipAddress", ipAddress);
        emailRequest.put("deviceInfo", deviceInfo);
        emailRequest.put("location", location);

        try {
            WebClient webClient = webClientBuilder.baseUrl(emailServiceUrl).build();

            webClient.post()
                    .uri("/send-login-notification")
                    .bodyValue(emailRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .doOnSuccess(
                            response -> log.info("✅ Login notification email sent successfully to: {}", recipientEmail))
                    .doOnError(error -> log.error("❌ Failed to send login notification email to {}: {} - {}",
                            recipientEmail, error.getClass().getSimpleName(), error.getMessage(), error))
                    .onErrorResume(error -> {
                        log.warn("Email service error, continuing without email notification.", error);
                        return Mono.empty();
                    })
                    .subscribe();
        } catch (Exception e) {
            log.error("Exception while sending login notification email", e);
        }
    }
}
