package com.app.product.dto;

import com.app.product.enums.BalanceType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductBalanceTypeResponse {
    
    private Long id;
    private BalanceType balanceType;
    private Boolean tracked;
    private String description;
}
