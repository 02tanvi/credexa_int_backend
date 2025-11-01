package com.app.calculator.enums;

/**
 * Type of interest calculation
 */
public enum CalculationType {
    SIMPLE("Simple Interest"),
    COMPOUND("Compound Interest");
    
    private final String description;
    
    CalculationType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
