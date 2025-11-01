# Login Service - Quick Start Guide

## Prerequisites
- MySQL running on localhost:3306
- Database 'login_db' created (or use createDatabaseIfNotExist=true)

## Quick Start

### 1. Start MySQL
Make sure MySQL is running with these credentials:
- Username: root
- Password: root
- Port: 3306

### 2. Build the project
```bash
cd c:\Users\dhruv\Coding\bt_khatam\credexa
mvn clean install
```

### 3. Run the Login Service
```bash
cd login-service
mvn spring-boot:run
```

### 4. Access Swagger UI
Open browser: http://localhost:8081/api/auth/swagger-ui.html

### 5. Test the APIs

#### Default Admin Login:
```json
{
  "usernameOrEmailOrMobile": "admin",
  "password": "Admin@123"
}
```

#### Register New User:
```json
{
  "username": "testuser",
  "password": "Test@123",
  "email": "test@example.com",
  "mobileNumber": "9876543210"
}
```

## Kafka (Optional)
Kafka is optional. The service works without it.
If you want to enable Kafka events:
1. Install Kafka
2. Start Zookeeper: `zookeeper-server-start.bat config\zookeeper.properties`
3. Start Kafka: `kafka-server-start.bat config\server.properties`

## Database Schema
Tables are auto-created on startup:
- users
- roles
- user_roles
- user_sessions
- audit_logs
- bank_configuration

## Default Data Created
- 6 Roles (ROLE_ADMIN, ROLE_USER, etc.)
- 1 Admin user (username: admin, password: Admin@123)
- 1 Bank configuration

## Troubleshooting

### MySQL Connection Error
Update src/main/resources/application.yml:
```yaml
spring:
  datasource:
    username: your_username
    password: your_password
```

### Port 8081 in use
Change port in application.yml:
```yaml
server:
  port: 8082  # or any available port
```
