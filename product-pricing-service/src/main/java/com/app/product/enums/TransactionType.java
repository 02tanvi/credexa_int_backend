package com.app.product.enums;

/**
 * Transaction types for product accounts
 */
public enum TransactionType {
    DEPOSIT("Deposit / Credit"),
    WITHDRAWAL("Withdrawal / Debit"),
    INTEREST_CREDIT("Interest Credit"),
    INTEREST_DEBIT("Interest Debit"),
    FEE_DEBIT("Fee Deduction"),
    TAX_DEBIT("Tax Deduction"),
    TRANSFER_IN("Transfer In"),
    TRANSFER_OUT("Transfer Out"),
    LOAN_DISBURSEMENT("Loan Disbursement"),
    LOAN_REPAYMENT("Loan Repayment / EMI"),
    PENALTY("Penalty Charge"),
    REVERSAL("Transaction Reversal");
    
    private final String description;
    
    TransactionType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
