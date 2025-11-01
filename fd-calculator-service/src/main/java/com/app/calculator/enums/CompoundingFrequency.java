package com.app.calculator.enums;

/**
 * Compounding frequency for compound interest calculations
 */
public enum CompoundingFrequency {
    DAILY(365, "Daily"),
    MONTHLY(12, "Monthly"),
    QUARTERLY(4, "Quarterly"),
    SEMI_ANNUALLY(2, "Semi-Annually"),
    ANNUALLY(1, "Annually");
    
    private final int periodsPerYear;
    private final String description;
    
    CompoundingFrequency(int periodsPerYear, String description) {
        this.periodsPerYear = periodsPerYear;
        this.description = description;
    }
    
    public int getPeriodsPerYear() {
        return periodsPerYear;
    }
    
    public String getDescription() {
        return description;
    }
}
