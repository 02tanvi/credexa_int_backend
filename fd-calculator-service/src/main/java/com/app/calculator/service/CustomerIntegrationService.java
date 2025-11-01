package com.app.calculator.service;

import com.app.calculator.dto.external.CustomerDto;
import com.app.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Service for integrating with customer-service
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerIntegrationService {
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${services.customer.url}")
    private String customerServiceUrl;
    
    /**
     * Get customer classification (cached)
     */
    @Cacheable(value = "customerClassifications", key = "#customerId")
    public String getCustomerClassification(Long customerId) {
        log.info("Fetching customer classification for ID: {}", customerId);
        
        try {
            WebClient webClient = webClientBuilder.baseUrl(customerServiceUrl).build();
            
            ApiResponse<CustomerDto> response = webClient.get()
                .uri("/{id}", customerId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<CustomerDto>>() {})
                .block();
            
            if (response != null && response.isSuccess() && response.getData() != null) {
                String classification = response.getData().getCustomerClassification();
                log.debug("Customer {} has classification: {}", customerId, classification);
                return classification;
            }
            
            log.warn("Customer not found with ID: {}", customerId);
            return null;
        } catch (Exception e) {
            log.error("Failed to fetch customer {}: {}", customerId, e.getMessage());
            return null;
        }
    }
}
