package com.app.fdaccount.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for premature withdrawal inquiry
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrematureWithdrawalInquiryResponse {

    private String accountNumber;
    private LocalDate effectiveDate;
    private LocalDate proposedWithdrawalDate;
    private LocalDate maturityDate;
    private Integer daysHeld;
    private Integer totalTermDays;
    
    private BigDecimal principalAmount;
    private BigDecimal normalInterestRate;
    private BigDecimal penaltyPercentage;
    private BigDecimal revisedInterestRate;
    
    private BigDecimal interestEarned;
    private BigDecimal penaltyAmount;
    private BigDecimal netInterest;
    private BigDecimal tdsAmount;
    private BigDecimal netPayable;
    
    private String message;
    private Boolean isEligible;
}
