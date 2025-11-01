-- ============================================
-- Credexa Login Service - Database Setup
-- ============================================
-- This script is OPTIONAL - tables are auto-created by Hibernate
-- Use this only if you want to create database manually

-- Create database
CREATE DATABASE IF NOT EXISTS login_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE login_db;

-- Note: The following tables are automatically created by Spring Boot JPA
-- This is just for reference to see the schema

-- Tables will be created automatically with these columns:

-- 1. users
-- ├── id (BIGINT, PRIMARY KEY, AUTO_INCREMENT)
-- ├── username (VARCHAR(100), UNIQUE, NOT NULL)
-- ├── password (VARCHAR(255), NOT NULL) -- BCrypt hashed
-- ├── email (VARCHAR(255), UNIQUE, NOT NULL)
-- ├── mobile_number (VARCHAR(15), UNIQUE)
-- ├── active (BOOLEAN, DEFAULT TRUE)
-- ├── account_locked (BOOLEAN, DEFAULT FALSE)
-- ├── failed_login_attempts (INT, DEFAULT 0)
-- ├── last_login (DATETIME)
-- ├── preferred_language (VARCHAR(10), DEFAULT 'en')
-- ├── preferred_currency (VARCHAR(10), DEFAULT 'USD')
-- ├── created_at (DATETIME)
-- ├── updated_at (DATETIME)
-- ├── created_by (VARCHAR(100))
-- └── updated_by (VARCHAR(100))

-- 2. roles
-- ├── id (BIGINT, PRIMARY KEY, AUTO_INCREMENT)
-- ├── name (VARCHAR(50), UNIQUE, NOT NULL)
-- └── description (VARCHAR(500))

-- 3. user_roles (Join Table)
-- ├── user_id (BIGINT, FOREIGN KEY -> users.id)
-- └── role_id (BIGINT, FOREIGN KEY -> roles.id)

-- 4. user_sessions
-- ├── id (BIGINT, PRIMARY KEY, AUTO_INCREMENT)
-- ├── user_id (BIGINT, FOREIGN KEY -> users.id)
-- ├── session_token (VARCHAR(500), UNIQUE, NOT NULL)
-- ├── last_activity (DATETIME)
-- ├── login_time (DATETIME)
-- ├── logout_time (DATETIME)
-- ├── is_active (BOOLEAN, DEFAULT TRUE)
-- ├── ip_address (VARCHAR(50))
-- └── user_agent (VARCHAR(500))

-- 5. audit_logs
-- ├── id (BIGINT, PRIMARY KEY, AUTO_INCREMENT)
-- ├── username (VARCHAR(100), NOT NULL)
-- ├── event_type (VARCHAR(50), NOT NULL)
-- ├── success (BOOLEAN, NOT NULL)
-- ├── message (VARCHAR(500))
-- ├── ip_address (VARCHAR(50))
-- ├── user_agent (VARCHAR(500))
-- └── event_time (DATETIME)

-- 6. bank_configuration
-- ├── id (BIGINT, PRIMARY KEY, AUTO_INCREMENT)
-- ├── bank_name (VARCHAR(255), NOT NULL)
-- ├── logo_url (VARCHAR(500))
-- ├── default_language (VARCHAR(10), DEFAULT 'en')
-- ├── default_currency (VARCHAR(10), DEFAULT 'USD')
-- ├── currency_decimal_places (INT, DEFAULT 2)
-- └── is_active (BOOLEAN, DEFAULT TRUE)

-- ============================================
-- Useful Queries for Development/Testing
-- ============================================

-- View all users
-- SELECT id, username, email, mobile_number, active, account_locked, 
--        failed_login_attempts, preferred_language, preferred_currency, 
--        last_login, created_at 
-- FROM users;

-- View all roles
-- SELECT * FROM roles;

-- View user-role mappings
-- SELECT u.username, r.name as role 
-- FROM users u 
-- JOIN user_roles ur ON u.id = ur.user_id 
-- JOIN roles r ON ur.role_id = r.id;

-- View active sessions
-- SELECT s.id, u.username, s.login_time, s.last_activity, 
--        s.ip_address, s.is_active 
-- FROM user_sessions s 
-- JOIN users u ON s.user_id = u.id 
-- WHERE s.is_active = TRUE;

-- View recent audit logs
-- SELECT username, event_type, success, message, 
--        ip_address, event_time 
-- FROM audit_logs 
-- ORDER BY event_time DESC 
-- LIMIT 20;

-- View bank configuration
-- SELECT * FROM bank_configuration WHERE is_active = TRUE;

-- ============================================
-- Reset/Cleanup Queries (Use with CAUTION!)
-- ============================================

-- Reset failed login attempts for a user
-- UPDATE users SET failed_login_attempts = 0, account_locked = FALSE 
-- WHERE username = 'your_username';

-- Logout all active sessions
-- UPDATE user_sessions SET is_active = FALSE, logout_time = NOW() 
-- WHERE is_active = TRUE;

-- Delete a specific user (CASCADE will handle user_roles and user_sessions)
-- DELETE FROM users WHERE username = 'username_to_delete';

-- ============================================
-- Performance Indexes (Optional)
-- ============================================
-- These are automatically created by JPA, but listed here for reference

-- ON users:
-- - PRIMARY KEY (id)
-- - UNIQUE INDEX (username)
-- - UNIQUE INDEX (email)
-- - UNIQUE INDEX (mobile_number)

-- ON user_sessions:
-- - PRIMARY KEY (id)
-- - UNIQUE INDEX (session_token)
-- - INDEX (user_id)
-- - INDEX (last_activity) -- for auto-logout query

-- ON audit_logs:
-- - PRIMARY KEY (id)
-- - INDEX (username)
-- - INDEX (event_type)
-- - INDEX (event_time)

-- ============================================
-- End of Script
-- ============================================
