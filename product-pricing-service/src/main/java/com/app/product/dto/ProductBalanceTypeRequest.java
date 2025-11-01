package com.app.product.dto;

import com.app.product.enums.BalanceType;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductBalanceTypeRequest {
    
    @NotNull(message = "Balance type is required")
    private BalanceType balanceType;
    
    private String description;

    @Builder.Default
    private Boolean tracked = true;
}
