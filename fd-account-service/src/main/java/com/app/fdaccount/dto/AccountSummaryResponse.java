package com.app.fdaccount.dto;

import com.app.fdaccount.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for account list/summary view
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSummaryResponse {

    private Long id;
    private String accountNumber;
    private String accountName;
    private String productCode;
    private String productName;
    private AccountStatus status;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer termMonths;
    private BigDecimal maturityAmount;
    private LocalDate effectiveDate;
    private LocalDate maturityDate;
    private String branchCode;
    private String primaryOwnerName;
    private Integer daysToMaturity;
}
