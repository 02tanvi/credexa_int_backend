# Email Service Integration Guide

This guide explains how to integrate the Email Service with other Credexa microservices.

## Table of Contents

1. [Quick Start](#quick-start)
2. [Login Service Integration](#login-service-integration)
3. [Customer Service Integration](#customer-service-integration)
4. [FD Account Service Integration](#fd-account-service-integration)
5. [Common Integration Patterns](#common-integration-patterns)

---

## Quick Start

### Add Dependencies

Add to your service's `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

### Configuration

Add to your `application.yml`:

```yaml
email-service:
  url: http://localhost:8085
  enabled: true
```

---

## Login Service Integration

### Use Case: Send Welcome Email on Registration

**File**: `login-service/src/main/java/com/credexa/login/service/UserService.java`

```java
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class UserService {

    private final WebClient emailClient;

    public UserService(WebClient.Builder builder,
                      @Value("${email-service.url}") String emailServiceUrl) {
        this.emailClient = builder.baseUrl(emailServiceUrl).build();
    }

    public void registerUser(UserRegistrationRequest request) {
        // ... existing registration logic

        // Send welcome email
        sendWelcomeEmail(user.getEmail(), user.getFullName());
    }

    private void sendWelcomeEmail(String email, String name) {
        Map<String, Object> emailRequest = Map.of(
            "to", email,
            "subject", "Welcome to Credexa",
            "templateName", "welcome",
            "templateData", Map.of(
                "name", name,
                "loginUrl", "https://credexa.com/login"
            )
        );

        emailClient.post()
            .uri("/api/email/send")
            .bodyValue(emailRequest)
            .retrieve()
            .bodyToMono(Map.class)
            .subscribe(
                response -> log.info("Welcome email sent to: {}", email),
                error -> log.error("Failed to send welcome email", error)
            );
    }
}
```

### Use Case: Send Password Reset Email

```java
public void sendPasswordResetEmail(String email, String otp) {
    Map<String, Object> emailRequest = Map.of(
        "to", email,
        "subject", "Password Reset Request",
        "templateName", "reset-password",
        "templateData", Map.of(
            "name", getUserName(email),
            "otp", otp,
            "expiryMinutes", "10"
        )
    );

    emailClient.post()
        .uri("/api/email/send")
        .bodyValue(emailRequest)
        .retrieve()
        .bodyToMono(Map.class)
        .subscribe();
}
```

---

## Customer Service Integration

### Use Case: Send Customer Account Notifications

**File**: `customer-service/src/main/java/com/credexa/customer/service/CustomerService.java`

```java
@Service
public class CustomerService {

    private final WebClient emailClient;

    public void notifyAccountCreation(Customer customer) {
        Map<String, Object> accountDetails = Map.of(
            "accountNumber", customer.getAccountNumber(),
            "accountType", customer.getAccountType(),
            "status", "ACTIVE"
        );

        Map<String, Object> emailRequest = Map.of(
            "to", customer.getEmail(),
            "subject", "Account Created Successfully",
            "templateName", "account-notification",
            "templateData", Map.of(
                "customerName", customer.getName(),
                "message", "Your account has been successfully created.",
                "accountDetails", accountDetails
            )
        );

        emailClient.post()
            .uri("/api/email/send")
            .bodyValue(emailRequest)
            .retrieve()
            .bodyToMono(Map.class)
            .subscribe();
    }
}
```

---

## FD Account Service Integration

### Use Case: FD Maturity Notification

**File**: `fd-account-service/src/main/java/com/credexa/fd/service/FDAccountService.java`

```java
@Service
public class FDAccountService {

    private final WebClient emailClient;

    public void notifyFDMaturity(FDAccount fdAccount) {
        Map<String, Object> accountDetails = Map.of(
            "accountNumber", fdAccount.getAccountNumber(),
            "accountType", "Fixed Deposit",
            "status", "MATURED",
            "maturityAmount", fdAccount.getMaturityAmount(),
            "maturityDate", fdAccount.getMaturityDate()
        );

        Map<String, Object> emailRequest = Map.of(
            "to", fdAccount.getCustomerEmail(),
            "subject", "FD Account Maturity Notification",
            "templateName", "account-notification",
            "templateData", Map.of(
                "customerName", fdAccount.getCustomerName(),
                "message", "Your Fixed Deposit has matured. Please visit the branch to collect your amount.",
                "accountDetails", accountDetails
            )
        );

        emailClient.post()
            .uri("/api/email/send")
            .bodyValue(emailRequest)
            .retrieve()
            .bodyToMono(Map.class)
            .subscribe();
    }
}
```

---

## Common Integration Patterns

### 1. Create Email Client Bean

**File**: `config/EmailClientConfig.java`

```java
@Configuration
public class EmailClientConfig {

    @Bean
    public WebClient emailServiceClient(WebClient.Builder builder,
                                       @Value("${email-service.url}") String url) {
        return builder
            .baseUrl(url)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
}
```

### 2. Create Email Service Wrapper

**File**: `service/EmailNotificationService.java`

```java
@Service
@Slf4j
public class EmailNotificationService {

    private final WebClient emailClient;

    @Value("${email-service.enabled:true}")
    private boolean emailEnabled;

    public EmailNotificationService(WebClient emailServiceClient) {
        this.emailClient = emailServiceClient;
    }

    public void sendTemplateEmail(String to, String subject, String templateName,
                                 Map<String, Object> templateData) {
        if (!emailEnabled) {
            log.info("Email service disabled. Skipping email to: {}", to);
            return;
        }

        Map<String, Object> emailRequest = Map.of(
            "to", to,
            "subject", subject,
            "templateName", templateName,
            "templateData", templateData
        );

        emailClient.post()
            .uri("/api/email/send")
            .bodyValue(emailRequest)
            .retrieve()
            .bodyToMono(Map.class)
            .doOnSuccess(response ->
                log.info("Email sent successfully to: {}", to))
            .doOnError(error ->
                log.error("Failed to send email to: {}", to, error))
            .subscribe();
    }

    public void sendSimpleEmail(String to, String subject, String body) {
        if (!emailEnabled) {
            log.info("Email service disabled. Skipping email to: {}", to);
            return;
        }

        Map<String, Object> emailRequest = Map.of(
            "to", to,
            "subject", subject,
            "body", body
        );

        emailClient.post()
            .uri("/api/email/send")
            .bodyValue(emailRequest)
            .retrieve()
            .bodyToMono(Map.class)
            .subscribe();
    }

    public void sendEmailWithAttachment(String to, String subject, String body,
                                       List<AttachmentData> attachments) {
        Map<String, Object> emailRequest = new HashMap<>();
        emailRequest.put("to", to);
        emailRequest.put("subject", subject);
        emailRequest.put("body", body);
        emailRequest.put("attachments", attachments);

        emailClient.post()
            .uri("/api/email/send")
            .bodyValue(emailRequest)
            .retrieve()
            .bodyToMono(Map.class)
            .subscribe();
    }
}
```

### 3. Error Handling

```java
@Service
public class ResilientEmailService {

    private final WebClient emailClient;

    public Mono<Boolean> sendEmailWithRetry(EmailRequest request) {
        return emailClient.post()
            .uri("/api/email/send")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(Map.class)
            .map(response -> true)
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)))
            .onErrorResume(error -> {
                log.error("Failed to send email after retries", error);
                return Mono.just(false);
            });
    }
}
```

### 4. Bulk Email Sending

```java
public void sendBulkEmails(List<String> recipients, String subject, String body) {
    recipients.forEach(recipient -> {
        sendSimpleEmail(recipient, subject, body);
        // Add delay to avoid overwhelming the email server
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    });
}
```

---

## Testing Integration

### Unit Test Example

```java
@SpringBootTest
class EmailIntegrationTest {

    @Autowired
    private EmailNotificationService emailService;

    @Test
    void testSendWelcomeEmail() {
        emailService.sendTemplateEmail(
            "test@example.com",
            "Welcome",
            "welcome",
            Map.of("name", "Test User")
        );

        // Verify email was sent (check email service logs)
    }
}
```

---

## Best Practices

1. **Always handle errors gracefully** - Don't let email failures break your main flow
2. **Use async calls** - Don't block the main thread waiting for email
3. **Log all email operations** - For debugging and audit trails
4. **Make email optional** - Use feature flags to disable emails if needed
5. **Rate limiting** - Don't send too many emails at once
6. **Template reuse** - Create reusable email templates
7. **Test with real emails** - But use test accounts in development

---

## Environment-Specific Configuration

### Development

```yaml
email-service:
  url: http://localhost:8085
  enabled: true
```

### Production

```yaml
email-service:
  url: ${EMAIL_SERVICE_URL:http://email-service:8085}
  enabled: true
```

---

## Troubleshooting

### Email not received

1. Check email service logs
2. Verify SMTP configuration
3. Check spam folder
4. Verify recipient email address

### Connection refused

1. Ensure email service is running on port 8085
2. Check network connectivity
3. Verify service URL configuration

### Template not found

1. Ensure template exists in `email-service/src/main/resources/templates/`
2. Verify template name in request
3. Check email service logs

---

## Support

For integration issues, contact the Email Service team or refer to the main README.md.
