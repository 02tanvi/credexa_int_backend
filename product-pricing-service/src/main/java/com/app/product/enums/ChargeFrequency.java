package com.app.product.enums;

/**
 * Charge/Fee calculation frequency
 */
public enum ChargeFrequency {
    ONE_TIME("One Time"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    QUARTERLY("Quarterly"),
    SEMI_ANNUALLY("Semi-Annually"),
    ANNUALLY("Annually"),
    ON_MATURITY("On Maturity"),
    ON_TRANSACTION("Per Transaction");
    
    private final String description;
    
    ChargeFrequency(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
