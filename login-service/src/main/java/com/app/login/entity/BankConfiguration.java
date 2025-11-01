package com.app.login.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bank configuration for multi-tenancy support
 * Stores bank name, logo, language, and currency preferences
 */
@Entity
@Table(name = "bank_configuration")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "default_language", length = 10)
    private String defaultLanguage = "en";

    @Column(name = "default_currency", length = 10)
    private String defaultCurrency = "USD";

    @Column(name = "currency_decimal_places")
    private int currencyDecimalPlaces = 2;

    @Column(name = "is_active")
    private boolean active = true;
}
