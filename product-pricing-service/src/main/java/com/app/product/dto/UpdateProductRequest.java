package com.app.product.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.app.product.enums.ProductStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating existing product
 * Only includes fields that can be updated after creation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductRequest {

    @Size(max = 255, message = "Product name cannot exceed 255 characters")
    private String productName;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private LocalDate endDate;

    private ProductStatus status;

    // Business Rules - Simple
    @Min(value = 0, message = "Minimum term months cannot be negative")
    private Integer minTermMonths;

    @Min(value = 0, message = "Maximum term months cannot be negative")
    private Integer maxTermMonths;

    @DecimalMin(value = "0.0", message = "Minimum amount cannot be negative")
    private BigDecimal minAmount;

    @DecimalMin(value = "0.0", message = "Maximum amount cannot be negative")
    private BigDecimal maxAmount;

    @DecimalMin(value = "0.0", message = "Minimum balance required cannot be negative")
    private BigDecimal minBalanceRequired;

    @DecimalMin(value = "0.0", message = "Base interest rate cannot be negative")
    private BigDecimal baseInterestRate;

    @Size(max = 50, message = "Interest calculation method cannot exceed 50 characters")
    private String interestCalculationMethod;

    @Size(max = 50, message = "Interest payout frequency cannot exceed 50 characters")
    private String interestPayoutFrequency;

    // Flags
    private Boolean prematureWithdrawalAllowed;
    private Boolean partialWithdrawalAllowed;
    private Boolean loanAgainstDepositAllowed;
    private Boolean autoRenewalAllowed;
    private Boolean nomineeAllowed;
    private Boolean jointAccountAllowed;

    // Tax
    @DecimalMin(value = "0.0", message = "TDS rate cannot be negative")
    private BigDecimal tdsRate;

    private Boolean tdsApplicable;

    @Size(max = 50, message = "Updated by cannot exceed 50 characters")
    private String updatedBy;
}
