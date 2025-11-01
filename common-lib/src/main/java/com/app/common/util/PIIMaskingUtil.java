package com.app.common.util;

import org.springframework.stereotype.Component;

/**
 * Utility for masking Personally Identifiable Information (PII)
 */
@Component
public class PIIMaskingUtil {

    /**
     * Masks email address
     * Example: john.doe@example.com -> j***.d**@example.com
     */
    public String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];
        
        if (localPart.length() <= 2) {
            return localPart.charAt(0) + "***@" + domain;
        }
        
        return localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1) + "@" + domain;
    }

    /**
     * Masks mobile number
     * Example: 9876543210 -> 98******10
     */
    public String maskMobileNumber(String mobile) {
        if (mobile == null || mobile.length() < 4) {
            return mobile;
        }
        
        return mobile.substring(0, 2) + "******" + mobile.substring(mobile.length() - 2);
    }

    /**
     * Generic masking for strings
     */
    public String maskString(String value) {
        if (value == null || value.length() <= 4) {
            return "****";
        }
        
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }
}
