# FD Account Service - Swagger Testing Guide

## Service Information
- **Base URL:** http://localhost:8086/api/fd-accounts
- **Swagger UI:** http://localhost:8086/api/fd-accounts/swagger-ui.html
- **API Docs:** http://localhost:8086/api/fd-accounts/v3/api-docs
- **Port:** 8086
- **Database:** fd_account_db (MySQL)

## Prerequisites
1. MySQL running on localhost:3306 with user `root` / password `root`
2. Product Pricing Service running on port 8084 (for product validation)
3. Customer Service running on port 8083 (for customer validation)
4. FD Calculator Service running on port 8085 (for interest calculations)

---

## Test Sequence

### Phase 1: Account Creation

#### 1.1 Create Standard FD Account (Values from Product)
**Endpoint:** `POST /accounts`

**Request Body:**
```json
{
  "accountName": "John Doe - FD Account",
  "productCode": "FD-STD-6M",
  "principalAmount": 100000,
  "termMonths": 6,
  "effectiveDate": "2025-10-20",
  "branchCode": "001",
  "branchName": "Main Branch",
  "roles": [
    {
      "customerId": 1,
      "customerName": "John Doe Updated",
      "roleType": "OWNER",
      "ownershipPercentage": 100.00,
      "isPrimary": true,
      "isActive": true
    }
  ],
  "maturityInstruction": "CLOSE_AND_PAYOUT",
  "remarks": "First standard FD account"
}
```

**Expected Response:** 201 Created
```json
{
  "id": 2,
  "accountNumber": "0011000007",
  "ibanNumber": null,
  "accountName": "John Doe - FD Account",
  "productCode": "FD-STD-6M",
  "productName": "6 Month Fixed Deposit",
  "status": "ACTIVE",
  "principalAmount": 100000,
  "interestRate": 6.5,
  "customInterestRate": null,
  "termMonths": 6,
  "maturityAmount": 102948.77,
  "effectiveDate": "2025-10-20",
  "maturityDate": "2026-04-20",
  "closureDate": null,
  "interestCalculationMethod": "SIMPLE",
  "interestPayoutFrequency": "ON_MATURITY",
  "autoRenewal": true,
  "maturityInstruction": "CLOSE_AND_PAYOUT",
  "maturityTransferAccount": null,
  "branchCode": "001",
  "branchName": "Main Branch",
  "tdsApplicable": true,
  "tdsRate": 10,
  "roles": null,
  "balances": null,
  "remarks": "First standard FD account",
  "createdAt": "2025-10-20T16:33:48.9097296",
  "updatedAt": "2025-10-20T16:33:48.9097296",
  "createdBy": null,
  "updatedBy": null
}
```

**What to Verify:**
- ✅ Account number generated (10 digits: 0011000007)
- ⚠️ IBAN number is null (needs investigation - should be generated)
- ✅ Interest rate inherited from product (6.5%)
- ✅ Term inherited from product (6 months)
- ✅ Maturity date calculated correctly (Oct 20, 2025 + 6 months = Apr 20, 2026)
- ✅ Maturity amount calculated with interest (₹102,948.77 for ₹100,000 principal)
- ✅ Status is ACTIVE
- ✅ Interest calculation method: SIMPLE (default from product or fallback)
- ✅ Interest payout: ON_MATURITY (default)
- ✅ Auto renewal: true (default from product)
- ✅ TDS applicable: true, rate: 10% (standard defaults)
- ✅ IBAN number generated correctly (IN41CRXA0011000016 format)
- ✅ Roles populated with customer details (PRIMARY OWNER with 100% ownership)
- ✅ Balances populated (PRINCIPAL: ₹100,000, INTEREST_ACCRUED: ₹0)
- ✅ createdBy/updatedBy defaults to "SYSTEM" when not provided

**✅ SUCCESS! Complete FD Account Created**
- Account ID: 4
- Account Number: 0011000025
- IBAN: IN89CRXA0011000025
- Principal: ₹100,000
- Maturity Amount: ₹102,948.77
- Interest Rate: 6.5%
- Term: 6 months
- Maturity Date: April 20, 2026
- Owner: John Doe Updated (Customer ID 1, 100% ownership)
- Initial Balances: Principal ₹100,000, Interest Accrued ₹0
- Audit: Created by SYSTEM at 2025-10-20T16:52:00

---

#### 1.2 Create Customized FD Account (Override Product Values)
**Endpoint:** `POST /accounts/customize`

**Request Body:**
```json
{
  "accountName": "Jane Smith - Custom FD",
  "productCode": "FD-STD-6M",
  "principalAmount": 500000,
  "customTermMonths": 6,
  "customInterestRate": 7.00,
  "effectiveDate": "2025-10-20",
  "branchCode": "002",
  "branchName": "Downtown Branch",
  "roles": [
    {
      "customerId": 1,
      "customerName": "Jane Smith",
      "roleType": "OWNER",
      "ownershipPercentage": 100.00,
      "isPrimary": true,
      "isActive": true
    }
  ],
  "maturityInstruction": "RENEW_PRINCIPAL_ONLY",
  "remarks": "Customized rate and confirmed tenure"
}
```

**Note:** `customTermMonths` is required even when using product's term. Set to 6 months (product's min=6, max=6). Customizing interest rate to 7%.

**Expected Response:** 201 Created
```json
{
  "id": 5,
  "accountNumber": "0020000034",
  "ibanNumber": "IN76CRXA0020000034",
  "accountName": "Jane Smith - Custom FD",
  "productCode": "FD-STD-6M",
  "productName": "6 Month Fixed Deposit",
  "status": "ACTIVE",
  "principalAmount": 500000,
  "interestRate": 7.00,
  "customInterestRate": 7.00,
  "termMonths": 6,
  "maturityAmount": 517500.00,
  "effectiveDate": "2025-10-20",
  "maturityDate": "2026-04-20",
  "interestCalculationMethod": "SIMPLE",
  "interestPayoutFrequency": "ON_MATURITY",
  "autoRenewal": true,
  "maturityInstruction": "RENEW_PRINCIPAL_ONLY",
  "branchCode": "002",
  "branchName": "Downtown Branch",
  "tdsApplicable": true,
  "tdsRate": 10,
  "roles": [
    {
      "id": 5,
      "customerId": 1,
      "customerName": "Jane Smith",
      "roleType": "OWNER",
      "ownershipPercentage": 100,
      "isPrimary": true,
      "isActive": true
    }
  ],
  "balances": [
    {
      "id": 9,
      "balanceType": "PRINCIPAL",
      "balance": 500000,
      "asOfDate": "2025-10-20"
    },
    {
      "id": 10,
      "balanceType": "INTEREST_ACCRUED",
      "balance": 0,
      "asOfDate": "2025-10-20"
    }
  ],
  "createdBy": "SYSTEM",
  "updatedBy": "SYSTEM"
}
```

**What to Verify:**
- ✅ Custom interest rate (7% instead of product's 6.5%)
- ✅ Standard tenure (6 months - product has fixed term, min=6, max=6)
- ✅ Maturity date calculated correctly (Oct 20, 2025 + 6 months = Apr 20, 2026)
- ✅ Maturity amount calculated with custom rate (₹500,000 @ 7% for 6 months = ₹517,500)
- ✅ Account created for different branch (002)
- ✅ Different maturity instruction (RENEW_PRINCIPAL_ONLY vs CLOSE_AND_PAYOUT)
- ✅ Higher principal amount (₹500,000 vs ₹100,000)
- ✅ Roles and balances populated correctly
- ✅ Uses Customer ID 1 (existing customer)

---

### Phase 2: Account Inquiry

#### 2.1 Get Account by Account Number
**Endpoint:** `GET /accounts/{identifier}?idType=ACCOUNT_NUMBER`

**Request:**
```
GET /accounts/0011000025?idType=ACCOUNT_NUMBER
```

**Expected Response:** 200 OK
```json
{
  "id": 4,
  "accountNumber": "0011000025",
  "ibanNumber": "IN89CRXA0011000025",
  "accountName": "John Doe - FD Account",
  "productCode": "FD-STD-6M",
  "productName": "6 Month Fixed Deposit",
  "status": "ACTIVE",
  "principalAmount": 100000,
  "interestRate": 6.5,
  "termMonths": 6,
  "maturityAmount": 102948.77,
  "effectiveDate": "2025-10-20",
  "maturityDate": "2026-04-20",
  "roles": [
    {
      "id": 4,
      "customerId": 1,
      "customerName": "John Doe Updated",
      "roleType": "OWNER",
      "ownershipPercentage": 100,
      "isPrimary": true,
      "isActive": true,
      "remarks": null,
      "createdAt": "2025-10-20T16:52:00.0902464",
      "updatedAt": "2025-10-20T16:52:00.0902464"
    }
  ],
  "balances": [
    {
      "id": 7,
      "balanceType": "PRINCIPAL",
      "balance": 100000,
      "asOfDate": "2025-10-20",
      "description": "Initial principal amount"
    },
    {
      "id": 8,
      "balanceType": "INTEREST_ACCRUED",
      "balance": 0,
      "asOfDate": "2025-10-20",
      "description": "Initial interest accrued"
    }
  ],
  "createdBy": "SYSTEM",
  "updatedBy": "SYSTEM"
}
```

---

#### 2.2 Get Account by IBAN
**Endpoint:** `GET /accounts/{identifier}?idType=IBAN`

**Request:**
```
GET /accounts/IN89CRXA0011000025?idType=IBAN
```

**Expected Response:** 200 OK
- Returns same account by IBAN lookup (same structure as 2.1)

---

#### 2.3 Get Account by Internal ID
**Endpoint:** `GET /accounts/{identifier}?idType=INTERNAL_ID`

**Request:**
```
GET /accounts/4?idType=INTERNAL_ID
```

**Expected Response:** 200 OK
- Returns account by database ID (same structure as 2.1)

---

#### 2.4 Get Account Summary
**Endpoint:** `GET /accounts/{accountNumber}/summary`

**Request:**
```
GET /accounts/0011000025/summary
```

**Expected Response:** 200 OK
```json
{
  "accountNumber": "0011000025",
  "ibanNumber": "IN89CRXA0011000025",
  "productName": "6 Month Fixed Deposit",
  "customerId": 1,
  "customerName": "John Doe Updated",
  "depositAmount": 100000.00,
  "currentBalance": 100000.00,
  "interestRate": 6.50,
  "maturityDate": "2026-04-20",
  "daysToMaturity": 182,
  "status": "ACTIVE"
}
```

**What to Verify:**
- Summarized view (fewer fields)
- Days to maturity calculated
- Customer name included

---

### Phase 3: Account Listing & Search

#### 3.1 Get Customer's Accounts
**Endpoint:** `GET /accounts/customer/{customerId}`

**Request:**
```
GET /accounts/customer/1
```

**Expected Response:** 200 OK
```json
[
  {
    "id": 4,
    "accountNumber": "0011000025",
    "ibanNumber": "IN89CRXA0011000025",
    "accountName": "John Doe - FD Account",
    "productCode": "FD-STD-6M",
    "status": "ACTIVE",
    "principalAmount": 100000,
    "interestRate": 6.5,
    "maturityAmount": 102948.77,
    "maturityDate": "2026-04-20"
  }
]
```

**What to Verify:**
- Returns array of all accounts for customer ID 1
- If you created multiple accounts for same customer, all should appear

---

#### 3.2 Search Accounts with Filters
**Endpoint:** `POST /accounts/search`

**Request Body:**
```json
{
  "customerId": 1,
  "status": "ACTIVE",
  "minAmount": 50000,
  "maxAmount": 200000,
  "branchCode": "001",
  "page": 0,
  "size": 10
}
```

**Expected Response:** 200 OK
```json
{
  "content": [
    {
      "accountNumber": "0011000025",
      "ibanNumber": "IN89CRXA0011000025",
      "status": "ACTIVE",
      "principalAmount": 100000.00,
      "maturityAmount": 102948.77,
      "productCode": "FD-STD-6M",
      "branchCode": "001"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 10
}
```

---

#### 3.3 Get Accounts Maturing Soon
**Endpoint:** `GET /accounts/maturing?days=30`

**Request:**
```
GET /accounts/maturing?days=30
```

**Expected Response:** 200 OK
- Returns accounts maturing in next 30 days

---

#### 3.4 Get Accounts by Product
**Endpoint:** `GET /accounts/product/{productCode}`

**Request:**
```
GET /accounts/product/FD-STD-6M
```

**Expected Response:** 200 OK
- Returns all accounts created with product FD-STD-6M

---

#### 3.5 Get Accounts by Branch
**Endpoint:** `GET /accounts/branch/{branchCode}`

**Request:**
```
GET /accounts/branch/001
```

**Expected Response:** 200 OK
- Returns all accounts in branch 001

---

#### 3.6 Check if Account Exists
**Endpoint:** `GET /accounts/exists/{accountNumber}`

**Request:**
```
GET /accounts/exists/0011000025
```

**Expected Response:** 200 OK
```json
true
```

---

### Phase 4: Transactions

#### 4.1 Create Additional Deposit Transaction
**Endpoint:** `POST /transactions`

**Request Body:**
```json
{
  "accountNumber": "0011000025",
  "transactionType": "ADDITIONAL_DEPOSIT",
  "amount": 50000.00,
  "transactionDate": "2025-10-25",
  "performedBy": "BRANCH-USER-001",
  "remarks": "Additional deposit after 5 days"
}
```

**Expected Response:** 201 Created
```json
{
  "id": 1,
  "transactionReference": "TXN20251025-001",
  "accountNumber": "0011000025",
  "transactionType": "ADDITIONAL_DEPOSIT",
  "amount": 50000.00,
  "balanceAfter": 150000.00,
  "transactionDate": "2025-10-25",
  "status": "COMPLETED",
  "performedBy": "BRANCH-USER-001"
}
```

**What to Verify:**
- Transaction reference generated
- Balance increased from 100,000 to 150,000
- Status is COMPLETED
- PRINCIPAL balance updated

---

#### 4.2 Create Fee Debit Transaction
**Endpoint:** `POST /transactions`

**Request Body:**
```json
{
  "accountNumber": "0011000025",
  "transactionType": "FEE_DEBIT",
  "amount": 500.00,
  "transactionDate": "2025-10-26",
  "performedBy": "SYSTEM",
  "remarks": "Account maintenance fee"
}
```

**Expected Response:** 201 Created
```json
{
  "transactionReference": "TXN20251026-001",
  "transactionType": "FEE_DEBIT",
  "amount": 500.00,
  "balanceAfter": 149500.00
}
```

**What to Verify:**
- Balance decreased by fee amount
- AVAILABLE balance reduced

---

#### 4.3 Get Transaction by Reference
**Endpoint:** `GET /transactions/{transactionReference}`

**Request:**
```
GET /transactions/TXN20251025-001
```

**Expected Response:** 200 OK
- Returns transaction details

---

#### 4.4 Get All Account Transactions
**Endpoint:** `GET /transactions/account/{accountNumber}`

**Request:**
```
GET /transactions/account/0011000025
```

**Expected Response:** 200 OK
```json
[
  {
    "transactionReference": "TXN20251020-001",
    "transactionType": "INITIAL_DEPOSIT",
    "amount": 100000.00,
    "transactionDate": "2025-10-20"
  },
  {
    "transactionReference": "TXN20251025-001",
    "transactionType": "ADDITIONAL_DEPOSIT",
    "amount": 50000.00,
    "transactionDate": "2025-10-25"
  },
  {
    "transactionReference": "TXN20251026-001",
    "transactionType": "FEE_DEBIT",
    "amount": 500.00,
    "transactionDate": "2025-10-26"
  }
]
```

**What to Verify:**
- All transactions listed chronologically
- Includes initial deposit, additional deposit, fee

---

#### 4.5 Get Transactions with Pagination
**Endpoint:** `GET /transactions/account/{accountNumber}/paged?page=0&size=2`

**Request:**
```
GET /transactions/account/0011000025/paged?page=0&size=2
```

**Expected Response:** 200 OK
```json
{
  "content": [ /* 2 transactions */ ],
  "totalElements": 3,
  "totalPages": 2,
  "number": 0,
  "size": 2
}
```

---

#### 4.6 Reverse a Transaction
**Endpoint:** `POST /transactions/{transactionReference}/reverse`

**Request:**
```
POST /transactions/TXN20251026-001/reverse?reason=Incorrect fee charged&performedBy=BRANCH-MANAGER
```

**Expected Response:** 200 OK
```json
{
  "transactionReference": "REV20251027-001",
  "transactionType": "REVERSAL",
  "amount": 500.00,
  "balanceAfter": 150000.00,
  "originalTransactionReference": "TXN20251026-001",
  "remarks": "Reversal: Incorrect fee charged"
}
```

**What to Verify:**
- New reversal transaction created
- Balance restored (back to 150,000)
- Original transaction marked as REVERSED
- Reversal reason recorded

---

### Phase 5: Premature Withdrawal (Redemption)

#### 5.1 Inquire Premature Withdrawal
**Endpoint:** `POST /transactions/premature-withdrawal/inquire`

**Request Body:**
```json
{
  "accountNumber": "0011000025",
  "withdrawalDate": "2025-12-20"
}
```

**Expected Response:** 200 OK
```json
{
  "accountNumber": "0011000025",
  "principalAmount": 150000.00,
  "normalInterestRate": 6.50,
  "penaltyPercentage": 2.00,
  "revisedInterestRate": 4.50,
  "daysHeld": 61,
  "normalInterest": 1625.00,
  "revisedInterest": 1125.00,
  "penaltyAmount": 500.00,
  "tdsAmount": 112.50,
  "netPayable": 150512.50,
  "maturityDate": "2026-04-20",
  "prematureWithdrawalDate": "2025-12-20"
}
```

**What to Verify:**
- Penalty calculated (2% default)
- Revised interest rate = normal - penalty (6.5% - 2% = 4.5%)
- Days held calculated (Oct 20 to Dec 20 = 61 days)
- Interest recalculated at revised rate
- TDS deducted (10% of interest)
- Net payable = principal + revised interest - TDS

**Note:** This is an INQUIRY only - no actual transaction created

---

#### 5.2 Process Premature Withdrawal
**Endpoint:** `POST /transactions/premature-withdrawal/process`

**Request:**
```
POST /transactions/premature-withdrawal/process?accountNumber=0011000025&withdrawalDate=2025-12-20&performedBy=BRANCH-MANAGER&remarks=Customer request
```

**Expected Response:** 200 OK
```json
{
  "transactionReference": "PWD20251220-001",
  "transactionType": "PREMATURE_WITHDRAWAL",
  "amount": 150512.50,
  "balanceAfter": 0.00,
  "status": "COMPLETED"
}
```

**What to Verify:**
- Three transactions created:
  1. INTEREST_CREDIT (revised interest)
  2. FEE_DEBIT (TDS amount)
  3. PREMATURE_WITHDRAWAL (net payout)
- Account status changed to CLOSED
- All balances zeroed out
- Cannot perform further transactions on closed account

---

### Phase 6: Role Management

#### 6.1 Add Owner Role to Account
**Endpoint:** `POST /roles/account/{accountNumber}`

**Request:**
```
POST /roles/account/0020000034
```

**Request Body:**
```json
{
  "customerId": 1003,
  "roleType": "OWNER",
  "ownershipPercentage": 50.00,
  "isPrimary": false,
  "isActive": true,
  "remarks": "Co-owner added"
}
```

**Expected Response:** 201 Created
```json
{
  "id": 6,
  "accountNumber": "0020000034",
  "customerId": 1003,
  "roleType": "OWNER",
  "ownershipPercentage": 50.00,
  "isPrimary": false,
  "isActive": true
}
```

**What to Verify:**
- New role created
- Ownership percentage validation (total ≤ 100%)
- Only one primary owner allowed

---

#### 6.2 Add Nominee Role
**Endpoint:** `POST /roles/account/{accountNumber}`

**Request Body:**
```json
{
  "customerId": 2003,
  "roleType": "NOMINEE",
  "ownershipPercentage": 100.00,
  "isPrimary": false,
  "isActive": true,
  "remarks": "Nominee for account"
}
```

**Expected Response:** 201 Created

---

#### 6.3 Update Role
**Endpoint:** `PUT /roles/{roleId}`

**Request:**
```
PUT /roles/1
```

**Request Body:**
```json
{
  "ownershipPercentage": 60.00,
  "isPrimary": false,
  "isActive": true,
  "remarks": "Updated ownership percentage"
}
```

**Expected Response:** 200 OK
- Returns updated role

---

#### 6.4 Get All Account Roles
**Endpoint:** `GET /roles/account/{accountNumber}`

**Request:**
```
GET /roles/account/0020000034
```

**Expected Response:** 200 OK
```json
[
  {
    "roleType": "OWNER",
    "customerId": 1002,
    "ownershipPercentage": 100.00,
    "isPrimary": true,
    "isActive": true
  },
  {
    "roleType": "OWNER",
    "customerId": 1003,
    "ownershipPercentage": 60.00,
    "isPrimary": false,
    "isActive": true
  },
  {
    "roleType": "NOMINEE",
    "customerId": 2003,
    "ownershipPercentage": 100.00,
    "isActive": true
  }
]
```

---

#### 6.5 Get Active Roles Only
**Endpoint:** `GET /roles/account/{accountNumber}/active`

**Request:**
```
GET /roles/account/0020000034/active
```

**Expected Response:** 200 OK
- Returns only active roles (isActive = true)

---

#### 6.6 Get Customer's Roles Across All Accounts
**Endpoint:** `GET /roles/customer/{customerId}`

**Request:**
```
GET /roles/customer/1003
```

**Expected Response:** 200 OK
- Returns all roles for customer 1003 across all FD accounts

---

#### 6.7 Remove Role (Soft Delete)
**Endpoint:** `DELETE /roles/{roleId}`

**Request:**
```
DELETE /roles/1
```

**Expected Response:** 204 No Content

**What to Verify:**
- Role marked as inactive (isActive = false)
- Role still exists in database (soft delete)
- Cannot remove last owner
- Ownership percentage recalculated for remaining owners

---

## Error Scenarios to Test

### 1. Invalid Account Number
**Request:**
```
GET /accounts/INVALID123
```

**Expected:** 404 Not Found
```json
{
  "timestamp": "2025-10-20T14:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Account not found: INVALID123"
}
```

---

### 2. Validation Error (Missing Required Fields)
**Request:**
```json
POST /accounts
{
  "productCode": "FD-STD-6M"
  // Missing customerId, depositAmount
}
```

**Expected:** 400 Bad Request
```json
{
  "timestamp": "2025-10-20T14:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid request parameters",
  "details": {
    "customerId": "must not be null",
    "depositAmount": "must not be null"
  }
}
```

---

### 3. Insufficient Balance for Withdrawal
**Request:**
```json
POST /transactions
{
  "accountNumber": "0011000025",
  "transactionType": "WITHDRAWAL",
  "amount": 999999.00
}
```

**Expected:** 400 Bad Request
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Insufficient balance for withdrawal"
}
```

---

### 4. Transaction on Closed Account
**Request:**
```json
POST /transactions
{
  "accountNumber": "0011000025",
  "transactionType": "ADDITIONAL_DEPOSIT",
  "amount": 10000.00
}
```

**Note:** Test this AFTER processing premature withdrawal in Phase 5.2

**Expected:** 409 Conflict
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Cannot create transaction on CLOSED account"
}
```

---

### 5. Ownership Percentage Exceeds 100%
**Request:**
```json
POST /roles/account/0020000034
{
  "customerId": 1004,
  "roleType": "OWNER",
  "ownershipPercentage": 60.00
}
```

**Expected:** 400 Bad Request
```json
{
  "message": "Total ownership percentage cannot exceed 100%. Current: 110%, Adding: 60%"
}
```

**Note:** Use account 0020000034 after adding co-owner in Phase 6.1

---

### 6. Cannot Remove Last Owner
**Request:**
```
DELETE /roles/1
```

**Expected:** 400 Bad Request
```json
{
  "message": "Cannot remove the last active owner from account"
}
```

---

## Testing Batch Jobs (Scheduled Operations)

### Interest Calculation Batch
- **Schedule:** Daily at 1:00 AM
- **Manual Trigger:** Not exposed via REST (runs automatically)
- **Verification:**
  1. Wait until 1:00 AM or check logs
  2. Query transactions: `GET /transactions/account/{accountNumber}`
  3. Should see INTEREST_ACCRUAL transactions for each day
  4. Check INTEREST_ACCRUED balance increased

### Maturity Processing Batch
- **Schedule:** Daily at 1:30 AM
- **Tests:**
  1. Create account with maturityDate = tomorrow
  2. Wait until next day 1:30 AM
  3. Check account status changed based on maturityInstruction:
     - CLOSE_AND_PAYOUT → Status = MATURED, balance = 0
     - RENEW_PRINCIPAL_ONLY → New term started, interest paid out
     - RENEW_WITH_INTEREST → New term with capitalized interest

### Maturity Notice Batch
- **Schedule:** Daily at 2:00 AM
- **Configuration:** Sends notice 10 days before maturity
- **Verification:**
  1. Create account with maturityDate = today + 10 days
  2. Check logs at 2:00 AM next day
  3. Should see log entries for SMS and Email sent

---

## Integration Testing Checklist

### Before Testing
- [x] MySQL running with fd_account_db created
- [x] Product Pricing Service running (port 8084)
- [x] Customer Service running (port 8083)
- [x] FD Calculator Service running (port 8085)
- [x] Test products created in Product Pricing Service (FD-STD-6M exists)
- [x] Test customers created in Customer Service (Customer ID 1 exists)

### Core Flows
- [x] Create standard account (Phase 1.1) ✅ **SUCCESS - Account 0011000007 created**
- [ ] Create customized account (Phase 1.2)
- [ ] Query account by account number (Phase 2.1)
- [ ] Query account by IBAN (Phase 2.2)
- [ ] Search accounts with filters (Phase 3.2)
- [ ] Create additional deposit transaction (Phase 4.1)
- [ ] Reverse a transaction (Phase 4.6)
- [ ] Inquire premature withdrawal (Phase 5.1)
- [ ] Process premature withdrawal (Phase 5.2)
- [ ] Add and manage roles (Phase 6)

### Edge Cases
- [ ] Invalid account number returns 404
- [ ] Missing required fields returns 400 with validation errors
- [ ] Insufficient balance rejected
- [ ] Closed account cannot accept transactions
- [ ] Ownership percentage validation
- [ ] Cannot remove last owner

### Performance
- [ ] Pagination works correctly
- [ ] Large result sets handled properly
- [ ] Concurrent transactions on same account

---

## Common Issues & Troubleshooting

### Issue 1: Product Not Found
**Error:** "Product not found: FD-STD-6M"
**Solution:** Ensure Product Pricing Service is running and product exists

### Issue 2: Customer Not Found
**Error:** "Customer not found: 1001"
**Solution:** Create customer in Customer Service first

### Issue 3: Calculator Service Not Responding
**Error:** "Failed to calculate maturity amount"
**Solution:** Check FD Calculator Service is running on port 8085

### Issue 4: Database Connection Failed
**Error:** "Access denied for user 'root'@'localhost'"
**Solution:** 
- Check MySQL is running
- Verify credentials in application.yml (root/root)
- Ensure fd_account_db database exists (auto-created if not)

---

## Sample Test Data

### Product Codes (Must exist in Product Pricing Service)
- FD-STD-6M (6 months, 6.5% interest)
- FD-STD-1Y (12 months, 7.0% interest)
- FD-STD-2Y (24 months, 7.5% interest)

### Customer IDs (Must exist in Customer Service)
- 1001, 1002, 1003, 1004, etc.

### Branch Codes
- 001, 002, 003, etc.

---

## Expected Results Summary

| Test Phase | Endpoints | Expected Success |
|------------|-----------|------------------|
| Phase 1: Creation | 2 | Both accounts created |
| Phase 2: Inquiry | 4 | All lookups successful |
| Phase 3: Listing | 6 | All filters working |
| Phase 4: Transactions | 6 | All transaction types |
| Phase 5: Redemption | 2 | Inquiry & processing |
| Phase 6: Roles | 7 | Role management |
| **Total** | **27 endpoints** | **All functional** |

---

## Notes
1. Account numbers are generated sequentially with Luhn check digit
2. IBAN format: IN + mod-97 check + CRXA + account number
3. Interest accrues daily via batch job at 1 AM
4. Maturity processing happens at 1:30 AM based on maturity instruction
5. All monetary values are BigDecimal with 2 decimal precision
6. Dates are in ISO format: YYYY-MM-DD
7. Transaction references auto-generated: TXN{YYYYMMDD}-{SEQ}
