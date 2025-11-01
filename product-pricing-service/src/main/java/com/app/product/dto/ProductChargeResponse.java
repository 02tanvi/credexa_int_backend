package com.app.product.dto;

import java.math.BigDecimal;

import com.app.product.enums.ChargeFrequency;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductChargeResponse {
    
    private Long id;
    private String chargeName;
    private String chargeType;
    private String description;
    private BigDecimal fixedAmount;
    private BigDecimal percentageRate;
    private ChargeFrequency frequency;
    private String applicableTransactionTypes;
    private Boolean waivable;
    private BigDecimal minCharge;
    private BigDecimal maxCharge;
    private Boolean active;
}
