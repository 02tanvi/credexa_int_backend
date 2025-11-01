# Customer Service - Complete Testing Guide with Security

## 🎯 Important: Correct Flow Understanding

**The proper microservice flow is:**
1. **Register** in login-service → Get userId back in response
2. **Login** to login-service → Get JWT token (contains username and roles)
3. **Create customer profile** in customer-service → userId is automatically fetched from login-service using your JWT username
4. **All subsequent operations** use JWT token for authentication

**Key Security Features:**
- ✅ userId is NOT sent in request - it's derived from JWT token via login-service API call
- ✅ Regular users can only create ONE customer profile (checked via username)
- ✅ Regular users can only update THEIR OWN profile
- ✅ Admins can create/update ANY profile
- ✅ Username from JWT is stored in customer record for ownership validation

---

## Prerequisites

1. **MySQL** running on localhost:3306
2. **login-service** running on port 8082
3. **customer-service** running on port 8083
4. Both databases cleared (see cleanup commands below)

---

## 📋 Cleanup Commands (Run First)

```sql
-- Clear login-service database
DELETE FROM login_db.user_sessions;
DELETE FROM login_db.user_roles;
DELETE FROM login_db.audit_logs;
DELETE FROM login_db.users;
ALTER TABLE login_db.users AUTO_INCREMENT = 1;

-- Clear customer-service database
DELETE FROM customer_db.customers;
ALTER TABLE customer_db.customers AUTO_INCREMENT = 1;
```

---

## 🧪 Test Scenario 1: Regular User - John Doe

### Step 1.1: Register John Doe
```http
POST http://localhost:8082/api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john.doe@example.com",
  "password": "Password123!",
  "mobileNumber": "9876543210"
}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": 1,  // ← Save this userId
    "username": "john_doe",
    "email": "john.doe@example.com",
    "mobileNumber": "9876543210"
    // password removed from response
  }
}
```

---

### Step 1.2: Login as John Doe
```http
POST http://localhost:8082/api/auth/login
Content-Type: application/json

{
  "usernameOrEmailOrMobile": "john_doe",
  "password": "Password123!"
}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",  // ← Save as JOHN_TOKEN
    "username": "john_doe",
    "roles": ["ROLE_USER"]
  }
}
```

---

### Step 1.3: ✅ John Creates His Customer Profile (SHOULD SUCCEED)

**NOTICE:** No `userId` field in the request! It's automatically fetched.

```http
POST http://localhost:8083/api/customer/customers
Authorization: Bearer {JOHN_TOKEN}
Content-Type: application/json

{
  "fullName": "John Doe",
  "mobileNumber": "9876543210",
  "email": "john.doe@example.com",
  "dateOfBirth": "1990-05-15",
  "gender": "MALE",
  "classification": "REGULAR",
  "addressLine1": "123 Main Street",
  "addressLine2": "Apartment 4B",
  "city": "Mumbai",
  "state": "Maharashtra",
  "pincode": "400001",
  "country": "India",
  "panNumber": "ABCDE1234F",
  "aadharNumber": "123456789012",
  "accountNumber": "1234567890",
  "ifscCode": "HDFC0001234"
}
```

**Expected Response:**
```json
{
  "id": 1,
  "userId": 1,  // ← Automatically fetched from login-service
  "username": "john_doe",  // ← From JWT token
  "fullName": "John Doe",
  "mobileNumber": "9876543210",
  "email": "john.doe@example.com",
  "dateOfBirth": "1990-05-15",
  "gender": "MALE",
  "classification": "REGULAR",
  "kycStatus": "PENDING",
  // ... other fields
}
```

---

### Step 1.4: ❌ John Tries to Create SECOND Profile (SHOULD FAIL)
```http
POST http://localhost:8083/api/customer/customers
Authorization: Bearer {JOHN_TOKEN}
Content-Type: application/json

{
  "fullName": "John Doe Second Profile",
  "mobileNumber": "9999999999",
  "email": "john.second@example.com",
  "dateOfBirth": "1990-05-15",
  "gender": "MALE",
  "classification": "REGULAR",
  "addressLine1": "456 Another Street",
  "city": "Mumbai",
  "state": "Maharashtra",
  "pincode": "400002",
  "country": "India"
}
```

**Expected Error:**
```json
{
  "error": "You already have a customer profile. Each user can only have one profile.",
  "timestamp": "2025-10-18T18:30:00"
}
```

---

### Step 1.5: ✅ John Updates His Own Profile (SHOULD SUCCEED)
```http
PUT http://localhost:8083/api/customer/customers/1
Authorization: Bearer {JOHN_TOKEN}
Content-Type: application/json

{
  "fullName": "John Doe Updated",
  "addressLine2": "Apartment 5B - Updated",
  "classification": "PREMIUM"
}
```

**Expected Response:** Updated customer with new values

---

## 🧪 Test Scenario 2: Another Regular User - Jane Smith

### Step 2.1: Register Jane Smith
```http
POST http://localhost:8082/api/auth/register
Content-Type: application/json

{
  "username": "jane_smith",
  "email": "jane.smith@example.com",
  "password": "Password123!",
  "mobileNumber": "9876543211"
}
```

**Expected:** userId: 2

---

### Step 2.2: Login as Jane Smith
```http
POST http://localhost:8082/api/auth/login
Content-Type: application/json

{
  "usernameOrEmailOrMobile": "jane_smith",
  "password": "Password123!"
}
```

**Save token as:** JANE_TOKEN

---

### Step 2.3: ✅ Jane Creates Her Profile - Testing Auto-Classification
```http
POST http://localhost:8083/api/customer/customers
Authorization: Bearer {JANE_TOKEN}
Content-Type: application/json

{
  "fullName": "Jane Smith",
  "mobileNumber": "9876543211",
  "email": "jane.smith@example.com",
  "dateOfBirth": "1942-07-18",
  "gender": "FEMALE",
  "classification": "REGULAR",
  "addressLine1": "456 Park Avenue",
  "city": "Delhi",
  "state": "Delhi",
  "pincode": "110001",
  "country": "India",
  "panNumber": "FGHIJ5678K",
  "aadharNumber": "987654321098"
}
```

**Expected Response:**
- Classification should be **"SUPER_SENIOR"** (not REGULAR)
- Age is 83 (born 1942), which triggers auto-classification
- userId: 2, username: "jane_smith"

---

### Step 2.4: ❌ Jane Tries to Update John's Profile (SHOULD FAIL)
```http
PUT http://localhost:8083/api/customer/customers/1
Authorization: Bearer {JANE_TOKEN}
Content-Type: application/json

{
  "fullName": "Hacked by Jane"
}
```

**Expected Error:**
```json
{
  "error": "You can only update your own customer profile. Admin access required to update other profiles.",
  "timestamp": "2025-10-18T18:35:00"
}
```
**Status Code:** 403 Forbidden

---

## 🧪 Test Scenario 3: Admin User

### Step 3.1: Register Admin User
```http
POST http://localhost:8082/api/auth/register
Content-Type: application/json

{
  "username": "admin1",
  "email": "admin1@credexa.com",
  "password": "AdminPass123!",
  "mobileNumber": "9876543212"
}
```

**Expected:** userId: 3

---

### Step 3.2: Promote to Admin (Run in MySQL)
```sql
-- First check role IDs
SELECT * FROM login_db.roles;

-- Assuming ROLE_ADMIN has id=2, add admin role
INSERT INTO login_db.user_roles (user_id, role_id) VALUES (3, 2);

-- Verify
SELECT u.id, u.username, r.name as role
FROM login_db.users u
JOIN login_db.user_roles ur ON u.id = ur.user_id
JOIN login_db.roles r ON ur.role_id = r.id
WHERE u.username = 'admin1';
```

---

### Step 3.3: Login as Admin
```http
POST http://localhost:8082/api/auth/login
Content-Type: application/json

{
  "usernameOrEmailOrMobile": "admin1",
  "password": "AdminPass123!"
}
```

**Expected Response:** Token with roles: ["ROLE_USER", "ROLE_ADMIN"]
**Save token as:** ADMIN_TOKEN

---

### Step 3.4: ✅ Admin Creates Profile for Another User (SHOULD SUCCEED)

Admin can create profiles and it will use their own username but can create multiple profiles.

```http
POST http://localhost:8083/api/customer/customers
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "fullName": "Robert Johnson",
  "mobileNumber": "9876543213",
  "email": "robert.johnson@example.com",
  "dateOfBirth": "1985-12-10",
  "gender": "MALE",
  "classification": "PREMIUM",
  "addressLine1": "789 Business District",
  "city": "Bangalore",
  "state": "Karnataka",
  "pincode": "560001",
  "country": "India",
  "panNumber": "KLMNO9012P",
  "aadharNumber": "456789123456"
}
```

**Expected Response:**
- userId: 3 (admin's own userId from login-service)
- username: "admin_user"
- Customer created successfully

**Note:** With current implementation, admin creates profiles linked to their own userId. If you want admins to create profiles for OTHER userIds, we need to add that userId back to the request but with admin-only validation.

---

### Step 3.5: ✅ Admin Updates Jane's Profile (SHOULD SUCCEED)
```http
PUT http://localhost:8083/api/customer/customers/2
Authorization: Bearer {ADMIN_TOKEN}
Content-Type: application/json

{
  "classification": "VIP",
  "addressLine2": "Updated by Admin",
  "kycStatus": "VERIFIED"
}
```

**Expected:** Success - Admin can update any profile

---

## 📊 Verify Database State

```sql
-- Check all customers
SELECT 
    c.id, 
    c.user_id, 
    c.username, 
    c.full_name, 
    c.email, 
    c.classification,
    c.kyc_status
FROM customer_db.customers c
ORDER BY c.id;
```

**Expected Results:**
```
+----+---------+------------+--------------------+---------------------------+-----------------+------------+
| id | user_id | username   | full_name          | email                     | classification  | kyc_status |
+----+---------+------------+--------------------+---------------------------+-----------------+------------+
|  1 |       1 | john_doe   | John Doe Updated   | john.doe@example.com      | PREMIUM         | PENDING    |
|  2 |       2 | jane_smith | Jane Smith         | jane.smith@example.com    | SUPER_SENIOR    | VERIFIED   |
|  3 |       3 | admin_user | Robert Johnson     | robert.johnson@...        | PREMIUM         | PENDING    |
+----+---------+------------+--------------------+---------------------------+-----------------+------------+
```

---

## 🔍 Test Other Endpoints

### Get Customer by ID
```http
GET http://localhost:8083/api/customer/customers/1
Authorization: Bearer {JOHN_TOKEN}
```

### Get Customer by User ID
```http
GET http://localhost:8083/api/customer/customers/user/1
Authorization: Bearer {JOHN_TOKEN}
```

### Get Customer Classification
```http
GET http://localhost:8083/api/customer/customers/2/classification
Authorization: Bearer {JANE_TOKEN}
```

**Expected for SUPER_SENIOR:**
```json
{
  "customerId": 2,
  "classification": "SUPER_SENIOR",
  "additionalInterestRate": 0.75,
  "benefits": [
    "Enhanced interest rates on all FD products",
    "Premium customer service",
    "Dedicated relationship manager",
    "Super senior citizen benefits (80+ years)"
  ]
}
```

### Get Customer 360° View
```http
GET http://localhost:8083/api/customer/customers/1/360-view
Authorization: Bearer {JOHN_TOKEN}
```

---

## 🎯 Auto-Classification Test Cases

| Age Range | DOB Example    | Expected Classification |
|-----------|----------------|------------------------|
| < 60      | 2000-01-01     | REGULAR/PREMIUM/VIP    |
| 60-79     | 1960-01-01     | SENIOR_CITIZEN         |
| 80+       | 1942-07-18     | SUPER_SENIOR           |

**To test:** Create customers with different DOBs and verify classification is automatically set correctly.

---

## 🔒 Security Summary

### ✅ What's Working:
1. **userId automatically fetched** from login-service using JWT username
2. **No userId in request** - eliminates security risk of users specifying wrong userId
3. **Username stored** in customer record for ownership validation
4. **Regular users:** Can only create ONE profile, can only update THEIR OWN profile
5. **Admin users:** Can create multiple profiles (linked to their userId), can update ANY profile
6. **Microservice communication:** customer-service → login-service API call

### 🔑 Key Security Checks:
- `existsByUsername()` - Prevents duplicate profiles for regular users
- `customer.getUsername().equals(authenticatedUsername)` - Validates ownership for updates
- `isAdmin` flag - Bypasses restrictions for admin users
- JWT validation - All requests require valid JWT token

---

## 🐛 Troubleshooting

### Error: "Unable to retrieve user information from login-service"
**Cause:** login-service not running or endpoint not accessible
**Fix:** Ensure login-service is running on port 8082

### Error: "You already have a customer profile"
**Cause:** Regular user trying to create second profile
**Solution:** Each regular user can only have one profile

### Error: "You can only update your own customer profile"
**Cause:** Regular user trying to update someone else's profile
**Solution:** Login as profile owner or use admin account

### Auto-classification not working
**Check logs for:** `"Auto-classifying customer as SUPER_SENIOR (age: XX)"`
**Verify:** Age calculation is correct

---

## 📝 Notes

1. **userId is derived from JWT token** - customer-service calls `GET /api/auth/user/{username}` on login-service
2. **Each regular user = 1 profile** - Enforced via username uniqueness check
3. **Admin behavior:** Currently, admins create profiles linked to their own userId. If you want admins to create profiles for OTHER users, we need to modify the flow.
4. **Security is username-based** - The username from JWT is the source of truth for ownership

