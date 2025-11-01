package com.app.login.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.common.util.JwtUtil;
import com.app.login.client.EmailServiceClient;
import com.app.login.dto.LoginRequest;
import com.app.login.dto.LoginResponse;
import com.app.login.dto.RegisterRequest;
import com.app.login.dto.TokenValidationResponse;
import com.app.login.entity.AuditLog;
import com.app.login.entity.Role;
import com.app.login.entity.User;
import com.app.login.entity.UserSession;
import com.app.login.event.LoginEvent;
import com.app.login.event.LoginEventPublisher;
import com.app.login.repository.AuditLogRepository;
import com.app.login.repository.RoleRepository;
import com.app.login.repository.UserRepository;
import com.app.login.repository.UserSessionRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for handling authentication and authorization
 */
@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserSessionRepository sessionRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailServiceClient emailServiceClient;

    @Autowired(required = false)
    private LoginEventPublisher eventPublisher;

    @Autowired
    @Lazy
    private AuthService self; // Self-injection for transactional methods

    @Value("${jwt.expiration:3600000}")
    private Long jwtExpiration;

    @Autowired
    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
            UserSessionRepository sessionRepository, AuditLogRepository auditLogRepository,
            PasswordEncoder passwordEncoder, JwtUtil jwtUtil, EmailServiceClient emailServiceClient) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.sessionRepository = sessionRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailServiceClient = emailServiceClient;
    }

    /**
     * Register a new user
     */
    @Transactional
    public User register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        // Validate uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (request.getMobileNumber() != null &&
                userRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new IllegalArgumentException("Mobile number already exists");
        }

        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword())) // BCrypt hashing
                .email(request.getEmail())
                .mobileNumber(request.getMobileNumber())
                .preferredLanguage(request.getPreferredLanguage())
                .preferredCurrency(request.getPreferredCurrency())
                .active(true)
                .accountLocked(false)
                .failedLoginAttempts(0)
                .createdBy(request.getUsername())
                .roles(new HashSet<>())
                .build();

        // Assign default role
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Default role not found"));
        user.getRoles().add(userRole);

        User savedUser = userRepository.save(user);

        // Log the registration
        logAuditEvent(request.getUsername(), AuditLog.EventType.USER_REGISTERED,
                true, "User registered successfully", null);

        log.info("User registered successfully: {}", savedUser.getUsername());
        return savedUser;
    }

    /**
     * Authenticate user and generate JWT token
     */
    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        log.info("Login attempt for: {}", request.getUsernameOrEmailOrMobile());

        String identifier = request.getUsernameOrEmailOrMobile();

        // Find user by username, email, or mobile
        User user = userRepository.findByUsernameOrEmailOrMobileNumber(identifier, identifier, identifier)
                .orElseThrow(() -> {
                    logAuditEvent(identifier, AuditLog.EventType.LOGIN_FAILURE,
                            false, "User not found", httpRequest);
                    return new UsernameNotFoundException("Invalid credentials");
                });

        // Check if account is locked
        if (user.isAccountLocked()) {
            logAuditEvent(user.getUsername(), AuditLog.EventType.LOGIN_FAILURE,
                    false, "Account is locked", httpRequest);
            throw new BadCredentialsException("Account is locked. Please contact administrator.");
        }

        // Check if account is active
        if (!user.isActive()) {
            logAuditEvent(user.getUsername(), AuditLog.EventType.LOGIN_FAILURE,
                    false, "Account is inactive", httpRequest);
            throw new BadCredentialsException("Account is inactive");
        }

        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            self.handleFailedLogin(user, httpRequest); // Use self to trigger transaction proxy
            throw new BadCredentialsException("Invalid credentials");
        }

        // Reset failed attempts on successful login
        user.setFailedLoginAttempts(0);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Generate JWT token
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());
        String token = jwtUtil.generateToken(user.getUsername(), roles);

        // Create session
        createUserSession(user, token, httpRequest);

        // Log successful login
        logAuditEvent(user.getUsername(), AuditLog.EventType.LOGIN_SUCCESS,
                true, "Login successful", httpRequest);

        // Publish login event to Kafka
        publishLoginEvent(user, "LOGIN_SUCCESS", httpRequest);

        // Send login notification email
        sendLoginNotificationEmail(user, httpRequest);

        log.info("User logged in successfully: {}", user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .roles(user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet()))
                .preferredLanguage(user.getPreferredLanguage())
                .preferredCurrency(user.getPreferredCurrency())
                .loginTime(LocalDateTime.now())
                .expiresIn(jwtExpiration)
                .build();
    }

    /**
     * Logout user
     */
    @Transactional
    public void logout(String token) {
        String username = jwtUtil.extractUsername(token);

        sessionRepository.findBySessionToken(token).ifPresent(session -> {
            session.setActive(false);
            session.setLogoutTime(LocalDateTime.now());
            sessionRepository.save(session);
        });

        logAuditEvent(username, AuditLog.EventType.LOGOUT, true, "User logged out", null);

        // Publish logout event
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            publishLoginEvent(user, "LOGOUT", null);
        }

        log.info("User logged out: {}", username);
    }

    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        log.info("Retrieving user information for username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    /**
     * Validate JWT token
     */
    public TokenValidationResponse validateToken(String token) {
        try {
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                List<String> roles = jwtUtil.extractRoles(token);

                User user = userRepository.findByUsername(username)
                        .orElse(null);

                return TokenValidationResponse.builder()
                        .valid(true)
                        .username(username)
                        .userId(user != null ? user.getId() : null)
                        .roles(roles.toArray(new String[0]))
                        .message("Token is valid")
                        .build();
            }
        } catch (Exception e) {
            log.error("Token validation failed", e);
        }

        return TokenValidationResponse.builder()
                .valid(false)
                .message("Invalid or expired token")
                .build();
    }

    /**
     * Handle failed login attempts
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void handleFailedLogin(User user, HttpServletRequest httpRequest) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

        // Lock account after 5 failed attempts
        if (user.getFailedLoginAttempts() >= 5) {
            user.setAccountLocked(true);
            logAuditEvent(user.getUsername(), AuditLog.EventType.ACCOUNT_LOCKED,
                    true, "Account locked due to multiple failed login attempts", httpRequest);
            log.warn("Account locked due to failed attempts: {}", user.getUsername());
        }

        userRepository.save(user);
        logAuditEvent(user.getUsername(), AuditLog.EventType.LOGIN_FAILURE,
                false, "Invalid password", httpRequest);
    }

    /**
     * Create user session
     */
    private void createUserSession(User user, String token, HttpServletRequest httpRequest) {
        UserSession session = UserSession.builder()
                .user(user)
                .sessionToken(token)
                .loginTime(LocalDateTime.now())
                .lastActivity(LocalDateTime.now())
                .active(true)
                .ipAddress(getClientIp(httpRequest))
                .userAgent(httpRequest.getHeader("User-Agent"))
                .build();

        sessionRepository.save(session);
    }

    /**
     * Log audit event
     */
    private void logAuditEvent(String username, AuditLog.EventType eventType,
            boolean success, String message, HttpServletRequest httpRequest) {
        AuditLog auditLog = AuditLog.builder()
                .username(username)
                .eventType(eventType)
                .success(success)
                .message(message)
                .eventTime(LocalDateTime.now())
                .build();

        if (httpRequest != null) {
            auditLog.setIpAddress(getClientIp(httpRequest));
            auditLog.setUserAgent(httpRequest.getHeader("User-Agent"));
        }

        auditLogRepository.save(auditLog);
    }

    /**
     * Get client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Publish login event to Kafka (if Kafka is enabled)
     */
    private void publishLoginEvent(User user, String eventType, HttpServletRequest request) {
        // Skip if Kafka is not configured
        if (eventPublisher == null) {
            log.debug("Kafka event publisher not available, skipping event publication");
            return;
        }

        try {
            LoginEvent event = LoginEvent.builder()
                    .eventId(java.util.UUID.randomUUID().toString())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .userId(user.getId())
                    .eventType(eventType)
                    .eventTime(LocalDateTime.now())
                    .ipAddress(request != null ? getClientIp(request) : null)
                    .userAgent(request != null ? request.getHeader("User-Agent") : null)
                    .build();

            eventPublisher.publishLoginEvent(event);
        } catch (Exception e) {
            log.error("Failed to publish login event", e);
            // Don't fail the operation if Kafka is unavailable
        }
    }

    /**
     * Send login notification email
     */
    private void sendLoginNotificationEmail(User user, HttpServletRequest request) {
        try {
            // Format login time
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss");
            String loginTime = LocalDateTime.now().format(formatter);

            // Get client information
            String ipAddress = request != null ? getClientIp(request) : "Unknown";
            String userAgent = request != null ? request.getHeader("User-Agent") : null;
            String deviceInfo = extractDeviceInfo(userAgent);
            String location = "Unknown"; // Can be enhanced with IP geolocation service

            // Send email
            emailServiceClient.sendLoginNotificationEmail(
                    user.getEmail(),
                    user.getUsername(),
                    loginTime,
                    ipAddress,
                    deviceInfo,
                    location);

            log.info("✅ Login notification email triggered for: {}", user.getEmail());
        } catch (Exception e) {
            log.error("❌ Failed to send login notification email, but login succeeded", e);
            // Don't fail the login if email fails
        }
    }

    /**
     * Extract device information from User-Agent
     */
    private String extractDeviceInfo(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown Device";
        }

        // Simple device detection
        if (userAgent.contains("Windows")) {
            return "Windows Desktop";
        } else if (userAgent.contains("Mac")) {
            return "Mac Desktop";
        } else if (userAgent.contains("Linux")) {
            return "Linux Desktop";
        } else if (userAgent.contains("Android")) {
            return "Android Mobile";
        } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            return "iOS Mobile";
        } else {
            return "Unknown Device";
        }
    }
}
