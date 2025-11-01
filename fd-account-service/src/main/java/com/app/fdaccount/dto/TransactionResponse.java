package com.app.fdaccount.dto;

import com.app.fdaccount.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for transaction details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private Long id;
    private String transactionReference;
    private TransactionType transactionType;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private LocalDate valueDate;
    private String description;
    private String performedBy;
    
    // Balance tracking
    private BigDecimal principalBalanceAfter;
    private BigDecimal interestBalanceAfter;
    private BigDecimal totalBalanceAfter;
    
    // Reversal info
    private Boolean isReversed;
    private Long reversalTransactionId;
    private LocalDateTime reversalDate;
    private String reversalReason;
    
    // Related transaction
    private Long relatedTransactionId;
    
    private LocalDateTime createdAt;
}
