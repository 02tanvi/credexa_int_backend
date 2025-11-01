# Product Pricing Service - Create Test Products Guide

## Service Information
- **Base URL:** http://localhost:8084/api/products
- **Swagger UI:** http://localhost:8084/api/products/swagger-ui.html
- **Database:** product_db (MySQL)
- **Port:** 8084

---

## Quick Overview

You need to create **3 FD products** for testing the FD Account Service:
1. **FD-STD-6M** - 6 Month FD (6.5% interest)
2. **FD-STD-1Y** - 12 Month FD (7.0% interest)
3. **FD-STD-2Y** - 24 Month FD (7.5% interest)

---

## Product 1: FD-STD-6M (6 Month Fixed Deposit)

**Endpoint:** `POST /products`

**Request Body:**
```json
{
  "productName": "6 Month Fixed Deposit",
  "productCode": "FD-STD-6M",
  "productType": "FIXED_DEPOSIT",
  "description": "Standard 6 month fixed deposit with competitive interest rates",
  "effectiveDate": "2025-01-01",
  "endDate": null,
  "bankBranchCode": "001",
  "currencyCode": "INR",
  "status": "ACTIVE",
  "minTermMonths": 6,
  "maxTermMonths": 6,
  "minAmount": 1000,
  "maxAmount": 10000000,
  "minBalanceRequired": 1000,
  "baseInterestRate": 6.50,
  "interestCalculationMethod": "SIMPLE",
  "interestPayoutFrequency": "ON_MATURITY",
  "prematureWithdrawalAllowed": true,
  "partialWithdrawalAllowed": false,
  "loanAgainstDepositAllowed": true,
  "autoRenewalAllowed": true,
  "nomineeAllowed": true,
  "jointAccountAllowed": true,
  "tdsRate": 10.00,
  "tdsApplicable": true,
  "allowedRoles": [
    {
      "roleType": "OWNER",
      "mandatory": true,
      "minCount": 1,
      "maxCount": 1,
      "description": "Primary account owner"
    },
    {
      "roleType": "NOMINEE",
      "mandatory": false,
      "minCount": 0,
      "maxCount": 3,
      "description": "Beneficiary in case of owner's death"
    }
  ],
  "charges": [],
  "interestRateMatrix": [
    {
      "minAmount": 1000,
      "maxAmount": 100000,
      "minTermMonths": 6,
      "maxTermMonths": 6,
      "customerClassification": "GENERAL",
      "interestRate": 6.50,
      "additionalRate": 0,
      "effectiveDate": "2025-01-01",
      "endDate": null,
      "remarks": "Base rate for general customers"
    },
    {
      "minAmount": 100001,
      "maxAmount": 1000000,
      "minTermMonths": 6,
      "maxTermMonths": 6,
      "customerClassification": "GENERAL",
      "interestRate": 6.75,
      "additionalRate": 0.25,
      "effectiveDate": "2025-01-01",
      "endDate": null,
      "remarks": "Higher amount slab"
    },
    {
      "minAmount": 1000,
      "maxAmount": 10000000,
      "minTermMonths": 6,
      "maxTermMonths": 6,
      "customerClassification": "SENIOR_CITIZEN",
      "interestRate": 7.00,
      "additionalRate": 0.50,
      "effectiveDate": "2025-01-01",
      "endDate": null,
      "remarks": "Senior citizen additional rate"
    }
  ]
}
```

**Expected Response:** 201 Created
```json
{
  "success": true,
  "message": "Product created successfully",
  "data": {
    "id": 1,
    "productCode": "FD-STD-6M",
    "productName": "6 Month Fixed Deposit",
    "productType": "FIXED_DEPOSIT",
    "status": "ACTIVE",
    "baseInterestRate": 6.50,
    "minAmount": 1000,
    "maxAmount": 10000000
  }
}
```

---

## Product 2: FD-STD-1Y (12 Month Fixed Deposit)

**Endpoint:** `POST /products`

**Request Body:**
```json
{
  "productName": "12 Month Fixed Deposit",
  "productCode": "FD-STD-1Y",
  "productType": "FIXED_DEPOSIT",
  "description": "Standard 1 year fixed deposit with attractive returns",
  "effectiveDate": "2025-01-01",
  "endDate": null,
  "bankBranchCode": "001",
  "currencyCode": "INR",
  "status": "ACTIVE",
  "minTermMonths": 12,
  "maxTermMonths": 12,
  "minAmount": 1000,
  "maxAmount": 10000000,
  "minBalanceRequired": 1000,
  "baseInterestRate": 7.00,
  "interestCalculationMethod": "SIMPLE",
  "interestPayoutFrequency": "ON_MATURITY",
  "prematureWithdrawalAllowed": true,
  "partialWithdrawalAllowed": false,
  "loanAgainstDepositAllowed": true,
  "autoRenewalAllowed": true,
  "nomineeAllowed": true,
  "jointAccountAllowed": true,
  "tdsRate": 10.00,
  "tdsApplicable": true,
  "allowedRoles": [
    {
      "roleType": "OWNER",
      "mandatory": true,
      "minCount": 1,
      "maxCount": 1,
      "description": "Primary account owner"
    },
    {
      "roleType": "NOMINEE",
      "mandatory": false,
      "minCount": 0,
      "maxCount": 3,
      "description": "Beneficiary"
    }
  ],
  "charges": [],
  "interestRateMatrix": [
    {
      "minAmount": 1000,
      "maxAmount": 100000,
      "minTermMonths": 12,
      "maxTermMonths": 12,
      "customerClassification": "GENERAL",
      "interestRate": 7.00,
      "additionalRate": 0,
      "effectiveDate": "2025-01-01",
      "endDate": null,
      "remarks": "Base rate for 1 year"
    },
    {
      "minAmount": 100001,
      "maxAmount": 1000000,
      "minTermMonths": 12,
      "maxTermMonths": 12,
      "customerClassification": "GENERAL",
      "interestRate": 7.25,
      "additionalRate": 0.25,
      "effectiveDate": "2025-01-01",
      "endDate": null,
      "remarks": "Higher slab for 1 year"
    },
    {
      "minAmount": 1000,
      "maxAmount": 10000000,
      "minTermMonths": 12,
      "maxTermMonths": 12,
      "customerClassification": "SENIOR_CITIZEN",
      "interestRate": 7.50,
      "additionalRate": 0.50,
      "effectiveDate": "2025-01-01",
      "endDate": null,
      "remarks": "Senior citizen rate"
    }
  ]
}
```

---

## Product 3: FD-STD-2Y (24 Month Fixed Deposit)

**Endpoint:** `POST /products`

**Request Body:**
```json
{
  "productName": "24 Month Fixed Deposit",
  "productCode": "FD-STD-2Y",
  "productType": "FIXED_DEPOSIT",
  "description": "Standard 2 year fixed deposit with maximum returns",
  "effectiveDate": "2025-01-01",
  "endDate": null,
  "bankBranchCode": "001",
  "currencyCode": "INR",
  "status": "ACTIVE",
  "minTermMonths": 24,
  "maxTermMonths": 24,
  "minAmount": 1000,
  "maxAmount": 10000000,
  "minBalanceRequired": 1000,
  "baseInterestRate": 7.50,
  "interestCalculationMethod": "SIMPLE",
  "interestPayoutFrequency": "ON_MATURITY",
  "prematureWithdrawalAllowed": true,
  "partialWithdrawalAllowed": false,
  "loanAgainstDepositAllowed": true,
  "autoRenewalAllowed": true,
  "nomineeAllowed": true,
  "jointAccountAllowed": true,
  "tdsRate": 10.00,
  "tdsApplicable": true,
  "allowedRoles": [
    {
      "roleType": "OWNER",
      "mandatory": true,
      "minCount": 1,
      "maxCount": 1,
      "description": "Primary account owner"
    },
    {
      "roleType": "NOMINEE",
      "mandatory": false,
      "minCount": 0,
      "maxCount": 3,
      "description": "Beneficiary"
    }
  ],
  "charges": [],
  "interestRateMatrix": [
    {
      "minAmount": 1000,
      "maxAmount": 100000,
      "minTermMonths": 24,
      "maxTermMonths": 24,
      "customerClassification": "GENERAL",
      "interestRate": 7.50,
      "additionalRate": 0,
      "effectiveDate": "2025-01-01",
      "endDate": null,
      "remarks": "Base rate for 2 years"
    },
    {
      "minAmount": 100001,
      "maxAmount": 1000000,
      "minTermMonths": 24,
      "maxTermMonths": 24,
      "customerClassification": "GENERAL",
      "interestRate": 7.75,
      "additionalRate": 0.25,
      "effectiveDate": "2025-01-01",
      "endDate": null,
      "remarks": "Higher slab for 2 years"
    },
    {
      "minAmount": 1000,
      "maxAmount": 10000000,
      "minTermMonths": 24,
      "maxTermMonths": 24,
      "customerClassification": "SENIOR_CITIZEN",
      "interestRate": 8.00,
      "additionalRate": 0.50,
      "effectiveDate": "2025-01-01",
      "endDate": null,
      "remarks": "Senior citizen rate"
    }
  ]
}
```

---

## Step-by-Step Instructions

### 1. Open Swagger UI
Navigate to: **http://localhost:8084/api/products/swagger-ui.html**

### 2. Locate POST /products Endpoint
- Look for **"Product Management"** section
- Find **POST /products** - "Create a new product"
- Click to expand it

### 3. Create Each Product
For each of the 3 products above:

1. Click **"Try it out"** button
2. Copy the entire JSON request body from above
3. Paste into the request body field
4. Click **"Execute"**
5. Verify you get **201 Created** response
6. Check the response contains the product data

### 4. Verify Products Created

**Option 1: Get All Products**
- Use **GET /products** endpoint
- Should return all 3 products

**Option 2: Get by Code**
- Use **GET /products/code/{code}** endpoint
- Try: `FD-STD-6M`, `FD-STD-1Y`, `FD-STD-2Y`
- Each should return the product details

---

## Field Explanations

### Required Fields (Must be present):
- **productName** - Display name of the product
- **productCode** - Unique identifier (FD-STD-6M, etc.)
- **productType** - Enum: FIXED_DEPOSIT, TAX_SAVER_FD, SENIOR_CITIZEN_FD, etc.
- **effectiveDate** - When product becomes active (YYYY-MM-DD format)
- **bankBranchCode** - Branch identifier
- **currencyCode** - 3-letter ISO code (INR, USD, etc.)

### Important Optional Fields:
- **status** - DRAFT, ACTIVE, INACTIVE, SUSPENDED, CLOSED (default: DRAFT, we use ACTIVE)
- **minTermMonths** / **maxTermMonths** - Term range in months
- **minAmount** / **maxAmount** - Deposit amount range
- **baseInterestRate** - Default interest rate
- **interestCalculationMethod** - SIMPLE, COMPOUND, etc.
- **interestPayoutFrequency** - ON_MATURITY, MONTHLY, QUARTERLY, etc.
- **tdsRate** - Tax deduction rate (10% = 10.00)

### Child Objects:

#### allowedRoles (Array):
- **roleType** - OWNER, NOMINEE, CO_OWNER, GUARDIAN, etc.
- **mandatory** - true/false
- **minCount** / **maxCount** - Number constraints

#### interestRateMatrix (Array):
- Defines interest rates based on amount slabs, term, and customer type
- **minAmount** / **maxAmount** - Amount range
- **minTermMonths** / **maxTermMonths** - Term range
- **customerClassification** - GENERAL, SENIOR_CITIZEN, etc.
- **interestRate** - Applicable rate
- **additionalRate** - Extra rate on top of base
- **effectiveDate** - When this rate becomes active

#### charges (Array):
- Empty for now, but can contain account maintenance fees, etc.

---

## Troubleshooting

### Error: "Product code already exists"
- Product with same code already created
- Either delete existing or use different code
- To check: GET /products/code/FD-STD-6M

### Error: "Currency code must be 3 characters"
- Use exactly 3 letters: INR, USD, EUR, etc.

### Error: "Effective date is required"
- Must provide date in format: YYYY-MM-DD
- Example: "2025-01-01"

### Error: Validation errors
- Check all required fields are present
- Ensure productType is valid enum value
- Verify roleType in allowedRoles is valid

---

## After Creating Products

### Test Product Lookup from FD Account Service

Once all 3 products are created, test the integration:

1. Go to FD Account Service Swagger: http://localhost:8086/api/fd-accounts/swagger-ui.html
2. Try **POST /accounts** with the Phase 1.1 request from SWAGGER-TESTING-GUIDE.md
3. Should now succeed without "Product not found" error

### Expected Integration Call

FD Account Service will call:
```
GET http://localhost:8084/api/products/code/FD-STD-6M
```

Should return:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "productCode": "FD-STD-6M",
    "productName": "6 Month Fixed Deposit",
    "productType": "FIXED_DEPOSIT",
    "baseInterestRate": 6.50,
    "minTermMonths": 6,
    "maxTermMonths": 6,
    "minAmount": 1000,
    "maxAmount": 10000000,
    "prematureWithdrawalAllowed": true,
    "tdsApplicable": true,
    "tdsRate": 10.00,
    "status": "ACTIVE"
  }
}
```

---

## Quick Reference

| Product Code | Term | Interest Rate | Purpose |
|--------------|------|---------------|---------|
| FD-STD-6M | 6 months | 6.50% | Testing Phase 1.1 & 1.2 |
| FD-STD-1Y | 12 months | 7.00% | Additional testing |
| FD-STD-2Y | 24 months | 7.50% | Long-term FD testing |

---

## Summary Checklist

- [ ] Product Pricing Service running on port 8084
- [ ] MySQL database `product_db` accessible
- [ ] Swagger UI accessible at http://localhost:8084/api/products/swagger-ui.html
- [ ] Created Product 1: FD-STD-6M (201 response)
- [ ] Created Product 2: FD-STD-1Y (201 response)
- [ ] Created Product 3: FD-STD-2Y (201 response)
- [ ] Verified products with GET /products or GET /products/code/{code}
- [ ] Ready to test FD Account Service creation endpoints

---

**You're all set! Now retry the FD Account creation from Phase 1.1** 🚀
