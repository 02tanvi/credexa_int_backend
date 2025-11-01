package com.app.product.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterestRateMatrixRequest {
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal minTermMonths;
    private BigDecimal maxTermMonths;
    private String customerClassification;
    @NotNull private BigDecimal interestRate;
    private BigDecimal additionalRate;
    @NotNull private LocalDate effectiveDate;
    private LocalDate endDate;
    private String remarks;
}
