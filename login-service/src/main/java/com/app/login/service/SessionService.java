package com.app.login.service;

import com.app.login.entity.UserSession;
import com.app.login.entity.AuditLog;
import com.app.login.repository.UserSessionRepository;
import com.app.login.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing user sessions and auto-logout
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final UserSessionRepository sessionRepository;
    private final AuditLogRepository auditLogRepository;

    @Value("${session.idle-timeout:300000}") // 5 minutes default
    private Long idleTimeout;

    /**
     * Update session activity
     */
    @Transactional
    public void updateSessionActivity(String token) {
        sessionRepository.findBySessionToken(token).ifPresent(session -> {
            session.setLastActivity(LocalDateTime.now());
            sessionRepository.save(session);
        });
    }

    /**
     * Check if session is expired
     */
    public boolean isSessionExpired(String token) {
        return sessionRepository.findBySessionToken(token)
                .map(session -> session.isExpired(idleTimeout))
                .orElse(true);
    }

    /**
     * Scheduled task to auto-logout inactive sessions
     * Runs every minute
     */
    @Scheduled(fixedRate = 60000) // Every 1 minute
    @Transactional
    public void autoLogoutInactiveSessions() {
        LocalDateTime threshold = LocalDateTime.now().minusNanos(idleTimeout * 1_000_000);
        List<UserSession> expiredSessions = sessionRepository.findByActiveTrueAndLastActivityBefore(threshold);

        if (!expiredSessions.isEmpty()) {
            log.info("Auto-logout: Found {} expired sessions", expiredSessions.size());
            
            expiredSessions.forEach(session -> {
                session.setActive(false);
                session.setLogoutTime(LocalDateTime.now());
                sessionRepository.save(session);

                // Log auto-logout event
                AuditLog auditLog = AuditLog.builder()
                        .username(session.getUser().getUsername())
                        .eventType(AuditLog.EventType.AUTO_LOGOUT)
                        .success(true)
                        .message("Session expired due to inactivity")
                        .eventTime(LocalDateTime.now())
                        .build();
                auditLogRepository.save(auditLog);

                log.info("Auto-logout: Session expired for user {}", session.getUser().getUsername());
            });
        }
    }

    /**
     * Get active sessions for a user
     */
    public long getActiveSessionCount(String username) {
        // This will be implemented when needed
        return 0;
    }
}
