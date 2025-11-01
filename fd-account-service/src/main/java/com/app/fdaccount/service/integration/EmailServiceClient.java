package com.app.fdaccount.service.integration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Service to integrate with email-service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${integration.email-service.url:http://localhost:8085/api/email}")
    private String emailServiceUrl;

    @Value("${integration.email-service.enabled:true}")
    private boolean emailServiceEnabled;

    /**
     * Send new FD account creation email
     */
    public void sendNewAccountEmail(String recipientEmail, String customerName, String accountNumber,
            String accountType, String amount, String interestRate,
            String tenure, String maturityDate, String maturityAmount) {
        if (!emailServiceEnabled) {
            log.info("Email service is disabled. Skipping new account email to: {}", recipientEmail);
            return;
        }

        log.info("Sending new account email to: {} ({})", customerName, recipientEmail);

        Map<String, Object> emailRequest = new HashMap<>();
        emailRequest.put("to", recipientEmail);
        emailRequest.put("customerName", customerName);
        emailRequest.put("accountNumber", accountNumber);
        emailRequest.put("accountType", accountType);
        emailRequest.put("amount", amount);
        emailRequest.put("interestRate", interestRate);
        emailRequest.put("tenure", tenure);
        emailRequest.put("maturityDate", maturityDate);
        emailRequest.put("maturityAmount", maturityAmount);

        try {
            WebClient webClient = webClientBuilder.baseUrl(emailServiceUrl).build();

            webClient.post()
                    .uri("/send-new-account")
                    .bodyValue(emailRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .doOnSuccess(response -> log.info("✅ New account email sent successfully to: {}", recipientEmail))
                    .doOnError(error -> log.error("❌ Failed to send new account email to {}: {} - {}",
                            recipientEmail, error.getClass().getSimpleName(), error.getMessage(), error))
                    .onErrorResume(error -> {
                        log.warn("Email service error, continuing without email notification.", error);
                        return Mono.empty();
                    })
                    .subscribe();
        } catch (Exception e) {
            log.error("Exception while sending new account email", e);
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
                    .uri("/send-simple")
                    .bodyValue(emailRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .doOnSuccess(response -> log.info("✅ Email sent successfully to: {}", recipientEmail))
                    .doOnError(error -> log.error("❌ Failed to send email to {}: {} - {}",
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
     * Send FD maturity notification email
     */
    public void sendFdMaturityNotificationEmail(String recipientEmail, String customerName, String accountNumber,
            String accountName, String principalAmount, String interestRate, String maturityDate,
            String maturityAmount, String daysUntilMaturity, String maturityInstruction, String transferAccount) {
        if (!emailServiceEnabled) {
            log.info("Email service is disabled. Skipping FD maturity notification to: {}", recipientEmail);
            return;
        }

        log.info("Sending FD maturity notification to: {} for account: {}", recipientEmail, accountNumber);

        Map<String, String> emailRequest = new HashMap<>();
        emailRequest.put("to", recipientEmail);
        emailRequest.put("customerName", customerName);
        emailRequest.put("accountNumber", accountNumber);
        emailRequest.put("accountName", accountName);
        emailRequest.put("principalAmount", principalAmount);
        emailRequest.put("interestRate", interestRate);
        emailRequest.put("maturityDate", maturityDate);
        emailRequest.put("maturityAmount", maturityAmount);
        emailRequest.put("daysUntilMaturity", daysUntilMaturity);
        emailRequest.put("maturityInstruction", maturityInstruction);
        emailRequest.put("transferAccount", transferAccount != null ? transferAccount : "");

        try {
            WebClient webClient = webClientBuilder.baseUrl(emailServiceUrl).build();

            webClient.post()
                    .uri("/send-fd-maturity-notification")
                    .bodyValue(emailRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .doOnSuccess(response -> log.info("✅ FD maturity notification sent to: {} for account: {}",
                            recipientEmail, accountNumber))
                    .doOnError(error -> log.error("❌ Failed to send FD maturity notification to {}: {} - {}",
                            recipientEmail, error.getClass().getSimpleName(), error.getMessage(), error))
                    .onErrorResume(error -> {
                        log.warn("Email service error, continuing without email notification.", error);
                        return Mono.empty();
                    })
                    .subscribe();
        } catch (Exception e) {
            log.error("Exception while sending FD maturity notification", e);
        }
    }
}
