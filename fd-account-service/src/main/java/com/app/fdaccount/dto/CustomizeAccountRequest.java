package com.app.fdaccount.dto;

import com.app.fdaccount.enums.MaturityInstruction;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for creating a new FD account with customized values within product limits
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomizeAccountRequest {

    @NotBlank(message = "Account name is required")
    @Size(max = 100, message = "Account name cannot exceed 100 characters")
    private String accountName;

    @NotBlank(message = "Product code is required")
    @Size(max = 50, message = "Product code cannot exceed 50 characters")
    private String productCode;

    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "1000.00", message = "Minimum principal amount is 1000")
    @DecimalMax(value = "99999999999999999.99", message = "Principal amount exceeds maximum limit")
    @Digits(integer = 17, fraction = 2, message = "Invalid principal amount format")
    private BigDecimal principalAmount;

    // Customized term - will be validated against product limits
    @NotNull(message = "Term in months is required")
    @Min(value = 1, message = "Minimum term is 1 month")
    @Max(value = 1200, message = "Maximum term is 1200 months")
    private Integer customTermMonths;

    // Customized interest rate - will be validated against product limits
    @DecimalMin(value = "0.01", message = "Minimum interest rate is 0.01%")
    @DecimalMax(value = "99.99", message = "Maximum interest rate is 99.99%")
    @Digits(integer = 2, fraction = 2, message = "Invalid interest rate format")
    private BigDecimal customInterestRate;

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;

    @NotNull(message = "At least one account role is required")
    @Size(min = 1, message = "At least one account owner/role is required")
    private List<AccountRoleRequest> roles;

    @NotBlank(message = "Branch code is required")
    @Size(max = 20, message = "Branch code cannot exceed 20 characters")
    private String branchCode;

    @Size(max = 100, message = "Branch name cannot exceed 100 characters")
    private String branchName;

    // Optional customizations
    private String customInterestCalculationMethod;

    private String customInterestPayoutFrequency;

    private Boolean autoRenewal;

    private MaturityInstruction maturityInstruction;

    @Size(max = 50, message = "Maturity transfer account cannot exceed 50 characters")
    private String maturityTransferAccount;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    private Boolean tdsApplicable;

    @DecimalMin(value = "0.00", message = "TDS rate cannot be negative")
    @DecimalMax(value = "100.00", message = "TDS rate cannot exceed 100%")
    @Digits(integer = 3, fraction = 2, message = "Invalid TDS rate format")
    private BigDecimal customTdsRate;

    @Size(max = 100, message = "Created by cannot exceed 100 characters")
    private String createdBy;
}
