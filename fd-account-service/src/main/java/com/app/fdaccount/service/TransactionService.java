package com.app.fdaccount.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.fdaccount.dto.TransactionRequest;
import com.app.fdaccount.dto.TransactionResponse;
import com.app.fdaccount.entity.AccountBalance;
import com.app.fdaccount.entity.AccountTransaction;
import com.app.fdaccount.entity.FdAccount;
import com.app.fdaccount.enums.TransactionType;
import com.app.fdaccount.repository.AccountTransactionRepository;
import com.app.fdaccount.repository.FdAccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for transaction operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final FdAccountRepository accountRepository;
    private final AccountTransactionRepository transactionRepository;

    /**
     * Create a new transaction
     */
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        log.info("Creating transaction: type={}, amount={} for account={}", 
                request.getTransactionType(), request.getAmount(), request.getAccountNumber());

        // 1. Find account
        FdAccount account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new RuntimeException("Account not found: " + request.getAccountNumber()));

        // 2. Validate transaction
        validateTransaction(account, request);

        // 3. Get current balances
        BigDecimal currentPrincipal = getCurrentBalance(account, "PRINCIPAL");
        BigDecimal currentInterest = getCurrentBalance(account, "INTEREST_ACCRUED");
        BigDecimal currentTotal = currentPrincipal.add(currentInterest);

        // 4. Calculate new balances based on transaction type
        BigDecimal newPrincipal = currentPrincipal;
        BigDecimal newInterest = currentInterest;
        BigDecimal newTotal = currentTotal;

        switch (request.getTransactionType()) {
            case ADDITIONAL_DEPOSIT:
                newPrincipal = currentPrincipal.add(request.getAmount());
                newTotal = newTotal.add(request.getAmount());
                break;

            case WITHDRAWAL:
                if (currentTotal.compareTo(request.getAmount()) < 0) {
                    throw new IllegalArgumentException("Insufficient balance for withdrawal");
                }
                // Withdraw from interest first, then principal
                if (currentInterest.compareTo(request.getAmount()) >= 0) {
                    newInterest = currentInterest.subtract(request.getAmount());
                } else {
                    BigDecimal remaining = request.getAmount().subtract(currentInterest);
                    newInterest = BigDecimal.ZERO;
                    newPrincipal = currentPrincipal.subtract(remaining);
                }
                newTotal = newTotal.subtract(request.getAmount());
                break;

            case INTEREST_CREDIT:
                newInterest = currentInterest.add(request.getAmount());
                newTotal = newTotal.add(request.getAmount());
                break;

            case INTEREST_ACCRUAL:
                newInterest = currentInterest.add(request.getAmount());
                newTotal = newTotal.add(request.getAmount());
                break;

            case INTEREST_CAPITALIZATION:
                // Move interest to principal
                newPrincipal = currentPrincipal.add(request.getAmount());
                newInterest = currentInterest.subtract(request.getAmount());
                break;

            case FEE_DEBIT:
            case PENALTY:
                if (currentTotal.compareTo(request.getAmount()) < 0) {
                    throw new IllegalArgumentException("Insufficient balance for fee/penalty");
                }
                // Deduct from interest first, then principal
                if (currentInterest.compareTo(request.getAmount()) >= 0) {
                    newInterest = currentInterest.subtract(request.getAmount());
                } else {
                    BigDecimal remaining = request.getAmount().subtract(currentInterest);
                    newInterest = BigDecimal.ZERO;
                    newPrincipal = currentPrincipal.subtract(remaining);
                }
                newTotal = newTotal.subtract(request.getAmount());
                break;

            case ADJUSTMENT:
                // Manual adjustment - can be positive or negative
                newPrincipal = currentPrincipal.add(request.getAmount());
                newTotal = newTotal.add(request.getAmount());
                break;

            default:
                throw new IllegalArgumentException("Transaction type not supported for manual creation: " + 
                        request.getTransactionType());
        }

        // 5. Create transaction
        AccountTransaction transaction = AccountTransaction.builder()
                .transactionReference(generateTransactionReference())
                .transactionType(request.getTransactionType())
                .amount(request.getAmount())
                .transactionDate(request.getTransactionDate())
                .valueDate(request.getValueDate() != null ? request.getValueDate() : request.getTransactionDate())
                .description(request.getDescription())
                .principalBalanceAfter(newPrincipal)
                .interestBalanceAfter(newInterest)
                .totalBalanceAfter(newTotal)
                .performedBy(request.getPerformedBy())
                .relatedTransactionId(request.getRelatedTransactionId())
                .isReversed(false)
                .build();

        account.addTransaction(transaction);

        // 6. Update balances
        updateAccountBalance(account, "PRINCIPAL", newPrincipal, request.getTransactionDate());
        updateAccountBalance(account, "INTEREST_ACCRUED", newInterest, request.getTransactionDate());
        updateAccountBalance(account, "AVAILABLE", newTotal, request.getTransactionDate());

        // 7. Save
        FdAccount savedAccount = accountRepository.save(account);
        AccountTransaction savedTransaction = savedAccount.getTransactions().stream()
                .filter(t -> t.getTransactionReference().equals(transaction.getTransactionReference()))
                .findFirst()
                .orElseThrow();

        log.info("✅ Created transaction: {} with reference: {}", 
                savedTransaction.getTransactionType(), savedTransaction.getTransactionReference());

        return mapToTransactionResponse(savedTransaction);
    }

    /**
     * Reverse a transaction
     */
    @Transactional
    public TransactionResponse reverseTransaction(String transactionReference, String reason, String performedBy) {
        log.info("Reversing transaction: {}", transactionReference);

        // 1. Find original transaction
        AccountTransaction originalTransaction = transactionRepository.findByTransactionReference(transactionReference)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionReference));

        if (originalTransaction.getIsReversed()) {
            throw new IllegalStateException("Transaction already reversed: " + transactionReference);
        }

        // 2. Find account
        FdAccount account = originalTransaction.getAccount();

        // 3. Get current balances
        BigDecimal currentPrincipal = getCurrentBalance(account, "PRINCIPAL");
        BigDecimal currentInterest = getCurrentBalance(account, "INTEREST_ACCRUED");
        BigDecimal currentTotal = currentPrincipal.add(currentInterest);

        // 4. Calculate reversed balances (opposite of original)
        BigDecimal reversalAmount = originalTransaction.getAmount();
        BigDecimal newPrincipal = currentPrincipal;
        BigDecimal newInterest = currentInterest;
        BigDecimal newTotal = currentTotal;

        switch (originalTransaction.getTransactionType()) {
            case ADDITIONAL_DEPOSIT:
            case INTEREST_CREDIT:
            case INTEREST_ACCRUAL:
                // Reverse means subtract
                newTotal = currentTotal.subtract(reversalAmount);
                if (originalTransaction.getTransactionType() == TransactionType.ADDITIONAL_DEPOSIT) {
                    newPrincipal = currentPrincipal.subtract(reversalAmount);
                } else {
                    newInterest = currentInterest.subtract(reversalAmount);
                }
                break;

            case WITHDRAWAL:
            case FEE_DEBIT:
            case PENALTY:
                // Reverse means add back
                newTotal = currentTotal.add(reversalAmount);
                newInterest = currentInterest.add(reversalAmount); // Add back to interest first
                break;

            default:
                throw new IllegalArgumentException("Cannot reverse transaction type: " + 
                        originalTransaction.getTransactionType());
        }

        // 5. Mark original as reversed
        originalTransaction.setIsReversed(true);
        originalTransaction.setReversalDate(LocalDateTime.now());
        originalTransaction.setReversalReason(reason);

        // 6. Create reversal transaction
        AccountTransaction reversalTransaction = AccountTransaction.builder()
                .transactionReference(generateTransactionReference())
                .transactionType(TransactionType.REVERSAL)
                .amount(reversalAmount)
                .transactionDate(LocalDate.now())
                .valueDate(LocalDate.now())
                .description("Reversal of " + transactionReference + " - " + reason)
                .principalBalanceAfter(newPrincipal)
                .interestBalanceAfter(newInterest)
                .totalBalanceAfter(newTotal)
                .performedBy(performedBy)
                .relatedTransactionId(originalTransaction.getId())
                .isReversed(false)
                .build();

        account.addTransaction(reversalTransaction);
        originalTransaction.setReversalTransactionId(reversalTransaction.getId());

        // 7. Update balances
        updateAccountBalance(account, "PRINCIPAL", newPrincipal, LocalDate.now());
        updateAccountBalance(account, "INTEREST_ACCRUED", newInterest, LocalDate.now());
        updateAccountBalance(account, "AVAILABLE", newTotal, LocalDate.now());

        // 8. Save
        accountRepository.save(account);
        transactionRepository.save(originalTransaction);

        log.info("✅ Reversed transaction: {}", transactionReference);

        return mapToTransactionResponse(reversalTransaction);
    }

    /**
     * Get transactions for an account
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAccountTransactions(String accountNumber) {
        List<AccountTransaction> transactions = transactionRepository.findByAccountNumber(accountNumber);

        return transactions.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get transactions with pagination
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAccountTransactionsPaged(String accountNumber, int page, int size) {
        FdAccount account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

        Pageable pageable = PageRequest.of(page, size);
        Page<AccountTransaction> transactions = transactionRepository.findByAccountIdPaged(account.getId(), pageable);

        return transactions.map(this::mapToTransactionResponse);
    }

    /**
     * Get transaction by reference
     */
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByReference(String transactionReference) {
        AccountTransaction transaction = transactionRepository.findByTransactionReference(transactionReference)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionReference));

        return mapToTransactionResponse(transaction);
    }

    /**
     * Validate transaction
     */
    private void validateTransaction(FdAccount account, TransactionRequest request) {
        // Check account is active
        if (account.getStatus() != com.app.fdaccount.enums.AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active: " + account.getAccountNumber());
        }

        // Check transaction date is not before effective date
        if (request.getTransactionDate().isBefore(account.getEffectiveDate())) {
            throw new IllegalArgumentException("Transaction date cannot be before account effective date");
        }

        // Check amount is positive
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
    }

    /**
     * Get current balance for a balance type
     */
    private BigDecimal getCurrentBalance(FdAccount account, String balanceType) {
        return account.getBalances().stream()
                .filter(b -> balanceType.equals(b.getBalanceType()))
                .max((b1, b2) -> b1.getAsOfDate().compareTo(b2.getAsOfDate()))
                .map(AccountBalance::getBalance)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Update or create account balance
     */
    private void updateAccountBalance(FdAccount account, String balanceType, BigDecimal balance, LocalDate asOfDate) {
        AccountBalance accountBalance = AccountBalance.builder()
                .balanceType(balanceType)
                .balance(balance)
                .asOfDate(asOfDate)
                .description("Balance after transaction on " + asOfDate)
                .build();

        account.addBalance(accountBalance);
    }

    /**
     * Generate unique transaction reference
     */
    private String generateTransactionReference() {
        return "TXN-" + LocalDate.now().toString().replace("-", "") + "-" + 
               UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Map entity to response DTO
     */
    private TransactionResponse mapToTransactionResponse(AccountTransaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionReference(transaction.getTransactionReference())
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .transactionDate(transaction.getTransactionDate())
                .valueDate(transaction.getValueDate())
                .description(transaction.getDescription())
                .performedBy(transaction.getPerformedBy())
                .principalBalanceAfter(transaction.getPrincipalBalanceAfter())
                .interestBalanceAfter(transaction.getInterestBalanceAfter())
                .totalBalanceAfter(transaction.getTotalBalanceAfter())
                .isReversed(transaction.getIsReversed())
                .reversalTransactionId(transaction.getReversalTransactionId())
                .reversalDate(transaction.getReversalDate())
                .reversalReason(transaction.getReversalReason())
                .relatedTransactionId(transaction.getRelatedTransactionId())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
