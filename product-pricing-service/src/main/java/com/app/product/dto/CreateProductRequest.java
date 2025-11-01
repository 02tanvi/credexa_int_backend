package com.app.product.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.app.product.enums.ProductStatus;
import com.app.product.enums.ProductType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new product
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String productName;

    @NotBlank(message = "Product code is required")
    @Size(max = 50, message = "Product code must not exceed 50 characters")
    private String productCode;

    @NotNull(message = "Product type is required")
    private ProductType productType;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;

    private LocalDate endDate;

    @NotBlank(message = "Bank/Branch code is required")
    @Size(max = 50)
    private String bankBranchCode;

    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters (ISO code)")
    private String currencyCode;

    private ProductStatus status;

    // Business Rules
    private BigDecimal minTermMonths;
    private BigDecimal maxTermMonths;
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

    // Related data
    private List<ProductRoleRequest> allowedRoles;
    private List<ProductChargeRequest> charges;
    private List<InterestRateMatrixRequest> interestRateMatrix;
}
