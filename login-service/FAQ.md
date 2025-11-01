# Login Service - Frequently Asked Questions (FAQ)

## 🔧 Setup & Configuration

### Q1: How do I change the MySQL database credentials?
**A:** Edit `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    username: your_username
    password: your_password
```

### Q2: How do I change the port from 8081 to something else?
**A:** Edit `application.yml`:
```yaml
server:
  port: 8082  # or any available port
```

### Q3: How do I change the JWT secret key?
**A:** Edit `application.yml`:
```yaml
jwt:
  secret: your-256-bit-secret-key-here
  expiration: 3600000  # 1 hour in milliseconds
```
**Important:** Secret must be at least 256 bits (32 characters) for HS256 algorithm.

### Q4: How do I change the auto-logout time from 5 minutes?
**A:** Edit `application.yml`:
```yaml
session:
  idle-timeout: 600000  # 10 minutes in milliseconds
```

### Q5: Can I disable Kafka if I don't have it installed?
**A:** Yes! The service will work without Kafka. It logs a warning but continues normally.
To completely disable Kafka, comment out these lines in `pom.xml`:
```xml
<!-- <dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency> -->
```

---

## 🔐 Security & Authentication

### Q6: How is the password stored in the database?
**A:** Passwords are hashed using **BCrypt** with a strength of 12. The hash includes a built-in salt. Example:
```
Original: Admin@123
Stored: $2a$12$XYZ...abc (60 characters)
```

### Q7: Can I change the BCrypt strength?
**A:** Yes, edit `SecurityConfig.java`:
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(14); // Change from 12 to 14 (stronger)
}
```
**Warning:** Higher strength = slower hashing = slower login.

### Q8: How long is the JWT token valid?
**A:** Default is 1 hour (3600000 milliseconds). Change in `application.yml`:
```yaml
jwt:
  expiration: 7200000  # 2 hours
```

### Q9: What happens after 5 failed login attempts?
**A:** The account is locked automatically. To unlock:
```sql
UPDATE users 
SET account_locked = FALSE, failed_login_attempts = 0 
WHERE username = 'locked_user';
```

### Q10: How does auto-logout work?
**A:** A scheduled task runs every minute, checking for sessions where `last_activity` is older than the configured idle timeout (default 5 minutes). These sessions are marked inactive.

---

## 👤 User Management

### Q11: How do I create an admin user manually?
**A:** Either:
1. Use the default admin (username: admin, password: Admin@123)
2. Register a user via API, then update their role in database:
```sql
-- Find role ID
SELECT id FROM roles WHERE name = 'ROLE_ADMIN';

-- Add admin role to user
INSERT INTO user_roles (user_id, role_id) 
VALUES ((SELECT id FROM users WHERE username = 'your_user'), 
        (SELECT id FROM roles WHERE name = 'ROLE_ADMIN'));
```

### Q12: Can a user have multiple roles?
**A:** Yes! The user_roles table is a many-to-many relationship. Add multiple entries:
```sql
INSERT INTO user_roles (user_id, role_id) VALUES 
  (user_id, (SELECT id FROM roles WHERE name = 'ROLE_USER')),
  (user_id, (SELECT id FROM roles WHERE name = 'ROLE_ADMIN'));
```

### Q13: How do I delete a user?
**A:** Use the API (to be created) or SQL:
```sql
DELETE FROM users WHERE username = 'user_to_delete';
```
Note: This will cascade delete user_roles and user_sessions.

### Q14: Can username be changed after registration?
**A:** Not currently. Username is unique and permanent. You'd need to create a new user.

### Q15: How do I reset a user's password?
**A:** Create an API endpoint or manually:
```java
// Generate BCrypt hash
String hashedPassword = passwordEncoder.encode("NewPassword@123");
```
Then update in database:
```sql
UPDATE users 
SET password = '$2a$12$...your_hashed_password...' 
WHERE username = 'user';
```

---

## 🔑 JWT & Tokens

### Q16: What's inside the JWT token?
**A:** Decode at https://jwt.io to see:
```json
{
  "sub": "username",
  "roles": ["ROLE_USER"],
  "iat": 1234567890,
  "exp": 1234571490
}
```

### Q17: How do other microservices validate the token?
**A:** Two ways:
1. Call `POST /api/auth/validate-token` endpoint
2. Use `JwtUtil` from common-lib (recommended):
```java
@Autowired
private JwtUtil jwtUtil;

boolean isValid = jwtUtil.validateToken(token);
String username = jwtUtil.extractUsername(token);
List<String> roles = jwtUtil.extractRoles(token);
```

### Q18: What happens if the token expires?
**A:** Any request with an expired token will get 401 Unauthorized. User must login again.

### Q19: Can I refresh a token without re-login?
**A:** Not currently implemented. You'd need to add a `/refresh-token` endpoint that:
1. Validates old token (even if expired, but within grace period)
2. Issues new token with extended expiry

### Q20: Where should the frontend store the JWT token?
**A:** Options:
- **localStorage** - Simple but vulnerable to XSS
- **sessionStorage** - Cleared when tab closes
- **Memory (React state)** - Most secure but lost on refresh
- **httpOnly cookie** - Secure but needs backend changes

---

## 🗄️ Database

### Q21: Can I use PostgreSQL instead of MySQL?
**A:** Yes! Change in `pom.xml`:
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```
And in `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/login_db
    driver-class-name: org.postgresql.Driver
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

### Q22: How do I see the auto-generated SQL queries?
**A:** Already enabled in `application.yml`:
```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```
Check console logs.

### Q23: How do I prevent database auto-creation in production?
**A:** Change in `application.yml`:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # instead of 'update'
```
Options:
- `create` - Drop and recreate (DANGEROUS!)
- `create-drop` - Drop on shutdown
- `update` - Update schema (safe for dev)
- `validate` - Only validate (for production)
- `none` - Do nothing

### Q24: How do I backup the database?
**A:** Use MySQL dump:
```bash
mysqldump -u root -p login_db > backup_2025_10_17.sql
```
Restore:
```bash
mysql -u root -p login_db < backup_2025_10_17.sql
```

---

## 📡 API & Integration

### Q25: How do I enable CORS for my React app on a different port?
**A:** Already configured in `SecurityConfig.java`:
```java
configuration.setAllowedOrigins(List.of(
    "http://localhost:3000",  // React default
    "http://localhost:4200"   // Angular default
));
```
Add more as needed.

### Q26: How do I test the API without Swagger?
**A:** Use:
- **Postman** - Import the provided collection
- **cURL** - Command line
- **HTTPie** - User-friendly CLI
- **Insomnia** - Alternative to Postman

### Q27: Why am I getting 401 Unauthorized on protected endpoints?
**A:** Common causes:
1. No `Authorization` header
2. Token format wrong (must be `Bearer YOUR_TOKEN`)
3. Token expired
4. Token invalid/tampered
5. User doesn't have required role

Check:
```bash
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8081/api/auth/logout
```

### Q28: How do I add a new role?
**A:** Either:
1. Add to `Role.java` enum:
```java
public enum RoleName {
    ROLE_ADMIN,
    ROLE_USER,
    ROLE_NEW_ROLE  // Add here
}
```
2. Or insert directly:
```sql
INSERT INTO roles (name, description) 
VALUES ('ROLE_NEW_ROLE', 'Description');
```

---

## 🐛 Troubleshooting

### Q29: Service won't start - "Port 8081 already in use"
**A:** Either:
1. Kill the process using port 8081:
```bash
# Windows
netstat -ano | findstr :8081
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8081 | xargs kill
```
2. Or change the port in `application.yml`

### Q30: "Cannot create PoolableConnectionFactory" error
**A:** MySQL is not running or credentials are wrong. Check:
```bash
mysql -u root -p
```
If fails, start MySQL service.

### Q31: Build fails with "Cannot resolve symbol"
**A:** 
```bash
# Clean and rebuild
mvn clean install -U

# If still fails, check Java version
java -version  # Must be 17+
```

### Q32: Swagger UI shows 404
**A:** Check:
1. Service is running: http://localhost:8081/api/auth/health
2. Correct URL: http://localhost:8081/api/auth/swagger-ui.html
3. Context path is `/api/auth` (not just `/`)

### Q33: "Failed to publish login event" in logs
**A:** Kafka is not running. Either:
1. Install and start Kafka (optional)
2. Ignore the warning - service works without Kafka
3. Disable Kafka in code (comment out `@Autowired LoginEventPublisher`)

### Q34: Auto-logout not working
**A:** Check:
1. `@EnableScheduling` is present in `LoginServiceApplication.java`
2. Wait at least 5 minutes + 1 minute (for cron job)
3. Check logs for "Auto-logout: Found X expired sessions"
4. Verify session idle-timeout in `application.yml`

---

## 🚀 Performance & Scaling

### Q35: How many concurrent users can the service handle?
**A:** Depends on:
- Server CPU/RAM
- Database connection pool size
- JWT validation is fast (stateless)
- Default connection pool: 10

Increase in `application.yml`:
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
```

### Q36: How do I add caching to reduce database calls?
**A:** Add Spring Cache:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```
Then use `@Cacheable` on service methods.

### Q37: Should I deploy one instance or multiple?
**A:** For production:
- Multiple instances behind a load balancer
- JWT is stateless, so no session stickiness needed
- Share the same MySQL database
- Share the same Kafka cluster

### Q38: How do I add rate limiting to prevent brute force?
**A:** Use Spring Boot Rate Limiter or implement custom:
```java
@GetMapping("/login")
@RateLimiter(name = "loginLimiter", fallbackMethod = "rateLimitFallback")
public ResponseEntity<?> login(...) { ... }
```

---

## 📦 Deployment

### Q39: How do I create a production-ready JAR?
**A:**
```bash
cd login-service
mvn clean package -DskipTests
```
JAR is in `target/login-service-0.0.1-SNAPSHOT.jar`

### Q40: How do I run the JAR in production?
**A:**
```bash
java -jar login-service-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --server.port=8081
```

### Q41: How do I use different configs for dev/prod?
**A:** Create multiple application files:
- `application.yml` (default)
- `application-dev.yml` (development)
- `application-prod.yml` (production)

Run with:
```bash
java -jar app.jar --spring.profiles.active=prod
```

### Q42: How do I Dockerize the service?
**A:** Create `Dockerfile`:
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/login-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```
Build and run:
```bash
docker build -t login-service .
docker run -p 8081:8081 login-service
```

### Q43: How do I monitor the service in production?
**A:** Add Spring Boot Actuator:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```
Access: http://localhost:8081/actuator/health

---

## 🔄 Common Customizations

### Q44: How do I add email verification on registration?
**A:** You'd need to:
1. Add `emailVerified` field to User entity
2. Generate verification token
3. Send email with verification link
4. Create `/verify-email/{token}` endpoint
5. Mark user as verified

### Q45: How do I add "Forgot Password" functionality?
**A:** Steps:
1. Create password reset token entity
2. Add `/forgot-password` endpoint (accepts email)
3. Generate reset token, save to DB
4. Send email with reset link
5. Add `/reset-password/{token}` endpoint
6. Validate token and update password

### Q46: How do I add 2FA (Two-Factor Authentication)?
**A:** Implement:
1. Generate TOTP secret (use Google Authenticator library)
2. Store secret in user table
3. After password validation, require TOTP code
4. Validate TOTP before issuing JWT

### Q47: How do I add OAuth2 login (Google, Facebook)?
**A:** Add Spring Security OAuth2:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```
Configure in `application.yml`.

---

## 🧪 Testing

### Q48: How do I run the tests?
**A:**
```bash
mvn test
```

### Q49: How do I add integration tests?
**A:** Use `@SpringBootTest`:
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AuthControllerIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void testLogin() {
        // Test here
    }
}
```

### Q50: How do I test with an in-memory database (H2)?
**A:** Add to `pom.xml`:
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```
Create `application-test.yml`:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
```

---

**Need more help?**
- Check the logs in console
- Review `application.yml` settings
- Consult the README.md
- Check Swagger UI for API details

**Still stuck?** Create a GitHub issue with:
1. Error message
2. Steps to reproduce
3. Environment (OS, Java version, MySQL version)
4. Relevant logs
