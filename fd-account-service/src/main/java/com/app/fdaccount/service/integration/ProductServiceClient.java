package com.app.fdaccount.service.integration;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.app.fdaccount.dto.external.ApiResponseWrapper;
import com.app.fdaccount.dto.external.ProductDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service to integrate with product-pricing-service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${integration.product-service.url}")
    private String productServiceUrl;

    @Value("${integration.product-service.timeout:5000}")
    private int timeout;

    /**
     * Get product by product code
     * Cached to reduce external calls
     */
    @Cacheable(value = "products", key = "#productCode")
    public ProductDto getProductByCode(String productCode) {
        log.debug("Fetching product: {}", productCode);

        try {
            ApiResponseWrapper<ProductDto> response = webClientBuilder.build()
                    .get()
                    .uri(productServiceUrl + "/code/{productCode}", productCode)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseWrapper<ProductDto>>() {})
                    .timeout(Duration.ofMillis(timeout))
                    .block();

            if (response == null || !response.isSuccess() || response.getData() == null) {
                throw new RuntimeException("Product not found: " + productCode);
            }

            ProductDto product = response.getData();

            if (!"ACTIVE".equals(product.getStatus()) && !Boolean.TRUE.equals(product.getCurrentlyActive())) {
                throw new RuntimeException("Product is not active: " + productCode);
            }

            log.info("✅ Fetched product: {} - {}", productCode, product.getProductName());
            return product;

        } catch (Exception e) {
            log.error("❌ Failed to fetch product: {}", productCode, e);
            throw new RuntimeException("Failed to fetch product details: " + e.getMessage(), e);
        }
    }

    /**
     * Validate product exists and is active
     */
    public boolean validateProduct(String productCode) {
        try {
            ProductDto product = getProductByCode(productCode);
            return product != null && ("ACTIVE".equals(product.getStatus()) || Boolean.TRUE.equals(product.getCurrentlyActive()));
        } catch (Exception e) {
            log.warn("Product validation failed for: {}", productCode);
            return false;
        }
    }

    /**
     * Check if product allows premature withdrawal
     */
    public boolean isPrematureWithdrawalAllowed(String productCode) {
        try {
            ProductDto product = getProductByCode(productCode);
            return product != null && Boolean.TRUE.equals(product.getPrematureWithdrawalAllowed());
        } catch (Exception e) {
            log.warn("Could not check premature withdrawal for: {}", productCode);
            return false;
        }
    }
}
