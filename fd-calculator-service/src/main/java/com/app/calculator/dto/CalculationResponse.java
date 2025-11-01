package com.app.calculator.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.app.calculator.enums.CalculationType;
import com.app.calculator.enums.CompoundingFrequency;
import com.app.calculator.enums.TenureUnit;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for FD calculations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "FD calculation results with detailed breakdown")
public class CalculationResponse {

    @Schema(description = "Principal/Investment amount", example = "100000.00")
    private BigDecimal principalAmount;

    @Schema(description = "Annual interest rate (%)", example = "7.50")
    private BigDecimal interestRate;

    @Schema(description = "Base interest rate from product", example = "7.00")
    private BigDecimal baseInterestRate;

    @Schema(description = "Additional interest from customer classifications", example = "0.50")
    private BigDecimal additionalInterestRate;

    @Schema(description = "Investment tenure", example = "12")
    private Integer tenure;

    @Schema(description = "Tenure unit", example = "MONTHS")
    private TenureUnit tenureUnit;

    @Schema(description = "Tenure in years", example = "1.0")
    private Double tenureInYears;

    @Schema(description = "Type of calculation", example = "COMPOUND")
    private CalculationType calculationType;

    @Schema(description = "Compounding frequency", example = "QUARTERLY")
    private CompoundingFrequency compoundingFrequency;

    @Schema(description = "Total interest earned", example = "7763.00")
    private BigDecimal interestEarned;

    @Schema(description = "TDS deducted", example = "776.30")
    private BigDecimal tdsAmount;

    @Schema(description = "TDS rate applied (%)", example = "10.0")
    private BigDecimal tdsRate;

    @Schema(description = "Maturity amount (principal + interest - TDS)", example = "106986.70")
    private BigDecimal maturityAmount;

    @Schema(description = "Net interest after TDS", example = "6986.70")
    private BigDecimal netInterest;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Investment start date", example = "2025-01-20")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Maturity date", example = "2026-01-20")
    private LocalDate maturityDate;

    @Schema(description = "Product ID (if product-based calculation)", example = "1")
    private Long productId;

    @Schema(description = "Product name", example = "Standard Fixed Deposit")
    private String productName;

    @Schema(description = "Product code", example = "FD-STD-001")
    private String productCode;

    @Schema(description = "Customer classifications applied", example = "[\"SENIOR_CITIZEN\"]")
    private List<String> customerClassifications;

    @Schema(description = "Monthly interest breakdown (optional)")
    private List<MonthlyBreakdown> monthlyBreakdown;

    //  Add this missing field to match the service
    @Schema(description = "Annual Percentage Yield (APY)", example = "7.72")
    private BigDecimal apy;
}