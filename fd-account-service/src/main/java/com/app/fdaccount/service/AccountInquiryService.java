package com.app.fdaccount.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.fdaccount.dto.AccountResponse;
import com.app.fdaccount.dto.AccountSummaryResponse;
import com.app.fdaccount.dto.BalanceResponse;
import com.app.fdaccount.dto.RoleResponse;
import com.app.fdaccount.dto.SearchAccountRequest;
import com.app.fdaccount.entity.FdAccount;
import com.app.fdaccount.enums.AccountIdType;
import com.app.fdaccount.repository.FdAccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for FD Account inquiry and search operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountInquiryService {

    private final FdAccountRepository accountRepository;

    /**
     * Get account by ID (account number, IBAN, or internal ID)
     */
    @Transactional(readOnly = true)
    public AccountResponse getAccount(String identifier, AccountIdType idType) {
        log.debug("Fetching account by {} = {}", idType, identifier);

        FdAccount account = switch (idType) {
            case ACCOUNT_NUMBER -> accountRepository.findByAccountNumber(identifier)
                    .orElseThrow(() -> new RuntimeException("Account not found: " + identifier));
            case IBAN -> accountRepository.findByIbanNumber(identifier)
                    .orElseThrow(() -> new RuntimeException("IBAN not found: " + identifier));
            case INTERNAL_ID -> accountRepository.findById(Long.parseLong(identifier))
                    .orElseThrow(() -> new RuntimeException("Account ID not found: " + identifier));
            default -> throw new IllegalArgumentException("Unsupported ID type: " + idType);
        };

        log.info("✅ Found account: {}", account.getAccountNumber());
        return mapToAccountResponse(account);
    }

    /**
     * Get account summary by account number
     */
    @Transactional(readOnly = true)
    public AccountSummaryResponse getAccountSummary(String accountNumber) {
        FdAccount account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

        return mapToAccountSummaryResponse(account);
    }

    /**
     * Get all accounts for a customer
     */
    @Transactional(readOnly = true)
    public List<AccountSummaryResponse> getAccountsByCustomer(Long customerId) {
        log.debug("Fetching accounts for customer: {}", customerId);

        List<FdAccount> accounts = accountRepository.findByCustomerId(customerId);

        log.info("Found {} accounts for customer: {}", accounts.size(), customerId);
        return accounts.stream()
                .map(this::mapToAccountSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search accounts with criteria and pagination
     */
    @Transactional(readOnly = true)
    public Page<AccountSummaryResponse> searchAccounts(SearchAccountRequest searchRequest) {
        log.debug("Searching accounts with criteria: {}", searchRequest);

        // Build pageable
        Sort sort = Sort.by(
                "DESC".equalsIgnoreCase(searchRequest.getSortDirection()) ? 
                        Sort.Direction.DESC : Sort.Direction.ASC,
                searchRequest.getSortBy()
        );
        Pageable pageable = PageRequest.of(
                searchRequest.getPage(),
                searchRequest.getSize(),
                sort
        );

        // Execute search
        Page<FdAccount> accountPage = accountRepository.searchAccounts(
                searchRequest.getAccountNumber(),
                searchRequest.getAccountName(),
                searchRequest.getProductCode(),
                searchRequest.getStatus(),
                searchRequest.getBranchCode(),
                searchRequest.getEffectiveDateFrom(),
                searchRequest.getEffectiveDateTo(),
                searchRequest.getMaturityDateFrom(),
                searchRequest.getMaturityDateTo(),
                pageable
        );

        log.info("Found {} accounts matching search criteria", accountPage.getTotalElements());

        return accountPage.map(this::mapToAccountSummaryResponse);
    }

    /**
     * Get accounts maturing within a date range
     */
    @Transactional(readOnly = true)
    public List<AccountSummaryResponse> getAccountsMaturingBetween(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching accounts maturing between {} and {}", startDate, endDate);

        List<FdAccount> accounts = accountRepository.findAccountsMaturingBetween(startDate, endDate);

        log.info("Found {} accounts maturing in date range", accounts.size());
        return accounts.stream()
                .map(this::mapToAccountSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get accounts maturing in next N days
     */
    @Transactional(readOnly = true)
    public List<AccountSummaryResponse> getAccountsMaturingInDays(int days) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(days);

        return getAccountsMaturingBetween(today, futureDate);
    }

    /**
     * Check if account number exists
     */
    public boolean accountExists(String accountNumber) {
        return accountRepository.existsByAccountNumber(accountNumber);
    }

    /**
     * Check if IBAN exists
     */
    public boolean ibanExists(String ibanNumber) {
        return accountRepository.existsByIbanNumber(ibanNumber);
    }

    /**
     * Get accounts by product code
     */
    @Transactional(readOnly = true)
    public List<AccountSummaryResponse> getAccountsByProduct(String productCode) {
        List<FdAccount> accounts = accountRepository.findByProductCode(productCode);

        return accounts.stream()
                .map(this::mapToAccountSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get accounts by branch
     */
    @Transactional(readOnly = true)
    public List<AccountSummaryResponse> getAccountsByBranch(String branchCode) {
        List<FdAccount> accounts = accountRepository.findByBranchCode(branchCode);

        return accounts.stream()
                .map(this::mapToAccountSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map entity to full account response
     */
    private AccountResponse mapToAccountResponse(FdAccount account) {
        // Map roles
        List<RoleResponse> roles = account.getRoles().stream()
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

        // Map balances
        List<BalanceResponse> balances = account.getBalances().stream()
                .map(balance -> BalanceResponse.builder()
                        .id(balance.getId())
                        .balanceType(balance.getBalanceType())
                        .balance(balance.getBalance())
                        .asOfDate(balance.getAsOfDate())
                        .description(balance.getDescription())
                        .build())
                .collect(Collectors.toList());

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
                .roles(roles)
                .balances(balances)
                .remarks(account.getRemarks())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .createdBy(account.getCreatedBy())
                .updatedBy(account.getUpdatedBy())
                .build();
    }

    /**
     * Map entity to summary response
     */
    private AccountSummaryResponse mapToAccountSummaryResponse(FdAccount account) {
        // Get primary owner name
        String primaryOwner = account.getRoles().stream()
                .filter(role -> Boolean.TRUE.equals(role.getIsPrimary()))
                .map(role -> role.getCustomerName())
                .findFirst()
                .orElse("N/A");

        // Calculate days to maturity
        Integer daysToMaturity = null;
        if (account.getMaturityDate() != null) {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), account.getMaturityDate());
            daysToMaturity = (int) days;
        }

        return AccountSummaryResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountName(account.getAccountName())
                .productCode(account.getProductCode())
                .productName(account.getProductName())
                .status(account.getStatus())
                .principalAmount(account.getPrincipalAmount())
                .interestRate(account.getCustomInterestRate() != null ? 
                        account.getCustomInterestRate() : account.getInterestRate())
                .termMonths(account.getTermMonths())
                .maturityAmount(account.getMaturityAmount())
                .effectiveDate(account.getEffectiveDate())
                .maturityDate(account.getMaturityDate())
                .branchCode(account.getBranchCode())
                .primaryOwnerName(primaryOwner)
                .daysToMaturity(daysToMaturity)
                .build();
    }
}
