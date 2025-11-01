package com.app.customer.dto;

import com.app.customer.entity.Customer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for customer classification response (used for FD rate determination)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerClassificationResponse {

    private Long customerId;
    private String fullName;
    private Customer.CustomerClassification classification;
    private BigDecimal additionalRatePercentage;
    private String classificationDescription;

    /**
     * Get additional interest rate based on classification
     */
    public static BigDecimal getAdditionalRate(Customer.CustomerClassification classification) {
        return switch (classification) {
            case REGULAR -> BigDecimal.ZERO;
            case PREMIUM -> new BigDecimal("0.25");
            case VIP -> new BigDecimal("0.50");
            case SENIOR_CITIZEN -> new BigDecimal("0.50");
            case SUPER_SENIOR -> new BigDecimal("0.75");
        };
    }

    /**
     * Get classification description
     */
    public static String getDescription(Customer.CustomerClassification classification) {
        return switch (classification) {
            case REGULAR -> "Standard customer with base interest rates";
            case PREMIUM -> "Premium customer with additional 0.25% interest rate";
            case VIP -> "VIP customer with additional 0.50% interest rate";
            case SENIOR_CITIZEN -> "Senior citizen (60-79 years) with additional 0.50% interest rate";
            case SUPER_SENIOR -> "Super senior citizen (80+ years) with additional 0.75% interest rate";
        };
    }

    /**
     * Create response from customer entity
     */
    public static CustomerClassificationResponse fromCustomer(Customer customer) {
        return CustomerClassificationResponse.builder()
                .customerId(customer.getId())
                .fullName(customer.getFullName())
                .classification(customer.getClassification())
                .additionalRatePercentage(getAdditionalRate(customer.getClassification()))
                .classificationDescription(getDescription(customer.getClassification()))
                .build();
    }
}
