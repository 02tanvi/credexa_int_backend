package com.app.calculator.dto;

import java.math.BigDecimal;
import java.util.List;

import com.app.calculator.enums.CalculationType;
import com.app.calculator.enums.CompoundingFrequency;
import com.app.calculator.enums.TenureUnit;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for product-based FD calculation
 * Uses product defaults with optional customization
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product-based FD calculation with defaults from product-pricing-service")
public class ProductBasedCalculationRequest {
    
    @NotNull(message = "Product ID is required")
    @Min(value = 1, message = "Invalid product ID")
    @Schema(description = "Product ID from product-pricing-service", example = "1", required = true)
    private Long productId;
    
    @NotNull(message = "Principal amount is required")
    @Schema(description = "Principal/Investment amount", example = "100000", required = true)
    private BigDecimal principalAmount;
    
    @NotNull(message = "Tenure is required")
    @Min(value = 1, message = "Tenure must be at least 1")
    @Schema(description = "Investment tenure", example = "12", required = true)
    private Integer tenure;
    
    @NotNull(message = "Tenure unit is required")
    @Schema(description = "Unit of tenure", example = "MONTHS", required = true)
    private TenureUnit tenureUnit;
    
    @Schema(description = "Override calculation type (uses product default if not provided)", example = "COMPOUND")
    private CalculationType calculationType;
    
    @Schema(description = "Override compounding frequency (uses product default if not provided)", example = "QUARTERLY")
    private CompoundingFrequency compoundingFrequency;
    
    @Schema(description = "Override interest rate (must be within product limits)", example = "7.75")
    private BigDecimal customInterestRate;
    
    @Schema(description = "Customer ID to fetch classification", example = "1")
    private Long customerId;
    
    @Schema(description = "Customer classifications for additional interest (max 2)", example = "[\"SENIOR_CITIZEN\"]")
    @Size(max = 2, message = "Maximum 2 customer classifications allowed")
    private List<String> customerClassifications;
    
    @Schema(description = "Apply TDS calculation", example = "true")
    private Boolean applyTds;
}
