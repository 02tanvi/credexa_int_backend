package com.app.fdaccount.dto.external;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for FD calculation from fd-calculator-service
 * Maps to StandaloneCalculationRequest in Calculator Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculationRequest {

    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer tenure;  // Renamed from termMonths
    private String tenureUnit;  // "MONTHS", "DAYS", "YEARS"
    private String calculationType;  // "SIMPLE" or "COMPOUND"
    private String compoundingFrequency;  // "MONTHLY", "QUARTERLY", "HALF_YEARLY", "YEARLY"
    private BigDecimal tdsRate;  // Optional TDS rate (default 10%)
    private List<String> customerClassifications;  // Optional (e.g., "SENIOR_CITIZEN")
}
