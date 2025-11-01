package com.app.product.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.app.product.enums.ProductStatus;
import com.app.product.enums.ProductType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight product response for list/search operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSummaryResponse {
    
    private Long productId;
    private String productName;
    private String productCode;
    private ProductType productType;
    private String description;
    private ProductStatus status;
    private LocalDate effectiveDate;
    private LocalDate endDate;
    private BigDecimal baseInterestRate;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Integer minTermMonths;
    private Integer maxTermMonths;
    private Boolean currentlyActive;
}
