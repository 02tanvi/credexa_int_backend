package com.app.fdaccount.batch;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.fdaccount.entity.AccountBalance;
import com.app.fdaccount.entity.AccountTransaction;
import com.app.fdaccount.entity.FdAccount;
import com.app.fdaccount.enums.AccountStatus;
import com.app.fdaccount.enums.MaturityInstruction;
import com.app.fdaccount.enums.TransactionType;
import com.app.fdaccount.repository.FdAccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Batch job for processing matured FD accounts
 * Runs at 1:30 AM daily (after interest calculation)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MaturityProcessingBatch {

    private final FdAccountRepository accountRepository;

    /**
     * Process all accounts that matured today
     * Scheduled to run at 1:30 AM daily
     */
    @Scheduled(cron = "${batch.maturity-processing.cron:0 30 1 * * ?}")
    @Transactional
    public void processMaturedAccounts() {
        log.info("🕐 Starting maturity processing batch...");

        LocalDate today = LocalDate.now();
        long startTime = System.currentTimeMillis();

        // Get accounts maturing today
        List<FdAccount> maturingAccounts = accountRepository.findByMaturityDateAndStatus(
                today, AccountStatus.ACTIVE);

        log.info("Found {} accounts maturing today", maturingAccounts.size());

        int successCount = 0;
        int errorCount = 0;

        for (FdAccount account : maturingAccounts) {
            try {
                processMaturedAccount(account, today);
                successCount++;
            } catch (Exception e) {
                log.error("❌ Error processing maturity for account: {}", account.getAccountNumber(), e);
                errorCount++;
            }
        }

        long duration = System.currentTimeMillis() - startTime;

        log.info("✅ Maturity processing batch completed in {}ms - Success: {}, Errors: {}",
                duration, successCount, errorCount);
    }

    /**
     * Process a single matured account
     */
    private void processMaturedAccount(FdAccount account, LocalDate maturityDate) {
        log.info("Processing maturity for account: {}", account.getAccountNumber());

        // Get current balances
        BigDecimal currentPrincipal = getCurrentBalance(account, "PRINCIPAL");
        BigDecimal currentInterest = getCurrentBalance(account, "INTEREST_ACCRUED");
        BigDecimal totalAmount = currentPrincipal.add(currentInterest);

        // Process based on maturity instruction
        MaturityInstruction instruction = account.getMaturityInstruction() != null ?
                account.getMaturityInstruction() : MaturityInstruction.HOLD;

        switch (instruction) {
            case CLOSE_AND_PAYOUT:
                processClosureAndPayout(account, totalAmount, maturityDate);
                break;

            case RENEW_PRINCIPAL_ONLY:
                processRenewalPrincipalOnly(account, currentPrincipal, currentInterest, maturityDate);
                break;

            case RENEW_WITH_INTEREST:
                processRenewalWithInterest(account, totalAmount, maturityDate);
                break;

            case TRANSFER_TO_SAVINGS:
            case TRANSFER_TO_CURRENT:
                processTransfer(account, totalAmount, maturityDate, instruction);
                break;

            case HOLD:
            default:
                processHold(account, maturityDate);
                break;
        }

        log.info("✅ Processed maturity for account: {} with instruction: {}",
                account.getAccountNumber(), instruction);
    }

    /**
     * Close account and payout full amount
     */
    private void processClosureAndPayout(FdAccount account, BigDecimal amount, LocalDate date) {
        // Create maturity payout transaction
        AccountTransaction transaction = AccountTransaction.builder()
                .transactionReference(generateTransactionReference())
                .transactionType(TransactionType.MATURITY_PAYOUT)
                .amount(amount)
                .transactionDate(date)
                .valueDate(date)
                .description("Maturity payout - Account closed")
                .principalBalanceAfter(BigDecimal.ZERO)
                .interestBalanceAfter(BigDecimal.ZERO)
                .totalBalanceAfter(BigDecimal.ZERO)
                .performedBy("SYSTEM-BATCH")
                .isReversed(false)
                .build();

        account.addTransaction(transaction);

        // Update account status
        account.setStatus(AccountStatus.MATURED);
        account.setClosureDate(date);

        // Update balances to zero
        updateBalance(account, "PRINCIPAL", BigDecimal.ZERO, date);
        updateBalance(account, "INTEREST_ACCRUED", BigDecimal.ZERO, date);
        updateBalance(account, "AVAILABLE", BigDecimal.ZERO, date);

        accountRepository.save(account);

        log.info("Account {} closed with payout: {}", account.getAccountNumber(), amount);
    }

    /**
     * Renew with principal only, payout interest
     */
    private void processRenewalPrincipalOnly(FdAccount account, BigDecimal principal,
                                             BigDecimal interest, LocalDate date) {
        // Payout interest
        if (interest.compareTo(BigDecimal.ZERO) > 0) {
            AccountTransaction interestPayout = AccountTransaction.builder()
                    .transactionReference(generateTransactionReference())
                    .transactionType(TransactionType.MATURITY_PAYOUT)
                    .amount(interest)
                    .transactionDate(date)
                    .valueDate(date)
                    .description("Interest payout on maturity - Renewing with principal")
                    .principalBalanceAfter(principal)
                    .interestBalanceAfter(BigDecimal.ZERO)
                    .totalBalanceAfter(principal)
                    .performedBy("SYSTEM-BATCH")
                    .isReversed(false)
                    .build();

            account.addTransaction(interestPayout);
        }

        // Create renewal transaction
        AccountTransaction renewal = AccountTransaction.builder()
                .transactionReference(generateTransactionReference())
                .transactionType(TransactionType.MATURITY_RENEWAL)
                .amount(principal)
                .transactionDate(date)
                .valueDate(date)
                .description("FD renewed with principal only")
                .principalBalanceAfter(principal)
                .interestBalanceAfter(BigDecimal.ZERO)
                .totalBalanceAfter(principal)
                .performedBy("SYSTEM-BATCH")
                .isReversed(false)
                .build();

        account.addTransaction(renewal);

        // Update account for new term
        account.setEffectiveDate(date);
        account.setMaturityDate(date.plusMonths(account.getTermMonths()));
        account.setStatus(AccountStatus.ACTIVE); // Keep active for new term

        // Reset interest balance
        updateBalance(account, "INTEREST_ACCRUED", BigDecimal.ZERO, date);
        updateBalance(account, "AVAILABLE", principal, date);

        accountRepository.save(account);

        log.info("Account {} renewed with principal: {}, interest payout: {}",
                account.getAccountNumber(), principal, interest);
    }

    /**
     * Renew with principal + interest
     */
    private void processRenewalWithInterest(FdAccount account, BigDecimal totalAmount, LocalDate date) {
        // Create renewal transaction
        AccountTransaction renewal = AccountTransaction.builder()
                .transactionReference(generateTransactionReference())
                .transactionType(TransactionType.MATURITY_RENEWAL)
                .amount(totalAmount)
                .transactionDate(date)
                .valueDate(date)
                .description("FD renewed with principal and interest")
                .principalBalanceAfter(totalAmount)
                .interestBalanceAfter(BigDecimal.ZERO)
                .totalBalanceAfter(totalAmount)
                .performedBy("SYSTEM-BATCH")
                .isReversed(false)
                .build();

        account.addTransaction(renewal);

        // Update account for new term
        account.setPrincipalAmount(totalAmount); // New principal includes old interest
        account.setEffectiveDate(date);
        account.setMaturityDate(date.plusMonths(account.getTermMonths()));
        account.setStatus(AccountStatus.ACTIVE);

        // Reset balances
        updateBalance(account, "PRINCIPAL", totalAmount, date);
        updateBalance(account, "INTEREST_ACCRUED", BigDecimal.ZERO, date);
        updateBalance(account, "AVAILABLE", totalAmount, date);

        accountRepository.save(account);

        log.info("Account {} renewed with total amount: {}", account.getAccountNumber(), totalAmount);
    }

    /**
     * Transfer to savings/current account
     */
    private void processTransfer(FdAccount account, BigDecimal amount, LocalDate date,
                                  MaturityInstruction instruction) {
        String transferAccount = account.getMaturityTransferAccount();

        // Create transfer transaction
        AccountTransaction transfer = AccountTransaction.builder()
                .transactionReference(generateTransactionReference())
                .transactionType(TransactionType.MATURITY_TRANSFER)
                .amount(amount)
                .transactionDate(date)
                .valueDate(date)
                .description(String.format("Maturity transfer to %s account: %s",
                        instruction == MaturityInstruction.TRANSFER_TO_SAVINGS ? "savings" : "current",
                        transferAccount != null ? transferAccount : "N/A"))
                .principalBalanceAfter(BigDecimal.ZERO)
                .interestBalanceAfter(BigDecimal.ZERO)
                .totalBalanceAfter(BigDecimal.ZERO)
                .performedBy("SYSTEM-BATCH")
                .isReversed(false)
                .build();

        account.addTransaction(transfer);

        // Update account status
        account.setStatus(AccountStatus.MATURED);
        account.setClosureDate(date);

        // Update balances to zero
        updateBalance(account, "PRINCIPAL", BigDecimal.ZERO, date);
        updateBalance(account, "INTEREST_ACCRUED", BigDecimal.ZERO, date);
        updateBalance(account, "AVAILABLE", BigDecimal.ZERO, date);

        accountRepository.save(account);

        log.info("Account {} transferred {} to account: {}",
                account.getAccountNumber(), amount, transferAccount);
    }

    /**
     * Hold maturity amount (no action)
     */
    private void processHold(FdAccount account, LocalDate date) {
        // Just update status to MATURED, keep balances as is
        account.setStatus(AccountStatus.MATURED);
        accountRepository.save(account);

        log.info("Account {} matured with HOLD instruction - no payout", account.getAccountNumber());
    }

    /**
     * Get current balance
     */
    private BigDecimal getCurrentBalance(FdAccount account, String balanceType) {
        return account.getBalances().stream()
                .filter(b -> balanceType.equals(b.getBalanceType()))
                .max((b1, b2) -> b1.getAsOfDate().compareTo(b2.getAsOfDate()))
                .map(AccountBalance::getBalance)
                .orElse(balanceType.equals("PRINCIPAL") ? account.getPrincipalAmount() : BigDecimal.ZERO);
    }

    /**
     * Update balance
     */
    private void updateBalance(FdAccount account, String balanceType, BigDecimal balance, LocalDate date) {
        AccountBalance accountBalance = AccountBalance.builder()
                .balanceType(balanceType)
                .balance(balance)
                .asOfDate(date)
                .description("Balance after maturity processing")
                .build();

        account.addBalance(accountBalance);
    }

    /**
     * Generate transaction reference
     */
    private String generateTransactionReference() {
        return "TXN-" + LocalDate.now().toString().replace("-", "") + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
