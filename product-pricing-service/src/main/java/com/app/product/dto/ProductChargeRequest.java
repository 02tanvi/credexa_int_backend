package com.app.product.dto;

import java.math.BigDecimal;

import com.app.product.enums.ChargeFrequency;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductChargeRequest {
    @NotBlank private String chargeName;
    @NotBlank private String chargeType;
    private String description;
    private BigDecimal fixedAmount;
    private BigDecimal percentageRate;
    @NotNull private ChargeFrequency frequency;
    private String applicableTransactionTypes;
    private Boolean waivable;
    private BigDecimal minCharge;
    private BigDecimal maxCharge;
}
