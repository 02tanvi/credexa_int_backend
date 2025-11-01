package com.app.fdaccount.enums;

/**
 * Account ID Type Enum
 * Represents different types of account identifiers
 */
public enum AccountIdType {
    ACCOUNT_NUMBER,      // Standard 10-digit account number
    INTERNAL_ID,         // System-generated UUID/Long ID
    IBAN,                // International Bank Account Number
    CUSTOM_REFERENCE     // Custom reference number
}
