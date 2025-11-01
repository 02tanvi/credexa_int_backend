package com.credexa.email.repository;

import com.credexa.email.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {

    List<EmailLog> findByRecipient(String recipient);

    List<EmailLog> findByStatus(EmailLog.EmailStatus status);

    @Query("SELECT e FROM EmailLog e WHERE e.status = :status AND e.retryCount < :maxRetries AND e.createdAt > :since")
    List<EmailLog> findFailedEmailsForRetry(EmailLog.EmailStatus status, Integer maxRetries, LocalDateTime since);

    List<EmailLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
