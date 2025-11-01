package com.app.fdaccount.service.integration;

import java.math.BigDecimal;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.app.fdaccount.dto.external.ApiResponseWrapper;
import com.app.fdaccount.dto.external.CalculationRequest;
import com.app.fdaccount.dto.external.CalculationResultDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service to integrate with fd-calculator-service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CalculatorServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${integration.calculator-service.url}")
    private String calculatorServiceUrl;

    @Value("${integration.calculator-service.timeout:10000}")
    private int timeout;

    /**
     * Calculate FD maturity amount and date
     */
    @Cacheable(value = "calculationResults", 
               key = "#request.principalAmount + '-' + #request.interestRate + '-' + #request.tenure")
    public CalculationResultDto calculateMaturity(CalculationRequest request) {
        log.debug("Calculating maturity for principal: {}, rate: {}, tenure: {} {}", 
                 request.getPrincipalAmount(), request.getInterestRate(), request.getTenure(), request.getTenureUnit());

        try {
            ApiResponseWrapper<CalculationResultDto> response = webClientBuilder.build()
                    .post()
                    .uri(calculatorServiceUrl + "/calculate/standalone")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseWrapper<CalculationResultDto>>() {})
                    .timeout(Duration.ofMillis(timeout))
                    .block();

            if (response == null || response.getData() == null) {
                throw new RuntimeException("Calculation service returned null result");
            }

            CalculationResultDto result = response.getData();
            log.info("✅ Calculated maturity: Amount={}, Date={}", 
                    result.getMaturityAmount(), result.getMaturityDate());
            return result;

        } catch (Exception e) {
            log.error("❌ Failed to calculate maturity", e);
            throw new RuntimeException("Failed to calculate maturity: " + e.getMessage(), e);
        }
    }

    /**
     * Calculate FD maturity with all parameters
     */
    public CalculationResultDto calculateMaturity(
            BigDecimal principalAmount,
            BigDecimal interestRate,
            Integer termMonths,
            String interestCalculationMethod,
            String compoundingFrequency) {

        CalculationRequest request = CalculationRequest.builder()
                .principalAmount(principalAmount)
                .interestRate(interestRate)
                .tenure(termMonths)
                .tenureUnit("MONTHS")  // Always MONTHS for FD accounts
                .calculationType(interestCalculationMethod != null ? interestCalculationMethod : "COMPOUND")
                .compoundingFrequency(compoundingFrequency != null ? compoundingFrequency : "QUARTERLY")
                .tdsRate(BigDecimal.valueOf(10.0))  // Default TDS rate
                .build();

        return calculateMaturity(request);
    }

    /**
     * Calculate interest for a specific period
     */
    public BigDecimal calculateInterest(
            BigDecimal principal,
            BigDecimal rate,
            int days,
            String calculationMethod) {

        log.debug("Calculating interest: principal={}, rate={}, days={}", principal, rate, days);

        try {
            // Simple interest calculation as fallback
            // In production, this would call calculator service
            BigDecimal dailyRate = rate.divide(BigDecimal.valueOf(36500), 10, BigDecimal.ROUND_HALF_UP);
            BigDecimal interest = principal
                    .multiply(dailyRate)
                    .multiply(BigDecimal.valueOf(days))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);

            log.debug("Calculated interest: {}", interest);
            return interest;

        } catch (Exception e) {
            log.error("Failed to calculate interest", e);
            return BigDecimal.ZERO;
        }
    }
}
