package com.app.calculator.dto;

import java.math.BigDecimal;
import java.util.List;

import com.app.calculator.enums.CalculationType;
import com.app.calculator.enums.CompoundingFrequency;
import com.app.calculator.enums.TenureUnit;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for standalone FD calculation (without product)
 * User provides all inputs manually
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standalone FD calculation request with manual inputs")
public class StandaloneCalculationRequest {
    
    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "1000.0", message = "Minimum principal amount is ₹1000")
    @DecimalMax(value = "100000000.0", message = "Maximum principal amount is ₹10 Crore")
    @Schema(description = "Principal/Investment amount", example = "100000", required = true)
    private BigDecimal principalAmount;
    
    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.1", message = "Interest rate must be at least 0.1%")
    @DecimalMax(value = "20.0", message = "Interest rate cannot exceed 20%")
    @Schema(description = "Annual interest rate (%)", example = "7.5", required = true)
    private BigDecimal interestRate;
    
    @NotNull(message = "Tenure is required")
    @Min(value = 1, message = "Tenure must be at least 1")
    @Max(value = 3650, message = "Tenure cannot exceed 10 years (3650 days)")
    @Schema(description = "Investment tenure", example = "12", required = true)
    private Integer tenure;
    
    @NotNull(message = "Tenure unit is required")
    @Schema(description = "Unit of tenure", example = "MONTHS", required = true)
    private TenureUnit tenureUnit;
    
    @NotNull(message = "Calculation type is required")
    @Schema(description = "Type of interest calculation", example = "COMPOUND", required = true)
    private CalculationType calculationType;
    
    @Schema(description = "Compounding frequency (required only for compound interest)", example = "QUARTERLY")
    private CompoundingFrequency compoundingFrequency;
    
    @DecimalMin(value = "0.0", message = "TDS rate cannot be negative")
    @DecimalMax(value = "30.0", message = "TDS rate cannot exceed 30%")
    @Schema(description = "TDS rate (%) - optional", example = "10.0")
    private BigDecimal tdsRate;
    
    @Schema(description = "Customer classifications for additional interest (max 2)", example = "[\"SENIOR_CITIZEN\", \"PREMIUM\"]")
    @Size(max = 2, message = "Maximum 2 customer classifications allowed")
    private List<String> customerClassifications;
}
