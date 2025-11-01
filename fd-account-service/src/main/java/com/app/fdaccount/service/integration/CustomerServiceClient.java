package com.app.fdaccount.service.integration;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;

import com.app.fdaccount.dto.external.CustomerDto;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service to integrate with customer-service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${integration.customer-service.url}")
    private String customerServiceUrl;

    @Value("${integration.customer-service.timeout:5000}")
    private int timeout;

    /**
     * Get JWT token from current request
     */
    private String getJwtToken() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    return authHeader.substring(7);
                }
            }
        } catch (Exception e) {
            log.warn("Could not extract JWT token from request: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Get customer by customer ID
     * Cached to reduce external calls
     */
    @Cacheable(value = "customers", key = "#customerId")
    public CustomerDto getCustomerById(Long customerId) {
        log.debug("Fetching customer: {}", customerId);

        try {
            String jwtToken = getJwtToken();
            WebClient.RequestHeadersSpec<?> requestSpec = webClientBuilder.build()
                    .get()
                    .uri(customerServiceUrl + "/{customerId}", customerId);

            // Add JWT token if available
            if (jwtToken != null) {
                requestSpec = requestSpec.header("Authorization", "Bearer " + jwtToken);
                log.debug("Added JWT token to request for customer: {}", customerId);
            } else {
                log.warn("No JWT token available for customer request: {}", customerId);
            }

            CustomerDto customer = requestSpec
                    .retrieve()
                    .bodyToMono(CustomerDto.class)
                    .timeout(Duration.ofMillis(timeout))
                    .block();

            if (customer == null) {
                throw new RuntimeException("Customer not found: " + customerId);
            }

            if (!Boolean.TRUE.equals(customer.getIsActive())) {
                throw new RuntimeException("Customer is not active: " + customerId);
            }

            log.info("✅ Fetched customer: {} - {}", customerId, customer.getCustomerName());
            return customer;

        } catch (Exception e) {
            log.error("❌ Failed to fetch customer: {}", customerId, e);
            throw new RuntimeException("Failed to fetch customer details: " + e.getMessage(), e);
        }
    }

    /**
     * Validate customer exists and is active
     */
    public boolean validateCustomer(Long customerId) {
        try {
            CustomerDto customer = getCustomerById(customerId);
            return customer != null && Boolean.TRUE.equals(customer.getIsActive());
        } catch (Exception e) {
            log.warn("Customer validation failed for: {}", customerId);
            return false;
        }
    }

    /**
     * Check if customer has completed KYC
     */
    public boolean isKycCompleted(Long customerId) {
        try {
            CustomerDto customer = getCustomerById(customerId);
            return customer != null && "COMPLETED".equalsIgnoreCase(customer.getKycStatus());
        } catch (Exception e) {
            log.warn("Could not check KYC status for: {}", customerId);
            return false;
        }
    }

    /**
     * Get customer classification
     */
    public String getCustomerClassification(Long customerId) {
        try {
            CustomerDto customer = getCustomerById(customerId);
            return customer != null ? customer.getCustomerClassification() : null;
        } catch (Exception e) {
            log.warn("Could not get classification for customer: {}", customerId);
            return null;
        }
    }
}
