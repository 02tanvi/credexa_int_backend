# FD Calculator Service - Swagger Testing Guide

## 🎯 Quick Start

1. **Start dependent services first:**
   - `login-service` on port 8082 (for JWT tokens)
   - `product-pricing-service` on port 8084 (for product data)
   - `customer-service` on port 8083 (optional - for customer classifications)

2. **Start FD Calculator Service:**
   ```bash
   cd fd-calculator-service
   mvn spring-boot:run
   ```

3. **Open Swagger UI:** http://localhost:8085/api/calculator/swagger-ui.html

4. **Get JWT Token:**  
   Login via login-service (port 8082) to get Bearer token

---

## 🔐 Authentication Setup

### Get JWT Token (Required for all endpoints)

1. **Login via login-service:**
```bash
POST http://localhost:8082/api/auth/login
Content-Type: application/json

{
  "usernameOrEmailOrMobile": "admin1",
  "password": "Admin@123"
}
```

2. **Copy the token from response:**
```json
{
  "data": {
    "token": "eyJhbGciOi..."
  }
}
```

3. **In Swagger UI:**
   - Click **"Authorize"** button (top right)
   - Enter: `Bearer eyJhbGciOi...`
   - Click **"Authorize"** then **"Close"**

---

## 🧮 Category 1: Standalone Calculations

### 1.1 Simple Interest Calculation (Years)

**Endpoint:** `POST /calculate/standalone`

**Swagger Steps:**
1. Expand **"FD Calculator"** section
2. Click **POST /calculate/standalone**
3. Click **"Try it out"**
4. Enter request body:

```json
{
  "principalAmount": 100000,
  "interestRate": 7.5,
  "tenure": 1,
  "tenureUnit": "YEARS",
  "calculationType": "SIMPLE",
  "tdsRate": 10.0,
  "customerClassifications": []
}
```

5. Click **"Execute"**

**Expected Response:**
```json
{
  "success": true,
  "message": "FD calculation completed successfully",
  "data": {
    "principalAmount": 100000.00,
    "interestRate": 7.50,
    "baseInterestRate": 7.50,
    "additionalInterestRate": 0.00,
    "tenure": 1,
    "tenureUnit": "YEARS",
    "tenureInYears": 1.0,
    "calculationType": "SIMPLE",
    "compoundingFrequency": null,
    "interestEarned": 7500.00,
    "tdsAmount": 750.00,
    "tdsRate": 10.0,
    "maturityAmount": 106750.00,
    "netInterest": 6750.00,
    "startDate": "2025-10-20",
    "maturityDate": "2026-10-20",
    "productId": null,
    "productName": null,
    "productCode": null,
    "customerClassifications": [],
    "monthlyBreakdown": [
      {
        "month": 1,
        "date": "2025-11-20",
        "openingBalance": 100000.00,
        "interestEarned": 625.00,
        "closingBalance": 100625.00,
        "cumulativeInterest": 625.00
      }
      // ... 11 more months
    ]
  }
}
```

**Key Points:**
- Simple interest: `Interest = P × R × T / 100`
- `P = 100000`, `R = 7.5%`, `T = 1 year`
- `Interest = 100000 × 7.5 × 1 / 100 = 7500`
- `TDS = 7500 × 10% = 750`
- `Net Interest = 7500 - 750 = 6750`
- `Maturity = 100000 + 6750 = 106750`

---

### 1.2 Simple Interest Calculation (Months)

**Request Body:**
```json
{
  "principalAmount": 50000,
  "interestRate": 6.5,
  "tenure": 18,
  "tenureUnit": "MONTHS",
  "calculationType": "SIMPLE",
  "tdsRate": 0,
  "customerClassifications": []
}
```

**Expected:**
- Tenure in years: `18/12 = 1.5 years`
- Interest: `50000 × 6.5 × 1.5 / 100 = 4875`
- Maturity: `50000 + 4875 = 54875`

---

### 1.3 Simple Interest Calculation (Days)

**Request Body:**
```json
{
  "principalAmount": 200000,
  "interestRate": 8.0,
  "tenure": 180,
  "tenureUnit": "DAYS",
  "calculationType": "SIMPLE",
  "tdsRate": 10.0,
  "customerClassifications": []
}
```

**Expected:**
- Tenure in years: `180/365 ≈ 0.493 years`
- Interest: `200000 × 8.0 × 0.493 / 100 ≈ 7890`
- TDS: `7890 × 10% = 789`
- Maturity: `200000 + 7890 - 789 ≈ 207101`

---

### 1.4 Compound Interest - Quarterly Compounding

**Request Body:**
```json
{
  "principalAmount": 100000,
  "interestRate": 7.5,
  "tenure": 12,
  "tenureUnit": "MONTHS",
  "calculationType": "COMPOUND",
  "compoundingFrequency": "QUARTERLY",
  "tdsRate": 10.0,
  "customerClassifications": []
}
```

**Expected:**
- Formula: `M = P × (1 + r/n)^(nt)`
- `P = 100000`, `r = 0.075`, `n = 4` (quarterly), `t = 1 year`
- `M = 100000 × (1 + 0.075/4)^(4×1)`
- `M = 100000 × (1.01875)^4 ≈ 107763`
- `Interest = 107763 - 100000 = 7763`
- `TDS = 7763 × 10% = 776.30`
- `Maturity = 107763 - 776.30 = 106986.70`

---

### 1.5 Compound Interest - Monthly Compounding

**Request Body:**
```json
{
  "principalAmount": 150000,
  "interestRate": 8.0,
  "tenure": 24,
  "tenureUnit": "MONTHS",
  "calculationType": "COMPOUND",
  "compoundingFrequency": "MONTHLY",
  "tdsRate": 10.0,
  "customerClassifications": []
}
```

**Expected:**
- Higher maturity than quarterly due to more frequent compounding
- `n = 12` (monthly), `t = 2 years`
- More interest earned compared to simple interest

---

### 1.6 With Customer Classifications (Additional Interest)

**Request Body:**
```json
{
  "principalAmount": 100000,
  "interestRate": 7.0,
  "tenure": 12,
  "tenureUnit": "MONTHS",
  "calculationType": "COMPOUND",
  "compoundingFrequency": "QUARTERLY",
  "tdsRate": 10.0,
  "customerClassifications": ["SENIOR_CITIZEN", "PREMIUM"]
}
```

**Expected:**
- Base rate: `7.0%`
- Additional rate: `0.25% × 2 classifications = 0.5%`
- Final rate: `7.0% + 0.5% = 7.5%`
- Higher maturity due to additional rate

---

## 💼 Category 2: Product-Based Calculations

### 2.1 Calculate with Product Defaults

**Endpoint:** `POST /calculate/product-based`

**Prerequisites:**
- Product-pricing-service must be running on port 8084
- Product ID 1 (Standard FD) must exist

**Request Body:**
```json
{
  "productId": 1,
  "principalAmount": 100000,
  "tenure": 12,
  "tenureUnit": "MONTHS",
  "applyTds": true
}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "FD calculation with product defaults completed successfully",
  "data": {
    "principalAmount": 100000.00,
    "interestRate": 6.50,
    "baseInterestRate": 6.50,
    "additionalInterestRate": 0.00,
    "tenure": 12,
    "tenureUnit": "MONTHS",
    "tenureInYears": 1.0,
    "calculationType": "COMPOUND",
    "compoundingFrequency": "QUARTERLY",
    "interestEarned": 6659.00,
    "tdsAmount": 665.90,
    "tdsRate": 10.0,
    "maturityAmount": 105993.10,
    "netInterest": 5993.10,
    "startDate": "2025-10-20",
    "maturityDate": "2026-10-20",
    "productId": 1,
    "productName": "Standard Fixed Deposit",
    "productCode": "FD-STD-001",
    "customerClassifications": [],
    "monthlyBreakdown": [...]
  }
}
```

**Key Features:**
- Automatically fetches product details from product-pricing-service
- Uses product's base interest rate
- Uses product's TDS settings
- Uses product's calculation method and frequency
- Validates principal amount against product min/max limits
- Validates tenure against product term limits

---

### 2.2 Product-Based with Custom Interest Rate (Capped)

**Request Body:**
```json
{
  "productId": 1,
  "principalAmount": 250000,
  "tenure": 24,
  "tenureUnit": "MONTHS",
  "calculationType": "COMPOUND",
  "compoundingFrequency": "MONTHLY",
  "customInterestRate": 8.5,
  "applyTds": true
}
```

**Expected:**
- Base rate from product: ~6.5%
- Custom rate: 8.5%
- Max allowed: Base + 2% = 8.5%
- Custom rate is within limit, will be accepted
- If custom rate > 8.5%, it will be capped at 8.5%

---

### 2.3 Product-Based with Customer Classification

**Request Body:**
```json
{
  "productId": 2,
  "principalAmount": 100000,
  "tenure": 12,
  "tenureUnit": "MONTHS",
  "customerClassifications": ["SENIOR_CITIZEN"],
  "applyTds": true
}
```

**Expected:**
- Fetches rate for SENIOR_CITIZEN classification from product 2
- Product 2 (Senior Citizen FD) has classification-specific rates
- Higher interest rate due to senior citizen benefits
- `customerClassifications` array in response will show ["SENIOR_CITIZEN"]

---

### 2.4 Product-Based with Customer ID

**Request Body:**
```json
{
  "productId": 1,
  "principalAmount": 150000,
  "tenure": 18,
  "tenureUnit": "MONTHS",
  "customerId": 1,
  "applyTds": true
}
```

**Expected:**
- Fetches customer classification from customer-service
- If customer has classification (e.g., PREMIUM), additional rate applied
- Combines customer's classification with any manually provided classifications
- Max 2 classifications total

---

### 2.5 Validation: Principal Below Minimum

**Request Body:**
```json
{
  "productId": 1,
  "principalAmount": 5000,
  "tenure": 12,
  "tenureUnit": "MONTHS"
}
```

**Expected Error:**
```json
{
  "success": false,
  "message": "Principal amount ₹5000 is below minimum ₹10000 for product FD-STD-001",
  "status": 400
}
```

---

### 2.6 Validation: Tenure Below Minimum

**Request Body:**
```json
{
  "productId": 1,
  "principalAmount": 100000,
  "tenure": 3,
  "tenureUnit": "MONTHS"
}
```

**Expected Error:**
```json
{
  "success": false,
  "message": "Tenure 3 months is below minimum 6 months for product FD-STD-001",
  "status": 400
}
```

---

## 🔄 Category 3: Scenario Comparison

### 3.1 Compare Simple vs Compound Interest

**Endpoint:** `POST /compare`

**Request Body:**
```json
{
  "commonPrincipal": 100000,
  "scenarios": [
    {
      "principalAmount": 100000,
      "interestRate": 7.5,
      "tenure": 12,
      "tenureUnit": "MONTHS",
      "calculationType": "SIMPLE",
      "tdsRate": 10.0
    },
    {
      "principalAmount": 100000,
      "interestRate": 7.5,
      "tenure": 12,
      "tenureUnit": "MONTHS",
      "calculationType": "COMPOUND",
      "compoundingFrequency": "QUARTERLY",
      "tdsRate": 10.0
    },
    {
      "principalAmount": 100000,
      "interestRate": 7.5,
      "tenure": 12,
      "tenureUnit": "MONTHS",
      "calculationType": "COMPOUND",
      "compoundingFrequency": "MONTHLY",
      "tdsRate": 10.0
    }
  ]
}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Successfully compared 3 FD scenarios",
  "data": {
    "scenarios": [
      {
        "calculationType": "SIMPLE",
        "maturityAmount": 106750.00,
        "interestEarned": 7500.00
      },
      {
        "calculationType": "COMPOUND",
        "compoundingFrequency": "QUARTERLY",
        "maturityAmount": 106986.70,
        "interestEarned": 7763.00
      },
      {
        "calculationType": "COMPOUND",
        "compoundingFrequency": "MONTHLY",
        "maturityAmount": 107015.50,
        "interestEarned": 7795.00
      }
    ],
    "bestScenario": {
      "calculationType": "COMPOUND",
      "compoundingFrequency": "MONTHLY",
      "maturityAmount": 107015.50
    },
    "bestScenarioIndex": 2
  }
}
```

**Key Insights:**
- Monthly compounding gives highest returns
- Compound interest always better than simple for same rate
- Higher compounding frequency = higher returns

---

### 3.2 Compare Different Tenures

**Request Body:**
```json
{
  "commonPrincipal": 100000,
  "scenarios": [
    {
      "principalAmount": 100000,
      "interestRate": 7.0,
      "tenure": 6,
      "tenureUnit": "MONTHS",
      "calculationType": "COMPOUND",
      "compoundingFrequency": "QUARTERLY",
      "tdsRate": 10.0
    },
    {
      "principalAmount": 100000,
      "interestRate": 7.0,
      "tenure": 12,
      "tenureUnit": "MONTHS",
      "calculationType": "COMPOUND",
      "compoundingFrequency": "QUARTERLY",
      "tdsRate": 10.0
    },
    {
      "principalAmount": 100000,
      "interestRate": 7.0,
      "tenure": 24,
      "tenureUnit": "MONTHS",
      "calculationType": "COMPOUND",
      "compoundingFrequency": "QUARTERLY",
      "tdsRate": 10.0
    }
  ]
}
```

**Expected:**
- Longer tenures = more interest earned
- bestScenario will be the 24-month option

---

### 3.3 Compare Different Interest Rates

**Request Body:**
```json
{
  "commonPrincipal": 100000,
  "scenarios": [
    {
      "principalAmount": 100000,
      "interestRate": 6.0,
      "tenure": 12,
      "tenureUnit": "MONTHS",
      "calculationType": "COMPOUND",
      "compoundingFrequency": "QUARTERLY",
      "tdsRate": 10.0
    },
    {
      "principalAmount": 100000,
      "interestRate": 7.0,
      "tenure": 12,
      "tenureUnit": "MONTHS",
      "calculationType": "COMPOUND",
      "compoundingFrequency": "QUARTERLY",
      "tdsRate": 10.0
    },
    {
      "principalAmount": 100000,
      "interestRate": 8.0,
      "tenure": 12,
      "tenureUnit": "MONTHS",
      "calculationType": "COMPOUND",
      "compoundingFrequency": "QUARTERLY",
      "tdsRate": 10.0
    }
  ]
}
```

**Expected:**
- Higher interest rates = higher maturity amount
- bestScenario will be 8% option

---

## 📋 Complete Test Checklist

### ✅ Standalone Calculations
- [ ] Simple interest with years tenure
- [ ] Simple interest with months tenure
- [ ] Simple interest with days tenure
- [ ] Compound interest with quarterly compounding
- [ ] Compound interest with monthly compounding
- [ ] Compound interest with daily compounding
- [ ] Compound interest with semi-annual compounding
- [ ] Compound interest with annual compounding
- [ ] Calculation with TDS
- [ ] Calculation without TDS
- [ ] Calculation with customer classifications
- [ ] Calculation with multiple classifications (max 2)
- [ ] Monthly breakdown generation

### ✅ Product-Based Calculations
- [ ] Calculate with product defaults (Product 1)
- [ ] Calculate with product defaults (Product 2)
- [ ] Calculate with custom interest rate (within cap)
- [ ] Calculate with custom interest rate (exceeding cap - should cap at +2%)
- [ ] Calculate with customer classification
- [ ] Calculate with customer ID (fetches from customer-service)
- [ ] Calculate with both customerId and customerClassifications
- [ ] Validation: Principal below minimum (400 error)
- [ ] Validation: Principal above maximum (400 error)
- [ ] Validation: Tenure below minimum (400 error)
- [ ] Validation: Tenure above maximum (400 error)
- [ ] Product not found (404 error)

### ✅ Scenario Comparison
- [ ] Compare simple vs compound
- [ ] Compare different compounding frequencies
- [ ] Compare different tenures
- [ ] Compare different interest rates
- [ ] Compare with common principal
- [ ] Identify best scenario correctly

### ✅ Integration Tests
- [ ] Integration with product-pricing-service
- [ ] Integration with customer-service
- [ ] Caching functionality (products cache)
- [ ] Caching functionality (interest rates cache)
- [ ] Caching functionality (customer classifications cache)

---

## 🎯 Complete Integration Test Scenario

### Scenario: Compare Products with Customer Classification

**Step 1: Get JWT Token**
- Login as admin1

**Step 2: Test Standalone Simple Interest**
```json
POST /calculate/standalone
{
  "principalAmount": 100000,
  "interestRate": 7.0,
  "tenure": 12,
  "tenureUnit": "MONTHS",
  "calculationType": "SIMPLE",
  "tdsRate": 10.0
}
```
Note the maturity amount.

**Step 3: Test Product 1 (Standard FD)**
```json
POST /calculate/product-based
{
  "productId": 1,
  "principalAmount": 100000,
  "tenure": 12,
  "tenureUnit": "MONTHS",
  "applyTds": true
}
```
Compare with Step 2.

**Step 4: Test Product 2 with Senior Citizen**
```json
POST /calculate/product-based
{
  "productId": 2,
  "principalAmount": 100000,
  "tenure": 12,
  "tenureUnit": "MONTHS",
  "customerClassifications": ["SENIOR_CITIZEN"],
  "applyTds": true
}
```
Should have higher interest rate.

**Step 5: Compare All Three**
```json
POST /compare
{
  "commonPrincipal": 100000,
  "scenarios": [
    // Use the 3 scenarios from steps 2, 3, 4
  ]
}
```

---

## 🐛 Troubleshooting

### Issue: "401 Unauthorized"
**Fix:** Get fresh JWT token from login-service and authorize in Swagger

### Issue: "Product not found" (404)
**Fix:** 
- Ensure product-pricing-service is running on port 8084
- Check that product ID exists: `GET http://localhost:8084/api/products/1`

### Issue: "Failed to fetch product details"
**Fix:**
- Verify product-pricing-service is accessible
- Check network connectivity
- Review application.yml service URLs

### Issue: "Principal amount validation error"
**Fix:**
- Check product min/max amount limits
- Use `GET /products/{id}` to see valid ranges

### Issue: "Caching not working"
**Fix:**
- Check cache configuration in application.yml
- Verify Caffeine dependency in pom.xml
- Check logs for cache hits/misses

---

## 📝 Quick Reference

### Calculation Formulas

**Simple Interest:**
```
M = P + (P × r × t / 100)
Where:
  M = Maturity amount
  P = Principal
  r = Annual rate (%)
  t = Time in years
```

**Compound Interest:**
```
M = P × (1 + r/n)^(nt)
Where:
  M = Maturity amount
  P = Principal
  r = Annual rate (as decimal, e.g., 0.075 for 7.5%)
  n = Compounding periods per year
  t = Time in years
```

### Compounding Frequencies
| Frequency | Periods/Year | Description |
|-----------|--------------|-------------|
| DAILY | 365 | Daily compounding |
| MONTHLY | 12 | Monthly compounding |
| QUARTERLY | 4 | Quarterly compounding |
| SEMI_ANNUALLY | 2 | Half-yearly compounding |
| ANNUALLY | 1 | Yearly compounding |

### Tenure Units
| Unit | Conversion |
|------|------------|
| DAYS | 1 day |
| MONTHS | 30 days |
| YEARS | 365 days |

### Customer Classifications
- REGULAR
- SENIOR_CITIZEN (60+ years)
- SUPER_SENIOR (80+ years)
- PREMIUM
- VIP

### Service URLs
- **FD Calculator:** http://localhost:8085/api/calculator
- **Product Pricing:** http://localhost:8084/api/products
- **Customer Service:** http://localhost:8083/api/customers
- **Login Service:** http://localhost:8082/api/auth

---

## 🎉 Success Criteria

You've successfully tested the FD Calculator Service when:

✅ All standalone calculations (simple & compound) work correctly  
✅ Product-based calculations integrate with product-pricing-service  
✅ Customer classifications add correct additional interest  
✅ Rate capping works (max 2% additional)  
✅ All validation errors work correctly  
✅ Scenario comparison identifies best option  
✅ Monthly breakdown generated for all calculations  
✅ TDS calculations accurate  
✅ Caching improves performance  

**Total Endpoints Tested:** 4 main endpoints (calculate/standalone, calculate/product-based, compare, health)

**Integration Points Verified:** 3 services (product-pricing, customer, login)

**Calculation Types Verified:** 2 types (simple, compound) × 5 frequencies = 10 combinations
