package com.app.fdaccount.batch;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.fdaccount.entity.AccountBalance;
import com.app.fdaccount.entity.AccountTransaction;
import com.app.fdaccount.entity.FdAccount;
import com.app.fdaccount.enums.TransactionType;
import com.app.fdaccount.repository.FdAccountRepository;
import com.app.fdaccount.service.integration.CalculatorServiceClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Batch job for daily interest accrual calculation
 * Runs at 1:00 AM daily
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InterestCalculationBatch {

    private final FdAccountRepository accountRepository;
    private final CalculatorServiceClient calculatorServiceClient;

    /**
     * Calculate and accrue interest for all active FD accounts
     * Scheduled to run at 1:00 AM daily
     */
    @Scheduled(cron = "${batch.interest-calculation.cron:0 0 1 * * ?}")
    @Transactional
    public void calculateDailyInterest() {
        log.info("🕐 Starting daily interest calculation batch...");

        LocalDate today = LocalDate.now();
        long startTime = System.currentTimeMillis();

        // Get all active accounts
        List<FdAccount> activeAccounts = accountRepository.findAllActiveAccounts();
        log.info("Found {} active accounts for interest calculation", activeAccounts.size());

        int successCount = 0;
        int skippedCount = 0;
        int errorCount = 0;

        for (FdAccount account : activeAccounts) {
            try {
                // Skip if today is after maturity date
                if (today.isAfter(account.getMaturityDate())) {
                    log.debug("Skipping account {} - already matured", account.getAccountNumber());
                    skippedCount++;
                    continue;
                }

                // Skip if interest was already calculated today
                boolean alreadyCalculated = account.getTransactions().stream()
                        .anyMatch(txn -> txn.getTransactionType() == TransactionType.INTEREST_ACCRUAL &&
                                txn.getTransactionDate().equals(today));

                if (alreadyCalculated) {
                    log.debug("Interest already calculated today for account: {}", account.getAccountNumber());
                    skippedCount++;
                    continue;
                }

                // Calculate daily interest
                BigDecimal interestForDay = calculateDailyInterest(account, today);

                if (interestForDay.compareTo(BigDecimal.ZERO) > 0) {
                    // Get current balances
                    BigDecimal currentPrincipal = getCurrentBalance(account, "PRINCIPAL");
                    BigDecimal currentInterest = getCurrentBalance(account, "INTEREST_ACCRUED");

                    // Calculate new balances
                    BigDecimal newInterest = currentInterest.add(interestForDay);
                    BigDecimal newTotal = currentPrincipal.add(newInterest);

                    // Create interest accrual transaction
                    AccountTransaction transaction = AccountTransaction.builder()
                            .transactionReference(generateTransactionReference())
                            .transactionType(TransactionType.INTEREST_ACCRUAL)
                            .amount(interestForDay)
                            .transactionDate(today)
                            .valueDate(today)
                            .description("Daily interest accrual")
                            .principalBalanceAfter(currentPrincipal)
                            .interestBalanceAfter(newInterest)
                            .totalBalanceAfter(newTotal)
                            .performedBy("SYSTEM-BATCH")
                            .isReversed(false)
                            .build();

                    account.addTransaction(transaction);

                    // Update balances
                    account.addBalance(AccountBalance.builder()
                            .balanceType("INTEREST_ACCRUED")
                            .balance(newInterest)
                            .asOfDate(today)
                            .description("Daily interest accrual")
                            .build());

                    account.addBalance(AccountBalance.builder()
                            .balanceType("AVAILABLE")
                            .balance(newTotal)
                            .asOfDate(today)
                            .description("Total balance after interest accrual")
                            .build());

                    // Save
                    accountRepository.save(account);

                    log.debug("✅ Accrued interest {} for account: {}", interestForDay, account.getAccountNumber());
                    successCount++;
                } else {
                    skippedCount++;
                }

            } catch (Exception e) {
                log.error("❌ Error calculating interest for account: {}", account.getAccountNumber(), e);
                errorCount++;
            }
        }

        long duration = System.currentTimeMillis() - startTime;

        log.info("✅ Interest calculation batch completed in {}ms - Success: {}, Skipped: {}, Errors: {}",
                duration, successCount, skippedCount, errorCount);
    }

    /**
     * Calculate daily interest for an account
     */
    private BigDecimal calculateDailyInterest(FdAccount account, LocalDate date) {
        // Get effective interest rate
        BigDecimal interestRate = account.getCustomInterestRate() != null ?
                account.getCustomInterestRate() : account.getInterestRate();

        // Get principal balance
        BigDecimal principal = getCurrentBalance(account, "PRINCIPAL");

        // Calculate days from effective date
        long daysFromStart = ChronoUnit.DAYS.between(account.getEffectiveDate(), date);

        if (daysFromStart < 0) {
            return BigDecimal.ZERO;
        }

        // Calculate interest for one day
        return calculatorServiceClient.calculateInterest(
                principal,
                interestRate,
                1,  // One day
                account.getInterestCalculationMethod()
        );
    }

    /**
     * Get current balance for a balance type
     */
    private BigDecimal getCurrentBalance(FdAccount account, String balanceType) {
        return account.getBalances().stream()
                .filter(b -> balanceType.equals(b.getBalanceType()))
                .max((b1, b2) -> b1.getAsOfDate().compareTo(b2.getAsOfDate()))
                .map(AccountBalance::getBalance)
                .orElse(balanceType.equals("PRINCIPAL") ? account.getPrincipalAmount() : BigDecimal.ZERO);
    }

    /**
     * Generate unique transaction reference
     */
    private String generateTransactionReference() {
        return "TXN-" + LocalDate.now().toString().replace("-", "") + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
