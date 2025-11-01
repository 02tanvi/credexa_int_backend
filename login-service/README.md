# Login Service

Authentication and Authorization microservice for the Credexa Fixed Deposit Application.

## Features

✅ **User Registration** - Register with username, email, and mobile number  
✅ **User Authentication** - Login with JWT token generation  
✅ **BCrypt Password Hashing** - Secure password storage with salt  
✅ **JWT Token Validation** - Validate tokens for other microservices  
✅ **Auto-Logout** - Automatic logout after 5 minutes of inactivity  
✅ **Account Locking** - Lock account after 5 failed login attempts  
✅ **Audit Logging** - Track all authentication events  
✅ **Multi-Language Support** - Configurable language preferences  
✅ **Multi-Currency Support** - Support for 0, 2, and 3 decimal currencies  
✅ **Swagger Documentation** - Interactive API documentation  
✅ **Kafka Events** - Publish login events to Kafka  

## Technology Stack

- **Spring Boot 3.5.6**
- **Spring Security** with JWT
- **Spring Data JPA** with MySQL
- **Spring Kafka** for event publishing
- **BCrypt** for password hashing
- **Swagger/OpenAPI** for API documentation
- **Lombok** for cleaner code

## Prerequisites

- Java 17 or higher
- MySQL 8.0 or higher
- Apache Kafka (optional, for events)
- Maven 3.6 or higher

## Database Setup

1. Create MySQL database:
```sql
CREATE DATABASE login_db;
```

2. Update `application.yml` with your MySQL credentials:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/login_db?createDatabaseIfNotExist=true
    username: your_username
    password: your_password
```

## Running the Application

### Option 1: Using Maven
```bash
cd login-service
mvn spring-boot:run
```

### Option 2: Using Java
```bash
cd login-service
mvn clean package
java -jar target/login-service-0.0.1-SNAPSHOT.jar
```

The application will start on **http://localhost:8081**

## API Documentation

Once the application is running, access Swagger UI at:
```
http://localhost:8081/api/auth/swagger-ui.html
```

## Default Credentials

After first startup, a default admin user is created:

- **Username:** `admin`
- **Password:** `Admin@123`
- **Email:** `admin@credexa.com`

**⚠️ IMPORTANT:** Change the default password immediately in production!

## API Endpoints

### Public Endpoints (No Authentication Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | User login |
| POST | `/api/auth/validate-token` | Validate JWT token |
| GET | `/api/auth/bank-config` | Get bank configuration |
| GET | `/api/auth/health` | Health check |

### Protected Endpoints (Requires JWT Token)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/logout` | User logout |

## Usage Examples

### 1. Register a New User

```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "password": "SecurePass@123",
    "email": "john.doe@example.com",
    "mobileNumber": "9876543210",
    "preferredLanguage": "en",
    "preferredCurrency": "USD"
  }'
```

### 2. Login

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmailOrMobile": "john.doe",
    "password": "SecurePass@123"
  }'
```

Response:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "userId": 2,
    "username": "john.doe",
    "email": "john.doe@example.com",
    "roles": ["ROLE_USER"],
    "expiresIn": 3600000
  }
}
```

### 3. Validate Token

```bash
curl -X POST http://localhost:8081/api/auth/validate-token \
  -H "Content-Type: application/json" \
  -d '"your-jwt-token-here"'
```

### 4. Logout

```bash
curl -X POST http://localhost:8081/api/auth/logout \
  -H "Authorization: Bearer your-jwt-token-here"
```

## Configuration

### JWT Configuration
```yaml
jwt:
  secret: your-secret-key-must-be-256-bits
  expiration: 3600000 # 1 hour in milliseconds
```

### Session Configuration
```yaml
session:
  idle-timeout: 300000 # 5 minutes in milliseconds
```

### Supported Currencies
- **USD** - 2 decimal places
- **EUR** - 2 decimal places
- **INR** - 2 decimal places
- **JPY** - 0 decimal places
- **BHD** - 3 decimal places

### Supported Languages
- **en** - English
- **hi** - Hindi
- **es** - Spanish

## Security Features

1. **BCrypt Password Hashing** - Passwords are hashed with BCrypt (strength 12)
2. **Account Locking** - Account locked after 5 failed login attempts
3. **JWT Authentication** - Stateless authentication using JWT tokens
4. **Auto-Logout** - Sessions expire after 5 minutes of inactivity
5. **Audit Logging** - All authentication events are logged

## Roles

- `ROLE_ADMIN` - Administrator access
- `ROLE_USER` - Regular user access
- `ROLE_CUSTOMER_MANAGER` - Customer module access
- `ROLE_PRODUCT_MANAGER` - Product & Pricing module access
- `ROLE_FD_MANAGER` - FD Account module access
- `ROLE_REPORT_VIEWER` - Reports module access

## Kafka Events

The service publishes login events to Kafka topic `login-events`:

Event Types:
- `LOGIN_SUCCESS`
- `LOGIN_FAILURE`
- `LOGOUT`
- `AUTO_LOGOUT`

## Troubleshooting

### Database Connection Issues
- Ensure MySQL is running
- Check database credentials in `application.yml`
- Verify database exists

### Kafka Connection Issues
- Kafka is optional - the service will work without it
- Update `spring.kafka.bootstrap-servers` if Kafka is on a different host

### Port Already in Use
- Change the port in `application.yml`:
```yaml
server:
  port: 8081 # Change to any available port
```

## Development

### Build
```bash
mvn clean install
```

### Run Tests
```bash
mvn test
```

### Package
```bash
mvn clean package
```

## Next Steps

After setting up the Login Service:
1. ✅ Test all endpoints using Swagger UI
2. ✅ Change default admin password
3. ✅ Configure bank settings (name, logo, currency)
4. 🔄 Integrate with other microservices (Customer, Product, FD Calculator, FD Account)

---

**Port:** 8081  
**Context Path:** `/api/auth`  
**Swagger UI:** http://localhost:8081/api/auth/swagger-ui.html  
**API Docs:** http://localhost:8081/api/auth/api-docs
