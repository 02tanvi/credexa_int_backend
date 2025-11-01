package com.app.fdaccount.dto.external;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for product data from product-pricing-service
 * Maps to ProductResponse from product-pricing-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private Long productId;
    private String productCode;
    private String productName;
    private String description;
    private String status; // DRAFT, ACTIVE, INACTIVE, etc.
    
    // Amount limits
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal minBalanceRequired;
    
    // Term limits (in months)
    private Integer minTermMonths;
    private Integer maxTermMonths;
    
    // Interest rate
    private BigDecimal baseInterestRate;
    
    // Settings
    private String interestCalculationMethod;
    private String interestPayoutFrequency;
    private Boolean prematureWithdrawalAllowed;
    private Boolean partialWithdrawalAllowed;
    private Boolean autoRenewalAllowed;
    private Boolean loanAgainstDepositAllowed;
    private Boolean nomineeAllowed;
    private Boolean jointAccountAllowed;
    
    // TDS
    private Boolean tdsApplicable;
    private BigDecimal tdsRate;
    
    // Helper
    private Boolean currentlyActive;
}
