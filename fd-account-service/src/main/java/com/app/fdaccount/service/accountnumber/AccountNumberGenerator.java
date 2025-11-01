package com.app.fdaccount.service.accountnumber;

/**
 * Plugin interface for account number generation
 * Allows different implementations: Standard, IBAN, Custom
 */
public interface AccountNumberGenerator {

    /**
     * Generate a unique account number
     * 
     * @param branchCode The branch code for the account
     * @return Generated account number
     */
    String generateAccountNumber(String branchCode);

    /**
     * Generate IBAN if supported by this generator
     * 
     * @param accountNumber The account number
     * @param countryCode The country code (e.g., "IN")
     * @param bankCode The bank code
     * @return Generated IBAN or null if not supported
     */
    String generateIBAN(String accountNumber, String countryCode, String bankCode);

    /**
     * Validate an account number
     * 
     * @param accountNumber The account number to validate
     * @return true if valid, false otherwise
     */
    boolean validateAccountNumber(String accountNumber);

    /**
     * Get the generator type
     * 
     * @return Generator type name (e.g., "standard", "iban", "custom")
     */
    String getGeneratorType();
}
