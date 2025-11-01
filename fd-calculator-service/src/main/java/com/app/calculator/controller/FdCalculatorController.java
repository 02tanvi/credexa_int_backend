package com.app.calculator.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.calculator.dto.CalculationResponse;
import com.app.calculator.dto.ComparisonRequest;
import com.app.calculator.dto.ComparisonResponse;
import com.app.calculator.dto.ProductBasedCalculationRequest;
import com.app.calculator.dto.StandaloneCalculationRequest;
import com.app.calculator.service.FdCalculatorService;
import com.app.common.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for FD Calculator operations
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
@Tag(name = "FD Calculator", description = "Fixed Deposit calculation and simulation endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class FdCalculatorController {
    
    private final FdCalculatorService fdCalculatorService;
    
    @PostMapping("/calculate/standalone")
    @Operation(
        summary = "Calculate FD with standalone inputs",
        description = "Calculate FD maturity amount and interest with manual inputs (no product required). " +
                     "User provides all parameters including principal, rate, tenure, and calculation type."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Calculation successful",
            content = @Content(schema = @Schema(implementation = CalculationResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid input parameters"
        )
    })
    public ResponseEntity<ApiResponse<CalculationResponse>> calculateStandalone(
            @Valid @RequestBody @Parameter(description = "Standalone calculation request with all parameters")
            StandaloneCalculationRequest request) {
        
        log.info("Standalone calculation request received for principal: {}", request.getPrincipalAmount());
        
        CalculationResponse response = fdCalculatorService.calculateStandalone(request);
        
        return ResponseEntity.ok(ApiResponse.success(
            "FD calculation completed successfully",
            response
        ));
    }
    
    @PostMapping("/calculate/product-based")
    @Operation(
        summary = "Calculate FD using product defaults",
        description = "Calculate FD maturity using product configuration from product-pricing-service. " +
                     "Fetches interest rates, TDS settings, and other defaults from the selected product. " +
                     "Allows customization within product limits (max 2% additional rate)."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Calculation successful",
            content = @Content(schema = @Schema(implementation = CalculationResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid input or product not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Product not found"
        )
    })
    public ResponseEntity<ApiResponse<CalculationResponse>> calculateWithProduct(
            @Valid @RequestBody @Parameter(description = "Product-based calculation request")
            ProductBasedCalculationRequest request) {
        
        log.info("Product-based calculation request received for product ID: {}", request.getProductId());
        
        try {
            CalculationResponse response = fdCalculatorService.calculateWithProduct(request);
            
            return ResponseEntity.ok(ApiResponse.success(
                "FD calculation with product defaults completed successfully",
                response
            ));
        } catch (IllegalArgumentException e) {
            log.warn("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(
                "Validation Error",
                e.getMessage()
            ));
        } catch (RuntimeException e) {
            log.error("Error calculating with product: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(
                "Calculation Error",
                "Failed to calculate FD: " + e.getMessage()
            ));
        }
    }
    
    @PostMapping("/compare")
    @Operation(
        summary = "Compare multiple FD scenarios",
        description = "Compare multiple FD calculation scenarios side-by-side. " +
                     "Provides detailed comparison and identifies the best scenario (highest maturity amount). " +
                     "Useful for evaluating different tenures, interest rates, or calculation types."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Comparison successful",
            content = @Content(schema = @Schema(implementation = ComparisonResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid comparison request"
        )
    })
    public ResponseEntity<ApiResponse<ComparisonResponse>> compareScenarios(
            @Valid @RequestBody @Parameter(description = "List of scenarios to compare")
            ComparisonRequest request) {
        
        log.info("Comparison request received for {} scenarios", request.getScenarios().size());
        
        ComparisonResponse response = fdCalculatorService.compareScenarios(request);
        
        return ResponseEntity.ok(ApiResponse.success(
            String.format("Successfully compared %d FD scenarios", response.getScenarios().size()),
            response
        ));
    }
    
    @GetMapping("/health")
    @Operation(
        summary = "Health check",
        description = "Check if the FD Calculator service is running"
    )
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success(
            "Service is healthy",
            "FD Calculator Service is running"
        ));
    }
}
