package com.app.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Main application class for Product and Pricing Service
 * 
 * This service manages:
 * - Product definitions and configurations
 * - Pricing rules and interest rate matrices
 * - Product roles and relationships
 * - Charges, fees, and taxes
 * - Product caching for performance
 */
@SpringBootApplication
@EnableCaching
public class ProductPricingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductPricingApplication.class, args);
    }
}
