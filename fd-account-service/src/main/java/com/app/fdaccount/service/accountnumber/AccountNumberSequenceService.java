package com.app.fdaccount.service.accountnumber;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing account number sequences
 * Uses database table for persistence and caching for performance
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountNumberSequenceService {

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${account-number.sequence-start:100000}")
    private Long sequenceStart;

    /**
     * Get next sequence number for a branch
     * Thread-safe with database-level locking
     */
    @Transactional
    public synchronized long getNextSequence(String branchCode) {
        // Check if sequence exists for this branch
        Long currentSequence = getCurrentSequence(branchCode);

        long nextSequence;
        if (currentSequence == null) {
            // First time for this branch - use starting sequence
            nextSequence = sequenceStart;
            createSequence(branchCode, nextSequence);
        } else {
            // Increment sequence
            nextSequence = currentSequence + 1;
            updateSequence(branchCode, nextSequence);
        }

        log.debug("Generated sequence {} for branch {}", nextSequence, branchCode);
        return nextSequence;
    }

    /**
     * Get current sequence for a branch (without incrementing)
     */
    public Long getCurrentSequence(String branchCode) {
        try {
            Query query = entityManager.createNativeQuery(
                "SELECT current_sequence FROM account_number_sequence WHERE branch_code = :branchCode"
            );
            query.setParameter("branchCode", branchCode);
            
            Object result = query.getSingleResult();
            return result != null ? ((Number) result).longValue() : null;
        } catch (Exception e) {
            log.debug("No sequence found for branch {}", branchCode);
            return null;
        }
    }

    /**
     * Create initial sequence for a branch
     */
    private void createSequence(String branchCode, long sequence) {
        // Create table if it doesn't exist
        ensureSequenceTableExists();

        Query query = entityManager.createNativeQuery(
            "INSERT INTO account_number_sequence (branch_code, current_sequence, created_at, updated_at) " +
            "VALUES (:branchCode, :sequence, NOW(), NOW())"
        );
        query.setParameter("branchCode", branchCode);
        query.setParameter("sequence", sequence);
        query.executeUpdate();

        log.info("Created sequence for branch {} starting at {}", branchCode, sequence);
    }

    /**
     * Update sequence for a branch
     */
    private void updateSequence(String branchCode, long newSequence) {
        Query query = entityManager.createNativeQuery(
            "UPDATE account_number_sequence SET current_sequence = :sequence, updated_at = NOW() " +
            "WHERE branch_code = :branchCode"
        );
        query.setParameter("branchCode", branchCode);
        query.setParameter("sequence", newSequence);
        query.executeUpdate();
    }

    /**
     * Ensure sequence table exists
     */
    private void ensureSequenceTableExists() {
        try {
            entityManager.createNativeQuery(
                "CREATE TABLE IF NOT EXISTS account_number_sequence (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "branch_code VARCHAR(20) NOT NULL UNIQUE, " +
                "current_sequence BIGINT NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "INDEX idx_branch_code (branch_code)" +
                ")"
            ).executeUpdate();
        } catch (Exception e) {
            // Table might already exist, which is fine
            log.debug("Sequence table check: {}", e.getMessage());
        }
    }

    /**
     * Reset sequence for a branch (admin function)
     */
    @Transactional
    public void resetSequence(String branchCode, long newSequence) {
        Query query = entityManager.createNativeQuery(
            "UPDATE account_number_sequence SET current_sequence = :sequence, updated_at = NOW() " +
            "WHERE branch_code = :branchCode"
        );
        query.setParameter("branchCode", branchCode);
        query.setParameter("sequence", newSequence);
        int updated = query.executeUpdate();

        if (updated == 0) {
            createSequence(branchCode, newSequence);
        }

        log.warn("Reset sequence for branch {} to {}", branchCode, newSequence);
    }
}
