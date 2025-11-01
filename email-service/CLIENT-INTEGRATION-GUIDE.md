# Email Service Client - Integration Guide

This guide shows how to integrate the Email Service into any microservice in the Credexa platform.

---

## 📋 Step-by-Step Integration

### Step 1: Add Dependencies to `pom.xml`

```xml
<!-- Spring WebFlux for WebClient -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<!-- Lombok (optional but recommended) -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

---

### Step 2: Configure Email Service URL in `application.yml`

```yaml
# Email Service Configuration
email-service:
  url: http://localhost:8085
  enabled: true # Set to false to disable emails in dev/test
```

---

### Step 3: Create EmailServiceClient Class

Create this file in your service's `client` package:

**Path**: `src/main/java/com/app/[your-service]/client/EmailServiceClient.java`

```java
package com.app.[your-service].client;

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
     * Send welcome email to new customer
     */
    public void sendWelcomeEmail(String recipientEmail, String customerName) {
        if (!emailServiceEnabled) {
            log.info("Email service is disabled. Skipping welcome email to: {}", recipientEmail);
            return;
        }

        log.info("Sending welcome email to: {} ({})", customerName, recipientEmail);

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("name", customerName);
        templateData.put("loginUrl", "https://credexa.com/login");

        sendTemplatedEmail(
            recipientEmail,
            "Welcome to Credexa - Your Account is Ready!",
            "welcome",
            templateData
        );
    }

    /**
     * Send KYC approval notification
     */
    public void sendKycApprovalEmail(String recipientEmail, String customerName) {
        if (!emailServiceEnabled) {
            log.info("Email service is disabled. Skipping KYC email to: {}", recipientEmail);
            return;
        }

        log.info("Sending KYC approval email to: {}", recipientEmail);

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("name", customerName);

        sendTemplatedEmail(
            recipientEmail,
            "KYC Verification Approved",
            "kyc-notification",
            templateData
        );
    }

    /**
     * Send new customer registration email
     */
    public void sendNewCustomerEmail(String recipientEmail, String customerName,
                                      String customerId, String email, String mobile,
                                      String registrationDate) {
        if (!emailServiceEnabled) {
            log.info("Email service is disabled. Skipping new customer email to: {}", recipientEmail);
            return;
        }

        log.info("Sending new customer email to: {}", recipientEmail);

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("customerName", customerName);
        templateData.put("customerId", customerId);
        templateData.put("email", email);
        templateData.put("mobile", mobile);
        templateData.put("registrationDate", registrationDate);
        templateData.put("loginUrl", "https://credexa.com/login");

        sendTemplatedEmail(
            recipientEmail,
            "Welcome to Credexa!",
            "new-customer",
            templateData
        );
    }

    /**
     * Send new account opening email (for FD/Savings accounts)
     */
    public void sendNewAccountEmail(String recipientEmail, String customerName,
                                     String accountType, String accountNumber,
                                     String openingDate, String depositAmount,
                                     String interestRate, String maturityDate,
                                     String maturityAmount) {
        if (!emailServiceEnabled) {
            log.info("Email service is disabled. Skipping new account email to: {}", recipientEmail);
            return;
        }

        log.info("Sending new account email to: {} for account: {}", recipientEmail, accountNumber);

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("customerName", customerName);
        templateData.put("accountType", accountType);
        templateData.put("accountNumber", accountNumber);
        templateData.put("openingDate", openingDate);

        // Optional fields for FD accounts
        if (depositAmount != null) templateData.put("depositAmount", depositAmount);
        if (interestRate != null) templateData.put("interestRate", interestRate);
        if (maturityDate != null) templateData.put("maturityDate", maturityDate);
        if (maturityAmount != null) templateData.put("maturityAmount", maturityAmount);

        templateData.put("accountUrl", "https://credexa.com/accounts/" + accountNumber);

        sendTemplatedEmail(
            recipientEmail,
            "New Account Opened Successfully!",
            "new-account",
            templateData
        );
    }

    /**
     * Send templated email (generic method)
     */
    public void sendTemplatedEmail(String recipientEmail, String subject,
                                    String templateName, Map<String, Object> templateData) {
        if (!emailServiceEnabled) {
            log.info("Email service is disabled. Skipping email to: {}", recipientEmail);
            return;
        }

        log.info("Sending templated email ({}) to: {}", templateName, recipientEmail);

        Map<String, Object> emailRequest = new HashMap<>();
        emailRequest.put("to", recipientEmail);
        emailRequest.put("subject", subject);
        emailRequest.put("templateName", templateName);
        emailRequest.put("templateData", templateData);

        try {
            WebClient webClient = webClientBuilder.baseUrl(emailServiceUrl).build();

            webClient.post()
                    .uri("/api/email/send")
                    .bodyValue(emailRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .doOnSuccess(response -> log.info("Email sent successfully to: {}", recipientEmail))
                    .doOnError(error -> log.error("Failed to send email to: {}", recipientEmail, error))
                    .onErrorResume(error -> {
                        log.warn("Email service error, continuing without email notification");
                        return Mono.empty();
                    })
                    .subscribe();
        } catch (Exception e) {
            log.error("Exception while sending email", e);
        }
    }

    /**
     * Send plain text email (without template)
     */
    public void sendPlainEmail(String recipientEmail, String subject, String body) {
        if (!emailServiceEnabled) {
            log.info("Email service is disabled. Skipping email to: {}", recipientEmail);
            return;
        }

        log.info("Sending plain email to: {}", recipientEmail);

        Map<String, Object> emailRequest = new HashMap<>();
        emailRequest.put("to", recipientEmail);
        emailRequest.put("subject", subject);
        emailRequest.put("body", body);

        try {
            WebClient webClient = webClientBuilder.baseUrl(emailServiceUrl).build();

            webClient.post()
                    .uri("/api/email/send")
                    .bodyValue(emailRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .doOnSuccess(response -> log.info("Email sent successfully to: {}", recipientEmail))
                    .doOnError(error -> log.error("Failed to send email to: {}", recipientEmail, error))
                    .onErrorResume(error -> {
                        log.warn("Email service error, continuing without email notification");
                        return Mono.empty();
                    })
                    .subscribe();
        } catch (Exception e) {
            log.error("Exception while sending email", e);
        }
    }
}
```

---

### Step 4: Configure WebClient Bean (Optional)

If you don't already have a `RestClientConfig`, create one:

**Path**: `src/main/java/com/app/[your-service]/config/RestClientConfig.java`

```java
package com.app.[your-service].config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class RestClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
```

---

## 🎯 Usage Examples

### Example 1: Customer Service - Send Welcome Email

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final EmailServiceClient emailServiceClient;

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        // Create customer
        Customer customer = customerRepository.save(buildCustomer(request));

        // Send welcome email (non-blocking)
        try {
            emailServiceClient.sendWelcomeEmail(
                customer.getEmail(),
                customer.getFullName()
            );
            log.info("Welcome email triggered for: {}", customer.getEmail());
        } catch (Exception e) {
            log.error("Failed to send welcome email, but customer creation succeeded", e);
            // Don't fail the customer creation if email fails
        }

        return CustomerResponse.fromEntity(customer);
    }
}
```

---

### Example 2: Customer Service - Send KYC Approval

```java
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final EmailServiceClient emailServiceClient;

    public void approveKyc(Long customerId) {
        Customer customer = findById(customerId);
        customer.setKycStatus(KycStatus.APPROVED);
        customerRepository.save(customer);

        // Send KYC approval email
        emailServiceClient.sendKycApprovalEmail(
            customer.getEmail(),
            customer.getFullName()
        );
    }
}
```

---

### Example 3: FD Account Service - New Account Email

```java
@Service
@RequiredArgsConstructor
public class FDAccountService {

    private final EmailServiceClient emailServiceClient;
    private final CustomerServiceClient customerServiceClient;

    public FDAccountResponse createFDAccount(CreateFDAccountRequest request) {
        // Create FD account
        FDAccount fdAccount = fdAccountRepository.save(buildFDAccount(request));

        // Get customer details
        CustomerResponse customer = customerServiceClient.getCustomerById(
            fdAccount.getCustomerId()
        );

        // Send new account email
        emailServiceClient.sendNewAccountEmail(
            customer.getEmail(),
            customer.getFullName(),
            "Fixed Deposit",
            fdAccount.getAccountNumber(),
            fdAccount.getOpeningDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
            String.format("%,.0f", fdAccount.getDepositAmount()),
            fdAccount.getInterestRate().toString(),
            fdAccount.getMaturityDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
            String.format("%,.0f", fdAccount.getMaturityAmount())
        );

        return FDAccountResponse.fromEntity(fdAccount);
    }
}
```

---

### Example 4: Login Service - Password Reset Email

```java
@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmailServiceClient emailServiceClient;

    public void sendPasswordResetEmail(String email, String resetToken) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Send custom email (since we don't have reset-password template)
        String subject = "Password Reset Request";
        String body = String.format(
            "Dear %s,\n\n" +
            "You requested a password reset. Click the link below:\n" +
            "https://credexa.com/reset-password?token=%s\n\n" +
            "This link expires in 15 minutes.\n\n" +
            "Best regards,\nCredexa Team",
            user.getFullName(),
            resetToken
        );

        emailServiceClient.sendPlainEmail(email, subject, body);
    }
}
```

---

### Example 5: Generic Templated Email

```java
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailServiceClient emailServiceClient;

    public void sendCustomNotification(String email, String name, String message) {
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("name", name);

        emailServiceClient.sendTemplatedEmail(
            email,
            "Important Notification",
            "kyc-notification",  // Reuse existing template
            templateData
        );
    }
}
```

---

## 📝 Available Email Templates

### 1. **welcome.html** - Welcome Email

**When to use**: New user signup

**Required Data**:

```java
Map<String, Object> templateData = new HashMap<>();
templateData.put("name", "John Doe");
templateData.put("loginUrl", "https://credexa.com/login"); // Optional
```

---

### 2. **kyc-notification.html** - KYC Approval

**When to use**: KYC verification approved

**Required Data**:

```java
Map<String, Object> templateData = new HashMap<>();
templateData.put("name", "John Doe");
```

---

### 3. **new-customer.html** - Customer Registration

**When to use**: Complete customer registration

**Required Data**:

```java
Map<String, Object> templateData = new HashMap<>();
templateData.put("customerName", "John Doe");
templateData.put("customerId", "CUST123456");
templateData.put("email", "john@example.com");
templateData.put("mobile", "+91 9876543210");
templateData.put("registrationDate", "October 22, 2025");
templateData.put("loginUrl", "https://credexa.com/login"); // Optional
```

---

### 4. **new-account.html** - New FD/Savings Account

**When to use**: New account opened

**Required Data**:

```java
Map<String, Object> templateData = new HashMap<>();
templateData.put("customerName", "John Doe");
templateData.put("accountType", "Fixed Deposit");
templateData.put("accountNumber", "FD123456789");
templateData.put("openingDate", "October 22, 2025");

// Optional for FD accounts
templateData.put("depositAmount", "50,000");
templateData.put("interestRate", "7.5");
templateData.put("maturityDate", "October 22, 2026");
templateData.put("maturityAmount", "53,750");
templateData.put("accountUrl", "https://credexa.com/accounts/FD123456789");
```

---

## ⚙️ Configuration Options

### Enable/Disable Email Service

**Development** (disable emails):

```yaml
email-service:
  enabled: false
```

**Production** (enable emails):

```yaml
email-service:
  enabled: true
```

### Change Email Service URL

```yaml
email-service:
  url: http://email-service:8085 # For Docker/K8s
  # url: http://localhost:8085     # For local development
```

---

## 🔥 Best Practices

### 1. **Non-Blocking Email Sending**

Always wrap email calls in try-catch to prevent failures from affecting business logic:

```java
try {
    emailServiceClient.sendWelcomeEmail(email, name);
    log.info("Email sent successfully");
} catch (Exception e) {
    log.error("Email failed, but operation succeeded", e);
    // Don't fail the main operation
}
```

### 2. **Async Email Sending**

The `EmailServiceClient` uses `.subscribe()` which is non-blocking. The email is sent asynchronously.

### 3. **Graceful Degradation**

If email service is down, operations should still succeed:

```java
.onErrorResume(error -> {
    log.warn("Email service error, continuing without email");
    return Mono.empty();
})
```

### 4. **Enable/Disable in Config**

Use `email-service.enabled` flag to disable emails in test/dev environments.

### 5. **Logging**

Always log email operations for troubleshooting:

```java
log.info("Sending email to: {}", email);
log.error("Failed to send email", exception);
```

---

## 🧪 Testing

### Unit Test Example

```java
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private EmailServiceClient emailServiceClient;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void createCustomer_shouldSendWelcomeEmail() {
        // Arrange
        CreateCustomerRequest request = new CreateCustomerRequest();
        request.setEmail("test@example.com");
        request.setFullName("Test User");

        Customer customer = Customer.builder()
            .email("test@example.com")
            .fullName("Test User")
            .build();

        when(customerRepository.save(any())).thenReturn(customer);

        // Act
        customerService.createCustomer(request);

        // Assert
        verify(emailServiceClient).sendWelcomeEmail(
            "test@example.com",
            "Test User"
        );
    }
}
```

---

## 🚨 Troubleshooting

### Issue: Emails not being sent

**Check**:

1. Is `email-service.enabled=true` in application.yml?
2. Is email-service running on port 8085?
3. Check logs for connection errors
4. Verify `email-service.url` is correct

### Issue: Email service down but operations failing

**Solution**: Wrap email calls in try-catch:

```java
try {
    emailServiceClient.sendEmail(...);
} catch (Exception e) {
    log.error("Email failed", e);
    // Continue with operation
}
```

### Issue: WebClient not found

**Solution**: Add dependency:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

---

## 📚 Complete Integration Checklist

- [ ] Add WebFlux dependency to pom.xml
- [ ] Add email-service configuration to application.yml
- [ ] Create EmailServiceClient.java in client package
- [ ] Create RestClientConfig.java (if not exists)
- [ ] Inject EmailServiceClient into your service
- [ ] Call appropriate email method after business logic
- [ ] Wrap email calls in try-catch
- [ ] Test with email-service running
- [ ] Test with email-service disabled (enabled=false)
- [ ] Add logging for email operations

---

## 📞 Support

For email template customization, see:

- `email-service/DYNAMIC-TEMPLATES-GUIDE.md`
- `email-service/README.md`

For Azure Communication Services setup, see:

- `email-service/INTEGRATION-GUIDE.md`
