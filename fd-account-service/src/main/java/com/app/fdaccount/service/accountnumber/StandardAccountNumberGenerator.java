package com.app.fdaccount.service.accountnumber;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Standard account number generator
 * Format: 10 digits = 3 digits branch code + 6 digits sequence + 1 digit check digit
 * Check digit calculated using Luhn algorithm
 */
@Slf4j
@Component("standardGenerator")
@RequiredArgsConstructor
public class StandardAccountNumberGenerator implements AccountNumberGenerator {

    private final AccountNumberSequenceService sequenceService;

    /**
     * Generate account number in format: BBB-SSSSSS-C
     * BBB = Branch code (3 digits)
     * SSSSSS = Sequence number (6 digits)
     * C = Check digit (1 digit, Luhn algorithm)
     */
    @Override
    public String generateAccountNumber(String branchCode) {
        // Ensure branch code is 3 digits
        String normalizedBranchCode = normalizeBranchCode(branchCode);
        
        // Get next sequence number
        long sequence = sequenceService.getNextSequence(normalizedBranchCode);
        
        // Format sequence as 6 digits
        String sequenceStr = String.format("%06d", sequence);
        
        // Combine branch code and sequence
        String baseNumber = normalizedBranchCode + sequenceStr;
        
        // Calculate check digit using Luhn algorithm
        int checkDigit = calculateLuhnCheckDigit(baseNumber);
        
        // Final account number
        String accountNumber = baseNumber + checkDigit;
        
        log.debug("Generated account number: {} for branch: {}, sequence: {}", 
                 accountNumber, branchCode, sequence);
        
        return accountNumber;
    }

    @Override
    public String generateIBAN(String accountNumber, String countryCode, String bankCode) {
        // India doesn't use IBAN system officially, but we'll generate a format for internal use
        // Format: IN + 2 check digits + 4 char bank code + 10 digit account number
        
        if (accountNumber == null || accountNumber.isEmpty()) {
            return null;
        }
        
        // Default values if not provided
        String country = (countryCode != null && !countryCode.isEmpty()) ? countryCode : "IN";
        String bank = (bankCode != null && !bankCode.isEmpty()) ? bankCode : "CRDX"; // CREDEXA bank code
        
        // Ensure bank code is 4 characters
        if (bank.length() < 4) {
            bank = String.format("%-4s", bank).replace(' ', '0');
        } else if (bank.length() > 4) {
            bank = bank.substring(0, 4);
        }
        
        // Remove any non-alphanumeric characters from account number
        String cleanAccountNumber = accountNumber.replaceAll("[^A-Za-z0-9]", "");
        
        // Calculate IBAN check digits
        // Move country code and check digits to end, replace letters with numbers (A=10, B=11, etc.)
        String rearranged = bank + cleanAccountNumber + country + "00";
        StringBuilder numeric = new StringBuilder();
        
        for (char c : rearranged.toCharArray()) {
            if (Character.isDigit(c)) {
                numeric.append(c);
            } else {
                // Convert letter to number (A=10, B=11, ..., Z=35)
                numeric.append(Character.toUpperCase(c) - 'A' + 10);
            }
        }
        
        // Calculate mod 97
        int checksum = 98 - mod97(numeric.toString());
        String checkDigits = String.format("%02d", checksum);
        
        // Final IBAN format: INXX CRDX 0011 0000 07
        String iban = country + checkDigits + bank + cleanAccountNumber;
        
        log.debug("Generated IBAN: {} for account: {}", iban, accountNumber);
        
        return iban;
    }
    
    /**
     * Calculate mod 97 for large numbers (used in IBAN check digit calculation)
     */
    private int mod97(String number) {
        int remainder = 0;
        for (int i = 0; i < number.length(); i++) {
            int digit = Character.getNumericValue(number.charAt(i));
            remainder = (remainder * 10 + digit) % 97;
        }
        return remainder;
    }

    @Override
    public boolean validateAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() != 10) {
            return false;
        }

        // Check if all characters are digits
        if (!accountNumber.matches("\\d{10}")) {
            return false;
        }

        // Extract check digit
        String baseNumber = accountNumber.substring(0, 9);
        int providedCheckDigit = Character.getNumericValue(accountNumber.charAt(9));

        // Calculate expected check digit
        int expectedCheckDigit = calculateLuhnCheckDigit(baseNumber);

        return providedCheckDigit == expectedCheckDigit;
    }

    @Override
    public String getGeneratorType() {
        return "standard";
    }

    /**
     * Normalize branch code to 3 digits
     */
    private String normalizeBranchCode(String branchCode) {
        if (branchCode == null || branchCode.isEmpty()) {
            return "001"; // Default branch
        }

        // Remove non-numeric characters
        String numericOnly = branchCode.replaceAll("\\D", "");

        if (numericOnly.isEmpty()) {
            return "001";
        }

        // Pad with zeros or truncate to 3 digits
        if (numericOnly.length() < 3) {
            return String.format("%03d", Integer.parseInt(numericOnly));
        } else if (numericOnly.length() > 3) {
            return numericOnly.substring(0, 3);
        }

        return numericOnly;
    }

    /**
     * Calculate Luhn check digit
     * The Luhn algorithm is used for credit card validation and account numbers
     */
    private int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = false;

        // Loop through digits from right to left
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        // Check digit is the amount needed to make sum divisible by 10
        return (10 - (sum % 10)) % 10;
    }
}
