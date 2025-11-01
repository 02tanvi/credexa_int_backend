package com.credexa.email.service;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.models.EmailAddress;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.credexa.email.dto.EmailRequest;
import com.credexa.email.dto.EmailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final EmailClient emailClient;
    private final TemplateEngine templateEngine;

    @Value("${azure.communication.sender}")
    private String senderAddress;

    /**
     * Send basic email with subject and body
     */
    public EmailResponse sendEmail(EmailRequest request) {
        try {
            log.info("Sending email to: {}", request.getTo());

            EmailMessage emailMessage = new EmailMessage()
                    .setSenderAddress(senderAddress)
                    .setToRecipients(new EmailAddress(request.getTo()))
                    .setSubject(request.getSubject())
                    .setBodyPlainText(request.getBody());

            // Add HTML body if provided
            if (request.getHtmlBody() != null && !request.getHtmlBody().isEmpty()) {
                emailMessage.setBodyHtml(request.getHtmlBody());
            }

            return sendAzureEmail(emailMessage, request.getTo());

        } catch (Exception e) {
            log.error("Error sending email to: {}", request.getTo(), e);
            return EmailResponse.builder()
                    .emailId(null)
                    .status("FAILED")
                    .message("Error sending email: " + e.getMessage())
                    .recipient(request.getTo())
                    .build();
        }
    }

    /**
     * Send simple text email
     */
    public EmailResponse sendSimpleEmail(String to, String subject, String body) {
        EmailRequest request = EmailRequest.builder()
                .to(to)
                .subject(subject)
                .body(body)
                .build();
        return sendEmail(request);
    }

    /**
     * Send new customer registration email using template
     */
    public EmailResponse sendNewCustomerEmail(String to, String customerName, String customerId, String email,
            String Phno, String registeredDate) {
        try {
            Context context = new Context();
            context.setVariable("customerName", customerName);
            context.setVariable("customerId", customerId);
            context.setVariable("mobile", Phno);
            context.setVariable("email", email);
            context.setVariable("registrationDate", registeredDate);

            String htmlBody = templateEngine.process("new-customer", context);

            EmailMessage emailMessage = new EmailMessage()
                    .setSenderAddress(senderAddress)
                    .setToRecipients(new EmailAddress(to))
                    .setSubject("Welcome to Credexa - Customer Registration Successful")
                    .setBodyPlainText("Welcome to Credexa! Your customer registration is successful.")
                    .setBodyHtml(htmlBody);

            return sendAzureEmail(emailMessage, to);

        } catch (Exception e) {
            log.error("Error sending new customer email to: {}", to, e);
            return EmailResponse.builder()
                    .emailId(null)
                    .status("FAILED")
                    .message("Error sending new customer email: " + e.getMessage())
                    .recipient(to)
                    .build();
        }
    }

    /**
     * Send new account creation email using template
     */
    public EmailResponse sendNewAccountEmail(String to, String customerName, String accountNumber,
            String accountType, String amount, String interestRate,
            String tenure, String maturityDate, String maturityAmount) {
        try {
            Context context = new Context();
            context.setVariable("customerName", customerName);
            context.setVariable("accountNumber", accountNumber);
            context.setVariable("accountType", accountType);
            context.setVariable("amount", amount);
            context.setVariable("interestRate", interestRate);
            context.setVariable("tenure", tenure);
            context.setVariable("maturityDate", maturityDate);
            context.setVariable("maturityAmount", maturityAmount);

            String htmlBody = templateEngine.process("new-account", context);

            EmailMessage emailMessage = new EmailMessage()
                    .setSenderAddress(senderAddress)
                    .setToRecipients(new EmailAddress(to))
                    .setSubject("New Account Created - Confirmation")
                    .setBodyPlainText("Your new account has been created successfully.")
                    .setBodyHtml(htmlBody);

            return sendAzureEmail(emailMessage, to);

        } catch (Exception e) {
            log.error("Error sending new account email to: {}", to, e);
            return EmailResponse.builder()
                    .emailId(null)
                    .status("FAILED")
                    .message("Error sending new account email: " + e.getMessage())
                    .recipient(to)
                    .build();
        }
    }

    /**
     * Send KYC notification email using template
     */
    public EmailResponse sendKycNotificationEmail(String to, String customerName) {
        try {
            Context context = new Context();
            context.setVariable("name", customerName);

            String htmlBody = templateEngine.process("kyc-notification", context);

            EmailMessage emailMessage = new EmailMessage()
                    .setSenderAddress(senderAddress)
                    .setToRecipients(new EmailAddress(to))
                    .setSubject("KYC Verification Approved")
                    .setBodyPlainText("Your KYC verification has been approved.")
                    .setBodyHtml(htmlBody);

            return sendAzureEmail(emailMessage, to);

        } catch (Exception e) {
            log.error("Error sending KYC notification email to: {}", to, e);
            return EmailResponse.builder()
                    .emailId(null)
                    .status("FAILED")
                    .message("Error sending KYC notification email: " + e.getMessage())
                    .recipient(to)
                    .build();
        }
    }

    /**
     * Send customer update notification email using template
     */
    public EmailResponse sendCustomerUpdateEmail(String to, String customerName, String updatedFields,
            String updateDate) {
        try {
            Context context = new Context();
            context.setVariable("customerName", customerName);
            context.setVariable("updatedFields", updatedFields);
            context.setVariable("updateDate", updateDate);

            String htmlBody = templateEngine.process("customer-update", context);

            EmailMessage emailMessage = new EmailMessage()
                    .setSenderAddress(senderAddress)
                    .setToRecipients(new EmailAddress(to))
                    .setSubject("Profile Updated - Credexa")
                    .setBodyPlainText("Your profile has been updated successfully.")
                    .setBodyHtml(htmlBody);

            return sendAzureEmail(emailMessage, to);

        } catch (Exception e) {
            log.error("Error sending customer update email to: {}", to, e);
            return EmailResponse.builder()
                    .emailId(null)
                    .status("FAILED")
                    .message("Error sending customer update email: " + e.getMessage())
                    .recipient(to)
                    .build();
        }
    }

    /**
     * Send login notification email using template
     */
    public EmailResponse sendLoginNotificationEmail(String to, String username, String loginTime,
            String ipAddress, String deviceInfo, String location) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("loginTime", loginTime);
            context.setVariable("ipAddress", ipAddress);
            context.setVariable("deviceInfo", deviceInfo);
            context.setVariable("location", location);

            String htmlBody = templateEngine.process("login-notification", context);

            EmailMessage emailMessage = new EmailMessage()
                    .setSenderAddress(senderAddress)
                    .setToRecipients(new EmailAddress(to))
                    .setSubject("Login Notification - Credexa")
                    .setBodyPlainText("A login to your account was detected.")
                    .setBodyHtml(htmlBody);

            return sendAzureEmail(emailMessage, to);

        } catch (Exception e) {
            log.error("Error sending login notification email to: {}", to, e);
            return EmailResponse.builder()
                    .emailId(null)
                    .status("FAILED")
                    .message("Error sending login notification email: " + e.getMessage())
                    .recipient(to)
                    .build();
        }
    }

    /**
     * Send FD maturity notification email using template
     */
    public EmailResponse sendFdMaturityNotificationEmail(String to, String customerName, String accountNumber,
            String accountName, String principalAmount, String interestRate, String maturityDate,
            String maturityAmount, String daysUntilMaturity, String maturityInstruction, String transferAccount) {
        try {
            Context context = new Context();
            context.setVariable("customerName", customerName);
            context.setVariable("accountNumber", accountNumber);
            context.setVariable("accountName", accountName);
            context.setVariable("principalAmount", principalAmount);
            context.setVariable("interestRate", interestRate);
            context.setVariable("maturityDate", maturityDate);
            context.setVariable("maturityAmount", maturityAmount);
            context.setVariable("daysUntilMaturity", daysUntilMaturity);
            context.setVariable("maturityInstruction", maturityInstruction);
            context.setVariable("transferAccount", transferAccount);

            String htmlBody = templateEngine.process("fd-maturity-notification", context);

            EmailMessage emailMessage = new EmailMessage()
                    .setSenderAddress(senderAddress)
                    .setToRecipients(new EmailAddress(to))
                    .setSubject("FD Maturity Notice - Credexa")
                    .setBodyPlainText("Your Fixed Deposit account is maturing soon.")
                    .setBodyHtml(htmlBody);

            return sendAzureEmail(emailMessage, to);

        } catch (Exception e) {
            log.error("Error sending FD maturity notification email to: {}", to, e);
            return EmailResponse.builder()
                    .emailId(null)
                    .status("FAILED")
                    .message("Error sending FD maturity notification email: " + e.getMessage())
                    .recipient(to)
                    .build();
        }
    }

    /**
     * Send email with custom template
     */
    public EmailResponse sendTemplatedEmail(String to, String subject, String templateName,
            Map<String, Object> templateData) {
        try {
            Context context = new Context();
            templateData.forEach(context::setVariable);

            String htmlBody = templateEngine.process(templateName, context);

            EmailMessage emailMessage = new EmailMessage()
                    .setSenderAddress(senderAddress)
                    .setToRecipients(new EmailAddress(to))
                    .setSubject(subject)
                    .setBodyPlainText("Please view this email in an HTML-capable email client.")
                    .setBodyHtml(htmlBody);

            return sendAzureEmail(emailMessage, to);

        } catch (Exception e) {
            log.error("Error sending templated email to: {}", to, e);
            return EmailResponse.builder()
                    .emailId(null)
                    .status("FAILED")
                    .message("Error sending templated email: " + e.getMessage())
                    .recipient(to)
                    .build();
        }
    }

    /**
     * Helper method to send email via Azure Communication Services
     */
    private EmailResponse sendAzureEmail(EmailMessage emailMessage, String recipient) {
        try {
            SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(emailMessage);
            PollResponse<EmailSendResult> response = poller.waitForCompletion(Duration.ofMinutes(2));

            if (response.getStatus().isComplete()) {
                EmailSendResult result = response.getValue();
                log.info("Email sent successfully to: {}. Message ID: {}", recipient, result.getId());

                return EmailResponse.builder()
                        .emailId(result.getId())
                        .status("SUCCESS")
                        .message("Email sent successfully")
                        .recipient(recipient)
                        .build();
            } else {
                log.error("Failed to send email to: {}", recipient);
                return EmailResponse.builder()
                        .emailId(null)
                        .status("FAILED")
                        .message("Failed to send email")
                        .recipient(recipient)
                        .build();
            }
        } catch (Exception e) {
            log.error("Azure email send error for: {}", recipient, e);
            throw new RuntimeException("Failed to send email via Azure", e);
        }
    }
}
