package com.app.fdaccount.repository;

import com.app.fdaccount.entity.AccountTransaction;
import com.app.fdaccount.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for AccountTransaction entity
 */
@Repository
public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, Long> {

    /**
     * Find transaction by reference number
     */
    Optional<AccountTransaction> findByTransactionReference(String transactionReference);

    /**
     * Find all transactions for an account
     */
    @Query("SELECT t FROM AccountTransaction t WHERE t.account.id = :accountId ORDER BY t.transactionDate DESC, t.createdAt DESC")
    List<AccountTransaction> findByAccountId(@Param("accountId") Long accountId);

    /**
     * Find transactions for an account with pagination
     */
    @Query("SELECT t FROM AccountTransaction t WHERE t.account.id = :accountId ORDER BY t.transactionDate DESC, t.createdAt DESC")
    Page<AccountTransaction> findByAccountIdPaged(@Param("accountId") Long accountId, Pageable pageable);

    /**
     * Find transactions by account number
     */
    @Query("SELECT t FROM AccountTransaction t WHERE t.account.accountNumber = :accountNumber ORDER BY t.transactionDate DESC")
    List<AccountTransaction> findByAccountNumber(@Param("accountNumber") String accountNumber);

    /**
     * Find transactions by type
     */
    List<AccountTransaction> findByTransactionType(TransactionType transactionType);

    /**
     * Find transactions by account and type
     */
    @Query("SELECT t FROM AccountTransaction t WHERE t.account.id = :accountId AND t.transactionType = :transactionType ORDER BY t.transactionDate DESC")
    List<AccountTransaction> findByAccountIdAndTransactionType(@Param("accountId") Long accountId, 
                                                                 @Param("transactionType") TransactionType transactionType);

    /**
     * Find transactions by date range
     */
    @Query("SELECT t FROM AccountTransaction t WHERE t.account.id = :accountId AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<AccountTransaction> findByAccountIdAndDateRange(@Param("accountId") Long accountId,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);

    /**
     * Find non-reversed transactions
     */
    @Query("SELECT t FROM AccountTransaction t WHERE t.account.id = :accountId AND t.isReversed = false ORDER BY t.transactionDate DESC")
    List<AccountTransaction> findNonReversedTransactionsByAccountId(@Param("accountId") Long accountId);

    /**
     * Find reversed transactions
     */
    @Query("SELECT t FROM AccountTransaction t WHERE t.account.id = :accountId AND t.isReversed = true ORDER BY t.reversalDate DESC")
    List<AccountTransaction> findReversedTransactionsByAccountId(@Param("accountId") Long accountId);

    /**
     * Find the latest transaction for an account
     */
    @Query("SELECT t FROM AccountTransaction t WHERE t.account.id = :accountId ORDER BY t.transactionDate DESC, t.createdAt DESC LIMIT 1")
    Optional<AccountTransaction> findLatestTransactionByAccountId(@Param("accountId") Long accountId);

    /**
     * Find related transactions
     */
    List<AccountTransaction> findByRelatedTransactionId(Long relatedTransactionId);

    /**
     * Sum of transactions by type
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM AccountTransaction t WHERE t.account.id = :accountId AND t.transactionType = :transactionType AND t.isReversed = false")
    Double sumAmountByAccountIdAndTransactionType(@Param("accountId") Long accountId, 
                                                   @Param("transactionType") TransactionType transactionType);

    /**
     * Count transactions by type
     */
    long countByAccountIdAndTransactionType(Long accountId, TransactionType transactionType);

    /**
     * Check if transaction reference exists
     */
    boolean existsByTransactionReference(String transactionReference);
}
