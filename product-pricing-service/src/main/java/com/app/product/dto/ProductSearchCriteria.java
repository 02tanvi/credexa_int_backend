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
 * Search criteria for filtering products
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSearchCriteria {
    
    private String productName;
    private String productCode;
    private ProductType productType;
    private ProductStatus status;
    private LocalDate effectiveDateFrom;
    private LocalDate effectiveDateTo;
    private BigDecimal minAmountFrom;
    private BigDecimal minAmountTo;
    private BigDecimal maxAmountFrom;
    private BigDecimal maxAmountTo;
    private String createdBy;
    private Boolean currentlyActive;
    
    // Pagination
    @Builder.Default
    private Integer page = 0;
    
    @Builder.Default
    private Integer size = 20;
    
    @Builder.Default
    private String sortBy = "createdAt";
    
    @Builder.Default
    private String sortDirection = "DESC";
}
