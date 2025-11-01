package com.app.product.enums;

/**
 * Enum representing different types of balances tracked in FD products
 */
public enum BalanceType {
    PRINCIPAL("Original deposit amount"),
    INTEREST_ACCRUED("Interest earned but not yet paid"),
    INTEREST_PAID("Interest already paid out"),
    MATURITY_AMOUNT("Principal + Interest at maturity"),
    TDS_DEDUCTED("Tax deducted at source"),
    PREMATURE_PENALTY("Penalty for early withdrawal"),
    AVAILABLE_BALANCE("Currently available balance"),
    LOCKED_AMOUNT("Amount locked/not withdrawable"),
    WITHDRAWN_AMOUNT("Total amount withdrawn");

    private final String description;

    BalanceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}