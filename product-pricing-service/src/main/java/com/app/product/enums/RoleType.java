package com.app.product.enums;

/**
 * Role types for product accounts
 */
public enum RoleType {
    OWNER("Primary Account Owner"),
    CO_OWNER("Co-Owner / Joint Account Holder"),
    GUARDIAN("Guardian for Minor Account"),
    NOMINEE("Nominee / Beneficiary"),
    BORROWER("Primary Borrower"),
    CO_BORROWER("Co-Borrower"),
    GUARANTOR("Loan Guarantor"),
    AUTHORIZED_SIGNATORY("Authorized Signatory");
    
    private final String description;
    
    RoleType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
