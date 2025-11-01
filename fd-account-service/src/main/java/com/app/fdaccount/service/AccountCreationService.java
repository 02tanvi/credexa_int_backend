package com.app.fdaccount.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.fdaccount.dto.AccountResponse;
import com.app.fdaccount.dto.AccountRoleRequest;
import com.app.fdaccount.dto.BalanceResponse;
import com.app.fdaccount.dto.CreateAccountRequest;
import com.app.fdaccount.dto.CustomizeAccountRequest;
import com.app.fdaccount.dto.RoleResponse;
import com.app.fdaccount.dto.external.CalculationResultDto;
import com.app.fdaccount.dto.external.CustomerDto;
import com.app.fdaccount.dto.external.ProductDto;
import com.app.fdaccount.entity.AccountBalance;
import com.app.fdaccount.entity.AccountRole;
import com.app.fdaccount.entity.AccountTransaction;
import com.app.fdaccount.entity.FdAccount;
import com.app.fdaccount.enums.AccountStatus;
import com.app.fdaccount.enums.MaturityInstruction;
import com.app.fdaccount.enums.TransactionType;
import com.app.fdaccount.repository.FdAccountRepository;
import com.app.fdaccount.service.accountnumber.AccountNumberGenerator;
import com.app.fdaccount.service.integration.CalculatorServiceClient;
import com.app.fdaccount.service.integration.CustomerServiceClient;
import com.app.fdaccount.service.integration.EmailServiceClient;
import com.app.fdaccount.service.integration.ProductServiceClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for FD Account creation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountCreationService {

        private final FdAccountRepository accountRepository;
        private final AccountNumberGenerator accountNumberGenerator;
        private final ProductServiceClient productServiceClient;
        private final CustomerServiceClient customerServiceClient;
        private final CalculatorServiceClient calculatorServiceClient;
        private final EmailServiceClient emailServiceClient;

        @Value("${account-number.generator.iban.country-code:IN}")
        private String ibanCountryCode;

        @Value("${account-number.generator.iban.bank-code:CRXA}")
        private String ibanBankCode;

        /**
         * Create account with values inherited from product
         */
        @Transactional
        public AccountResponse createAccount(CreateAccountRequest request) {
                log.info("Creating FD account with product: {}", request.getProductCode());

                // 1. Validate and fetch product
                log.debug("Fetching product: {}", request.getProductCode());
                ProductDto product = productServiceClient.getProductByCode(request.getProductCode());
                log.debug("Product fetched: {} - {}", product.getProductCode(), product.getProductName());
                validateProductLimits(product, request.getPrincipalAmount(), request.getTermMonths());

                // 2. Validate all customers
                for (AccountRoleRequest roleRequest : request.getRoles()) {
                        log.debug("Fetching customer: {}", roleRequest.getCustomerId());
                        CustomerDto customer = customerServiceClient
                                        .getCustomerById(roleRequest.getCustomerId());
                        log.debug("Validated customer: {} - {}", customer.getCustomerId(),
                                        customer.getCustomerName());
                }

                // 3. Calculate maturity
                log.debug("Calculating maturity for amount: {}, rate: {}, term: {} months",
                                request.getPrincipalAmount(), product.getBaseInterestRate(),
                                request.getTermMonths());
                CalculationResultDto calculation = calculatorServiceClient.calculateMaturity(
                                request.getPrincipalAmount(),
                                product.getBaseInterestRate(),
                                request.getTermMonths(),
                                "COMPOUND", // Default calculation type for FD accounts
                                "QUARTERLY" // Default compounding frequency
                );
                log.debug("Maturity calculated: Amount={}, Date={}",
                                calculation.getMaturityAmount(), calculation.getMaturityDate());

                // 4. Generate account number
                String accountNumber = accountNumberGenerator.generateAccountNumber(request.getBranchCode());
                String ibanNumber = accountNumberGenerator.generateIBAN(accountNumber, ibanCountryCode,
                                ibanBankCode);

                // 5. Create account entity
                FdAccount account = FdAccount.builder()
                                .accountNumber(accountNumber)
                                .ibanNumber(ibanNumber)
                                .accountName(request.getAccountName())
                                .productCode(product.getProductCode())
                                .productName(product.getProductName())
                                .status(AccountStatus.ACTIVE)
                                .principalAmount(request.getPrincipalAmount())
                                .interestRate(product.getBaseInterestRate())
                                .termMonths(request.getTermMonths())
                                .maturityAmount(calculation.getMaturityAmount())
                                .effectiveDate(request.getEffectiveDate())
                                .maturityDate(calculation.getMaturityDate())
                                .interestCalculationMethod(product.getInterestCalculationMethod() != null
                                                ? product.getInterestCalculationMethod()
                                                : "COMPOUND")
                                .interestPayoutFrequency(product.getInterestPayoutFrequency() != null
                                                ? product.getInterestPayoutFrequency()
                                                : "ON_MATURITY")
                                .autoRenewal(request.getAutoRenewal() != null ? request.getAutoRenewal()
                                                : (product.getAutoRenewalAllowed() != null
                                                                ? product.getAutoRenewalAllowed()
                                                                : false))
                                .maturityInstruction(request.getMaturityInstruction() != null
                                                ? request.getMaturityInstruction()
                                                : MaturityInstruction.HOLD)
                                .maturityTransferAccount(request.getMaturityTransferAccount())
                                .branchCode(request.getBranchCode())
                                .branchName(request.getBranchName())
                                .tdsApplicable(request.getTdsApplicable() != null ? request.getTdsApplicable()
                                                : (product.getTdsApplicable() != null
                                                                ? product.getTdsApplicable()
                                                                : true))
                                .tdsRate(product.getTdsRate() != null ? product.getTdsRate()
                                                : BigDecimal.valueOf(10.0))
                                .remarks(request.getRemarks())
                                .createdBy(request.getCreatedBy() != null ? request.getCreatedBy() : "SYSTEM")
                                .updatedBy(request.getCreatedBy() != null ? request.getCreatedBy() : "SYSTEM")
                                .build();

                // 6. Add roles
                for (AccountRoleRequest roleRequest : request.getRoles()) {
                        AccountRole role = AccountRole.builder()
                                        .customerId(roleRequest.getCustomerId())
                                        .customerName(roleRequest.getCustomerName())
                                        .roleType(roleRequest.getRoleType())
                                        .ownershipPercentage(roleRequest.getOwnershipPercentage())
                                        .isPrimary(roleRequest.getIsPrimary())
                                        .isActive(true)
                                        .remarks(roleRequest.getRemarks())
                                        .build();
                        account.addRole(role);
                }

                // 7. Create initial deposit transaction
                AccountTransaction initialDeposit = AccountTransaction.builder()
                                .transactionReference(generateTransactionReference())
                                .transactionType(TransactionType.INITIAL_DEPOSIT)
                                .amount(request.getPrincipalAmount())
                                .transactionDate(request.getEffectiveDate())
                                .valueDate(request.getEffectiveDate())
                                .description("Initial deposit for FD account opening")
                                .principalBalanceAfter(request.getPrincipalAmount())
                                .interestBalanceAfter(BigDecimal.ZERO)
                                .totalBalanceAfter(request.getPrincipalAmount())
                                .performedBy(request.getCreatedBy())
                                .isReversed(false)
                                .build();
                account.addTransaction(initialDeposit);

                // 8. Create initial balances
                AccountBalance principalBalance = AccountBalance.builder()
                                .balanceType("PRINCIPAL")
                                .balance(request.getPrincipalAmount())
                                .asOfDate(request.getEffectiveDate())
                                .description("Initial principal amount")
                                .build();
                account.addBalance(principalBalance);

                AccountBalance interestBalance = AccountBalance.builder()
                                .balanceType("INTEREST_ACCRUED")
                                .balance(BigDecimal.ZERO)
                                .asOfDate(request.getEffectiveDate())
                                .description("Initial interest accrued")
                                .build();
                account.addBalance(interestBalance);

                // 9. Save account
                FdAccount savedAccount = accountRepository.save(account);

                log.info("✅ Created FD account: {} for customer with principal: {}",
                                savedAccount.getAccountNumber(), savedAccount.getPrincipalAmount());

                // 10. Send new account email to primary account holder
                sendNewAccountEmail(savedAccount, product);

                return mapToAccountResponse(savedAccount);
        }

        /**
         * Create account with customized values within product limits
         */
        @Transactional
        public AccountResponse createCustomizedAccount(CustomizeAccountRequest request) {
                log.info("Creating customized FD account with product: {}", request.getProductCode());

                // 1. Validate and fetch product
                ProductDto product = productServiceClient.getProductByCode(request.getProductCode());

                // 2. Validate customized values against product limits
                validateCustomizedValues(product, request);

                // 3. Validate all customers
                for (AccountRoleRequest roleRequest : request.getRoles()) {
                        CustomerDto customer = customerServiceClient.getCustomerById(roleRequest.getCustomerId());
                        log.debug("Validated customer: {} - {}", customer.getCustomerId(), customer.getCustomerName());
                }

                // 4. Use customized values or defaults
                BigDecimal interestRate = request.getCustomInterestRate() != null ? request.getCustomInterestRate()
                                : product.getBaseInterestRate();
                Integer termMonths = request.getCustomTermMonths();
                String interestCalcMethod = request.getCustomInterestCalculationMethod() != null
                                ? request.getCustomInterestCalculationMethod()
                                : product.getInterestCalculationMethod();
                String interestPayoutFreq = request.getCustomInterestPayoutFrequency() != null
                                ? request.getCustomInterestPayoutFrequency()
                                : product.getInterestPayoutFrequency();
                BigDecimal tdsRate = request.getCustomTdsRate() != null ? request.getCustomTdsRate()
                                : product.getTdsRate();

                // 5. Calculate maturity with customized values
                CalculationResultDto calculation = calculatorServiceClient.calculateMaturity(
                                request.getPrincipalAmount(),
                                interestRate,
                                termMonths,
                                request.getCustomInterestCalculationMethod() != null
                                                ? request.getCustomInterestCalculationMethod()
                                                : "COMPOUND",
                                request.getCustomInterestPayoutFrequency() != null
                                                ? request.getCustomInterestPayoutFrequency()
                                                : "QUARTERLY");

                // 6. Generate account number
                String accountNumber = accountNumberGenerator.generateAccountNumber(request.getBranchCode());
                String ibanNumber = accountNumberGenerator.generateIBAN(accountNumber, ibanCountryCode, ibanBankCode);

                // 7. Create account entity with customized values
                FdAccount account = FdAccount.builder()
                                .accountNumber(accountNumber)
                                .ibanNumber(ibanNumber)
                                .accountName(request.getAccountName())
                                .productCode(product.getProductCode())
                                .productName(product.getProductName())
                                .status(AccountStatus.ACTIVE)
                                .principalAmount(request.getPrincipalAmount())
                                .interestRate(product.getBaseInterestRate())
                                .customInterestRate(interestRate) // Store customized rate
                                .termMonths(termMonths)
                                .maturityAmount(calculation.getMaturityAmount())
                                .effectiveDate(request.getEffectiveDate())
                                .maturityDate(calculation.getMaturityDate())
                                .interestCalculationMethod(interestCalcMethod)
                                .interestPayoutFrequency(interestPayoutFreq)
                                .autoRenewal(request.getAutoRenewal() != null ? request.getAutoRenewal() : false)
                                .maturityInstruction(request.getMaturityInstruction() != null
                                                ? request.getMaturityInstruction()
                                                : MaturityInstruction.HOLD)
                                .maturityTransferAccount(request.getMaturityTransferAccount())
                                .branchCode(request.getBranchCode())
                                .branchName(request.getBranchName())
                                .tdsApplicable(request.getTdsApplicable() != null ? request.getTdsApplicable() : true)
                                .tdsRate(tdsRate)
                                .remarks(request.getRemarks())
                                .createdBy(request.getCreatedBy())
                                .updatedBy(request.getCreatedBy())
                                .build();

                // 8. Add roles (same as standard)
                for (AccountRoleRequest roleRequest : request.getRoles()) {
                        AccountRole role = AccountRole.builder()
                                        .customerId(roleRequest.getCustomerId())
                                        .customerName(roleRequest.getCustomerName())
                                        .roleType(roleRequest.getRoleType())
                                        .ownershipPercentage(roleRequest.getOwnershipPercentage())
                                        .isPrimary(roleRequest.getIsPrimary())
                                        .isActive(true)
                                        .remarks(roleRequest.getRemarks())
                                        .build();
                        account.addRole(role);
                }

                // 9. Create initial deposit transaction
                AccountTransaction initialDeposit = AccountTransaction.builder()
                                .transactionReference(generateTransactionReference())
                                .transactionType(TransactionType.INITIAL_DEPOSIT)
                                .amount(request.getPrincipalAmount())
                                .transactionDate(request.getEffectiveDate())
                                .valueDate(request.getEffectiveDate())
                                .description("Initial deposit for customized FD account opening")
                                .principalBalanceAfter(request.getPrincipalAmount())
                                .interestBalanceAfter(BigDecimal.ZERO)
                                .totalBalanceAfter(request.getPrincipalAmount())
                                .performedBy(request.getCreatedBy())
                                .isReversed(false)
                                .build();
                account.addTransaction(initialDeposit);

                // 10. Create initial balances
                account.addBalance(AccountBalance.builder()
                                .balanceType("PRINCIPAL")
                                .balance(request.getPrincipalAmount())
                                .asOfDate(request.getEffectiveDate())
                                .description("Initial principal amount")
                                .build());

                account.addBalance(AccountBalance.builder()
                                .balanceType("INTEREST_ACCRUED")
                                .balance(BigDecimal.ZERO)
                                .asOfDate(request.getEffectiveDate())
                                .description("Initial interest accrued")
                                .build());

                // 11. Save account
                FdAccount savedAccount = accountRepository.save(account);

                log.info("✅ Created customized FD account: {} with custom rate: {}%, term: {} months",
                                savedAccount.getAccountNumber(), interestRate, termMonths);

                // 12. Send new account email to primary account holder
                sendNewAccountEmail(savedAccount, product);

                return mapToAccountResponse(savedAccount);
        }

        /**
         * Validate product limits
         */
        private void validateProductLimits(ProductDto product, BigDecimal amount, Integer term) {
                // Validate amount
                if (product.getMinAmount() != null && amount.compareTo(product.getMinAmount()) < 0) {
                        throw new IllegalArgumentException(
                                        String.format("Principal amount %.2f is less than minimum %.2f",
                                                        amount, product.getMinAmount()));
                }
                if (product.getMaxAmount() != null && amount.compareTo(product.getMaxAmount()) > 0) {
                        throw new IllegalArgumentException(
                                        String.format("Principal amount %.2f exceeds maximum %.2f",
                                                        amount, product.getMaxAmount()));
                }

                // Validate term
                if (product.getMinTermMonths() != null && term < product.getMinTermMonths()) {
                        throw new IllegalArgumentException(
                                        String.format("Term %d months is less than minimum %d months",
                                                        term, product.getMinTermMonths()));
                }
                if (product.getMaxTermMonths() != null && term > product.getMaxTermMonths()) {
                        throw new IllegalArgumentException(
                                        String.format("Term %d months exceeds maximum %d months",
                                                        term, product.getMaxTermMonths()));
                }
        }

        /**
         * Validate customized values against product limits
         */
        private void validateCustomizedValues(ProductDto product, CustomizeAccountRequest request) {
                // Validate amount and term
                validateProductLimits(product, request.getPrincipalAmount(), request.getCustomTermMonths());

                // Validate custom interest rate if provided (basic sanity check)
                if (request.getCustomInterestRate() != null) {
                        if (request.getCustomInterestRate().compareTo(BigDecimal.ZERO) <= 0) {
                                throw new IllegalArgumentException("Custom interest rate must be greater than 0");
                        }
                        if (request.getCustomInterestRate().compareTo(BigDecimal.valueOf(50)) > 0) {
                                throw new IllegalArgumentException("Custom interest rate cannot exceed 50%");
                        }
                }

                // Validate custom TDS rate if provided
                if (request.getCustomTdsRate() != null) {
                        if (request.getCustomTdsRate().compareTo(BigDecimal.ZERO) < 0 ||
                                        request.getCustomTdsRate().compareTo(BigDecimal.valueOf(100)) > 0) {
                                throw new IllegalArgumentException("TDS rate must be between 0 and 100");
                        }
                }
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
        private AccountResponse mapToAccountResponse(FdAccount account) {
                return AccountResponse.builder()
                                .id(account.getId())
                                .accountNumber(account.getAccountNumber())
                                .ibanNumber(account.getIbanNumber())
                                .accountName(account.getAccountName())
                                .productCode(account.getProductCode())
                                .productName(account.getProductName())
                                .status(account.getStatus())
                                .principalAmount(account.getPrincipalAmount())
                                .interestRate(account.getInterestRate())
                                .customInterestRate(account.getCustomInterestRate())
                                .termMonths(account.getTermMonths())
                                .maturityAmount(account.getMaturityAmount())
                                .effectiveDate(account.getEffectiveDate())
                                .maturityDate(account.getMaturityDate())
                                .closureDate(account.getClosureDate())
                                .interestCalculationMethod(account.getInterestCalculationMethod())
                                .interestPayoutFrequency(account.getInterestPayoutFrequency())
                                .autoRenewal(account.getAutoRenewal())
                                .maturityInstruction(account.getMaturityInstruction())
                                .maturityTransferAccount(account.getMaturityTransferAccount())
                                .branchCode(account.getBranchCode())
                                .branchName(account.getBranchName())
                                .tdsApplicable(account.getTdsApplicable())
                                .tdsRate(account.getTdsRate())
                                .roles(mapRoles(account.getRoles()))
                                .balances(mapBalances(account.getBalances()))
                                .remarks(account.getRemarks())
                                .createdAt(account.getCreatedAt())
                                .updatedAt(account.getUpdatedAt())
                                .createdBy(account.getCreatedBy())
                                .updatedBy(account.getUpdatedBy())
                                .build();
        }

        private List<RoleResponse> mapRoles(List<AccountRole> roles) {
                if (roles == null || roles.isEmpty()) {
                        return null;
                }
                return roles.stream()
                                .map(role -> RoleResponse.builder()
                                                .id(role.getId())
                                                .customerId(role.getCustomerId())
                                                .customerName(role.getCustomerName())
                                                .roleType(role.getRoleType())
                                                .ownershipPercentage(role.getOwnershipPercentage())
                                                .isPrimary(role.getIsPrimary())
                                                .isActive(role.getIsActive())
                                                .remarks(role.getRemarks())
                                                .createdAt(role.getCreatedAt())
                                                .updatedAt(role.getUpdatedAt())
                                                .build())
                                .collect(Collectors.toList());
        }

        private List<BalanceResponse> mapBalances(List<AccountBalance> balances) {
                if (balances == null || balances.isEmpty()) {
                        return null;
                }
                return balances.stream()
                                .map(balance -> BalanceResponse.builder()
                                                .id(balance.getId())
                                                .balanceType(balance.getBalanceType())
                                                .balance(balance.getBalance())
                                                .asOfDate(balance.getAsOfDate())
                                                .description(balance.getDescription())
                                                .build())
                                .collect(Collectors.toList());
        }

        /**
         * Send new account creation email to primary account holder
         */
        private void sendNewAccountEmail(FdAccount account, ProductDto product) {
                try {
                        // Find primary account holder
                        AccountRole primaryRole = account.getRoles().stream()
                                        .filter(AccountRole::getIsPrimary)
                                        .findFirst()
                                        .orElse(account.getRoles().get(0)); // Fallback to first role if no primary

                        // Get customer details
                        CustomerDto customer = customerServiceClient.getCustomerById(primaryRole.getCustomerId());

                        // Format data for email
                        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
                        String maturityDateStr = account.getMaturityDate().format(dateFormatter);
                        String tenure = account.getTermMonths() + " months";

                        BigDecimal interestRate = account.getCustomInterestRate() != null
                                        ? account.getCustomInterestRate()
                                        : account.getInterestRate();

                        // Send email
                        emailServiceClient.sendNewAccountEmail(
                                        customer.getEmail(),
                                        customer.getCustomerName(),
                                        account.getAccountNumber(),
                                        product.getProductName(),
                                        "₹ " + account.getPrincipalAmount().toString(),
                                        interestRate.toString() + "%",
                                        tenure,
                                        maturityDateStr,
                                        "₹ " + account.getMaturityAmount().toString());

                        log.info("✅ New account email triggered for: {}", customer.getEmail());
                } catch (Exception e) {
                        log.error("❌ Failed to send new account email, but account creation succeeded", e);
                        // Don't fail the account creation if email fails
                }
        }
}
