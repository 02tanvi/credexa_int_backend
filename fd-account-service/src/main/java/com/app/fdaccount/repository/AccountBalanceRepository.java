package com.app.fdaccount.repository;

import com.app.fdaccount.entity.AccountBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for AccountBalance entity
 */
@Repository
public interface AccountBalanceRepository extends JpaRepository<AccountBalance, Long> {

    /**
     * Find all balances for an account
     */
    @Query("SELECT b FROM AccountBalance b WHERE b.account.id = :accountId ORDER BY b.asOfDate DESC")
    List<AccountBalance> findByAccountId(@Param("accountId") Long accountId);

    /**
     * Find balance by account and balance type
     */
    @Query("SELECT b FROM AccountBalance b WHERE b.account.id = :accountId AND b.balanceType = :balanceType ORDER BY b.asOfDate DESC")
    List<AccountBalance> findByAccountIdAndBalanceType(@Param("accountId") Long accountId, 
                                                        @Param("balanceType") String balanceType);

    /**
     * Find latest balance for an account and balance type
     */
    @Query("SELECT b FROM AccountBalance b WHERE b.account.id = :accountId AND b.balanceType = :balanceType ORDER BY b.asOfDate DESC LIMIT 1")
    Optional<AccountBalance> findLatestBalanceByAccountIdAndType(@Param("accountId") Long accountId, 
                                                                  @Param("balanceType") String balanceType);

    /**
     * Find balances as of a specific date
     */
    @Query("SELECT b FROM AccountBalance b WHERE b.account.id = :accountId AND b.asOfDate <= :asOfDate ORDER BY b.asOfDate DESC")
    List<AccountBalance> findByAccountIdAsOfDate(@Param("accountId") Long accountId, 
                                                  @Param("asOfDate") LocalDate asOfDate);

    /**
     * Find balances between dates
     */
    @Query("SELECT b FROM AccountBalance b WHERE b.account.id = :accountId AND b.asOfDate BETWEEN :startDate AND :endDate ORDER BY b.asOfDate DESC")
    List<AccountBalance> findByAccountIdAndDateRange(@Param("accountId") Long accountId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    /**
     * Find latest balances for an account
     */
    @Query("SELECT b FROM AccountBalance b WHERE b.account.id = :accountId AND b.asOfDate = (SELECT MAX(b2.asOfDate) FROM AccountBalance b2 WHERE b2.account.id = :accountId)")
    List<AccountBalance> findLatestBalancesByAccountId(@Param("accountId") Long accountId);

    /**
     * Delete old balances (for cleanup)
     */
    @Query("DELETE FROM AccountBalance b WHERE b.account.id = :accountId AND b.asOfDate < :cutoffDate")
    void deleteOldBalances(@Param("accountId") Long accountId, @Param("cutoffDate") LocalDate cutoffDate);
}
