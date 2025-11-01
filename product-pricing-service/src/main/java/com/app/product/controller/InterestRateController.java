package com.app.product.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.product.dto.ApiResponse;
import com.app.product.dto.InterestRateMatrixResponse;
import com.app.product.service.InterestRateService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for Interest Rate operations
 */
@RestController
@RequestMapping("/products/{productId}/interest-rates")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Interest Rate Management", description = "APIs for managing product interest rates")
public class InterestRateController {

    private final InterestRateService interestRateService;

    @GetMapping
    @Operation(summary = "Get interest rates for product", 
               description = "Retrieves all interest rate slabs configured for a product")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Interest rates retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<InterestRateMatrixResponse>>> getInterestRates(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        
        log.info("REST: Fetching interest rates for product ID: {}", productId);
        List<InterestRateMatrixResponse> response = interestRateService.getInterestRatesForProduct(productId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active interest rates", 
               description = "Retrieves interest rates active on a specific date")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Active rates retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<InterestRateMatrixResponse>>> getActiveRates(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Date (yyyy-MM-dd)") @RequestParam(required = false) LocalDate date) {
        
        LocalDate effectiveDate = date != null ? date : LocalDate.now();
        log.info("REST: Fetching active rates for product {} on date {}", productId, effectiveDate);
        
        List<InterestRateMatrixResponse> response = interestRateService.getActiveRatesOnDate(productId, effectiveDate);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/applicable")
    @Operation(summary = "Find applicable interest rate", 
               description = "Finds the best applicable interest rate for given amount, term, and customer classification")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Applicable rate found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No applicable rate found")
    })
    public ResponseEntity<ApiResponse<InterestRateMatrixResponse>> findApplicableRate(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Deposit amount") @RequestParam BigDecimal amount,
            @Parameter(description = "Term in months") @RequestParam Integer termMonths,
            @Parameter(description = "Customer classification") @RequestParam(required = false) String classification) {
        
        log.info("REST: Finding applicable rate for product {} - Amount: {}, Term: {}, Classification: {}", 
                productId, amount, termMonths, classification);
        
        Optional<InterestRateMatrixResponse> response = interestRateService.findApplicableRate(
                productId, amount, termMonths, classification
        );
        
        if (response.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(response.get()));
        } else {
            return ResponseEntity.ok(ApiResponse.error("No applicable interest rate found for the given criteria"));
        }
    }

    @GetMapping("/calculate")
    @Operation(summary = "Calculate effective interest rate", 
               description = "Calculates the effective interest rate considering base rate and matrix")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Rate calculated successfully")
    })
    public ResponseEntity<ApiResponse<BigDecimal>> calculateEffectiveRate(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Base interest rate") @RequestParam BigDecimal baseRate,
            @Parameter(description = "Deposit amount") @RequestParam BigDecimal amount,
            @Parameter(description = "Term in months") @RequestParam Integer termMonths,
            @Parameter(description = "Customer classification") @RequestParam(required = false) String classification) {
        
        log.info("REST: Calculating effective rate for product {}", productId);
        
        BigDecimal effectiveRate = interestRateService.calculateEffectiveRate(
                productId, baseRate, amount, termMonths, classification
        );
        
        return ResponseEntity.ok(ApiResponse.success("Effective rate calculated", effectiveRate));
    }
}
