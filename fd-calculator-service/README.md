# FD Calculator Service

## 📊 Overview

The **FD Calculator Service** is a microservice that provides comprehensive Fixed Deposit calculation and simulation capabilities. It supports both standalone calculations (manual inputs) and product-based calculations (integrated with product-pricing-service).

## 🎯 Features

### Core Capabilities
- ✅ **Simple Interest Calculation** - Linear interest calculation
- ✅ **Compound Interest Calculation** - With multiple compounding frequencies
- ✅ **Product-Based Calculation** - Integrate with product configurations
- ✅ **Customer Classification Support** - Additional rates for senior citizens, premium customers, etc.
- ✅ **Scenario Comparison** - Compare multiple FD options side-by-side
- ✅ **Monthly Breakdown** - Detailed month-by-month interest and balance
- ✅ **TDS Calculation** - Tax deduction at source support
- ✅ **Rate Capping** - Max 2% additional rate from base

### Integration
- **Product-Pricing Service** - Fetch product details and interest rates
- **Customer Service** - Fetch customer classifications
- **Login Service** - JWT authentication

### Technical Features
- **In-Memory Caching** - Caffeine cache for products, rates, and customer data (24-hour TTL)
- **WebClient Integration** - Reactive inter-service communication
- **Swagger/OpenAPI** - Comprehensive API documentation
- **Input Validation** - Jakarta validation for all requests
- **Error Handling** - Detailed error responses

---

## 🏗️ Architecture

```
fd-calculator-service (Port: 8085)
├── Controller Layer
│   └── FdCalculatorController - REST endpoints
├── Service Layer
│   ├── FdCalculatorService - Main orchestration
│   ├── SimpleInterestCalculator - Simple interest logic
│   ├── CompoundInterestCalculator - Compound interest logic
│   ├── ProductIntegrationService - Product-pricing integration
│   └── CustomerIntegrationService - Customer service integration
├── DTOs
│   ├── StandaloneCalculationRequest
│   ├── ProductBasedCalculationRequest
│   ├── CalculationResponse
│   ├── ComparisonRequest
│   └── ComparisonResponse
├── Enums
│   ├── CalculationType (SIMPLE, COMPOUND)
│   ├── CompoundingFrequency (DAILY, MONTHLY, QUARTERLY, SEMI_ANNUALLY, ANNUALLY)
│   └── TenureUnit (DAYS, MONTHS, YEARS)
└── Configuration
    ├── CacheConfig - Caffeine cache
    ├── WebClientConfig - Inter-service communication
    └── SwaggerConfig - API documentation
```

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Running instances of:
  - login-service (port 8082)
  - product-pricing-service (port 8084) - **Required for product-based calculations**
  - customer-service (port 8083) - Optional

### Installation

1. **Clone the repository:**
```bash
cd credexa/fd-calculator-service
```

2. **Update application.yml:**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/calculator_db?createDatabaseIfNotExist=true
    username: root
    password: your_password
```

3. **Build the project:**
```bash
mvn clean install
```

4. **Run the service:**
```bash
mvn spring-boot:run
```

5. **Verify:**
- Service: http://localhost:8085/api/calculator/health
- Swagger: http://localhost:8085/api/calculator/swagger-ui.html

---

## 📡 API Endpoints

### 1. Calculate Standalone FD
**POST** `/calculate/standalone`

Calculate FD with manual inputs (no product required).

**Request:**
```json
{
  "principalAmount": 100000,
  "interestRate": 7.5,
  "tenure": 12,
  "tenureUnit": "MONTHS",
  "calculationType": "COMPOUND",
  "compoundingFrequency": "QUARTERLY",
  "tdsRate": 10.0,
  "customerClassifications": ["SENIOR_CITIZEN"]
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "maturityAmount": 106986.70,
    "interestEarned": 7763.00,
    "tdsAmount": 776.30,
    "netInterest": 6986.70,
    "monthlyBreakdown": [...]
  }
}
```

---

### 2. Calculate with Product
**POST** `/calculate/product-based`

Calculate using product defaults from product-pricing-service.

**Request:**
```json
{
  "productId": 1,
  "principalAmount": 100000,
  "tenure": 12,
  "tenureUnit": "MONTHS",
  "customerId": 1,
  "applyTds": true
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "productName": "Standard Fixed Deposit",
    "productCode": "FD-STD-001",
    "baseInterestRate": 6.50,
    "additionalInterestRate": 0.25,
    "interestRate": 6.75,
    "maturityAmount": 106250.00
  }
}
```

---

### 3. Compare Scenarios
**POST** `/compare`

Compare multiple FD calculation scenarios.

**Request:**
```json
{
  "commonPrincipal": 100000,
  "scenarios": [
    {
      "principalAmount": 100000,
      "interestRate": 7.0,
      "tenure": 12,
      "tenureUnit": "MONTHS",
      "calculationType": "SIMPLE",
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
    }
  ]
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "scenarios": [...],
    "bestScenario": {...},
    "bestScenarioIndex": 1
  }
}
```

---

### 4. Health Check
**GET** `/health`

Check if service is running.

---

## 🧮 Calculation Formulas

### Simple Interest
```
Interest = (P × R × T) / 100
Maturity = P + Interest - TDS

Where:
P = Principal amount
R = Annual interest rate (%)
T = Time in years
TDS = Interest × TDS Rate / 100
```

**Example:**
- P = ₹100,000
- R = 7.5%
- T = 1 year
- TDS = 10%

```
Interest = (100000 × 7.5 × 1) / 100 = ₹7,500
TDS = 7500 × 10 / 100 = ₹750
Net Interest = 7500 - 750 = ₹6,750
Maturity = 100000 + 6750 = ₹106,750
```

---

### Compound Interest
```
M = P × (1 + r/n)^(nt)
Interest = M - P
Net Interest = Interest - TDS

Where:
M = Maturity amount
P = Principal
r = Annual rate (as decimal, e.g., 0.075 for 7.5%)
n = Compounding periods per year
t = Time in years
```

**Example (Quarterly Compounding):**
- P = ₹100,000
- r = 0.075 (7.5%)
- n = 4 (quarterly)
- t = 1 year

```
M = 100000 × (1 + 0.075/4)^(4×1)
M = 100000 × (1.01875)^4
M ≈ ₹107,763
Interest = 107763 - 100000 = ₹7,763
TDS = 7763 × 10% = ₹776.30
Net = 7763 - 776.30 = ₹6,986.70
Maturity = 100000 + 6986.70 = ₹106,986.70
```

---

## 🔧 Configuration

### application.yml

```yaml
server:
  port: 8085
  servlet:
    context-path: /api/calculator

spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=500,expireAfterWrite=24h
    cache-names:
      - products
      - interestRates
      - customerClassifications

services:
  product-pricing:
    url: http://localhost:8084/api/products
  customer:
    url: http://localhost:8083/api/customers

cache:
  refresh:
    cron: "0 0 2 * * ?"  # Daily at 2 AM
    enabled: true
```

---

## 📦 Dependencies

- **Spring Boot 3.5.6**
- **Spring Web** - REST APIs
- **Spring Data JPA** - Database operations (optional)
- **Spring Cache** - Caching abstraction
- **Caffeine** - In-memory cache implementation
- **WebFlux** - WebClient for inter-service calls
- **Swagger/OpenAPI** - API documentation
- **Jakarta Validation** - Input validation
- **MySQL** - Database
- **Lombok** - Boilerplate reduction
- **Common-Lib** - Shared utilities

---

## 🧪 Testing

### Run Tests
```bash
mvn test
```

### Manual Testing with Swagger
1. Start all required services
2. Open http://localhost:8085/api/calculator/swagger-ui.html
3. Authorize with JWT token from login-service
4. Follow **SWAGGER-TESTING-GUIDE.md** for comprehensive testing

---

## 🔒 Security

- **JWT Authentication** - All endpoints require valid JWT token
- **Bearer Token** - Use `Authorization: Bearer <token>` header
- **Get Token** - Login via login-service on port 8082

---

## 📊 Caching Strategy

### Cached Data
1. **Products** (24-hour TTL)
   - Product details from product-pricing-service
   - Reduces load on product-pricing-service
   
2. **Interest Rates** (24-hour TTL)
   - Applicable rates for product/amount/tenure/classification combinations
   - Significantly improves calculation performance
   
3. **Customer Classifications** (24-hour TTL)
   - Customer classification data
   - Reduces calls to customer-service

### Cache Refresh
- Automatic refresh: Daily at 2 AM
- Manual eviction: Restart service or use Spring Boot Actuator endpoints

### Monitor Cache
```bash
GET http://localhost:8085/api/calculator/actuator/caches
```

---

## 🚨 Error Handling

### Validation Errors (400)
```json
{
  "success": false,
  "message": "Principal amount ₹5000 is below minimum ₹10000 for product FD-STD-001",
  "status": 400
}
```

### Product Not Found (404)
```json
{
  "success": false,
  "message": "Product not found with ID: 99",
  "status": 404
}
```

### Service Integration Error (500)
```json
{
  "success": false,
  "message": "Failed to calculate FD: Unable to fetch product details",
  "status": 500
}
```

---

## 📈 Performance

- **Standalone Calculation**: ~10ms (no external calls)
- **Product-Based (Cached)**: ~50ms (with cache hits)
- **Product-Based (Uncached)**: ~200-300ms (first call, includes external service calls)
- **Comparison (3 scenarios)**: ~30ms (all standalone)

---

## 🔗 Integration with Other Services

### Product-Pricing Service
- **GET /products/{id}** - Fetch product details
- **GET /products/{id}/interest-rates/applicable** - Get applicable rate
- **Caching**: 24-hour cache to minimize calls

### Customer Service
- **GET /customers/{id}** - Fetch customer classification
- **Caching**: 24-hour cache

### Login Service
- **POST /auth/login** - Get JWT token
- **Used for**: Authentication across all calculator endpoints

---

## 📝 Logging

### Log Levels
```yaml
logging:
  level:
    com.app.calculator: DEBUG
    org.springframework.cache: DEBUG
```

### Key Log Messages
- Service startup confirmation
- Calculation requests (principal, product ID)
- External service calls (product fetch, customer fetch)
- Cache hits/misses
- Validation errors
- Integration failures

---

## 🛠️ Development

### Project Structure
```
fd-calculator-service/
├── src/main/java/com/app/calculator/
│   ├── FdCalculatorApplication.java
│   ├── config/
│   ├── controller/
│   ├── dto/
│   ├── enums/
│   └── service/
├── src/main/resources/
│   └── application.yml
├── SWAGGER-TESTING-GUIDE.md
├── README.md
└── pom.xml
```

### Build Commands
```bash
# Clean build
mvn clean install

# Skip tests
mvn clean install -DskipTests

# Run only tests
mvn test

# Package JAR
mvn package

# Run service
mvn spring-boot:run
```

---

## 📖 Documentation

- **Swagger UI**: http://localhost:8085/api/calculator/swagger-ui.html
- **API Docs (JSON)**: http://localhost:8085/api/calculator/v3/api-docs
- **Testing Guide**: See `SWAGGER-TESTING-GUIDE.md`

---

## 🎯 Use Cases

1. **FD Calculator UI** - Provide calculations for web/mobile UI
2. **FD Account Creation** - Calculate maturity during account opening
3. **Customer Advisory** - Compare different FD options for customers
4. **Product Comparison** - Help customers choose best FD product
5. **Interest Forecasting** - Project future maturity amounts
6. **Tax Planning** - Calculate TDS for tax filing

---

## 🔮 Future Enhancements

- [ ] Save calculation history to database
- [ ] Email calculation results to customer
- [ ] PDF report generation
- [ ] Premature withdrawal penalty calculation
- [ ] Loan against FD calculation
- [ ] Multi-currency support
- [ ] Historical interest rate trends
- [ ] ML-based rate prediction

---

## 👥 Support

For issues or questions:
- Check **SWAGGER-TESTING-GUIDE.md**
- Review logs in console
- Verify all services are running
- Check service URLs in application.yml

---

## 📜 License

Apache 2.0

---

## 🎉 Quick Start Summary

```bash
# 1. Start dependencies
cd ../login-service && mvn spring-boot:run &
cd ../product-pricing-service && mvn spring-boot:run &

# 2. Start calculator service
cd ../fd-calculator-service
mvn spring-boot:run

# 3. Open Swagger
open http://localhost:8085/api/calculator/swagger-ui.html

# 4. Test health
curl http://localhost:8085/api/calculator/health

# 5. Get JWT token from login-service
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmailOrMobile":"admin1","password":"Admin@123"}'

# 6. Test calculation (replace TOKEN)
curl -X POST http://localhost:8085/api/calculator/calculate/standalone \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"principalAmount":100000,"interestRate":7.5,"tenure":12,"tenureUnit":"MONTHS","calculationType":"COMPOUND","compoundingFrequency":"QUARTERLY","tdsRate":10.0}'
```

**Service is ready! 🚀**
