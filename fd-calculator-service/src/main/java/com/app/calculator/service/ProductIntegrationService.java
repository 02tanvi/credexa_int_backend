package com.app.calculator.service;

import com.app.calculator.dto.external.InterestRateDto;
import com.app.calculator.dto.external.ProductDto;
import com.app.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for integrating with product-pricing-service
 *
 * REFACTORED (without CalculatedRates):
 * - Provides base and capped additional rate calculation directly.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProductIntegrationService {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.product-pricing.url}")
    private String productPricingUrl;

    // Per the document: "Rate to be capped... max of 2% excess"
    private static final BigDecimal MAX_ADDITIONAL_RATE = new BigDecimal("2.0");

    /**
     * Get product details by ID (cached)
     */
    @Cacheable(value = "products", key = "#productId")
    public ProductDto getProduct(Long productId) {
        log.info("Fetching product details for ID: {}", productId);

        try {
            WebClient webClient = webClientBuilder.baseUrl(productPricingUrl).build();

            ApiResponse<ProductDto> response = webClient.get()
                    .uri("/products/{id}", productId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<ProductDto>>() {})
                    .block();

            if (response != null && response.isSuccess() && response.getData() != null) {
                log.debug("Successfully fetched product: {}", response.getData().getProductCode());
                return response.getData();
            }

            throw new RuntimeException("Product not found with ID: " + productId);
        } catch (Exception e) {
            log.error("Failed to fetch product {}: {}", productId, e.getMessage());
            throw new RuntimeException("Unable to fetch product details: " + e.getMessage(), e);
        }
    }

    /**
     * Orchestrates fetching all applicable rates and applies business logic.
     * Implements the "max 2 categories" and "2% cap" rules.
     *
     * @return the total interest rate (base + capped additional)
     */
    public BigDecimal getFinalInterestRate(Long productId, BigDecimal amount,
                                           Integer termMonths, List<String> classifications) {

        // 1. Fetch the Base Rate
        InterestRateDto baseRateDto = fetchApplicableRate(productId, amount, termMonths, null);
        if (baseRateDto == null || baseRateDto.getInterestRate() == null) {
            log.warn("No specific base rate found, falling back to product's default rate.");
            baseRateDto = new InterestRateDto();
            baseRateDto.setInterestRate(getProduct(productId).getBaseInterestRate());
        }
        BigDecimal baseRate = baseRateDto.getInterestRate();
        log.debug("Base rate: {}%", baseRate);

        // 2. Fetch Additional Rates
        BigDecimal totalAdditionalRate = BigDecimal.ZERO;
        if (classifications != null) {
            for (String classification : classifications.stream().distinct().limit(2).toList()) {
                InterestRateDto additionalRateDto = fetchApplicableRate(productId, amount, termMonths, classification);
                if (additionalRateDto != null && additionalRateDto.getAdditionalRate() != null) {
                    log.debug("Additional rate for {}: {}%", classification, additionalRateDto.getAdditionalRate());
                    totalAdditionalRate = totalAdditionalRate.add(additionalRateDto.getAdditionalRate());
                }
            }
        }

        // 3. Apply 2% Cap
        BigDecimal cappedAdditionalRate = totalAdditionalRate;
        if (totalAdditionalRate.compareTo(MAX_ADDITIONAL_RATE) > 0) {
            log.warn("Total additional rate {} exceeds cap {}. Capping.", totalAdditionalRate, MAX_ADDITIONAL_RATE);
            cappedAdditionalRate = MAX_ADDITIONAL_RATE;
        }

        // 4. Return Final Rate
        BigDecimal finalRate = baseRate.add(cappedAdditionalRate);
        log.info("Final interest rate (base + capped additional): {}%", finalRate);

        return finalRate;
    }

    /**
     * Private helper to fetch applicable interest rate (cached)
     */
    @Cacheable(value = "interestRates", key = "#productId + '-' + #amount + '-' + #termMonths + '-' + #classification")
    private InterestRateDto fetchApplicableRate(Long productId, BigDecimal amount,
                                                Integer termMonths, String classification) {
        log.info("Fetching applicable rate for product: {}, amount: {}, term: {} months, classification: {}",
                productId, amount, termMonths, classification);

        try {
            WebClient webClient = webClientBuilder.baseUrl(productPricingUrl).build();

            String uri = String.format("/products/%d/interest-rates/applicable?amount=%s&termMonths=%d",
                    productId, amount.toString(), termMonths);

            if (classification != null && !classification.isBlank()) {
                uri += "&classification=" + classification;
            }

            ApiResponse<InterestRateDto> response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<InterestRateDto>>() {})
                    .block();

            if (response != null && response.isSuccess() && response.getData() != null) {
                log.debug("Applicable rate DTO: {}", response.getData());
                return response.getData();
            }

            log.warn("No applicable rate found for product {} with classification {}", productId, classification);
            return null;
        } catch (Exception e) {
            log.error("Failed to fetch interest rate for product {}: {}", productId, e.getMessage());
            return null;
        }
    }
}
