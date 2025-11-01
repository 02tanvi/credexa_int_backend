package com.app.fdaccount.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for account balance details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {

    private Long id;
    private String balanceType;
    private BigDecimal balance;
    private LocalDate asOfDate;
    private String description;
}
