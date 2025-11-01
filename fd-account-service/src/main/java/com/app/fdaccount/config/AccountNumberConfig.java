package com.app.fdaccount.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.app.fdaccount.service.accountnumber.AccountNumberGenerator;
import com.app.fdaccount.service.accountnumber.IBANAccountNumberGenerator;
import com.app.fdaccount.service.accountnumber.StandardAccountNumberGenerator;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuration for account number generation
 * Selects the appropriate generator based on application.yml settings
 */
@Slf4j
@Configuration
public class AccountNumberConfig {

    @Value("${account-number.generator.type:standard}")
    private String generatorType;

    /**
     * Primary account number generator bean
     * Selected based on configuration
     */
    @Bean
    @Primary
    public AccountNumberGenerator accountNumberGenerator(
            @Qualifier("standardGenerator") StandardAccountNumberGenerator standardGenerator,
            @Qualifier("ibanGenerator") IBANAccountNumberGenerator ibanGenerator) {

        AccountNumberGenerator generator;

        switch (generatorType.toLowerCase()) {
            case "iban":
                generator = ibanGenerator;
                log.info("✅ Using IBAN Account Number Generator");
                break;
            case "standard":
            default:
                generator = standardGenerator;
                log.info("✅ Using Standard Account Number Generator");
                break;
        }

        log.info("Account number format: {}", getFormatDescription(generator.getGeneratorType()));
        return generator;
    }

    private String getFormatDescription(String type) {
        switch (type) {
            case "iban":
                return "IBAN format (e.g., IN47CRXA0010123456)";
            case "standard":
            default:
                return "10-digit format (Branch-Sequence-CheckDigit)";
        }
    }
}
