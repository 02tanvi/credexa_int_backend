package com.app.fdaccount.enums;

/**
 * Role Type Enum
 * Represents the role of a customer on an FD account
 */
public enum RoleType {
    OWNER,              // Primary account owner
    CO_OWNER,           // Joint account co-owner
    NOMINEE,            // Nominee for the account
    AUTHORIZED_SIGNATORY, // Person authorized to transact
    GUARDIAN            // Guardian for minor accounts
}
