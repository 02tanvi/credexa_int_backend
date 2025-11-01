package com.app.fdaccount.enums;

/**
 * Transaction Type Enum
 * Represents different types of transactions on an FD account
 */
public enum TransactionType {
    // Core transactions
    INITIAL_DEPOSIT,      // First deposit when account is created
    ADDITIONAL_DEPOSIT,   // Additional deposit to existing account
    WITHDRAWAL,           // Regular withdrawal
    PREMATURE_WITHDRAWAL, // Early withdrawal with penalty
    
    // Interest transactions
    INTEREST_CREDIT,      // Interest credited to account
    INTEREST_ACCRUAL,     // Interest accrued but not credited
    INTEREST_CAPITALIZATION, // Interest added to principal
    
    // Fees and charges
    FEE_DEBIT,            // Fee charged
    PENALTY,              // Penalty charged (e.g., premature withdrawal penalty)
    
    // Maturity transactions
    MATURITY_PAYOUT,      // Payout on maturity
    MATURITY_TRANSFER,    // Transfer to another account on maturity
    MATURITY_RENEWAL,     // Renewal on maturity
    
    // Other
    REVERSAL,             // Transaction reversal
    ADJUSTMENT            // Manual adjustment
}
