# ✅ Login Service - Testing Checklist

Use this checklist to verify that everything is working correctly.

## 🔧 Pre-Deployment Checklist

### Environment Setup
- [ ] Java 17 or higher installed (`java -version`)
- [ ] Maven 3.6+ installed (`mvn -version`)
- [ ] MySQL 8.0+ installed and running
- [ ] Database `login_db` created
- [ ] MySQL credentials updated in `application.yml` (if not using root/root)

### Optional (Kafka)
- [ ] Apache Kafka installed (optional)
- [ ] Zookeeper running (optional)
- [ ] Kafka server running (optional)

---

## 🏗️ Build & Start

### Build Process
- [ ] Navigate to `credexa` folder
- [ ] Run `mvn clean install` (parent project)
- [ ] Build succeeds without errors
- [ ] `common-lib` JAR created
- [ ] `login-service` JAR created

### Start Service
- [ ] Navigate to `login-service` folder
- [ ] Run `mvn spring-boot:run` OR use `build-and-run.bat`
- [ ] Service starts on port 8081
- [ ] No errors in console
- [ ] See "Default Admin User Created" message in logs
- [ ] Database tables auto-created (check MySQL)

---

## 🌐 Basic Connectivity

### Health Checks
- [ ] Open browser: `http://localhost:8081/api/auth/health`
- [ ] Response: `{"success":true,"data":"Login Service is running"}`
- [ ] Swagger UI loads: `http://localhost:8081/api/auth/swagger-ui.html`
- [ ] All endpoints visible in Swagger

---

## 📝 Database Verification

### Check Tables Created
```sql
USE login_db;
SHOW TABLES;
```
Expected tables:
- [ ] `users`
- [ ] `roles`
- [ ] `user_roles`
- [ ] `user_sessions`
- [ ] `audit_logs`
- [ ] `bank_configuration`

### Check Default Data
```sql
-- Should have 6 roles
SELECT * FROM roles;

-- Should have 1 admin user
SELECT username, email, mobile_number FROM users;

-- Should have 1 bank config
SELECT * FROM bank_configuration;
```

---

## 🧪 Functional Testing

### Test 1: Get Bank Configuration
- [ ] Open Swagger UI
- [ ] Try `GET /bank-config`
- [ ] Response contains bank name "Credexa Bank"
- [ ] Default language is "en"
- [ ] Default currency is "USD"

### Test 2: Login with Admin
**Using Swagger:**
- [ ] Open `POST /login` endpoint
- [ ] Enter credentials:
  ```json
  {
    "usernameOrEmailOrMobile": "admin",
    "password": "Admin@123"
  }
  ```
- [ ] Click "Execute"
- [ ] Response status: 200 OK
- [ ] Response contains JWT token
- [ ] Response contains user details
- [ ] Response contains roles: ["ROLE_ADMIN"]
- [ ] Copy the JWT token for next tests

**Check Database:**
```sql
-- Last login should be updated
SELECT username, last_login FROM users WHERE username = 'admin';

-- Audit log should have LOGIN_SUCCESS
SELECT * FROM audit_logs ORDER BY event_time DESC LIMIT 5;

-- Active session should exist
SELECT * FROM user_sessions WHERE is_active = TRUE;
```

### Test 3: Register New User
**Using Swagger:**
- [ ] Open `POST /register` endpoint
- [ ] Enter user data:
  ```json
  {
    "username": "testuser",
    "password": "Test@123",
    "email": "test@example.com",
    "mobileNumber": "9876543210",
    "preferredLanguage": "en",
    "preferredCurrency": "USD"
  }
  ```
- [ ] Click "Execute"
- [ ] Response status: 201 Created
- [ ] Response contains user details (password should be null)
- [ ] User has ROLE_USER

**Check Database:**
```sql
-- New user should exist
SELECT * FROM users WHERE username = 'testuser';

-- Password should be BCrypt hashed (starts with $2a$ or $2b$)
SELECT password FROM users WHERE username = 'testuser';

-- User should have ROLE_USER
SELECT u.username, r.name 
FROM users u 
JOIN user_roles ur ON u.id = ur.user_id 
JOIN roles r ON ur.role_id = r.id 
WHERE u.username = 'testuser';
```

### Test 4: Login with New User
- [ ] Use `POST /login` with testuser credentials
- [ ] Login succeeds
- [ ] JWT token received
- [ ] Last login updated in database

### Test 5: Validate Token
- [ ] Use `POST /validate-token`
- [ ] Paste JWT token (in quotes): `"your-token-here"`
- [ ] Response: `{"valid": true, "username": "...", ...}`

### Test 6: Protected Endpoint (Logout)
**Using Swagger:**
- [ ] Click "Authorize" button (🔓 icon, top right)
- [ ] Enter: `Bearer YOUR_JWT_TOKEN`
- [ ] Click "Authorize"
- [ ] Try `POST /logout`
- [ ] Response: "Logout successful"

**Check Database:**
```sql
-- Session should be inactive
SELECT is_active, logout_time FROM user_sessions 
WHERE session_token = 'your-token';

-- Audit log should have LOGOUT event
SELECT * FROM audit_logs 
WHERE event_type = 'LOGOUT' 
ORDER BY event_time DESC LIMIT 1;
```

### Test 7: Token After Logout
- [ ] Try to validate the token you just logged out
- [ ] Session should be marked inactive
- [ ] User would need to login again

### Test 8: Failed Login Attempts
**Test Account Locking:**
- [ ] Try to login with wrong password 5 times
- [ ] 6th attempt should return "Account is locked"

**Check Database:**
```sql
SELECT username, failed_login_attempts, account_locked 
FROM users WHERE username = 'testuser';
```

**Reset (for testing):**
```sql
UPDATE users 
SET failed_login_attempts = 0, account_locked = FALSE 
WHERE username = 'testuser';
```

### Test 9: Auto-Logout (5 minutes idle)
**This test takes 5 minutes:**
- [ ] Login with a user
- [ ] Wait 5 minutes without any activity
- [ ] Run scheduled task (or wait for cron job - runs every minute)
- [ ] Check database - session should be inactive
- [ ] Audit log should have AUTO_LOGOUT event

**Manual Check:**
```sql
-- Check session last activity
SELECT user_id, last_activity, is_active 
FROM user_sessions 
WHERE is_active = TRUE;

-- Should auto-logout sessions older than 5 minutes
```

### Test 10: Validation Errors
**Test required fields:**
- [ ] Try register without username → Error: "Username is required"
- [ ] Try register without email → Error: "Email is required"
- [ ] Try register with short password → Error: "Password must be at least 8 characters"
- [ ] Try register with invalid email → Error: "Email should be valid"

**Test uniqueness:**
- [ ] Try register with existing username → Error: "Username already exists"
- [ ] Try register with existing email → Error: "Email already exists"
- [ ] Try register with existing mobile → Error: "Mobile number already exists"

---

## 🔐 Security Testing

### BCrypt Password Hashing
- [ ] Check password in database (should be hashed)
- [ ] Hash should start with `$2a$` or `$2b$`
- [ ] Password should be ~60 characters long

```sql
SELECT username, password, LENGTH(password) as pwd_length 
FROM users;
```

### JWT Token Structure
- [ ] Token has 3 parts separated by dots (header.payload.signature)
- [ ] Can decode header and payload (Base64)
- [ ] Contains username in `sub` claim
- [ ] Contains roles in `roles` claim
- [ ] Has `iat` (issued at) and `exp` (expiration) timestamps

**Decode at:** https://jwt.io

### Session Security
- [ ] Each login creates new session with unique token
- [ ] Old sessions are marked inactive on logout
- [ ] Session contains IP address and User-Agent

---

## 📊 Audit Logging

### Check Audit Logs
```sql
-- All login attempts
SELECT username, event_type, success, message, event_time 
FROM audit_logs 
WHERE event_type LIKE '%LOGIN%' 
ORDER BY event_time DESC;

-- Failed login attempts
SELECT username, COUNT(*) as failed_attempts 
FROM audit_logs 
WHERE event_type = 'LOGIN_FAILURE' 
GROUP BY username;
```

Expected event types:
- [ ] USER_REGISTERED
- [ ] LOGIN_SUCCESS
- [ ] LOGIN_FAILURE
- [ ] LOGOUT
- [ ] AUTO_LOGOUT
- [ ] ACCOUNT_LOCKED

---

## 📡 Kafka Testing (If Kafka is running)

### Check Kafka Topics
```bash
# List topics
kafka-topics.bat --list --bootstrap-server localhost:9092

# Should see: login-events
```

### Consume Events
```bash
kafka-console-consumer.bat --bootstrap-server localhost:9092 \
  --topic login-events --from-beginning
```

Expected events after login:
- [ ] Event with `eventType: "LOGIN_SUCCESS"`
- [ ] Contains username, userId, timestamp
- [ ] Contains IP address and User-Agent

---

## 🧰 Postman Testing

### Import Collection
- [ ] Open Postman
- [ ] Import `Credexa-Login-Service.postman_collection.json`
- [ ] 7 requests imported

### Run Collection
- [ ] 1. Health Check → 200 OK
- [ ] 2. Get Bank Config → 200 OK
- [ ] 3. Register New User → 201 Created
- [ ] 4. Login with Admin → 200 OK (save token)
- [ ] 5. Login with User → 200 OK (save token)
- [ ] 6. Validate Token → 200 OK (paste token from step 4 or 5)
- [ ] 7. Logout → 200 OK (use token in Authorization header)

---

## 🐛 Error Handling

### Test Exception Handling
- [ ] Invalid JSON → 400 Bad Request
- [ ] Missing required field → 400 Bad Request with validation message
- [ ] Invalid credentials → 401 Unauthorized
- [ ] Locked account → 401 Unauthorized with "Account is locked" message
- [ ] Invalid token → 401 Unauthorized
- [ ] Expired token → 401 Unauthorized

---

## 📱 Multi-Language & Currency

### Test Language Preference
- [ ] Register user with `preferredLanguage: "hi"` (Hindi)
- [ ] Check database - should be saved
- [ ] Login response should include preferred language

### Test Currency Preference
- [ ] Register user with `preferredCurrency: "INR"`
- [ ] Check database - should be saved
- [ ] Login response should include preferred currency

### Supported Currencies
- [ ] USD - 2 decimals
- [ ] EUR - 2 decimals
- [ ] INR - 2 decimals
- [ ] JPY - 0 decimals
- [ ] BHD - 3 decimals

---

## 🔍 PII Masking (Utility Testing)

**Note:** PII masking is a utility in common-lib. Will be used by other services.

Test in Java code or create a simple controller:
```java
String email = "john.doe@example.com";
String masked = maskingUtil.maskEmail(email);
// Result: j***.d**@example.com
```

---

## 📈 Performance Testing (Optional)

### Load Test
- [ ] Use JMeter or Postman Runner
- [ ] Send 100 concurrent login requests
- [ ] All should succeed
- [ ] Response time < 500ms
- [ ] No database connection errors

---

## ✅ Final Verification

### Code Quality
- [ ] No compilation errors
- [ ] No runtime errors in logs
- [ ] All dependencies resolved
- [ ] Tests pass: `mvn test`

### Documentation
- [ ] README.md is complete
- [ ] QUICKSTART.md is clear
- [ ] IMPLEMENTATION_SUMMARY.md is accurate
- [ ] Swagger documentation is accessible

### Deployment Ready
- [ ] Application starts successfully
- [ ] Can login with admin user
- [ ] Can register new users
- [ ] JWT authentication works
- [ ] Auto-logout works
- [ ] Database is properly configured

---

## 🎉 Completion Checklist

When all above tests pass:
- [ ] ✅ Login Service is FULLY FUNCTIONAL
- [ ] ✅ All requirements from specification are met
- [ ] ✅ Ready for integration with other services
- [ ] ✅ Ready to move to next module

---

## 📝 Next Steps

After completing all tests above:
1. ✅ Mark Login Service as COMPLETE
2. 🔄 Provide details for **Customer Service**
3. 🔄 Provide details for **Product & Pricing Service**
4. 🔄 Provide details for **FD Calculator Service**
5. 🔄 Provide details for **FD Account Service**

---

**Need Help?**
- Check logs in console
- Check `login_db` database
- Review `application.yml` configuration
- Check MySQL is running
- Check port 8081 is not in use

**Report Issues:**
- Include error message
- Include request/response
- Include relevant logs
