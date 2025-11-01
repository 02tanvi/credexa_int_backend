package com.app.product.enums;

/**
 * Fixed Deposit product types
 */
public enum ProductType {
    FIXED_DEPOSIT("Fixed Deposit"),
    TAX_SAVER_FD("Tax Saver Fixed Deposit"),
    SENIOR_CITIZEN_FD("Senior Citizen Fixed Deposit"),
    FLEXI_FD("Flexi Fixed Deposit"),
    CUMULATIVE_FD("Cumulative Fixed Deposit"),
    NON_CUMULATIVE_FD("Non-Cumulative Fixed Deposit");
    
    private final String description;
    
    ProductType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
