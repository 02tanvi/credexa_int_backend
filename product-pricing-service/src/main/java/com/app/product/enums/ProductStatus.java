package com.app.product.enums;

/**
 * Product status
 */
public enum ProductStatus {
    DRAFT("Draft - Under development"),
    ACTIVE("Active - Available for account creation"),
    INACTIVE("Inactive - Not available for new accounts"),
    SUSPENDED("Suspended - Temporarily unavailable"),
    CLOSED("Closed - Discontinued");
    
    private final String description;
    
    ProductStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
