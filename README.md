# Credexa - Fixed Deposit Banking Application

A comprehensive microservices-based Fixed Deposit banking application built with Spring Boot, MySQL, Kafka, and React.

## 🏗️ Architecture

This is a **multi-module Maven project** consisting of 6 modules:

### Modules

| Module | Port | Description | Status |
|--------|------|-------------|--------|
| **common-lib** | - | Shared utilities (JWT, DTOs, Encryption, PII Masking) | ✅ Complete |
| **login-service** | 8081 | Authentication & Authorization | ✅ Complete |
| **customer-service** | 8083 | Customer Management | 🔄 Pending |
| **product-pricing-service** | 8082 | Product & Pricing Management | 🔄 Pending |
| **fd-calculator-service** | 8084 | FD Interest Calculator | 🔄 Pending |
| **fd-account-service** | 8085 | FD Account Management | 🔄 Pending |

## 🚀 Technology Stack

### Backend
- **Java 17**
- **Spring Boot 3.5.6**
- **Spring Security** with JWT
- **Spring Data JPA**
- **Spring Kafka**
- **MySQL 8.0**
- **Swagger/OpenAPI**
- **Lombok**
- **BCrypt** for password hashing

### Frontend (Planned)
- **React**

## 📋 Features

### Login Service ✅
- ✅ User Registration (username, email, mobile)
- ✅ JWT Authentication
- ✅ BCrypt Password Hashing with Salt
- ✅ Auto-logout after 5 minutes idle
- ✅ Account locking after 5 failed attempts
- ✅ Multi-language support (English, Hindi, Spanish)
- ✅ Multi-currency support (0, 2, 3 decimal places)
- ✅ Audit logging
- ✅ Kafka event publishing
- ✅ Swagger API documentation
- ✅ PII data masking

## 🛠️ Prerequisites

- **Java 17** or higher
- **Maven 3.6** or higher
- **MySQL 8.0** or higher
- **Apache Kafka** (optional)
- **Node.js & npm** (for React frontend, later)

## 📦 Installation

### 1. Clone the Repository
```bash
git clone <repository-url>
cd credexa
```

### 2. Setup MySQL
Create databases for each service:
```sql
CREATE DATABASE login_db;
CREATE DATABASE customer_db;
CREATE DATABASE product_pricing_db;
CREATE DATABASE fd_calculator_db;
CREATE DATABASE fd_account_db;
```

### 3. Configure Database
Update credentials in each service's `application.yml`:
```yaml
spring:
  datasource:
    username: your_username
    password: your_password
```

### 4. Build All Modules
```bash
mvn clean install
```

## 🏃 Running the Application

### Option 1: Run All Services (from root)
```bash
# Build all modules
mvn clean install

# Run each service in separate terminals
cd login-service && mvn spring-boot:run
cd customer-service && mvn spring-boot:run
cd product-pricing-service && mvn spring-boot:run
cd fd-calculator-service && mvn spring-boot:run
cd fd-account-service && mvn spring-boot:run
```

### Option 2: Run Individual Service
```bash
cd login-service
mvn spring-boot:run
```

## 🌐 Service URLs

| Service | URL | Swagger UI |
|---------|-----|------------|
| Login | http://localhost:8081/api/auth | http://localhost:8081/api/auth/swagger-ui.html |
| Customer | http://localhost:8083/api/customer | http://localhost:8083/api/customer/swagger-ui.html |
| Product & Pricing | http://localhost:8082/api/products | http://localhost:8082/api/products/swagger-ui.html |
| FD Calculator | http://localhost:8084/api/calculator | http://localhost:8084/api/calculator/swagger-ui.html |
| FD Account | http://localhost:8085/api/fd-accounts | http://localhost:8085/api/fd-accounts/swagger-ui.html |

## 🔐 Default Credentials

**Admin User (Login Service):**
- Username: `admin`
- Password: `Admin@123`
- Email: `admin@credexa.com`

**⚠️ IMPORTANT:** Change default password in production!

## 📖 API Documentation

Each microservice has its own Swagger UI for testing APIs:

### Login Service
- **Swagger UI:** http://localhost:8081/api/auth/swagger-ui.html
- **API Docs:** http://localhost:8081/api/auth/api-docs

See individual service README files for detailed API documentation.

## 🏗️ Project Structure

```
credexa/
├── pom.xml                      # Parent POM
├── common-lib/                  # Shared library
│   ├── src/main/java/com/app/common/
│   │   ├── dto/                 # Common DTOs
│   │   └── util/                # JWT, Encryption, PII Masking
│   └── pom.xml
├── login-service/               # Authentication Service
│   ├── src/main/java/com/app/login/
│   │   ├── config/              # Security, Swagger, Kafka
│   │   ├── controller/          # REST Controllers
│   │   ├── dto/                 # Request/Response DTOs
│   │   ├── entity/              # JPA Entities
│   │   ├── event/               # Kafka Events
│   │   ├── repository/          # JPA Repositories
│   │   ├── service/             # Business Logic
│   │   └── exception/           # Exception Handlers
│   ├── src/main/resources/
│   │   └── application.yml      # Configuration
│   ├── README.md
│   └── pom.xml
├── customer-service/            # (To be created)
├── product-pricing-service/     # (To be created)
├── fd-calculator-service/       # (To be created)
└── fd-account-service/          # (To be created)
```

## 🔒 Security Features

1. **JWT Authentication** - Stateless authentication
2. **BCrypt Password Hashing** - Strength 12
3. **Account Locking** - After 5 failed login attempts
4. **Auto-Logout** - 5 minutes idle timeout
5. **PII Data Masking** - Email and mobile masking
6. **Audit Logging** - All auth events tracked
7. **CORS Configuration** - Configured for React frontend

## 📡 Inter-Service Communication

- **Synchronous:** REST APIs (using RestTemplate/WebClient)
- **Asynchronous:** Kafka events for notifications

### Kafka Topics
- `login-events` - Login/logout events
- (More topics to be added as services are created)

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Run Tests for Specific Service
```bash
cd login-service
mvn test
```

## 📚 Development Guide

### Adding a New Microservice

1. Create module directory
2. Create `pom.xml` with parent reference
3. Add module to parent `pom.xml`
4. Follow the same structure as `login-service`
5. Configure unique port in `application.yml`
6. Create Swagger configuration
7. Add JWT authentication filter if needed

### Common Library Usage

All services can use common utilities:
```java
@Autowired
private JwtUtil jwtUtil;

@Autowired
private EncryptionUtil encryptionUtil;

@Autowired
private PIIMaskingUtil maskingUtil;
```

## 🐛 Troubleshooting

### MySQL Connection Issues
- Ensure MySQL is running: `mysql --version`
- Check credentials in `application.yml`
- Verify database exists

### Port Already in Use
- Change port in respective `application.yml`
- Kill process using port: `netstat -ano | findstr :8081`

### Kafka Connection Issues
- Kafka is optional for login service
- Start Zookeeper first, then Kafka
- Update `bootstrap-servers` in `application.yml`

### Build Failures
- Clean install: `mvn clean install -U`
- Check Java version: `java -version`
- Ensure Java 17 or higher

## 📝 TODO

- [ ] Complete Customer Service
- [ ] Complete Product & Pricing Service
- [ ] Complete FD Calculator Service
- [ ] Complete FD Account Service
- [ ] Add Reports Module
- [ ] Build React Frontend
- [ ] Add API Gateway (Spring Cloud Gateway)
- [ ] Add Service Discovery (Eureka)
- [ ] Add Config Server
- [ ] Add Docker support
- [ ] Add Kubernetes deployment
- [ ] Add CI/CD pipeline

## 🤝 Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/my-feature`
3. Commit changes: `git commit -m 'Add some feature'`
4. Push to branch: `git push origin feature/my-feature`
5. Submit pull request

## 📄 License

This project is licensed under the Apache 2.0 License.

## 👥 Team

Credexa Development Team

## 📞 Support

For issues and questions:
- Email: dev@credexa.com
- GitHub Issues: (Add repository URL)

---

**Status:** 🚧 In Development  
**Version:** 0.0.1-SNAPSHOT  
**Last Updated:** October 2025
