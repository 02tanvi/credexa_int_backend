package com.app.product.dto;

import com.app.product.enums.BalanceType;
import com.app.product.enums.TransactionType;

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
public class TransactionBalanceRelationshipRequest {
    
    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;
    
    @NotNull(message = "Balance type is required")
    private BalanceType balanceType;
    
    @NotBlank(message = "Impact type is required (DEBIT/CREDIT/NO_IMPACT)")
    private String impactType;
    
    private String description;
    private Boolean active;
}
