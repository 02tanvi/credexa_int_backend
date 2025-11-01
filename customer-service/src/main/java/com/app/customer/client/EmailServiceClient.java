package com.app.customer.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Client for Email Service integration
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${email-service.url}")
    private String emailServiceUrl;

    @Value("${email-service.enabled:true}")
    private boolean emailServiceEnabled;

    /**
     * Send new customer registration email
     */
    public void sendNewCustomerEmail(String recipientEmail, String customerName, String customerId, String mobile,
            String registeredDate) {
        if (!emailServiceEnabled) {
            log.info("Email service is disabled. Skipping new customer email to: {}", recipientEmail);
            return;
        }

        log.info("Sending new customer email to: {} ({})", customerName, recipientEmail);

        Map<String, Object> emailRequest = new HashMap<>();
        emailRequest.put("to", recipientEmail);
        emailRequest.put("customerName", customerName);
        emailRequest.put("customerId", customerId);
        emailRequest.put("email", recipientEmail);
        emailRequest.put("mobile", mobile);
        emailRequest.put("registeredDate", registeredDate);

        try {
            WebClient webClient = webClientBuilder.baseUrl(emailServiceUrl).build();

            webClient.post()
                    .uri("/api/email/send-new-customer")
                    .bodyValue(emailRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .doOnSuccess(response -> log.info("New customer email sent successfully to: {}", recipientEmail))
                    .doOnError(error -> log.error("Failed to send new customer email to {}: {} - {}",
                            recipientEmail, error.getClass().getSimpleName(), error.getMessage(), error))
                    .onErrorResume(error -> {
                        log.warn("Email service error, continuing without email notification.", error);
                        return Mono.empty();
                    })
                    .subscribe();
        } catch (Exception e) {
            log.error("Exception while sending new customer email", e);
        }
    }

    /**
     * Send custom email
     */
    public void sendEmail(String recipientEmail, String subject, String body) {
        if (!emailServiceEnabled) {
            log.info("Email service is disabled. Skipping email to: {}", recipientEmail);
            return;
        }

        log.info("Sending custom email to: {}", recipientEmail);

        Map<String, Object> emailRequest = new HashMap<>();
        emailRequest.put("to", recipientEmail);
        emailRequest.put("subject", subject);
        emailRequest.put("body", body);

        try {
            WebClient webClient = webClientBuilder.baseUrl(emailServiceUrl).build();

            webClient.post()
                    .uri("/api/email/send-simple")
                    .bodyValue(emailRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .doOnSuccess(response -> log.info("Email sent successfully to: {}", recipientEmail))
                    .doOnError(error -> log.error("Failed to send email to {}: {} - {}",
                            recipientEmail, error.getClass().getSimpleName(), error.getMessage(), error))
                    .onErrorResume(error -> {
                        log.warn("Email service error, continuing without email notification.", error);
                        return Mono.empty();
                    })
                    .subscribe();
        } catch (Exception e) {
            log.error("Exception while sending email", e);
        }
    }

    /**
     * Send account notification email
     */
    public void sendAccountNotification(String recipientEmail, String customerName, String message) {
        if (!emailServiceEnabled) {
            log.info("Email service is disabled. Skipping account notification to: {}", recipientEmail);
            return;
        }

        log.info("Sending account notification to: {} ({})", customerName, recipientEmail);

        Map<String, Object> emailRequest = new HashMap<>();
        emailRequest.put("to", recipientEmail);
        emailRequest.put("customerName", customerName);
        emailRequest.put("message", message);

        try {
            WebClient webClient = webClientBuilder.baseUrl(emailServiceUrl).build();

            webClient.post()
                    .uri("/api/email/send-notification")
                    .bodyValue(emailRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .doOnSuccess(response -> log.info("Account notification sent successfully to: {}", recipientEmail))
                    .doOnError(error -> log.error("Failed to send account notification to {}: {} - {}",
                            recipientEmail, error.getClass().getSimpleName(), error.getMessage(), error))
                    .onErrorResume(error -> {
                        log.warn("Email service error, continuing without email notification.", error);
                        return Mono.empty();
                    })
                    .subscribe();
        } catch (Exception e) {
            log.error("Exception while sending account notification", e);
        }
    }

    /**
     * Send customer update notification email
     */
    public void sendCustomerUpdateEmail(String recipientEmail, String customerName, String updatedFields,
            String updateDate) {
        if (!emailServiceEnabled) {
            log.info("Email service is disabled. Skipping customer update email to: {}", recipientEmail);
            return;
        }

        log.info("Sending customer update notification to: {} ({})", customerName, recipientEmail);

        Map<String, Object> emailRequest = new HashMap<>();
        emailRequest.put("to", recipientEmail);
        emailRequest.put("customerName", customerName);
        emailRequest.put("updatedFields", updatedFields);
        emailRequest.put("updateDate", updateDate);

        try {
            WebClient webClient = webClientBuilder.baseUrl(emailServiceUrl).build();

            webClient.post()
                    .uri("/api/email/send-customer-update")
                    .bodyValue(emailRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .doOnSuccess(response -> log.info("Customer update email sent successfully to: {}", recipientEmail))
                    .doOnError(error -> log.error("Failed to send customer update email to {}: {} - {}",
                            recipientEmail, error.getClass().getSimpleName(), error.getMessage(), error))
                    .onErrorResume(error -> {
                        log.warn("Email service error, continuing without email notification.", error);
                        return Mono.empty();
                    })
                    .subscribe();
        } catch (Exception e) {
            log.error("Exception while sending customer update email", e);
        }
    }
}