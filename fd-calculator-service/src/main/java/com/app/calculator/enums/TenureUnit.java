package com.app.calculator.enums;

/**
 * Unit of tenure for FD calculations
 */
public enum TenureUnit {
    DAYS(1, "Days"),
    MONTHS(30, "Months"),
    YEARS(365, "Years");
    
    private final int daysMultiplier;
    private final String description;
    
    TenureUnit(int daysMultiplier, String description) {
        this.daysMultiplier = daysMultiplier;
        this.description = description;
    }
    
    public int getDaysMultiplier() {
        return daysMultiplier;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Convert tenure to years
     */
    public double toYears(int tenure) {
        return switch (this) {
            case YEARS -> tenure;
            case MONTHS -> tenure / 12.0;
            case DAYS -> tenure / 365.0;
        };
    }
    
    /**
     * Convert tenure to months
     */
    public int toMonths(int tenure) {
        return switch (this) {
            case YEARS -> tenure * 12;
            case MONTHS -> tenure;
            case DAYS -> tenure / 30;
        };
    }
    
    /**
     * Convert tenure to days
     */
    public int toDays(int tenure) {
        return switch (this) {
            case YEARS -> tenure * 365;
            case MONTHS -> tenure * 30;
            case DAYS -> tenure;
        };
    }
}
