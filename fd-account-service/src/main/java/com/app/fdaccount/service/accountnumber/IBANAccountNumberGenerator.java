package com.app.fdaccount.service.accountnumber;

import java.math.BigInteger;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * IBAN account number generator
 * Format: IBAN with country code, check digits, bank code, and account number
 * Example: IN47CRXA0010123456 (for India)
 */
@Slf4j
@Component("ibanGenerator")
@RequiredArgsConstructor
public class IBANAccountNumberGenerator implements AccountNumberGenerator {

    private final AccountNumberSequenceService sequenceService;
    private final StandardAccountNumberGenerator standardGenerator;

    /**
     * Generate account number (uses standard format internally)
     */
    @Override
    public String generateAccountNumber(String branchCode) {
        // Generate standard account number first
        return standardGenerator.generateAccountNumber(branchCode);
    }

    /**
     * Generate IBAN from account number
     * Format: CC-KK-BBBB-AAAAAAAAAA
     * CC = Country code (2 letters)
     * KK = IBAN check digits (2 digits)
     * BBBB = Bank code (4 characters)
     * AAAAAAAAAA = Account number (10 digits)
     */
    @Override
    public String generateIBAN(String accountNumber, String countryCode, String bankCode) {
        if (accountNumber == null || accountNumber.length() != 10) {
            throw new IllegalArgumentException("Account number must be 10 digits for IBAN generation");
        }

        // Normalize inputs
        String normalizedCountryCode = countryCode.toUpperCase();
        String normalizedBankCode = normalizeBankCode(bankCode);

        // Calculate IBAN check digits
        String checkDigits = calculateIBANCheckDigits(normalizedCountryCode, normalizedBankCode, accountNumber);

        // Construct IBAN
        String iban = normalizedCountryCode + checkDigits + normalizedBankCode + accountNumber;

        log.debug("Generated IBAN: {} for account: {}", iban, accountNumber);

        return iban;
    }

    @Override
    public boolean validateAccountNumber(String accountNumber) {
        return standardGenerator.validateAccountNumber(accountNumber);
    }

    /**
     * Validate IBAN format and check digits
     */
    public boolean validateIBAN(String iban) {
        if (iban == null || iban.length() < 15) {
            return false;
        }

        // Remove spaces and convert to uppercase
        iban = iban.replaceAll("\\s", "").toUpperCase();

        // Check format: 2 letters + 2 digits + alphanumeric
        if (!iban.matches("^[A-Z]{2}\\d{2}[A-Z0-9]+$")) {
            return false;
        }

        // Rearrange: Move first 4 characters to end
        String rearranged = iban.substring(4) + iban.substring(0, 4);

        // Replace letters with numbers (A=10, B=11, ..., Z=35)
        StringBuilder numericIban = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            if (Character.isLetter(c)) {
                numericIban.append(Character.getNumericValue(c));
            } else {
                numericIban.append(c);
            }
        }

        // Perform mod-97 check
        BigInteger ibanNumber = new BigInteger(numericIban.toString());
        return ibanNumber.mod(BigInteger.valueOf(97)).intValue() == 1;
    }

    @Override
    public String getGeneratorType() {
        return "iban";
    }

    /**
     * Normalize bank code to 4 characters
     */
    private String normalizeBankCode(String bankCode) {
        if (bankCode == null || bankCode.isEmpty()) {
            return "CRXA"; // Default bank code
        }

        String normalized = bankCode.toUpperCase().replaceAll("[^A-Z0-9]", "");

        if (normalized.length() < 4) {
            // Pad with zeros
            return String.format("%-4s", normalized).replace(' ', '0');
        } else if (normalized.length() > 4) {
            return normalized.substring(0, 4);
        }

        return normalized;
    }

    /**
     * Calculate IBAN check digits using mod-97 algorithm
     */
    private String calculateIBANCheckDigits(String countryCode, String bankCode, String accountNumber) {
        // Construct IBAN with check digits as "00"
        String tempIban = bankCode + accountNumber + countryCode + "00";

        // Replace letters with numbers (A=10, B=11, ..., Z=35)
        StringBuilder numericIban = new StringBuilder();
        for (char c : tempIban.toCharArray()) {
            if (Character.isLetter(c)) {
                numericIban.append(Character.getNumericValue(c));
            } else {
                numericIban.append(c);
            }
        }

        // Calculate mod-97
        BigInteger ibanNumber = new BigInteger(numericIban.toString());
        int remainder = ibanNumber.mod(BigInteger.valueOf(97)).intValue();

        // Check digits = 98 - remainder
        int checkDigits = 98 - remainder;

        return String.format("%02d", checkDigits);
    }
}
