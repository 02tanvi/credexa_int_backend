package com.app.calculator.dto;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to compare multiple FD scenarios
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Compare multiple FD calculation scenarios")
public class ComparisonRequest {
    
    @NotNull(message = "At least one standalone calculation is required")
    @Schema(description = "List of standalone calculations to compare", required = true)
    private List<StandaloneCalculationRequest> scenarios;
    
    @Schema(description = "Principal amount (same for all scenarios)", example = "100000")
    private BigDecimal commonPrincipal;
}
