package com.app.calculator.dto.external;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for interest rate details from product-pricing-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterestRateDto {
    
    private Long id;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Integer minTermMonths;
    private Integer maxTermMonths;
    
    private String customerClassification;
    private BigDecimal interestRate;
    private BigDecimal additionalRate;
    private BigDecimal totalRate;
    
    private LocalDate effectiveDate;
    private LocalDate expiryDate;
    private Boolean active;
    private String remarks;
}
