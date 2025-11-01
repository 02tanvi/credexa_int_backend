package com.app.product.dto;

import com.app.product.enums.TransactionType;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductTransactionTypeRequest {
    
    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;
    
    private String description;
}
