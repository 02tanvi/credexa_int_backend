package com.app.calculator.dto.external;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for product details from product-pricing-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    
    private Long id;
    private String productName;
    private String productCode;
    private String productType;
    private String description;
    
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Integer minTermMonths;
    private Integer maxTermMonths;
    
    private BigDecimal baseInterestRate;
    private String interestCalculationMethod;
    private String interestPayoutFrequency;
    
    private Boolean tdsApplicable;
    private BigDecimal tdsRate;
    
    private Boolean active;
    private String status;
}
