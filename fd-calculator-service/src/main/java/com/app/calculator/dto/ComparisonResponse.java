package com.app.calculator.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for comparing multiple FD scenarios
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Comparison results for multiple FD scenarios")
public class ComparisonResponse {
    
    @Schema(description = "List of calculation results for each scenario")
    private List<CalculationResponse> scenarios;
    
    @Schema(description = "Best scenario (highest maturity amount)")
    private CalculationResponse bestScenario;
    
    @Schema(description = "Index of the best scenario (0-based)", example = "2")
    private Integer bestScenarioIndex;
}
