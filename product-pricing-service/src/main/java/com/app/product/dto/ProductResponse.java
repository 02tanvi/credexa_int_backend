package com.app.product.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.app.product.enums.ProductStatus;
import com.app.product.enums.ProductType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Complete product response DTO with all details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Long productId;
    private String productName;
    private String productCode;
    private ProductType productType;
    private String description;
    private LocalDate effectiveDate;
    private LocalDate endDate;
    private String bankBranchCode;
    private String currencyCode;
    private ProductStatus status;

    // Business Rules - Simple
    private Integer minTermMonths;
    private Integer maxTermMonths;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal minBalanceRequired;
    private BigDecimal baseInterestRate;
    private String interestCalculationMethod;
    private String interestPayoutFrequency;

    // Flags
    private Boolean prematureWithdrawalAllowed;
    private Boolean partialWithdrawalAllowed;
    private Boolean loanAgainstDepositAllowed;
    private Boolean autoRenewalAllowed;
    private Boolean nomineeAllowed;
    private Boolean jointAccountAllowed;

    // Tax
    private BigDecimal tdsRate;
    private Boolean tdsApplicable;

    // Relationships
    private List<ProductRoleResponse> allowedRoles;
    private List<ProductChargeResponse> charges;
    private List<InterestRateMatrixResponse> interestRateMatrix;
    private List<ProductTransactionTypeResponse> transactionTypes;
    private List<ProductBalanceTypeResponse> balanceTypes;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Helper
    private Boolean currentlyActive;
}
