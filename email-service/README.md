# Email Service

A microservice for sending emails with template support that can be integrated with other Credexa microservices.

## Features

- ✉️ **Send Emails**: Simple and template-based email sending
- 📧 **Template Support**: Thymeleaf templates for professional emails
- 📊 **Email Logging**: Track all sent emails with status and history
- 🔄 **Async Processing**: Non-blocking email sending
- 📎 **Attachments**: Support for file attachments
- 📝 **Swagger Documentation**: Interactive API documentation
- 🔍 **Email History**: Query email logs by recipient or date range

## Technology Stack

- **Java 17**
- **Spring Boot 3.5.6**
- **Azure Communication Services** (Email sending)
- **Thymeleaf** (Email templating)
- **Swagger/OpenAPI** (API documentation)
- **Lombok** (Code simplification)

## Prerequisites

- Java 17 or higher
- Azure Communication Services account
- SMTP server credentials (Gmail, SendGrid, etc.)

## Configuration

### Azure Communication Services Setup

Update `application.yml` with your Azure Communication Services credentials:

```yaml
azure:
  communication:
    connection-string: endpoint=https://godseye-comm.india.communication.azure.com/;accesskey=YOUR_ACCESS_KEY
    sender: DoNotReply@7723d5ff-013c-4140-b7dd-b07d238bad77.azurecomm.net
```

**Note:** Keep your connection string secure. In production, use environment variables or Azure Key Vault.

## Running the Service

### Using Maven

```bash
cd email-service
mvn clean install
mvn spring-boot:run
```

### Using Java

```bash
java -jar target/email-service-1.0.0.jar
```

The service will start on **port 8085**.

## API Endpoints

### Send Email

**POST** `/api/email/send`

```json
{
  "to": "recipient@example.com",
  "subject": "Test Email",
  "body": "This is a test email"
}
```

### Send Email with Template

**POST** `/api/email/send`

```json
{
  "to": "user@example.com",
  "subject": "Welcome to Credexa",
  "templateName": "welcome",
  "templateData": {
    "name": "John Doe",
    "loginUrl": "https://credexa.com/login"
  }
}
```

### Health Check

**GET** `/api/email/health`

## Available Email Templates

### 1. Welcome Email (`welcome.html`)

Used for new user registration.

**Template Data:**

- `name`: User's name
- `loginUrl`: Login URL (optional)

### 2. Password Reset (`reset-password.html`)

Used for password reset requests.

**Template Data:**

- `name`: User's name
- `otp`: OTP code
- `expiryMinutes`: OTP expiry time

### 3. Account Notification (`account-notification.html`)

Used for account-related notifications.

**Template Data:**

- `customerName`: Customer's name
- `message`: Notification message
- `accountDetails`: Object with `accountNumber`, `accountType`, `status`

## Integration with Other Services

### 1. Using REST Client (Spring Boot)

Add dependency:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

Create a client:

```java
@Service
public class EmailClient {

    private final WebClient webClient;

    public EmailClient(WebClient.Builder builder) {
        this.webClient = builder
            .baseUrl("http://localhost:8085")
            .build();
    }

    public void sendWelcomeEmail(String email, String name) {
        EmailRequest request = EmailRequest.builder()
            .to(email)
            .subject("Welcome to Credexa")
            .templateName("welcome")
            .templateData(Map.of("name", name))
            .build();

        webClient.post()
            .uri("/api/email/send")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(EmailResponse.class)
            .subscribe();
    }
}
```

### 2. Using RestTemplate

```java
@Service
public class EmailService {

    private final RestTemplate restTemplate;
    private static final String EMAIL_SERVICE_URL = "http://localhost:8085/api/email/send";

    public void sendEmail(String to, String subject, String body) {
        EmailRequest request = new EmailRequest();
        request.setTo(to);
        request.setSubject(subject);
        request.setBody(body);

        restTemplate.postForObject(EMAIL_SERVICE_URL, request, EmailResponse.class);
    }
}
```

## Swagger Documentation

Access the interactive API documentation at:

- **Swagger UI**: http://localhost:8085/swagger-ui.html
- **API Docs**: http://localhost:8085/api-docs

## Monitoring

Health check endpoint:

- **Actuator Health**: http://localhost:8085/actuator/health

## Error Handling

The service includes comprehensive error handling:

- Azure Communication Services error handling
- Detailed error messages in responses
- Retry mechanism for transient failures

## Security Considerations

- Store Azure connection string securely (use environment variables or Azure Key Vault)
- Implement rate limiting for production use
- Add authentication/authorization for API endpoints
- Validate email addresses before sending
- Use managed identities in Azure for enhanced security

## Future Enhancements

- [ ] Email queue management
- [ ] Bulk email sending
- [ ] Email scheduling
- [ ] Advanced retry logic with exponential backoff
- [ ] Email analytics and reporting
- [ ] Support for multiple email providers
- [ ] Email template editor
- [ ] Webhook notifications for email events

## Troubleshooting

### Email Not Sending

1. Verify Azure Communication Services connection string
2. Check sender email address is verified in Azure
3. Review application logs for detailed error messages
4. Ensure network connectivity to Azure services
5. Check Azure Communication Services quota limits

### Azure Connection Issues

1. Verify connection string format
2. Check access key is valid
3. Ensure Azure Communication Services resource is active
4. Review Azure portal for service health status

## Support

For issues or questions, contact the development team or create an issue in the repository.

## License

© 2025 Credexa. All rights reserved.
