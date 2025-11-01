package com.app.calculator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * A DTO to hold the results of a rate calculation from the ProductIntegrationService.
 * This separates the final base rate from the final (capped) additional rate.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalculatedRates {

    /**
     * The applicable base interest rate (e.g., 7.0%).
     */
    private BigDecimal baseRate;

    /**
     * The final, capped additional interest rate from all classifications (e.g., 2.0%).
     */
    private BigDecimal additionalRate;
}



