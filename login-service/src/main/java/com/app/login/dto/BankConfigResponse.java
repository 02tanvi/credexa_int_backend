package com.app.login.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for bank configuration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankConfigResponse {

    private String bankName;
    private String logoUrl;
    private String defaultLanguage;
    private String defaultCurrency;
    private int currencyDecimalPlaces;
}
