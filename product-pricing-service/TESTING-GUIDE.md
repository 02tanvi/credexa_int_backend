# Product and Pricing Service - Testing Guide

## ✅ Complete Implementation Summary

### 🎉 ALL FILES CREATED! Service is Ready to Build and Test

**Total Files Created: 60+ files**

#### Project Configuration ✅
- ✅ pom.xml (all dependencies)
- ✅ application.yml (complete configuration)
- ✅ ProductPricingApplication.java (main class)

#### Enums (6 files) ✅
- ✅ ProductType
- ✅ ProductStatus
- ✅ RoleType
- ✅ TransactionType
- ✅ BalanceType
- ✅ ChargeFrequency

#### Entities (7 files) ✅
- ✅ Product (main entity with all relationships)
- ✅ ProductRole
- ✅ ProductCharge
- ✅ InterestRateMatrix (with business logic)
- ✅ ProductTransactionType
- ✅ ProductBalanceType
- ✅ TransactionBalanceRelationship

#### DTOs (11 files) ✅
- ✅ CreateProductRequest
- ✅ UpdateProductRequest
- ✅ ProductResponse
- ✅ ProductSummaryResponse
- ✅ ProductListResponse
- ✅ ProductSearchCriteria
- ✅ ProductRoleRequest & Response
- ✅ ProductChargeRequest & Response
- ✅ InterestRateMatrixRequest & Response
- ✅ ProductTransactionTypeResponse
- ✅ ProductBalanceTypeResponse
- ✅ ApiResponse (wrapper)

#### Repositories (6 files) ✅
- ✅ ProductRepository (with custom queries)
- ✅ InterestRateMatrixRepository (with slab matching logic)
- ✅ ProductChargeRepository
- ✅ ProductRoleRepository
- ✅ ProductTransactionTypeRepository
- ✅ ProductBalanceTypeRepository

#### Services (3 files) ✅
- ✅ ProductService (full CRUD + caching)
- ✅ InterestRateService (rate calculations)
- ✅ ProductMapper (entity-DTO conversions)

#### Controllers (2 files) ✅
- ✅ ProductController (full Swagger docs)
- ✅ InterestRateController (rate APIs)

#### Security (4 files) ✅
- ✅ SecurityConfig
- ✅ JwtAuthenticationFilter
- ✅ JwtUtil
- ✅ OpenApiConfig (Swagger security)

#### Exception Handling (5 files) ✅
- ✅ GlobalExceptionHandler
- ✅ ProductNotFoundException
- ✅ DuplicateProductCodeException
- ✅ InvalidProductException
- ✅ ErrorResponse

#### Documentation (4 files) ✅
- ✅ README.md
- ✅ PROJECT-OVERVIEW.md
- ✅ IMPLEMENTATION-STATUS.md
- ✅ TESTING-GUIDE.md (this file)

---

## 🚀 Step 1: Build the Project

### Navigate to project directory
```cmd
cd c:\Users\dhruv\Coding\bt_khatam\credexa\product-pricing-service
```

### Clean and compile
```cmd
mvn clean compile
```

**Expected Output:** BUILD SUCCESS

### Run the service
```cmd
mvn spring-boot:run
```

**Service will start on:** http://localhost:8084/api/products

---

## 📖 Step 2: Access Swagger UI

Once the service is running, open your browser:

**Swagger UI:** http://localhost:8084/api/products/swagger-ui/index.html

**OpenAPI JSON:** http://localhost:8084/api/products/v3/api-docs

---

## 🔐 Step 3: Get JWT Token (from Login Service)

### Option A: Use existing john_doe token
```bash
# From your login-service, get a token for john_doe
POST http://localhost:8081/api/auth/login
{
  "username": "john_doe",
  "password": "password123"
}

# Copy the JWT token from response
```

### Option B: Use customer-service john_doe
The token you used for customer-service testing will work!

### Add token to Swagger
1. Click **"Authorize"** button in Swagger UI
2. Enter: `Bearer YOUR_JWT_TOKEN_HERE`
3. Click **Authorize**
4. Now all API calls will include the token

---

## 🧪 Step 4: Test Product Creation

### Test 1: Create a Short-Term Fixed Deposit Product

**Endpoint:** `POST /products`

**Request Body:**
```json
{
  "productName": "Short-Term Fixed Deposit",
  "productCode": "FD-SHORT-001",
  "productType": "FIXED_DEPOSIT",
  "description": "Fixed deposit for 6-36 months with attractive interest rates",
  "effectiveDate": "2025-01-01",
  "endDate": null,
  "bankBranchCode": "BR001",
  "currencyCode": "INR",
  "status": "ACTIVE",
  
  "minTermMonths": 6,
  "maxTermMonths": 36,
  "minAmount": 10000,
  "maxAmount": 10000000,
  "minBalanceRequired": 10000,
  "baseInterestRate": 6.5,
  "interestCalculationMethod": "COMPOUND",
  "interestPayoutFrequency": "ON_MATURITY",
  
  "prematureWithdrawalAllowed": true,
  "partialWithdrawalAllowed": false,
  "loanAgainstDepositAllowed": true,
  "autoRenewalAllowed": true,
  "nomineeAllowed": true,
  "jointAccountAllowed": true,
  
  "tdsRate": 10.0,
  "tdsApplicable": true,
  
  "allowedRoles": [
    {
      "roleType": "OWNER",
      "mandatory": true,
      "minCount": 1,
      "maxCount": 1,
      "description": "Primary account holder"
    },
    {
      "roleType": "NOMINEE",
      "mandatory": false,
      "minCount": 0,
      "maxCount": 2,
      "description": "Nominee in case of death"
    }
  ],
  
  "charges": [
    {
      "chargeName": "Premature Withdrawal Penalty",
      "chargeType": "PENALTY",
      "description": "Penalty for premature withdrawal",
      "percentageRate": 1.0,
      "frequency": "ONE_TIME",
      "waivable": true
    }
  ],
  
  "interestRateMatrix": [
    {
      "minAmount": 10000,
      "maxAmount": 100000,
      "minTermMonths": 6,
      "maxTermMonths": 12,
      "interestRate": 6.5,
      "effectiveDate": "2025-01-01"
    },
    {
      "minAmount": 100000,
      "maxAmount": 1000000,
      "minTermMonths": 6,
      "maxTermMonths": 12,
      "interestRate": 7.0,
      "additionalRate": 0.25,
      "effectiveDate": "2025-01-01",
      "remarks": "Premium rate for high-value deposits"
    },
    {
      "minAmount": 10000,
      "maxAmount": 1000000,
      "minTermMonths": 12,
      "maxTermMonths": 36,
      "interestRate": 7.5,
      "effectiveDate": "2025-01-01"
    },
    {
      "minAmount": 1000000,
      "maxAmount": 10000000,
      "minTermMonths": 12,
      "maxTermMonths": 36,
      "customerClassification": "SENIOR_CITIZEN",
      "interestRate": 8.0,
      "additionalRate": 0.5,
      "effectiveDate": "2025-01-01",
      "remarks": "Special rate for senior citizens"
    }
  ]
}
```

**Expected Response:** 201 Created with product details

---

### Test 2: Create a Savings Account Product

**Request Body:**
```json
{
  "productName": "Regular Savings Account",
  "productCode": "SAV-001",
  "productType": "SAVINGS",
  "description": "Basic savings account with 4% interest",
  "effectiveDate": "2025-01-01",
  "bankBranchCode": "BR001",
  "currencyCode": "INR",
  "status": "ACTIVE",
  
  "minAmount": 1000,
  "minBalanceRequired": 1000,
  "baseInterestRate": 4.0,
  "interestCalculationMethod": "SIMPLE",
  "interestPayoutFrequency": "QUARTERLY",
  
  "prematureWithdrawalAllowed": true,
  "partialWithdrawalAllowed": true,
  "nomineeAllowed": true,
  "jointAccountAllowed": true,
  
  "tdsApplicable": false,
  
  "allowedRoles": [
    {
      "roleType": "OWNER",
      "mandatory": true,
      "minCount": 1,
      "maxCount": 2,
      "description": "Account holder(s)"
    }
  ],
  
  "charges": [
    {
      "chargeName": "Minimum Balance Penalty",
      "chargeType": "PENALTY",
      "description": "Charged when balance falls below minimum",
      "fixedAmount": 100,
      "frequency": "MONTHLY",
      "waivable": false
    },
    {
      "chargeName": "SMS Alert Charges",
      "chargeType": "FEE",
      "description": "Monthly SMS alert service",
      "fixedAmount": 20,
      "frequency": "MONTHLY",
      "waivable": true
    }
  ]
}
```

---

## 🔍 Step 5: Test Search and Query APIs

### Get All Products (Paginated)
**Endpoint:** `GET /products?page=0&size=10&sortBy=createdAt&sortDirection=DESC`

### Get Product by ID
**Endpoint:** `GET /products/{id}`

### Get Product by Code
**Endpoint:** `GET /products/code/FD-SHORT-001`

### Get Products by Type
**Endpoint:** `GET /products/type/FIXED_DEPOSIT`

### Get Active Products
**Endpoint:** `GET /products/active`

### Get Currently Active Products
**Endpoint:** `GET /products/currently-active`

### Search Products
**Endpoint:** `POST /products/search`

**Request Body:**
```json
{
  "productName": "Fixed",
  "productType": "FIXED_DEPOSIT",
  "status": "ACTIVE",
  "page": 0,
  "size": 20,
  "sortBy": "createdAt",
  "sortDirection": "DESC"
}
```

---

## 💰 Step 6: Test Interest Rate APIs

### Get Interest Rates for Product
**Endpoint:** `GET /products/{productId}/interest-rates`

### Get Active Interest Rates
**Endpoint:** `GET /products/{productId}/interest-rates/active?date=2025-01-15`

### Find Applicable Rate
**Endpoint:** `GET /products/{productId}/interest-rates/applicable`

**Query Parameters:**
- amount: 250000
- termMonths: 18
- classification: SENIOR_CITIZEN

**Example:**
```
GET /products/1/interest-rates/applicable?amount=250000&termMonths=18&classification=SENIOR_CITIZEN
```

**Expected:** Returns the best matching rate slab (8.5% for senior citizens)

### Calculate Effective Rate
**Endpoint:** `GET /products/{productId}/interest-rates/calculate`

**Query Parameters:**
- baseRate: 6.5
- amount: 250000
- termMonths: 18
- classification: SENIOR_CITIZEN

---

## ✏️ Step 7: Test Update Operations

### Update Product
**Endpoint:** `PUT /products/{id}`

**Request Body:**
```json
{
  "description": "Updated description",
  "baseInterestRate": 7.0,
  "updatedBy": "admin"
}
```

### Update Product Status
**Endpoint:** `PUT /products/{id}/status?status=INACTIVE`

---

## 🗑️ Step 8: Test Delete Operations

### Soft Delete (Set Status to CLOSED)
**Endpoint:** `DELETE /products/{id}`

### Hard Delete (Permanent - Use with Caution!)
**Endpoint:** `DELETE /products/{id}/hard`

---

## 🧪 Step 9: Test Caching

### Test Cache Performance

1. **First Call** - Cache Miss
   ```
   GET /products/1
   ```
   Check logs for: "Fetching product by ID: 1"

2. **Second Call** - Cache Hit
   ```
   GET /products/1
   ```
   Should be faster, served from cache

3. **Update Product** - Cache Eviction
   ```
   PUT /products/1
   ```
   Cache cleared

4. **Third Call** - Cache Miss Again
   ```
   GET /products/1
   ```
   Fresh data loaded from DB

---

## 🔧 Step 10: Verify Database

### Check MySQL Database

```sql
-- Connect to database
USE product_db;

-- View all products
SELECT * FROM products;

-- View product with roles
SELECT p.*, pr.* 
FROM products p 
LEFT JOIN product_roles pr ON p.id = pr.product_id;

-- View product with interest rates
SELECT p.product_name, i.* 
FROM products p 
LEFT JOIN interest_rate_matrix i ON p.id = i.product_id;

-- View product with charges
SELECT p.product_name, c.* 
FROM products p 
LEFT JOIN product_charges c ON p.id = c.product_id;
```

---

## 🐛 Troubleshooting

### Issue 1: Port Already in Use
```
Error: Port 8084 is already in use
```
**Solution:** Stop other service or change port in application.yml

### Issue 2: Database Connection Failed
```
Error: Cannot connect to MySQL
```
**Solution:** 
- Check MySQL is running
- Verify credentials (root/root)
- Create database: `CREATE DATABASE product_db;`

### Issue 3: JWT Authentication Failed
```
Error: 403 Forbidden
```
**Solution:**
- Get fresh JWT token from login-service
- Add "Bearer " prefix in Swagger Authorize
- Check token hasn't expired

### Issue 4: Swagger UI Not Loading
```
Error: 404 Not Found
```
**Solution:**
- Ensure service is running
- Use correct URL: http://localhost:8084/api/products/swagger-ui/index.html
- Check application.yml context-path

---

## ✅ Success Criteria

Your product-pricing-service is working correctly when:

✅ Service starts without errors on port 8084
✅ Swagger UI loads and displays all APIs
✅ JWT authentication works (with token from login-service)
✅ Can create products with roles, charges, and interest rate matrix
✅ Can search and filter products
✅ Interest rate matrix correctly returns applicable rates
✅ Caching improves performance on repeated queries
✅ Database shows all entities correctly saved with relationships
✅ Exception handling returns proper error messages

---

## 🎯 Next Steps

1. **Integration Testing**
   - Test with login-service for authentication
   - Test with customer-service for customer classifications
   - Verify interest rates apply correctly based on customer classification

2. **Load Testing**
   - Test caching performance with high load
   - Verify database query optimization

3. **Business Logic Validation**
   - Verify interest rate slabs select correct rate
   - Test business rule validations
   - Confirm charges calculate correctly

---

## 📞 Support

If you encounter any issues:
1. Check logs in console
2. Verify all services are running (MySQL, login-service)
3. Check database tables created correctly
4. Ensure JWT token is valid and not expired

**Congratulations! Your Product and Pricing Service is Complete! 🎉**
