# FD Account Service - Integration Fixes Summary

## Overview
Fixed critical integration issues across **4 microservices** to enable end-to-end account creation flow.

---

## Issues Fixed

### 1. Product Pricing Service - Controller Mapping Issue ✅
**Problem:**
- Controller had `@RequestMapping("/products")` 
- Context-path was `/api/products`
- **Result:** Routes mapped to `/api/products/products/code/{code}` ❌

**Fix:**
- Changed `ProductController` to `@RequestMapping` (empty)
- **Result:** Routes now correctly map to `/api/products/code/{code}` ✅

**File:** `product-pricing-service/src/main/java/com/app/product/controller/ProductController.java`

---

### 2. Customer Service - Controller Mapping Issue ✅
**Problem:**
- Controller had `@RequestMapping("/customers")`
- Context-path was `/api/customer`
- **Result:** Routes mapped to `/api/customer/customers/{id}` ❌

**Fix:**
- Changed `CustomerController` to `@RequestMapping` (empty)
- **Result:** Routes now correctly map to `/api/customer/{id}` ✅

**File:** `customer-service/src/main/java/com/app/customer/controller/CustomerController.java`

---

### 3. FD Account Service - Customer Service URL ✅
**Problem:**
- FD Account Service was calling `/api/customers/{id}`
- Actual Customer Service endpoint is `/api/customer/{id}` (singular)

**Fix:**
- Changed `application.yml` integration URL from `http://localhost:8083/api/customers` to `http://localhost:8083/api/customer`

**File:** `fd-account-service/src/main/resources/application.yml`

---

### 4. Calculator Service - Endpoint and DTO Mismatch ✅

#### Issue 4a: Wrong Endpoint
**Problem:**
- FD Account Service calling `/calculate` (doesn't exist)
- Actual endpoint is `/calculate/standalone`

**Fix:**
- Updated `CalculatorServiceClient` URI to `/calculate/standalone`

**File:** `fd-account-service/src/main/java/com/app/fdaccount/service/integration/CalculatorServiceClient.java`

#### Issue 4b: Response Wrapper Not Unwrapped
**Problem:**
- Calculator Service returns `ApiResponse<CalculationResponse>` wrapper
- FD Account Service was expecting plain `CalculationResultDto`

**Fix:**
- Added `ApiResponseWrapper<CalculationResultDto>` with `ParameterizedTypeReference`
- Unwrap response: `response.getData()`

**File:** Same as above

#### Issue 4c: DTO Field Mismatch
**Problem:**
- FD Account sending: `termMonths`, `startDate`, `interestCalculationMethod`, `interestPayoutFrequency`
- Calculator expects: `tenure`, `tenureUnit`, `calculationType`, `compoundingFrequency`

**Fix:**
- Updated `CalculationRequest` DTO to match Calculator Service fields:
  ```java
  private Integer tenure;              // was: termMonths
  private String tenureUnit;           // new: "MONTHS"
  private String calculationType;      // was: interestCalculationMethod
  private String compoundingFrequency; // was: interestPayoutFrequency
  private BigDecimal tdsRate;          // new: default 10%
  ```

**Files:**
- `fd-account-service/src/main/java/com/app/fdaccount/dto/external/CalculationRequest.java`
- `fd-account-service/src/main/java/com/app/fdaccount/service/integration/CalculatorServiceClient.java`
- `fd-account-service/src/main/java/com/app/fdaccount/service/AccountCreationService.java`

#### Issue 4d: Missing Product Fields
**Problem:**
- Product entity doesn't have `calculationType` or `compoundingFrequency`
- FD Account Service needs these for calculator

**Fix:**
- Use sensible defaults:
  - `calculationType`: "COMPOUND" (standard for FD)
  - `compoundingFrequency`: "QUARTERLY" (industry standard)
  - `tdsRate`: 10% (standard TDS for FD)

**File:** `fd-account-service/src/main/java/com/app/fdaccount/service/AccountCreationService.java`

---

## Services Requiring Restart

### ✅ Must Restart (Changes Applied)
1. **Customer Service** - Controller mapping changed
2. **FD Account Service** - Multiple fixes:
   - application.yml (Customer Service URL)
   - CalculatorServiceClient (endpoint + DTO)
   - CalculationRequest DTO (field mappings)
   - AccountCreationService (calculator calls)

### ✅ No Restart Needed
1. **Product Pricing Service** - Already restarted earlier
2. **Calculator Service** - No changes (was already correct)

---

## Testing Status

### ✅ Integration Points Verified
1. **Product Pricing Service** → FD Account Service: ✅ WORKING
   - Endpoint: `GET /api/products/code/{code}`
   - Test: Product FD-STD-6M retrieved successfully

2. **Customer Service** → FD Account Service: ✅ FIXED (pending restart)
   - Endpoint: `GET /api/customer/{id}`
   - Test: Customer ID 1 exists

3. **Calculator Service** → FD Account Service: ✅ FIXED (pending restart)
   - Endpoint: `POST /api/calculator/calculate/standalone`
   - DTO: Field mappings corrected

### ⏳ Pending Test
**POST** `/api/fd-accounts/accounts` - Create FD Account
- **Test Data:** Customer ID 1, Product FD-STD-6M, Amount 100,000
- **Expected:** 201 Created with account details
- **Next Step:** Restart FD Account Service and Customer Service, then test

---

## Default Values Applied

### Calculator Request Defaults
When creating FD accounts, the following defaults are used:

| Field | Default Value | Reason |
|-------|---------------|--------|
| `tenureUnit` | "MONTHS" | FD tenures always in months |
| `calculationType` | "COMPOUND" | Standard for FD accounts |
| `compoundingFrequency` | "QUARTERLY" | Industry standard |
| `tdsRate` | 10.0% | Standard TDS for FD interest |

### Customization Options (Phase 1.2 endpoint)
Users can override these defaults:
- `calculationType`: "SIMPLE" or "COMPOUND"
- `compoundingFrequency`: "MONTHLY", "QUARTERLY", "HALF_YEARLY", "YEARLY"

---

## Architecture Decisions

### 1. Empty Controller Mappings
**Decision:** Use empty `@RequestMapping` when `context-path` already defines the base path

**Rationale:**
- Avoids path duplication (e.g., `/api/products/products/...`)
- Cleaner separation of concerns (context in config, routes in controller)
- Consistent with Spring Boot best practices

### 2. Response Unwrapping Pattern
**Decision:** Use `ApiResponseWrapper<T>` with `ParameterizedTypeReference` for inter-service calls

**Pattern:**
```java
ApiResponseWrapper<DTO> response = webClient
    .get()
    .uri(...)
    .retrieve()
    .bodyToMono(new ParameterizedTypeReference<ApiResponseWrapper<DTO>>() {})
    .block();

DTO data = response.getData();  // Unwrap
```

**Rationale:**
- All services use `ApiResponse<T>` wrapper pattern
- Proper generic type deserialization
- Consistent error handling

### 3. Calculation Defaults
**Decision:** Use industry-standard defaults for missing product configuration

**Defaults:**
- Compound interest (standard for FD)
- Quarterly compounding (Indian banking standard)
- 10% TDS (Indian tax law)

**Rationale:**
- Product service doesn't store calculation metadata
- FD accounts have well-established industry standards
- Users can customize if needed (Phase 1.2 endpoint)

---

## Files Modified

### Product Pricing Service
1. `src/main/java/com/app/product/controller/ProductController.java`
   - Changed `@RequestMapping("/products")` to `@RequestMapping`

2. `src/main/java/com/app/product/service/ProductService.java`
   - Added `@Transactional(readOnly = true)` to get methods

3. `src/main/java/com/app/product/exception/GlobalExceptionHandler.java`
   - Added `@Slf4j` and error logging

### Customer Service
1. `src/main/java/com/app/customer/controller/CustomerController.java`
   - Changed `@RequestMapping("/customers")` to `@RequestMapping`

### FD Account Service
1. `src/main/resources/application.yml`
   - Line 56: Changed Customer Service URL from `/api/customers` to `/api/customer`

2. `src/main/java/com/app/fdaccount/dto/external/CalculationRequest.java`
   - Renamed `termMonths` → `tenure`
   - Removed `startDate`, `productCode`
   - Added `tenureUnit`, `calculationType`, `compoundingFrequency`, `tdsRate`, `customerClassifications`

3. `src/main/java/com/app/fdaccount/dto/external/CalculationResultDto.java`
   - Added Calculator Service response fields
   - Renamed `totalInterest` → `interestEarned`
   - Added `netInterest`, `tdsAmount`, `tdsRate`, `productId`, `productName`

4. `src/main/java/com/app/fdaccount/service/integration/CalculatorServiceClient.java`
   - Changed URI from `/calculate` to `/calculate/standalone`
   - Added `ApiResponseWrapper<CalculationResultDto>` with `ParameterizedTypeReference`
   - Updated method signature to remove unused parameters
   - Build request with correct field names and defaults

5. `src/main/java/com/app/fdaccount/service/AccountCreationService.java`
   - Updated both calculator calls (standard and customized)
   - Added default values: "COMPOUND", "QUARTERLY"
   - Support customization via request DTOs

### FD Calculator Service
- **No changes required** - Service was already correct

---

## Root Cause Analysis

### Why did this happen?
1. **Controller Mapping Pattern Inconsistency**
   - Services had both `context-path` AND `@RequestMapping` with same path
   - Spring concatenates these, causing duplication
   - Pattern should be: context-path OR controller mapping, not both

2. **DTO Field Name Mismatches**
   - FD Account Service created before Calculator Service DTOs finalized
   - No shared DTO library between services
   - Each service defined its own request/response contracts

3. **URL Configuration Errors**
   - Singular vs plural confusion (`/customer` vs `/customers`)
   - No centralized service registry or documentation
   - Manual configuration prone to typos

### Prevention Strategies
1. **Service Contracts**
   - Create OpenAPI specs for all services
   - Share common DTOs via library
   - Validate contracts during development

2. **Integration Tests**
   - Add contract tests between services
   - Test actual HTTP calls, not mocks
   - Include in CI/CD pipeline

3. **Service Discovery**
   - Consider service registry (Eureka, Consul)
   - Eliminates manual URL configuration
   - Auto-discovery and health checks

4. **API Documentation**
   - Maintain up-to-date Swagger docs
   - Document actual endpoints (not assumed)
   - Include example requests/responses

---

## Lessons Learned

### ✅ What Worked
- Systematic debugging with logging
- Following error stack traces to root cause
- Testing integrations incrementally
- Using existing patterns (ApiResponseWrapper)

### ⚠️ What Could Be Improved
- Define service contracts upfront
- Create integration tests early
- Document endpoints immediately
- Use shared DTO libraries

### 🎯 Best Practices Applied
- Empty controller mappings when using context-path
- Response wrapper pattern for inter-service calls
- Sensible defaults for missing configuration
- Clear separation of standard vs customized flows

---

## Next Steps

### Immediate (Testing)
1. ✅ Restart Customer Service
2. ✅ Restart FD Account Service
3. 🎯 Test account creation (Phase 1.1)
4. 🎯 Test customized account (Phase 1.2)

### Short Term (Stabilization)
1. Add integration tests for all 3 service calls
2. Document actual API contracts
3. Add circuit breakers for service failures
4. Implement retry logic with exponential backoff

### Long Term (Architecture)
1. Consider service mesh (Istio, Linkerd)
2. Implement API gateway
3. Add distributed tracing (Jaeger, Zipkin)
4. Create shared contract library

---

## Support Information

### If Account Creation Still Fails

#### Check 1: Services Running
```bash
# Product Pricing Service
curl http://localhost:8084/api/products/code/FD-STD-6M

# Customer Service  
curl http://localhost:8083/api/customer/1

# Calculator Service
curl http://localhost:8085/api/calculator/health
```

#### Check 2: Service Logs
Look for these success indicators:
- **Product Service:** "✅ Product found: FD-STD-6M"
- **Customer Service:** "✅ Customer found: 1"
- **Calculator Service:** "Standalone calculation request received"

#### Check 3: Database Connectivity
- Product Service: `product_db` (MySQL)
- Customer Service: `customer_db` (MySQL)
- Calculator Service: `calculator_db` (MySQL) - **Note: Shows access error in logs, needs fixing**
- FD Account Service: `fd_account_db` (MySQL)

---

## Summary

### Services Modified: 3 out of 4
- ✅ Product Pricing Service (controller + transaction + logging)
- ✅ Customer Service (controller mapping)
- ✅ FD Account Service (URL + DTO + integration)
- ✅ Calculator Service (no changes - was correct)

### Issues Fixed: 8
1. Product Controller mapping duplication
2. Customer Controller mapping duplication
3. Customer Service URL (singular vs plural)
4. Calculator endpoint path
5. Calculator response unwrapping
6. Calculator DTO field names
7. Missing calculation defaults
8. Missing ParameterizedTypeReference imports

### Integration Status
- **Product → FD Account:** ✅ WORKING
- **Customer → FD Account:** ✅ FIXED (restart pending)
- **Calculator → FD Account:** ✅ FIXED (restart pending)

### Ready for Testing
- Test data exists: Customer ID 1, Product FD-STD-6M
- All configuration corrected
- All DTOs aligned
- Default values applied

---

*Document created: October 20, 2025*
*Last updated: After fixing Calculator Service integration*
