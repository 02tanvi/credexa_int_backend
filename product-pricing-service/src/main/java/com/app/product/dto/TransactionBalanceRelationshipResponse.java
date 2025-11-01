package com.app.product.dto;

import com.app.product.enums.BalanceType;
import com.app.product.enums.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionBalanceRelationshipResponse {
    
    private Long id;
    private TransactionType transactionType;
    private BalanceType balanceType;
    private String impactType;
    private String description;
    private Boolean active;
}
