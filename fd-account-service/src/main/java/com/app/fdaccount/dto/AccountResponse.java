package com.app.fdaccount.dto;

import com.app.fdaccount.enums.AccountStatus;
import com.app.fdaccount.enums.MaturityInstruction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO containing complete FD account details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private Long id;
    private String accountNumber;
    private String ibanNumber;
    private String accountName;
    private String productCode;
    private String productName;
    private AccountStatus status;
    
    // Financial details
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private BigDecimal customInterestRate;
    private Integer termMonths;
    private BigDecimal maturityAmount;
    
    // Dates
    private LocalDate effectiveDate;
    private LocalDate maturityDate;
    private LocalDate closureDate;
    
    // Settings
    private String interestCalculationMethod;
    private String interestPayoutFrequency;
    private Boolean autoRenewal;
    private MaturityInstruction maturityInstruction;
    private String maturityTransferAccount;
    
    // Branch
    private String branchCode;
    private String branchName;
    
    // TDS
    private Boolean tdsApplicable;
    private BigDecimal tdsRate;
    
    // Relationships
    private List<RoleResponse> roles;
    private List<BalanceResponse> balances;
    
    // Audit
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
