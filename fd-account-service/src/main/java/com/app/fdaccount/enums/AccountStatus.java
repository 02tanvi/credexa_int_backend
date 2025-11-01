package com.app.fdaccount.enums;

/**
 * Account Status Enum
 * Represents the current state of an FD account
 */
public enum AccountStatus {
    ACTIVE,          // Account is active and operational
    CLOSED,          // Account has been closed
    MATURED,         // Account has reached maturity date
    SUSPENDED,       // Account is temporarily suspended
    PENDING_APPROVAL // Account creation pending approval (if workflow exists)
}
