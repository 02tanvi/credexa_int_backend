package com.app.login.service;

import com.app.login.dto.BankConfigResponse;
import com.app.login.entity.BankConfiguration;
import com.app.login.repository.BankConfigurationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for bank configuration management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BankConfigService {

    private final BankConfigurationRepository bankConfigRepository;

    /**
     * Get active bank configuration
     */
    public BankConfigResponse getBankConfiguration() {
        BankConfiguration config = bankConfigRepository.findByActiveTrue()
                .orElseGet(this::getDefaultConfiguration);

        return BankConfigResponse.builder()
                .bankName(config.getBankName())
                .logoUrl(config.getLogoUrl())
                .defaultLanguage(config.getDefaultLanguage())
                .defaultCurrency(config.getDefaultCurrency())
                .currencyDecimalPlaces(config.getCurrencyDecimalPlaces())
                .build();
    }

    /**
     * Get default configuration if none exists
     */
    private BankConfiguration getDefaultConfiguration() {
        return BankConfiguration.builder()
                .bankName("Credexa Bank")
                .logoUrl("/assets/logo.png")
                .defaultLanguage("en")
                .defaultCurrency("USD")
                .currencyDecimalPlaces(2)
                .active(true)
                .build();
    }
}
