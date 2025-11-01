# Product Pricing Service - Swagger Testing Guide

## 🎯 Quick Start

1. **Start the service:** `mvn spring-boot:run`
2. **Open Swagger UI:** http://localhost:8084/api/products/swagger-ui/index.html
3. **Default FD Products:** 6 products auto-loaded on startup (FD-STD-001, FD-SR-001, FD-TAX-001, FD-CUM-001, FD-NCUM-001, FD-FLEXI-001)

---

## 🔐 Authentication Setup

Most endpoints require JWT authentication. Get a token from login-service first:

### Get JWT Token (via login-service)
```bash
# 1. Register user (if needed)
POST http://localhost:8082/api/auth/register
{
  "username": "john_doe",
  "email": "john.doe@example.com",
  "password": "Password123!",
  "mobileNumber": "9876543210"
}

# 2. Login to get token
POST http://localhost:8082/api/auth/login
{
  "usernameOrEmailOrMobile": "john_doe",
  "password": "Password123!"
}

# 3. Copy the token from response
Response: { "data": { "token": "eyJhbGciOi..." } }

# 4. In Swagger UI, click "Authorize" button (top right)
# 5. Enter: Bearer eyJhbGciOi...
# 6. Click "Authorize" then "Close"
```

---

## 📦 Category 1: Products (Core CRUD)

### 1.1 List All Products
**Endpoint:** `GET /api/products`

**Swagger Steps:**
1. Expand **"Product Management"** section
2. Click **GET /api/products**
3. Click **"Try it out"**
4. Leave parameters default (page=0, size=10)
5. Click **"Execute"**

**Expected Response:**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Standard Fixed Deposit",
        "code": "FD-STD-001",
        "type": "FIXED_DEPOSIT",
        "description": "Regular fixed deposit with competitive interest rates",
        "minAmount": 10000,
        "maxAmount": 10000000,
        "minTermMonths": 6,
        "maxTermMonths": 60,
        "active": true,
        "status": "ACTIVE"
      },
      // ... 5 more seeded products
    ],
    "totalElements": 6
  }
}
```

---

### 1.2 Get Product by ID
**Endpoint:** `GET /api/products/{id}`

**Swagger Steps:**
1. Click **GET /api/products/{id}**
2. Click **"Try it out"**
3. Enter **id:** `1`
4. Click **"Execute"**

**Expected:** Single product details for FD-STD-001

---

### 1.3 Get Product by Code
**Endpoint:** `GET /api/products/code/{code}`

**Swagger Steps:**
1. Click **GET /api/products/code/{code}**
2. Click **"Try it out"**
3. Enter **code:** `FD-SR-001` (Senior Citizen FD)
4. Click **"Execute"**

**Expected:** Senior Citizen FD product details

---

### 1.4 Create New Product (Admin)
**Endpoint:** `POST /api/products`

**Swagger Steps:**
1. Ensure you're **Authorized** (Bearer token set)
2. Click **POST /api/products**
3. Click **"Try it out"**
4. Replace JSON with:

```json
{
  "productName": "Super Saver Fixed Deposit",
  "productCode": "FD-SUPER-001",
  "productType": "FIXED_DEPOSIT",
  "description": "High interest FD for super savers with excellent returns",
  "effectiveDate": "2025-01-01",
  "bankBranchCode": "BR001",
  "currencyCode": "INR",
  "status": "ACTIVE",
  "minTermMonths": 12,
  "maxTermMonths": 120,
  "minAmount": 50000,
  "maxAmount": 5000000,
  "baseInterestRate": 7.5,
  "interestCalculationMethod": "COMPOUND",
  "interestPayoutFrequency": "QUARTERLY",
  "prematureWithdrawalAllowed": true,
  "partialWithdrawalAllowed": false,
  "loanAgainstDepositAllowed": true,
  "autoRenewalAllowed": false,
  "nomineeAllowed": true,
  "jointAccountAllowed": true,
  "tdsApplicable": true,
  "tdsRate": 10.0
}
```

5. Click **"Execute"**

**Expected:** 201 Created with new product id (7 or higher)

**Required Fields:**
- `productName` - Product display name
- `productCode` - Unique product identifier
- `productType` - Enum: FIXED_DEPOSIT, RECURRING_DEPOSIT, etc.
- `effectiveDate` - Date format: YYYY-MM-DD
- `bankBranchCode` - Branch identifier
- `currencyCode` - 3-letter ISO code (e.g., INR, USD)

---

### 1.5 Update Product
**Endpoint:** `PUT /api/products/{id}`

**Swagger Steps:**
1. Click **PUT /api/products/{id}**
2. Click **"Try it out"**
3. Enter **id:** `7` (use id from step 1.4)
4. Modify JSON (partial update):

```json
{
  "productName": "Super Saver FD - Premium Edition",
  "description": "Updated description - Best rates for super savers!",
  "maxAmount": 10000000,
  "baseInterestRate": 7.75,
  "status": "ACTIVE"
}
```

5. Click **"Execute"**

**Expected:** 200 OK with updated product

---

### 1.6 Search Products
**Endpoint:** `POST /api/products/search`

**Swagger Steps:**
1. Click **POST /api/products/search**
2. Click **"Try it out"**
3. Use search criteria:

```json
{
  "productName": "Senior",
  "productType": "SENIOR_CITIZEN_FD",
  "status": "ACTIVE",
  "currentlyActive": true,
  "page": 0,
  "size": 20,
  "sortBy": "createdAt",
  "sortDirection": "DESC"
}
```

4. Click **"Execute"**

**Expected:** Senior Citizen FD in results

**Available Search Fields:**
- `productName` - Partial name match
- `productCode` - Product code filter
- `productType` - Enum: FIXED_DEPOSIT, SENIOR_CITIZEN_FD, etc.
- `status` - Enum: ACTIVE, INACTIVE, SUSPENDED, etc.
- `effectiveDateFrom` / `effectiveDateTo` - Date range
- `minAmountFrom` / `minAmountTo` - Amount range
- `maxAmountFrom` / `maxAmountTo` - Amount range
- `createdBy` - Created by user
- `currentlyActive` - Boolean (true/false)
- `page`, `size`, `sortBy`, `sortDirection` - Pagination options

---

### 1.7 Change Product Status
**Endpoint:** `PUT /api/products/{id}/status`

**Swagger Steps:**
1. Click **PUT /api/products/{id}/status**
2. Click **"Try it out"**
3. Enter **id:** `7`
4. Select **status:** `SUSPENDED` (dropdown)
5. Click **"Execute"**

**Expected:** Product status changed to SUSPENDED

---

## 💰 Category 2: Interest Rates

### 2.1 Get Interest Rates for Product
**Endpoint:** `GET /api/products/{productId}/interest-rates`

**Swagger Steps:**
1. Expand **"Interest Rate Management"** section
2. Click **GET /api/products/{productId}/interest-rates**
3. Click **"Try it out"**
4. Enter **productId:** `1` (Standard FD)
5. Click **"Execute"**

**Expected Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "minTermMonths": 6,
      "maxTermMonths": 11,
      "minAmount": 10000,
      "maxAmount": 500000,
      "baseRate": 6.50,
      "seniorCitizenRate": 7.00,
      "effectiveFrom": "2024-01-01",
      "active": true
    },
    // ... more rate slabs
  ]
}
```

---

### 2.2 Get Active Interest Rates
**Endpoint:** `GET /api/products/{productId}/interest-rates/active`

**Swagger Steps:**
1. Click **GET /api/products/{productId}/interest-rates/active**
2. Click **"Try it out"**
3. Enter **productId:** `2` (Senior Citizen FD)
4. Click **"Execute"**

**Expected:** Only active rate slabs

---

### 2.3 Calculate Applicable Rate
**Endpoint:** `GET /api/products/{productId}/interest-rates/applicable`

**Swagger Steps:**
1. Click **GET /api/products/{productId}/interest-rates/applicable**
2. Click **"Try it out"**
3. Enter:
   - **productId:** `2` (Senior Citizen FD - has classification-specific rates)
   - **amount:** `100000`
   - **termMonths:** `24`
   - **classification:** `SENIOR_CITIZEN`
4. Click **"Execute"**

**Expected Response:**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "id": 4,
    "minAmount": 25000,
    "maxAmount": 10000000,
    "minTermMonths": 12,
    "maxTermMonths": 36,
    "customerClassification": "SENIOR_CITIZEN",
    "interestRate": 8.0,
    "additionalRate": 0.5,
    "effectiveDate": "2025-01-01",
    "totalRate": 8.5,
    "remarks": "Special rate for senior citizens"
  }
}
```

**Test without classification (regular rate):**
- Use **productId:** `1` (Standard FD)
- Leave **classification** empty
- Expected: Returns general rate with `customerClassification: null`

**Classification Values:**
- Leave empty or null - Regular customer rate (no classification filter)
- `SENIOR_CITIZEN` - Senior citizen benefits (60+ years)
- `SUPER_SENIOR` - Super senior benefits (80+ years)
- `PREMIUM` - Premium customer benefits
- `VIP` - VIP customer benefits

**Important Notes:**
- **Product 1 (Standard FD)** has NO classification-specific rates - all rates have `customerClassification: null`
- **Product 2 (Senior Citizen FD)** has classification-specific rates with `customerClassification: "SENIOR_CITIZEN"`
- If you query Product 1 with `classification=SENIOR_CITIZEN`, it returns a general rate (since no classification-specific rate exists)
- For best results, use Product 2 to test classification-based rates!

---

## 💸 Category 3: Product Charges (Fees & Penalties)

### 3.1 Add Charge to Product
**Endpoint:** `POST /api/products/{productId}/charges`

**Swagger Steps:**
1. Expand **"Product Charges"** section
2. Click **POST /api/products/{productId}/charges**
3. Click **"Try it out"**
4. Enter **productId:** `1`
5. Request body:

```json
{
  "chargeName": "Premature Withdrawal Penalty",
  "chargeType": "PENALTY",
  "description": "1% penalty on premature withdrawal",
  "fixedAmount": 500.00,
  "percentageRate": 1.0,
  "frequency": "ONE_TIME",
  "applicableTransactionTypes": null,
  "waivable": true,
  "minCharge": 500,
  "maxCharge": 10000
}
```

6. Click **"Execute"**

**Expected Response:**
```json
{
  "success": true,
  "message": "Charge created successfully",
  "data": {
    "id": 1,
    "chargeName": "Premature Withdrawal Penalty",
    "chargeType": "PENALTY",
    "description": "1% penalty on premature withdrawal",
    "fixedAmount": 500.00,
    "percentageRate": 1.0,
    "frequency": "ONE_TIME",
    "applicableTransactionTypes": null,
    "waivable": true,
    "minCharge": 500,
    "maxCharge": 10000,
    "active": true
  }
}
```

**Field Notes:**
- `chargeName` - Name of the charge (required)
- `chargeType` - Type: FEE or PENALTY (required)
- `fixedAmount` - Fixed charge amount (optional, use if flat fee)
- `percentageRate` - Percentage rate (optional, use if percentage-based)
- `frequency` - Enum: ONE_TIME, MONTHLY, QUARTERLY, ANNUALLY (required)
- `waivable` - Can charge be waived (optional, default false)

---

### 3.2 List All Charges for Product
**Endpoint:** `GET /api/products/{productId}/charges`

**Swagger Steps:**
1. Click **GET /api/products/{productId}/charges**
2. Click **"Try it out"**
3. Enter **productId:** `1`
4. Click **"Execute"**

**Expected Response:**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": [
    {
      "id": 1,
      "chargeName": "Premature Withdrawal Penalty",
      "chargeType": "PENALTY",
      "description": "1% penalty for early withdrawal",
      "fixedAmount": null,
      "percentageRate": 1,
      "frequency": "ONE_TIME",
      "applicableTransactionTypes": null,
      "waivable": true,
      "minCharge": null,
      "maxCharge": null,
      "active": true
    }
  ]
}
```

**Note:** The seeded charge (id=1) has `minCharge` and `maxCharge` as `null` because DataInitializer didn't set them. When you create a new charge in step 3.1, it will include those values if you specify them.

---

### 3.3 Get Charges by Type
**Endpoint:** `GET /api/products/{productId}/charges/type/{chargeType}`

**Swagger Steps:**
1. Click **GET /api/products/{productId}/charges/type/{chargeType}**
2. Click **"Try it out"**
3. Enter **productId:** `1`, **chargeType:** `PENALTY`
4. Click **"Execute"**

**Expected:** Only PENALTY charges

---

### 3.4 Update Charge
**Endpoint:** `PUT /api/charges/{chargeId}` (under "Charges Management")

**Swagger Steps:**
1. Expand **"Charges Management"** section
2. Click **PUT /api/charges/{chargeId}**
3. Click **"Try it out"**
4. Enter **chargeId:** (use id from step 3.1)
5. Update:

```json
{
  "chargeName": "Premature Withdrawal Penalty - Updated",
  "description": "1.5% penalty on premature withdrawal",
  "fixedAmount": 750.00,
  "percentageRate": 1.5,
  "frequency": "ONE_TIME",
  "waivable": true
}
```

6. Click **"Execute"**

**Expected:** 200 OK with updated charge

---

### 3.5 Delete Charge
**Endpoint:** `DELETE /api/charges/{chargeId}`

**Swagger Steps:**
1. Click **DELETE /api/charges/{chargeId}**
2. Click **"Try it out"**
3. Enter **chargeId:** (use id from step 3.1)
4. Click **"Execute"**

**Expected:** 200 OK - charge deleted

---

## 👥 Category 4: Product Roles

### 4.1 Add Role Configuration
**Endpoint:** `POST /api/products/{productId}/roles`

**Swagger Steps:**
1. Expand **"Product Roles"** section
2. Click **POST /api/products/{productId}/roles**
3. Click **"Try it out"**
4. Enter **productId:** `1`
5. Request:

```json
{
  "role": "OWNER",
  "description": "Primary account owner - mandatory for all FDs",
  "mandatory": true,
  "minCount": 1,
  "maxCount": 1,
  "kycRequired": true,
  "documentRequired": true,
  "approvalRequired": false,
  "active": true
}
```

6. Click **"Execute"**

**Expected:** 201 Created with role id

---

### 4.2 Add Multiple Roles (Nominee)
**Repeat step 4.1 with:**

```json
{
  "role": "NOMINEE",
  "description": "Nominee for the FD account",
  "mandatory": false,
  "minCount": 0,
  "maxCount": 3,
  "kycRequired": true,
  "documentRequired": true,
  "approvalRequired": false,
  "active": true
}
```

---

### 4.3 Add Joint Owner Role
**Repeat with:**

```json
{
  "role": "CO_OWNER",
  "description": "Joint account holder",
  "mandatory": false,
  "minCount": 0,
  "maxCount": 2,
  "kycRequired": true,
  "documentRequired": true,
  "approvalRequired": true,
  "active": true
}
```

---

### 4.4 List Roles for Product
**Endpoint:** `GET /api/products/{productId}/roles`

**Swagger Steps:**
1. Click **GET /api/products/{productId}/roles**
2. Click **"Try it out"**
3. Enter **productId:** `1`
4. Click **"Execute"**

**Expected:** All 3 roles you created

---

### 4.5 Get Roles by Type
**Endpoint:** `GET /api/products/{productId}/roles/type/{roleType}`

**Swagger Steps:**
1. Click **GET /api/products/{productId}/roles/type/{roleType}**
2. Click **"Try it out"**
3. Enter **productId:** `1`, **roleType:** `OWNER`
4. Click **"Execute"**

**Expected:** Only OWNER role

---

## 💳 Category 5: Transaction Types

### 5.1 Add Transaction Types
**Endpoint:** `POST /api/products/{productId}/transaction-types`

**Swagger Steps:**
1. Expand **"Transaction Types"** section
2. Click **POST /api/products/{productId}/transaction-types**
3. Click **"Try it out"**
4. Enter **productId:** `1`
5. Add DEPOSIT:

```json
{
  "transactionType": "DEPOSIT",
  "description": "Customer deposits funds into FD account"
}
```

6. Click **"Execute"** (expect 201)
7. Repeat for WITHDRAWAL:

```json
{
  "transactionType": "WITHDRAWAL",
  "description": "Customer withdraws from FD (premature/maturity)"
}
```

8. Repeat for INTEREST_CREDIT:

```json
{
  "transactionType": "INTEREST_CREDIT",
  "description": "Interest credited to account"
}
```

---

### 5.2 List Transaction Types
**Endpoint:** `GET /api/products/{productId}/transaction-types`

**Swagger Steps:**
1. Click **GET /api/products/{productId}/transaction-types**
2. Click **"Try it out"**
3. Enter **productId:** `1`
4. Click **"Execute"**

**Expected:** All 3 transaction types

---

## 📊 Category 6: Balance Types

### 6.1 Add Balance Types
**Endpoint:** `POST /api/products/{productId}/balance-types`

**Swagger Steps:**
1. Expand **"Balance Types"** section
2. Click **POST /api/products/{productId}/balance-types**
3. Click **"Try it out"**
4. Enter **productId:** `1`
5. Add PRINCIPAL:

```json
{
  "balanceType": "PRINCIPAL",
  "description": "Original principal amount deposited"
}
```

6. Click **"Execute"**
7. Repeat for INTEREST:

```json
{
  "balanceType": "INTEREST_ACCRUED",
  "description": "Interest accrued but not yet paid"
}
```

8. Repeat for AVAILABLE:

```json
{
  "balanceType": "AVAILABLE_BALANCE",
  "description": "Total balance available (principal + interest)"
}
```

**Available BalanceType Values:**
- `PRINCIPAL` - Principal Amount
- `INTEREST_ACCRUED` - Accrued Interest
- `AVAILABLE_BALANCE` - Available Balance
- `CURRENT_BALANCE` - Current Balance
- `MINIMUM_BALANCE` - Minimum Required Balance
- `HOLD_AMOUNT` - Amount on Hold
- `OVERDRAFT_LIMIT` - Overdraft Limit
- `OUTSTANDING_PRINCIPAL` - Outstanding Loan Principal
- `OUTSTANDING_INTEREST` - Outstanding Loan Interest

---

### 6.2 List Balance Types
**Endpoint:** `GET /api/products/{productId}/balance-types`

**Swagger Steps:**
1. Click **GET /api/products/{productId}/balance-types**
2. Click **"Try it out"**
3. Enter **productId:** `1`
4. Click **"Execute"**

**Expected:** All 3 balance types

---

## 🔗 Category 7: Transaction-Balance Relationships

### 7.1 Create Relationship (DEPOSIT → PRINCIPAL)
**Endpoint:** `POST /api/transaction-balance-relationships`

**Swagger Steps:**
1. Expand **"Transaction-Balance Relationships"** section
2. Click **POST /api/transaction-balance-relationships**
3. Click **"Try it out"**
4. Request:

```json
{
  "transactionType": "DEPOSIT",
  "balanceType": "PRINCIPAL",
  "impactType": "CREDIT",
  "description": "Deposit increases principal balance",
  "active": true
}
```

5. Click **"Execute"**

**Expected:** 201 Created

---

### 7.2 Create More Relationships

**DEPOSIT → AVAILABLE_BALANCE:**
```json
{
  "transactionType": "DEPOSIT",
  "balanceType": "AVAILABLE_BALANCE",
  "impactType": "CREDIT",
  "description": "Deposit increases available balance",
  "active": true
}
```

**WITHDRAWAL → AVAILABLE_BALANCE:**
```json
{
  "transactionType": "WITHDRAWAL",
  "balanceType": "AVAILABLE_BALANCE",
  "impactType": "DEBIT",
  "description": "Withdrawal reduces available balance",
  "active": true
}
```

**INTEREST_CREDIT → INTEREST_ACCRUED:**
```json
{
  "transactionType": "INTEREST_CREDIT",
  "balanceType": "INTEREST_ACCRUED",
  "impactType": "CREDIT",
  "description": "Interest credit increases accrued interest",
  "active": true
}
```

**INTEREST_CREDIT → AVAILABLE_BALANCE:**
```json
{
  "transactionType": "INTEREST_CREDIT",
  "balanceType": "AVAILABLE_BALANCE",
  "impactType": "CREDIT",
  "description": "Interest credit increases available balance",
  "active": true
}
```

---

### 7.3 List All Relationships
**Endpoint:** `GET /api/transaction-balance-relationships`

**Swagger Steps:**
1. Click **GET /api/transaction-balance-relationships**
2. Click **"Try it out"**
3. Click **"Execute"**

**Expected:** All 5 relationships

---

### 7.4 Lookup Specific Relationship
**Endpoint:** `GET /api/transaction-balance-relationships/lookup`

**Swagger Steps:**
1. Click **GET /api/transaction-balance-relationships/lookup**
2. Click **"Try it out"**
3. Enter:
   - **transactionType:** `DEPOSIT`
   - **balanceType:** `AVAILABLE_BALANCE`
4. Click **"Execute"**

**Expected:** Single relationship matching the criteria

---

### 7.5 Get by Transaction Type
**Endpoint:** `GET /api/transaction-balance-relationships/transaction/{transactionType}`

**Swagger Steps:**
1. Click **GET /api/transaction-balance-relationships/transaction/{transactionType}**
2. Click **"Try it out"**
3. Enter **transactionType:** `INTEREST_CREDIT`
4. Click **"Execute"**

**Expected:** All relationships where transaction = INTEREST_CREDIT

---

### 7.6 Get Active Relationships Only
**Endpoint:** `GET /api/transaction-balance-relationships/active`

**Swagger Steps:**
1. Click **GET /api/transaction-balance-relationships/active**
2. Click **"Try it out"**
3. Click **"Execute"**

**Expected:** Only active relationships

---

## 📧 Category 8: Customer Communications

### 8.1 Add Communication Template
**Endpoint:** `POST /api/products/{productId}/communications`

**Swagger Steps:**
1. Expand **"Customer Communications"** section
2. Click **POST /api/products/{productId}/communications**
3. Click **"Try it out"**
4. Enter **productId:** `1`
5. Maturity Reminder Email:

```json
{
  "communicationType": "EMAIL",
  "event": "MATURITY_REMINDER",
  "template": "fd_maturity_reminder_v1",
  "subject": "Your FD is maturing soon - Action Required",
  "content": "Dear {{customerName}}, your Fixed Deposit (FD Number: {{fdNumber}}) will mature on {{maturityDate}}. Please visit our branch or login to decide on renewal options.",
  "mandatory": true,
  "active": true
}
```

6. Click **"Execute"**

---

### 8.2 Add More Templates

**Account Opening Confirmation:**
```json
{
  "communicationType": "EMAIL",
  "event": "ACCOUNT_OPENING",
  "template": "fd_welcome_v1",
  "subject": "Welcome to {{bankName}} - FD Account Opened",
  "content": "Congratulations! Your FD account {{fdNumber}} has been successfully opened with an amount of ₹{{amount}} for {{tenure}} months at {{interestRate}}% interest rate.",
  "mandatory": true,
  "active": true
}
```

**Interest Credit Notification:**
```json
{
  "communicationType": "SMS",
  "event": "INTEREST_CREDIT",
  "template": "fd_interest_credit_sms",
  "subject": "",
  "content": "Interest of Rs.{{amount}} credited to your FD account {{fdNumber}} on {{date}}. Total balance: Rs.{{totalBalance}}",
  "mandatory": false,
  "active": true
}
```

**Premature Withdrawal Warning:**
```json
{
  "communicationType": "EMAIL",
  "event": "PREMATURE_WITHDRAWAL_REQUEST",
  "template": "fd_premature_warning",
  "subject": "FD Premature Withdrawal - Penalty Notice",
  "content": "You have requested premature withdrawal of FD {{fdNumber}}. Please note: A penalty of {{penaltyAmount}} will be deducted. Revised interest rate: {{revisedRate}}%.",
  "mandatory": true,
  "active": true
}
```

---

### 8.3 List Communications for Product
**Endpoint:** `GET /api/products/{productId}/communications`

**Swagger Steps:**
1. Click **GET /api/products/{productId}/communications**
2. Click **"Try it out"**
3. Enter **productId:** `1`
4. Click **"Execute"**

**Expected:** All 4 communication templates

---

### 8.4 Get by Communication Type
**Endpoint:** `GET /api/products/{productId}/communications/type/{type}`

**Swagger Steps:**
1. Click **GET /api/products/{productId}/communications/type/{type}**
2. Click **"Try it out"**
3. Enter **productId:** `1`, **type:** `EMAIL`
4. Click **"Execute"**

**Expected:** Only EMAIL templates (3 templates)

---

### 8.5 Get by Event
**Endpoint:** `GET /api/products/{productId}/communications/event/{event}`

**Swagger Steps:**
1. Click **GET /api/products/{productId}/communications/event/{event}**
2. Click **"Try it out"**
3. Enter **productId:** `1`, **event:** `MATURITY_REMINDER`
4. Click **"Execute"**

**Expected:** Maturity reminder template

---

## 🔍 Category 9: Advanced Queries

### 9.1 Filter Products by Type
**Endpoint:** `GET /api/products/type/{productType}`

**Swagger Steps:**
1. Expand **"Product Management"** section
2. Click **GET /api/products/type/{productType}**
3. Click **"Try it out"**
4. Enter **productType:** `FIXED_DEPOSIT`
5. Click **"Execute"**

**Expected:** All 7 FD products (6 seeded + 1 you created)

---

### 9.2 Get Active Products Only
**Endpoint:** `GET /api/products/active`

**Swagger Steps:**
1. Click **GET /api/products/active**
2. Click **"Try it out"**
3. Click **"Execute"**

**Expected:** Only active products (excluding SUSPENDED from step 1.7)

---

### 9.3 Get Products by Status
**Endpoint:** `GET /api/products/status/{status}`

**Swagger Steps:**
1. Click **GET /api/products/status/{status}**
2. Click **"Try it out"**
3. Enter **status:** `ACTIVE`
4. Click **"Execute"**

**Expected:** All ACTIVE products

---

## 📋 Complete Test Checklist

### ✅ Basic Operations
- [ ] List all products (should see 6 seeded FDs)
- [ ] Get product by id
- [ ] Get product by code (FD-STD-001, FD-SR-001, etc.)
- [ ] Create new product
- [ ] Update product
- [ ] Change product status

### ✅ Interest Rates
- [ ] List interest rates for product
- [ ] Get active rates only
- [ ] Calculate applicable rate with customer age

### ✅ Charges (Fees/Penalties)
- [ ] Add charge to product
- [ ] List charges
- [ ] Filter by charge type (FEE/PENALTY)
- [ ] Update charge
- [ ] Delete charge

### ✅ Roles
- [ ] Add OWNER role (mandatory)
- [ ] Add NOMINEE role (optional)
- [ ] Add CO_OWNER role (joint account)
- [ ] List all roles
- [ ] Filter by role type

### ✅ Transaction Types
- [ ] Add DEPOSIT transaction type
- [ ] Add WITHDRAWAL transaction type
- [ ] Add INTEREST_CREDIT transaction type
- [ ] List all transaction types

### ✅ Balance Types
- [ ] Add PRINCIPAL
- [ ] Add INTEREST_ACCRUED
- [ ] Add AVAILABLE_BALANCE
- [ ] List all balance types

### ✅ Transaction-Balance Relationships
- [ ] Create 5 relationships (DEPOSIT→PRINCIPAL, DEPOSIT→AVAILABLE, etc.)
- [ ] List all relationships
- [ ] Lookup specific relationship
- [ ] Filter by transaction type
- [ ] Filter by balance type
- [ ] Get active relationships only

### ✅ Communications
- [ ] Add MATURITY_REMINDER email
- [ ] Add ACCOUNT_OPENING email
- [ ] Add INTEREST_CREDIT SMS
- [ ] Add PREMATURE_WITHDRAWAL_REQUEST email
- [ ] List all communications
- [ ] Filter by type (EMAIL/SMS)
- [ ] Filter by event

### ✅ Advanced Queries
- [ ] Filter products by type
- [ ] Get active products only
- [ ] Get products by status

---

## 🎯 Complete Integration Test Scenario

**Goal:** Configure a complete FD product with all features

### Step 1: Create Product
Use step 1.4 to create "Super Saver FD" (productId = 7)

### Step 2: Add Interest Rate Slabs
(These might already exist from seeded data, or use interest rate endpoints to add custom slabs)

### Step 3: Add Charges
- Processing Fee: ₹100 (ONE_TIME, FEE)
- Premature Withdrawal Penalty: 1% (PENALTY)
- Annual Maintenance: ₹500 (ANNUALLY, FEE)

### Step 4: Configure Roles
- OWNER (mandatory, 1-1)
- NOMINEE (optional, 0-3)
- CO_OWNER (optional, 0-2)

### Step 5: Define Transaction Types
- DEPOSIT
- WITHDRAWAL
- INTEREST_CREDIT
- FEE_DEDUCTION

### Step 6: Define Balance Types
- PRINCIPAL
- INTEREST_ACCRUED
- AVAILABLE_BALANCE
- CURRENT_BALANCE

### Step 7: Map Relationships
- DEPOSIT → PRINCIPAL (CREDIT)
- DEPOSIT → AVAILABLE_BALANCE (CREDIT)
- WITHDRAWAL → AVAILABLE_BALANCE (DEBIT)
- INTEREST_CREDIT → INTEREST_ACCRUED (CREDIT)
- INTEREST_CREDIT → AVAILABLE_BALANCE (CREDIT)
- FEE_DEDUCTION → AVAILABLE_BALANCE (DEBIT)

### Step 8: Setup Communications
- ACCOUNT_OPENING (EMAIL, mandatory)
- MATURITY_REMINDER (EMAIL + SMS, mandatory)
- INTEREST_CREDIT (SMS, optional)
- PREMATURE_WITHDRAWAL_REQUEST (EMAIL, mandatory)

### Step 9: Verify Everything
```
GET /api/products/7
GET /api/products/7/charges
GET /api/products/7/roles
GET /api/products/7/transaction-types
GET /api/products/7/balance-types
GET /api/products/7/communications
```

---

## 🐛 Troubleshooting

### Issue: "401 Unauthorized"
**Fix:** Click "Authorize" button in Swagger and enter Bearer token

### Issue: "404 Not Found" for product
**Fix:** Run `GET /api/products` to see available product IDs

### Issue: Seeded products not visible
**Fix:** Check application logs for DataInitializer messages. Restart service.

### Issue: Cannot create/update (403 Forbidden)
**Fix:** Ensure JWT token has ROLE_ADMIN (regular users may have limited access)

### Issue: Validation errors (400 Bad Request)
**Fix:** Check required fields in request body. All amounts should be positive, dates in ISO format.

---

## 📝 Quick Reference

### Seeded FD Products (Auto-loaded on startup)
| ID | Code | Name | Term (months) | Min Amount |
|----|------|------|---------------|------------|
| 1 | FD-STD-001 | Standard Fixed Deposit | 6-60 | ₹10,000 |
| 2 | FD-SR-001 | Senior Citizen FD | 12-120 | ₹25,000 |
| 3 | FD-TAX-001 | Tax Saver FD | 60 (fixed) | ₹10,000 |
| 4 | FD-CUM-001 | Cumulative FD | 12-120 | ₹50,000 |
| 5 | FD-NCUM-001 | Non-Cumulative FD | 12-60 | ₹100,000 |
| 6 | FD-FLEXI-001 | Flexi FD | 6-36 | ₹25,000 |

### Enum Values Quick Reference

**ProductType:** FIXED_DEPOSIT, RECURRING_DEPOSIT, SAVINGS_ACCOUNT, CURRENT_ACCOUNT, LOAN

**ProductStatus:** DRAFT, ACTIVE, INACTIVE, SUSPENDED, CLOSED

**ChargeType:** FEE, PENALTY

**ChargeFrequency:** ONE_TIME, MONTHLY, QUARTERLY, ANNUALLY

**RoleType:** OWNER, CO_OWNER, NOMINEE, GUARDIAN, AUTHORIZED_SIGNATORY

**TransactionType:** DEPOSIT, WITHDRAWAL, INTEREST_CREDIT, FEE_DEDUCTION, PENALTY_CHARGE, TRANSFER_IN, TRANSFER_OUT

**BalanceType:** PRINCIPAL_BALANCE, INTEREST_ACCRUED, AVAILABLE_BALANCE, BLOCKED_BALANCE, MINIMUM_BALANCE

**CommunicationType:** EMAIL, SMS, PUSH, LETTER

---

## 🎉 Success Criteria

You've successfully tested the product-pricing-service when:

✅ All 6 seeded FD products are visible
✅ Created a custom FD product with full configuration
✅ Added charges, roles, transaction types, balance types
✅ Created transaction-balance relationships
✅ Setup communication templates
✅ All CRUD operations work (Create, Read, Update, Delete)
✅ Search and filter operations return correct results
✅ Interest rate calculation works for senior citizens

**Total Endpoints Tested:** 50+ REST APIs across 9 categories!
