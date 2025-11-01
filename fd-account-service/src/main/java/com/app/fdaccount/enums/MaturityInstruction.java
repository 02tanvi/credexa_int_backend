package com.app.fdaccount.enums;

/**
 * Maturity Instruction Enum
 * Defines what should happen when an FD account matures
 */
public enum MaturityInstruction {
    TRANSFER_TO_SAVINGS,  // Transfer maturity amount to savings account
    TRANSFER_TO_CURRENT,  // Transfer maturity amount to current account
    RENEW_PRINCIPAL_ONLY, // Renew with principal only, transfer interest
    RENEW_WITH_INTEREST,  // Renew with principal + interest
    CLOSE_AND_PAYOUT,     // Close account and payout to customer
    HOLD                  // Hold until customer instruction
}
