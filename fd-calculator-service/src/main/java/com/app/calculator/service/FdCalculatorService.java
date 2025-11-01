package com.app.calculator.service;

import com.app.calculator.dto.CalculationResponse;
import com.app.calculator.dto.ComparisonRequest;
import com.app.calculator.dto.ComparisonResponse;
import com.app.calculator.dto.MonthlyBreakdown;
import com.app.calculator.dto.ProductBasedCalculationRequest;
import com.app.calculator.dto.StandaloneCalculationRequest;
import com.app.calculator.dto.external.ProductDto;
import com.app.calculator.enums.CompoundingFrequency;
import com.app.calculator.enums.TenureUnit;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * Fixed Deposit (FD) Calculator Service
 * Calculates maturity amount, interest, TDS, and APY (Annual Percentage Yield)
 *
 * - Injects RestTemplate as a bean (assumed defined in a @Configuration class)
 * - Fetches BOTH base rate and additional rate from the API
 * - Accepts ProductBasedCalculationRequest as input
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FdCalculatorService {

    private final CompoundInterestCalculator compoundInterestCalculator;
    private final RestTemplate restTemplate;

    private final ProductIntegrationService productIntegrationService;

    /**
 * Calculates FD returns for standalone (manual) input.
 * Now supports customer classifications to add additional rates.
 */
public CalculationResponse calculateStandalone(StandaloneCalculationRequest request) {
    log.info("Performing standalone FD calculation: {}", request);

    BigDecimal principal = request.getPrincipalAmount();
    BigDecimal baseRate = request.getInterestRate();
    int tenure = request.getTenure();
    
    // ✅ NEW: Handle customer classifications for additional rates
    BigDecimal additionalRate = BigDecimal.ZERO;
    if (request.getCustomerClassifications() != null && !request.getCustomerClassifications().isEmpty()) {
        additionalRate = calculateStandaloneAdditionalRate(request.getCustomerClassifications());
        log.info("Additional rate from classifications: {}%", additionalRate);
    }
    
    // ✅ Final rate = Base rate (from user input) + Additional rate (from classifications)
    BigDecimal finalRate = baseRate.add(additionalRate);
    log.info("Final rate: {}% (base: {}% + additional: {}%)", finalRate, baseRate, additionalRate);

    // Default compounding frequency
    CompoundingFrequency frequency = request.getCompoundingFrequency() != null 
            ? request.getCompoundingFrequency() 
            : CompoundingFrequency.QUARTERLY;

    TenureUnit tenureUnit = request.getTenureUnit() != null 
            ? request.getTenureUnit() 
            : TenureUnit.MONTHS;

    int tenureMonths = tenureUnit.toMonths(tenure);
    
    // ✅ Use compound interest calculator for accurate calculation
    BigDecimal interest = compoundInterestCalculator.calculateInterest(
            principal, finalRate, tenure, tenureUnit, frequency);

    BigDecimal tdsRate = request.getTdsRate() != null 
            ? request.getTdsRate() 
            : BigDecimal.ZERO;

    BigDecimal maturityAmount = compoundInterestCalculator.calculateMaturityAmount(
            principal, finalRate, tenure, tenureUnit, frequency, tdsRate);

    // Calculate TDS amount
    BigDecimal tdsAmount = interest.multiply(tdsRate)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

    BigDecimal netInterest = interest.subtract(tdsAmount);

    // Compute APY (Annual Percentage Yield)
    BigDecimal apy = calculateAPY(finalRate, frequency);

    LocalDate startDate = LocalDate.now();
    LocalDate maturityDate = startDate.plusMonths(tenureMonths);

    // ✅ Generate monthly breakdown
    List<MonthlyBreakdown> breakdown = compoundInterestCalculator.generateMonthlyBreakdown(
            principal, finalRate, tenureMonths, frequency, startDate);

    return CalculationResponse.builder()
            .principalAmount(principal)
            .interestRate(finalRate)
            .baseInterestRate(baseRate)
            .additionalInterestRate(additionalRate)
            .tenure(tenure)
            .tenureUnit(tenureUnit)
            .calculationType(request.getCalculationType())
            .compoundingFrequency(frequency)
            .interestEarned(interest)
            .tdsAmount(tdsAmount)
            .tdsRate(tdsRate)
            .netInterest(netInterest)
            .maturityAmount(maturityAmount)
            .apy(apy)
            .startDate(startDate)
            .maturityDate(maturityDate)
            .customerClassifications(request.getCustomerClassifications())
            .monthlyBreakdown(breakdown)
            .build();
}

/**
 * Calculate additional rate for standalone calculations based on predefined rules.
 * This uses hardcoded additional rates (not from database).
 */
private BigDecimal calculateStandaloneAdditionalRate(List<String> classifications) {
    if (classifications == null || classifications.isEmpty()) {
        return BigDecimal.ZERO;
    }

    // Remove duplicates
    List<String> uniqueClassifications = new ArrayList<>(new HashSet<>(classifications));
    
    BigDecimal totalAdditionalRate = BigDecimal.ZERO;

    // ✅ Hardcoded additional rates (matching DataInitializer rates)
    for (String classification : uniqueClassifications) {
        BigDecimal rate = switch (classification.toUpperCase()) {
            case "SENIOR_CITIZEN" -> BigDecimal.valueOf(1.00);
            case "EMPLOYEE" -> BigDecimal.valueOf(1.50);
            case "SILVER" -> BigDecimal.valueOf(0.50);
            case "GOLD" -> BigDecimal.valueOf(1.00);
            case "PLATINUM" -> BigDecimal.valueOf(1.50);
            case "PREMIUM" -> BigDecimal.valueOf(0.75); // Example additional category
            default -> {
                log.warn("Unknown classification: {}", classification);
                yield BigDecimal.ZERO;
            }
        };
        
        totalAdditionalRate = totalAdditionalRate.add(rate);
        log.debug("Added {}% for classification: {}", rate, classification);
    }

    // Apply 2% cap on additional rate
    BigDecimal maxCap = new BigDecimal("2.0");
    if (totalAdditionalRate.compareTo(maxCap) > 0) {
        log.info("Total additional rate {} exceeds 2% cap, applying cap", totalAdditionalRate);
        totalAdditionalRate = maxCap;
    }

    return totalAdditionalRate.setScale(2, RoundingMode.HALF_UP);
}
    /**
     * Calculate FD details using a ProductBasedCalculationRequest
     */
    public CalculationResponse calculateWithProduct(ProductBasedCalculationRequest request) {

    log.info("Calculating FD for product={}, principal={}, tenure={}{}",
            request.getProductId(), request.getPrincipalAmount(), request.getTenure(), request.getTenureUnit());
    
    // ✅ Remove duplicate customer classifications (if any)
    if (request.getCustomerClassifications() != null) {
        request.setCustomerClassifications(
        new ArrayList<>(new HashSet<>(request.getCustomerClassifications()))
        );
    }

    // Step 1: Fetch the entire rate matrix from the API once
    JsonNode dataNode = fetchRatesFromApi(request.getProductId());
    if (dataNode == null) {
        throw new RuntimeException("Failed to fetch rate matrix from product service.");
    }

    // Step 2: Find the Base Rate from the matrix
    int tenureMonths = request.getTenureUnit().toMonths(request.getTenure());
    BigDecimal baseRate = findBaseRate(dataNode, request.getPrincipalAmount(), tenureMonths);

    // Step 3: Compute additional rate from the same matrix
    BigDecimal additionalRate = calculateAdditionalRate(dataNode, request.getCustomerClassifications());
    log.debug("Found baseRate={}%, additionalRate={}%", baseRate, additionalRate);

    // Step 4: Final rate = Base + Additional
    BigDecimal finalRate = baseRate.add(additionalRate);

    // Default compounding frequency if not provided
    CompoundingFrequency frequency = request.getCompoundingFrequency() != null
            ? request.getCompoundingFrequency()
            : CompoundingFrequency.QUARTERLY;

    // Assuming TDS Rate = 0 if not specified
    BigDecimal tdsRate = BigDecimal.ZERO;

    // Step 5: Calculate interest and maturity
    BigDecimal interest = compoundInterestCalculator.calculateInterest(
            request.getPrincipalAmount(), finalRate, request.getTenure(), request.getTenureUnit(), frequency);

    BigDecimal maturityAmount = compoundInterestCalculator.calculateMaturityAmount(
            request.getPrincipalAmount(), finalRate, request.getTenure(), request.getTenureUnit(), frequency, tdsRate);

    // Step 6: Calculate APY (Effective Annual Rate)
    BigDecimal apy = calculateAPY(finalRate, frequency);
    LocalDate startDate = LocalDate.now();

    // Step 7: Generate monthly breakdown
    List<MonthlyBreakdown> breakdown = compoundInterestCalculator.generateMonthlyBreakdown(
            request.getPrincipalAmount(), finalRate, tenureMonths, frequency, startDate);

    log.info("FD calculation complete: Maturity={}, APY={}%", maturityAmount, apy);

    // ✅ Step 8: Fetch product metadata (optional if you have a ProductIntegrationService)
    String productName = null;
    String productCode = null;
    try {
        ProductDto productDetails = productIntegrationService.getProduct(request.getProductId());
        if (productDetails != null) {
            productName = productDetails.getProductName();
            productCode = productDetails.getProductCode();
        }
    } catch (Exception e) {
        log.warn("Unable to fetch product metadata: {}", e.getMessage());
    }

    // ✅ Step 9: Build response with all required fields
    return CalculationResponse.builder()
            .principalAmount(request.getPrincipalAmount())
            .interestRate(finalRate)
            .baseInterestRate(baseRate)
            .additionalInterestRate(additionalRate)
            .tenure(request.getTenure())
            .tenureUnit(request.getTenureUnit())
            .calculationType(request.getCalculationType())
            .compoundingFrequency(frequency)
            .interestEarned(interest)
            .maturityAmount(maturityAmount)
            .apy(apy)
            .tdsRate(tdsRate)
            .maturityDate(startDate.plusMonths(tenureMonths))
            .monthlyBreakdown(breakdown)
            // ✅ Newly added fields
            .productId(request.getProductId())
            .productName(productName)
            .productCode(productCode)
            .customerClassifications(request.getCustomerClassifications())
            .build();
    }


    /**
     * Fetches the rate matrix from the Product & Pricing service API
     */
    private JsonNode fetchRatesFromApi(Long productId) {
        try {
            String url = "http://localhost:8084/api/products/products/" + productId + "/interest-rates";
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode dataNode = response.getBody().get("data");
                if (dataNode != null && dataNode.isArray()) {
                    return dataNode;
                }
            }
            log.warn("No valid data array found in product API for productId {}", productId);
            return null;
        } catch (Exception e) {
            log.error("Error fetching interest-rate matrix from pricing service: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Finds the applicable base rate from the API data node.
     */
    private BigDecimal findBaseRate(JsonNode dataNode, BigDecimal principal, int tenureMonths) {
    log.debug("Searching for base rate: principal={}, tenureMonths={}", principal, tenureMonths);
    
    for (JsonNode node : dataNode) {
        if (node.path("customerClassification").isNull()) {
            BigDecimal minAmount = node.path("minAmount").decimalValue();
            BigDecimal maxAmount = node.path("maxAmount").decimalValue();
            int minTerm = node.path("minTermMonths").asInt();
            int maxTerm = node.path("maxTermMonths").asInt();

            boolean amountMatch = principal.compareTo(minAmount) >= 0 && principal.compareTo(maxAmount) <= 0;
            boolean tenureMatch = tenureMonths >= minTerm && tenureMonths <= maxTerm;

            log.debug("Checking rate: minAmount={}, maxAmount={}, minTerm={}, maxTerm={}, amountMatch={}, tenureMatch={}", 
                     minAmount, maxAmount, minTerm, maxTerm, amountMatch, tenureMatch);

            if (amountMatch && tenureMatch) {
                BigDecimal rate = node.path("interestRate").decimalValue();
                log.debug("Found matching base rate: {}%", rate);
                return rate;
            }
        }
    }
    log.warn("No matching base rate found for principal {} and tenure {} months. Defaulting to 0.", principal, tenureMonths);
    return BigDecimal.ZERO;
}

    /**
     * Calculates the total additional rate based on customer classifications
     */
    private BigDecimal calculateAdditionalRate(JsonNode dataNode, List<String> classifications) {
        if (classifications == null || classifications.isEmpty() || dataNode == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalAdditionalRate = BigDecimal.ZERO;

        for (String classification : classifications) {
            for (JsonNode node : dataNode) {
                String apiClassification = node.path("customerClassification").asText(null);

                if (Objects.equals(apiClassification, classification)) {
                    BigDecimal addRate = node.path("additionalRate").decimalValue();
                    totalAdditionalRate = totalAdditionalRate.add(addRate);
                    log.debug("Matched classification {} -> +{}%", classification, addRate);
                    break; // Move to next classification
                }
            }
        }

        // Apply 2% cap on additional rate
        BigDecimal maxCap = new BigDecimal("2.0");
        if (totalAdditionalRate.compareTo(maxCap) > 0) {
            log.info("Total additional rate {} exceeds 2% cap, applying cap", totalAdditionalRate);
            totalAdditionalRate = maxCap;
        }

        return totalAdditionalRate.setScale(2, RoundingMode.HALF_UP);
    }

    public ComparisonResponse compareScenarios(ComparisonRequest request) {
        log.info("Comparing {} FD scenarios", request.getScenarios().size());

        List<CalculationResponse> results = new ArrayList<>();
        CalculationResponse bestScenario = null;
        int bestIndex = 0;

        for (int i = 0; i < request.getScenarios().size(); i++) {
            StandaloneCalculationRequest scenario = request.getScenarios().get(i);

            // Override principal if common principal is provided
            if (request.getCommonPrincipal() != null) {
                scenario.setPrincipalAmount(request.getCommonPrincipal());
            }

            CalculationResponse result = calculateStandalone(scenario);
            results.add(result);

            // Track best scenario
            if (bestScenario == null ||
                result.getMaturityAmount().compareTo(bestScenario.getMaturityAmount()) > 0) {
                bestScenario = result;
                bestIndex = i;
            }
        }

        return ComparisonResponse.builder()
            .scenarios(results)
            .bestScenario(bestScenario)
            .bestScenarioIndex(bestIndex)
            .build();
    }

    /**
     * Calculates APY (Effective Annual Yield)
     * Formula: APY = (1 + r/n)^n - 1
     */
    private BigDecimal calculateAPY(BigDecimal annualRate, CompoundingFrequency frequency) {
        int n = frequency.getPeriodsPerYear();
        BigDecimal rateDecimal = annualRate.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        BigDecimal onePlusRate = BigDecimal.ONE.add(rateDecimal.divide(BigDecimal.valueOf(n), 10, RoundingMode.HALF_UP));

        double apyValue = Math.pow(onePlusRate.doubleValue(), n) - 1;
        BigDecimal apy = BigDecimal.valueOf(apyValue * 100).setScale(2, RoundingMode.HALF_UP);

        log.debug("Calculated APY for rate {}% and frequency {} = {}%", annualRate, frequency, apy);
        return apy;
    }
}
