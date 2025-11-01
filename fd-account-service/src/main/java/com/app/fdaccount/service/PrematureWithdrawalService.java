package com.app.fdaccount.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.fdaccount.dto.PrematureWithdrawalInquiryRequest;
import com.app.fdaccount.dto.PrematureWithdrawalInquiryResponse;
import com.app.fdaccount.dto.TransactionRequest;
import com.app.fdaccount.dto.TransactionResponse;
import com.app.fdaccount.dto.external.ProductDto;
import com.app.fdaccount.entity.FdAccount;
import com.app.fdaccount.enums.AccountStatus;
import com.app.fdaccount.enums.TransactionType;
import com.app.fdaccount.repository.FdAccountRepository;
import com.app.fdaccount.service.integration.CalculatorServiceClient;
import com.app.fdaccount.service.integration.ProductServiceClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for premature withdrawal operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrematureWithdrawalService {

    private final FdAccountRepository accountRepository;
    private final ProductServiceClient productServiceClient;
    private final CalculatorServiceClient calculatorServiceClient;
    private final TransactionService transactionService;

    @Value("${transaction.premature-withdrawal-penalty:2.0}")
    private BigDecimal defaultPenaltyPercentage;

    /**
     * Inquire about premature withdrawal (calculate penalty and net amount)
     */
    @Transactional(readOnly = true)
    public PrematureWithdrawalInquiryResponse inquirePrematureWithdrawal(PrematureWithdrawalInquiryRequest request) {
        log.info("Premature withdrawal inquiry for account: {}", request.getAccountNumber());

        // 1. Find account
        FdAccount account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new RuntimeException("Account not found: " + request.getAccountNumber()));

        // 2. Validate account status
        if (account.getStatus() != AccountStatus.ACTIVE) {
            return buildIneligibleResponse(account, request.getWithdrawalDate(), 
                    "Account is not active");
        }

        // 3. Check if withdrawal date is before maturity
        if (!request.getWithdrawalDate().isBefore(account.getMaturityDate())) {
            return buildIneligibleResponse(account, request.getWithdrawalDate(), 
                    "Withdrawal date is on or after maturity date. Please use regular maturity process.");
        }

        // 4. Check if product allows premature withdrawal
        ProductDto product = productServiceClient.getProductByCode(account.getProductCode());
        if (!Boolean.TRUE.equals(product.getPrematureWithdrawalAllowed())) {
            return buildIneligibleResponse(account, request.getWithdrawalDate(), 
                    "Product does not allow premature withdrawal");
        }

        // 5. Calculate days held
        long daysHeld = ChronoUnit.DAYS.between(account.getEffectiveDate(), request.getWithdrawalDate());
        long totalTermDays = ChronoUnit.DAYS.between(account.getEffectiveDate(), account.getMaturityDate());

        // 6. Get penalty percentage (use default as product doesn't provide it)
        BigDecimal penaltyPercentage = defaultPenaltyPercentage;

        // 7. Calculate interest rate with penalty
        BigDecimal effectiveRate = account.getCustomInterestRate() != null ? 
                account.getCustomInterestRate() : account.getInterestRate();
        BigDecimal revisedRate = effectiveRate.subtract(penaltyPercentage);
        if (revisedRate.compareTo(BigDecimal.ZERO) < 0) {
            revisedRate = BigDecimal.ZERO;
        }

        // 8. Calculate interest earned (simple interest for held period)
        BigDecimal interestEarned = calculatorServiceClient.calculateInterest(
                account.getPrincipalAmount(),
                revisedRate,
                (int) daysHeld,
                account.getInterestCalculationMethod()
        );

        // 9. Calculate penalty amount (difference between normal and revised interest)
        BigDecimal normalInterest = calculatorServiceClient.calculateInterest(
                account.getPrincipalAmount(),
                effectiveRate,
                (int) daysHeld,
                account.getInterestCalculationMethod()
        );
        BigDecimal penaltyAmount = normalInterest.subtract(interestEarned);

        // 10. Calculate TDS if applicable
        BigDecimal tdsAmount = BigDecimal.ZERO;
        if (Boolean.TRUE.equals(account.getTdsApplicable()) && interestEarned.compareTo(BigDecimal.ZERO) > 0) {
            tdsAmount = interestEarned
                    .multiply(account.getTdsRate())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        // 11. Calculate net payable amount
        BigDecimal netInterest = interestEarned.subtract(tdsAmount);
        BigDecimal netPayable = account.getPrincipalAmount().add(netInterest);

        // 12. Build response
        return PrematureWithdrawalInquiryResponse.builder()
                .accountNumber(account.getAccountNumber())
                .effectiveDate(account.getEffectiveDate())
                .proposedWithdrawalDate(request.getWithdrawalDate())
                .maturityDate(account.getMaturityDate())
                .daysHeld((int) daysHeld)
                .totalTermDays((int) totalTermDays)
                .principalAmount(account.getPrincipalAmount())
                .normalInterestRate(effectiveRate)
                .penaltyPercentage(penaltyPercentage)
                .revisedInterestRate(revisedRate)
                .interestEarned(interestEarned)
                .penaltyAmount(penaltyAmount)
                .netInterest(netInterest)
                .tdsAmount(tdsAmount)
                .netPayable(netPayable)
                .message(String.format("Premature withdrawal will result in %.2f%% penalty. Net payable: %.2f", 
                        penaltyPercentage, netPayable))
                .isEligible(true)
                .build();
    }

    /**
     * Process premature withdrawal
     */
    @Transactional
    public TransactionResponse processPrematureWithdrawal(
            String accountNumber, 
            LocalDate withdrawalDate, 
            String performedBy,
            String remarks) {
        
        log.info("Processing premature withdrawal for account: {}", accountNumber);

        // 1. Get inquiry details first
        PrematureWithdrawalInquiryRequest inquiryRequest = PrematureWithdrawalInquiryRequest.builder()
                .accountNumber(accountNumber)
                .withdrawalDate(withdrawalDate)
                .build();

        PrematureWithdrawalInquiryResponse inquiry = inquirePrematureWithdrawal(inquiryRequest);

        if (!inquiry.getIsEligible()) {
            throw new IllegalStateException("Account not eligible for premature withdrawal: " + inquiry.getMessage());
        }

        // 2. Find account
        FdAccount account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

        // 3. Create interest credit transaction (with penalty already applied)
        TransactionRequest interestTxn = TransactionRequest.builder()
                .accountNumber(accountNumber)
                .transactionType(TransactionType.INTEREST_CREDIT)
                .amount(inquiry.getInterestEarned())
                .transactionDate(withdrawalDate)
                .valueDate(withdrawalDate)
                .description(String.format("Interest credit for premature withdrawal (%.2f%% penalty applied)", 
                        inquiry.getPenaltyPercentage()))
                .performedBy(performedBy)
                .build();

        TransactionResponse interestTxnResponse = transactionService.createTransaction(interestTxn);

        // 4. Create TDS deduction transaction if applicable
        TransactionResponse tdsTxnResponse = null;
        if (inquiry.getTdsAmount().compareTo(BigDecimal.ZERO) > 0) {
            TransactionRequest tdsTxn = TransactionRequest.builder()
                    .accountNumber(accountNumber)
                    .transactionType(TransactionType.FEE_DEBIT)
                    .amount(inquiry.getTdsAmount())
                    .transactionDate(withdrawalDate)
                    .valueDate(withdrawalDate)
                    .description("TDS deduction on premature withdrawal interest")
                    .performedBy(performedBy)
                    .relatedTransactionId(interestTxnResponse.getId())
                    .build();

            tdsTxnResponse = transactionService.createTransaction(tdsTxn);
        }

        // 5. Create premature withdrawal transaction
        TransactionRequest withdrawalTxn = TransactionRequest.builder()
                .accountNumber(accountNumber)
                .transactionType(TransactionType.PREMATURE_WITHDRAWAL)
                .amount(inquiry.getNetPayable())
                .transactionDate(withdrawalDate)
                .valueDate(withdrawalDate)
                .description("Premature withdrawal - " + (remarks != null ? remarks : "Customer request"))
                .performedBy(performedBy)
                .relatedTransactionId(interestTxnResponse.getId())
                .build();

        TransactionResponse withdrawalResponse = transactionService.createTransaction(withdrawalTxn);

        // 6. Update account status to CLOSED
        account.setStatus(AccountStatus.CLOSED);
        account.setClosureDate(withdrawalDate);
        account.setUpdatedBy(performedBy);
        accountRepository.save(account);

        log.info("✅ Processed premature withdrawal for account: {} with penalty: {}", 
                accountNumber, inquiry.getPenaltyAmount());

        return withdrawalResponse;
    }

    /**
     * Build ineligible response
     */
    private PrematureWithdrawalInquiryResponse buildIneligibleResponse(
            FdAccount account, 
            LocalDate withdrawalDate, 
            String message) {
        
        return PrematureWithdrawalInquiryResponse.builder()
                .accountNumber(account.getAccountNumber())
                .effectiveDate(account.getEffectiveDate())
                .proposedWithdrawalDate(withdrawalDate)
                .maturityDate(account.getMaturityDate())
                .principalAmount(account.getPrincipalAmount())
                .message(message)
                .isEligible(false)
                .build();
    }
}
