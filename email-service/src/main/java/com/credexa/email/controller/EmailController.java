package com.credexa.email.controller;

import com.credexa.email.dto.EmailRequest;
import com.credexa.email.dto.EmailResponse;
import com.credexa.email.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@Tag(name = "Email Service", description = "Email sending APIs using Azure Communication Services with Thymeleaf templates")
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    @Operation(summary = "Send an email", description = "Send a single email with subject and body")
    public ResponseEntity<EmailResponse> sendEmail(@Valid @RequestBody EmailRequest request) {
        EmailResponse response = emailService.sendEmail(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-simple")
    @Operation(summary = "Send a simple email", description = "Send a simple text email")
    public ResponseEntity<EmailResponse> sendSimpleEmail(@RequestBody Map<String, String> request) {
        String to = request.get("to");
        String subject = request.get("subject");
        String body = request.get("body");

        EmailResponse response = emailService.sendSimpleEmail(to, subject, body);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-new-customer")
    @Operation(summary = "Send new customer email", description = "Send new customer registration email using template")
    public ResponseEntity<EmailResponse> sendNewCustomerEmail(@RequestBody Map<String, String> request) {
        String to = request.get("to");
        String customerName = request.get("customerName");
        String customerId = request.get("customerId");
        String email = request.get("email");
        String phno = request.get("mobile");
        String registeredDate = request.get("registeredDate");

        EmailResponse response = emailService.sendNewCustomerEmail(to, customerName, customerId, email, phno,
                registeredDate);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-new-account")
    @Operation(summary = "Send new account email", description = "Send new account creation email using template")
    public ResponseEntity<EmailResponse> sendNewAccountEmail(@RequestBody Map<String, String> request) {
        String to = request.get("to");
        String customerName = request.get("customerName");
        String accountNumber = request.get("accountNumber");
        String accountType = request.get("accountType");
        String amount = request.get("amount");
        String interestRate = request.get("interestRate");
        String tenure = request.get("tenure");
        String maturityDate = request.get("maturityDate");
        String maturityAmount = request.get("maturityAmount");

        EmailResponse response = emailService.sendNewAccountEmail(to, customerName, accountNumber,
                accountType, amount, interestRate, tenure, maturityDate, maturityAmount);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-kyc-notification")
    @Operation(summary = "Send KYC notification", description = "Send KYC verification approval email using template")
    public ResponseEntity<EmailResponse> sendKycNotification(@RequestBody Map<String, String> request) {
        String to = request.get("to");
        String customerName = request.get("customerName");

        EmailResponse response = emailService.sendKycNotificationEmail(to, customerName);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-login-notification")
    @Operation(summary = "Send login notification email", description = "Send login notification email with login details")
    public ResponseEntity<EmailResponse> sendLoginNotification(@RequestBody Map<String, String> request) {
        String to = request.get("to");
        String username = request.get("username");
        String loginTime = request.get("loginTime");
        String ipAddress = request.get("ipAddress");
        String deviceInfo = request.get("deviceInfo");
        String location = request.get("location");

        EmailResponse response = emailService.sendLoginNotificationEmail(to, username, loginTime, ipAddress, deviceInfo,
                location);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-template")
    @Operation(summary = "Send templated email", description = "Send email using custom template with dynamic data")
    public ResponseEntity<EmailResponse> sendTemplatedEmail(@RequestBody Map<String, Object> request) {
        String to = (String) request.get("to");
        String subject = (String) request.get("subject");
        String templateName = (String) request.get("templateName");
        @SuppressWarnings("unchecked")
        Map<String, Object> templateData = (Map<String, Object>) request.get("templateData");

        EmailResponse response = emailService.sendTemplatedEmail(to, subject, templateName, templateData);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send-fd-maturity-notification")
    @Operation(summary = "Send FD maturity notification", description = "Send FD maturity notification email using template")
    public ResponseEntity<EmailResponse> sendFdMaturityNotification(@RequestBody Map<String, String> request) {
        String to = request.get("to");
        String customerName = request.get("customerName");
        String accountNumber = request.get("accountNumber");
        String accountName = request.get("accountName");
        String principalAmount = request.get("principalAmount");
        String interestRate = request.get("interestRate");
        String maturityDate = request.get("maturityDate");
        String maturityAmount = request.get("maturityAmount");
        String daysUntilMaturity = request.get("daysUntilMaturity");
        String maturityInstruction = request.get("maturityInstruction");
        String transferAccount = request.get("transferAccount");

        EmailResponse response = emailService.sendFdMaturityNotificationEmail(
                to, customerName, accountNumber, accountName, principalAmount, interestRate,
                maturityDate, maturityAmount, daysUntilMaturity, maturityInstruction, transferAccount);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the email service is running")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Email service is running with Azure Communication Services and Thymeleaf templates");
    }
}
