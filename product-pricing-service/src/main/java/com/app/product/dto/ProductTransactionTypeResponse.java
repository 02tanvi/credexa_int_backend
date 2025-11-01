package com.app.product.dto;

import com.app.product.enums.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductTransactionTypeResponse {
    
    private Long id;
    private TransactionType transactionType;
    private Boolean allowed;
    private Boolean requiresApproval;
    private String description;
    private String validationRules;
}
