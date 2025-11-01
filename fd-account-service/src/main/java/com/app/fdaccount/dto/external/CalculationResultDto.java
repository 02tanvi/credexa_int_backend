package com.app.fdaccount.dto.external;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for FD calculation from fd-calculator-service
 * Maps to CalculationResponse from Calculator Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculationResultDto {

    // Core calculation fields - mapped from CalculationResponse
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private BigDecimal baseInterestRate;
    private BigDecimal additionalInterestRate;
    private Integer tenure;  // Maps to termMonths in usage
    private LocalDate startDate;
    private LocalDate maturityDate;
    private BigDecimal maturityAmount;
    private BigDecimal interestEarned;  // Total interest (was totalInterest)
    private BigDecimal netInterest;     // Interest after TDS
    private BigDecimal tdsAmount;
    private BigDecimal tdsRate;
    
    // Product information (optional)
    private Long productId;
    private String productName;
    private String productCode;
    
    // Additional fields
    private List<String> customerClassifications;
    
    // Detailed breakdown
    private List<InterestBreakdown> interestBreakdowns;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InterestBreakdown {
        private Integer month;
        private LocalDate date;
        private BigDecimal principalBalance;
        private BigDecimal interestEarned;
        private BigDecimal cumulativeInterest;
        private BigDecimal totalBalance;
    }
}
