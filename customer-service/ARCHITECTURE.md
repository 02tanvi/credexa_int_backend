# Customer Service - Security Architecture Summary

## 🏗️ Architecture Overview

### Microservices Communication Flow

```
┌─────────────┐         ┌──────────────────┐         ┌──────────────────┐
│   Client    │         │  login-service   │         │ customer-service │
│  (Browser)  │         │   Port: 8082     │         │   Port: 8083     │
└──────┬──────┘         └────────┬─────────┘         └────────┬─────────┘
       │                         │                            │
       │ 1. POST /register       │                            │
       │─────────────────────────>                            │
       │                         │                            │
       │ 2. Response: userId=1   │                            │
       │<─────────────────────────                            │
       │                         │                            │
       │ 3. POST /login          │                            │
       │─────────────────────────>                            │
       │                         │                            │
       │ 4. JWT Token            │                            │
       │    (username, roles)    │                            │
       │<─────────────────────────                            │
       │                         │                            │
       │ 5. POST /customers      │                            │
       │    (JWT in header)      │                            │
       │─────────────────────────────────────────────────────>│
       │                         │                            │
       │                         │ 6. GET /user/{username}    │
       │                         │<───────────────────────────│
       │                         │                            │
       │                         │ 7. User info (userId)      │
       │                         │────────────────────────────>│
       │                         │                            │
       │                         │    8. Create customer      │
       │                         │       userId=1 (from API)  │
       │                         │       username=john_doe    │
       │                         │                            │
       │ 9. Customer created     │                            │
       │<─────────────────────────────────────────────────────│
       │                         │                            │
```

---

## 🔐 Security Implementation

### Option B: API Call to Login-Service (IMPLEMENTED)

**How it works:**
1. User logs in → Gets JWT with **username** (not userId)
2. User creates customer profile → Sends JWT token in Authorization header
3. customer-service extracts **username** from JWT
4. customer-service calls login-service: `GET /api/auth/user/{username}`
5. login-service returns user info including **userId**
6. customer-service uses that userId to create customer record
7. customer-service stores **username** in customer table for future ownership checks

### Benefits:
✅ User cannot manipulate userId in request (it's not in the request!)
✅ Single source of truth for userId (login-service)
✅ Microservice communication established
✅ Username stored for fast ownership validation

### Trade-offs:
⚠️ Adds latency (API call to login-service on every customer creation)
⚠️ Dependency on login-service being available
⚠️ Network call overhead

---

## 🗄️ Database Schema Changes

### Customer Table - New Fields

```sql
-- Added username column
ALTER TABLE customers
ADD COLUMN username VARCHAR(50) NOT NULL,
ADD CONSTRAINT unique_username UNIQUE (username);
```

**Before:**
- Only had `userId` (FK to login-service)
- No way to validate ownership without calling login-service

**After:**
- Has both `userId` AND `username`
- Fast ownership validation: `customer.getUsername() == jwt.username`
- Unique constraint prevents duplicate usernames

---

## 📋 DTO Changes

### CreateCustomerRequest - Field Removed

**BEFORE (❌ Security Risk):**
```java
public class CreateCustomerRequest {
    @NotNull
    private Long userId;  // ❌ User could specify any userId!
    
    private String fullName;
    private String email;
    // ...
}
```

**AFTER (✅ Secure):**
```java
public class CreateCustomerRequest {
    // userId REMOVED - fetched from login-service automatically
    
    private String fullName;
    private String email;
    // ...
}
```

---

## 🔧 New Components

### 1. LoginServiceClient
**Location:** `com.app.customer.client.LoginServiceClient`

**Purpose:** REST client to communicate with login-service

**Key Method:**
```java
public Long getUserIdByUsername(String username) {
    String url = loginServiceUrl + "/user/" + username;
    UserInfoResponse response = restTemplate.getForObject(url, UserInfoResponse.class);
    return response.getUserId();
}
```

**Configuration:**
```yaml
login-service:
  url: http://localhost:8082/api/auth
```

---

### 2. RestClientConfig
**Location:** `com.app.customer.config.RestClientConfig`

**Purpose:** Provides RestTemplate bean for HTTP calls

```java
@Configuration
public class RestClientConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

---

### 3. AuthController - New Endpoint (Login-Service)
**Location:** `com.app.login.controller.AuthController`

**New Endpoint:**
```java
@GetMapping("/user/{username}")
public ResponseEntity<ApiResponse<User>> getUserByUsername(@PathVariable String username) {
    User user = authService.getUserByUsername(username);
    user.setPassword(null); // Remove sensitive data
    return ResponseEntity.ok(ApiResponse.success("User found", user));
}
```

**URL:** `GET http://localhost:8082/api/auth/user/{username}`

**Response:**
```json
{
  "success": true,
  "message": "User found",
  "data": {
    "id": 1,
    "username": "john_doe",
    "email": "john.doe@example.com",
    "fullName": "John Doe",
    "mobileNumber": "9876543210"
  }
}
```

---

## 🔄 Service Flow Changes

### CustomerService.createCustomer()

**OLD FLOW (❌):**
```java
public CustomerResponse createCustomer(CreateCustomerRequest request) {
    Long userId = request.getUserId(); // ❌ Trusting client input!
    
    if (customerRepository.existsByUserId(userId)) {
        throw new DuplicateCustomerException();
    }
    
    Customer customer = Customer.builder()
        .userId(userId)
        .fullName(request.getFullName())
        // ...
        .build();
}
```

**NEW FLOW (✅):**
```java
public CustomerResponse createCustomer(
        CreateCustomerRequest request, 
        String authenticatedUsername, 
        boolean isAdmin) {
    
    // 1. Get userId from login-service
    Long userId = loginServiceClient.getUserIdByUsername(authenticatedUsername);
    
    // 2. Check if user already has profile
    if (!isAdmin && customerRepository.existsByUsername(authenticatedUsername)) {
        throw new DuplicateCustomerException("You already have a profile");
    }
    
    // 3. Check for duplicate userId
    if (customerRepository.existsByUserId(userId)) {
        throw new DuplicateCustomerException("Profile exists for this user");
    }
    
    // 4. Build customer with verified userId and username
    Customer customer = Customer.builder()
        .userId(userId)  // ✅ From login-service
        .username(authenticatedUsername)  // ✅ From JWT
        .fullName(request.getFullName())
        // ...
        .build();
}
```

---

## 🛡️ Security Validations

### 1. Create Customer Security

```java
// Regular users: Can only create ONE profile
if (!isAdmin && customerRepository.existsByUsername(authenticatedUsername)) {
    throw new DuplicateCustomerException("You already have a customer profile");
}
```

**Check:** `existsByUsername(authenticatedUsername)`
**Result:** Regular user cannot create multiple profiles

---

### 2. Update Customer Security

```java
// Regular users: Can only update THEIR OWN profile
if (!isAdmin && !customer.getUsername().equals(authenticatedUsername)) {
    throw new UnauthorizedAccessException(
        "You can only update your own customer profile"
    );
}
```

**Check:** `customer.getUsername().equals(authenticatedUsername)`
**Result:** Regular user cannot update other users' profiles

---

### 3. Admin Bypass

```java
boolean isAdmin = authentication.getAuthorities().stream()
    .map(GrantedAuthority::getAuthority)
    .anyMatch(role -> role.equals("ROLE_ADMIN"));

if (isAdmin) {
    // Skip regular user restrictions
}
```

**Check:** JWT contains `ROLE_ADMIN`
**Result:** Admin users can create/update any profile

---

## 📊 Data Flow Diagram

### Customer Creation Flow

```
┌──────────────────────────────────────────────────────────────────────┐
│                         CLIENT REQUEST                                │
│  POST /api/customer/customers                                         │
│  Authorization: Bearer eyJhbGci...                                    │
│  Body: { fullName, email, ... } // ❌ No userId!                     │
└───────────────────────────────┬──────────────────────────────────────┘
                                │
                                ▼
┌──────────────────────────────────────────────────────────────────────┐
│                    CUSTOMER CONTROLLER                                │
│  1. Extract username from JWT: "john_doe"                            │
│  2. Check if user is admin: isAdmin = false                          │
│  3. Call service.createCustomer(request, "john_doe", false)          │
└───────────────────────────────┬──────────────────────────────────────┘
                                │
                                ▼
┌──────────────────────────────────────────────────────────────────────┐
│                     CUSTOMER SERVICE                                  │
│  4. Call loginServiceClient.getUserIdByUsername("john_doe")          │
└───────────────────────────────┬──────────────────────────────────────┘
                                │
                                ▼
┌──────────────────────────────────────────────────────────────────────┐
│                   LOGIN SERVICE CLIENT                                │
│  5. HTTP GET: http://localhost:8082/api/auth/user/john_doe          │
└───────────────────────────────┬──────────────────────────────────────┘
                                │
                                ▼
┌──────────────────────────────────────────────────────────────────────┐
│                      LOGIN SERVICE                                    │
│  6. Query database: SELECT * FROM users WHERE username='john_doe'    │
│  7. Return: { id: 1, username: "john_doe", ... }                    │
└───────────────────────────────┬──────────────────────────────────────┘
                                │
                                ▼
┌──────────────────────────────────────────────────────────────────────┐
│                     CUSTOMER SERVICE                                  │
│  8. Received userId = 1                                              │
│  9. Check: existsByUsername("john_doe") → false                      │
│  10. Check: existsByUserId(1) → false                                │
│  11. Build customer:                                                  │
│      - userId = 1 (from login-service)                               │
│      - username = "john_doe" (from JWT)                              │
│      - fullName, email, etc. (from request)                          │
│  12. Save to database                                                 │
└───────────────────────────────┬──────────────────────────────────────┘
                                │
                                ▼
┌──────────────────────────────────────────────────────────────────────┐
│                        RESPONSE TO CLIENT                             │
│  {                                                                    │
│    "id": 1,                                                           │
│    "userId": 1,     // ✅ From login-service                         │
│    "username": "john_doe",  // ✅ From JWT                           │
│    "fullName": "John Doe",                                           │
│    ...                                                                │
│  }                                                                    │
└──────────────────────────────────────────────────────────────────────┘
```

---

## ✅ Security Checklist

- [x] userId removed from CreateCustomerRequest
- [x] userId fetched from login-service via API call
- [x] Username from JWT stored in customer table
- [x] Regular users can only create ONE profile (username check)
- [x] Regular users can only update THEIR OWN profile (username match)
- [x] Admin users can bypass restrictions
- [x] UnauthorizedAccessException handler returns 403 Forbidden
- [x] DuplicateCustomerException for duplicate profiles
- [x] Unique constraints on username and userId in database
- [x] Auto-classification based on age works correctly

---

## 🚀 Next Steps

1. **Test all scenarios** using the TESTING-GUIDE.md
2. **Monitor logs** for "Auto-classifying customer" messages
3. **Verify database** shows correct userId and username mapping
4. **Test error cases** (duplicate profiles, unauthorized updates)
5. **Performance test** the login-service API call overhead

---

## 📝 Configuration Files

### customer-service/application.yml
```yaml
login-service:
  url: http://localhost:8082/api/auth
```

### Required Beans
- `RestTemplate` - For HTTP calls to login-service
- `LoginServiceClient` - Client wrapper for login-service APIs

---

## 🔗 API Endpoints Summary

### Login-Service (Port 8082)
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT
- `GET /api/auth/user/{username}` - **NEW** Get user info by username

### Customer-Service (Port 8083)
- `POST /api/customer/customers` - Create customer (no userId in request!)
- `GET /api/customer/customers/{id}` - Get customer by ID
- `GET /api/customer/customers/user/{userId}` - Get customer by userId
- `PUT /api/customer/customers/{id}` - Update customer (ownership check)
- `GET /api/customer/customers/{id}/classification` - Get classification
- `GET /api/customer/customers/{id}/360-view` - Get 360° view

