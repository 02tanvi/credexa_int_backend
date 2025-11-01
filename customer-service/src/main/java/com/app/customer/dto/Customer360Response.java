package com.app.customer.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for 360-degree customer view
 * This provides a comprehensive overview of the customer including all FD accounts
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer360Response {

    private CustomerResponse customerInfo;
    private CustomerClassificationResponse classificationInfo;
    private AccountSummary accountSummary;
    private List<FdAccountSummary> fdAccounts; // Will be populated when FD service is ready

    /**
     * Account summary for the customer
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountSummary {
        private Integer totalFdAccounts;
        private Integer activeFdAccounts;
        private Integer maturedFdAccounts;
        private Integer closedFdAccounts;
        private BigDecimal totalInvestedAmount;
        private BigDecimal totalMaturityAmount;
        private BigDecimal totalInterestEarned;
    }

    /**
     * FD account summary (placeholder - will be populated from FD service)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FdAccountSummary {
        private Long fdAccountId;
        private String accountNumber;
        private String productName;
        private BigDecimal principalAmount;
        private BigDecimal maturityAmount;
        private BigDecimal interestRate;
        private Integer tenureMonths;
        private String status;
        private String openingDate;
        private String maturityDate;
    }
}
