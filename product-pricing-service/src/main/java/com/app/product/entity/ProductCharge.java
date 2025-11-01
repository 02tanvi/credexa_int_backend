package com.app.product.entity;

import java.math.BigDecimal;

import com.app.product.enums.ChargeFrequency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Product Charge Entity
 * Defines charges/fees associated with a product
 */
@Entity
@Table(name = "product_charges")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCharge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotBlank(message = "Charge name is required")
    @Column(nullable = false, length = 200)
    private String chargeName;

    @NotBlank(message = "Charge type is required")
    @Column(nullable = false, length = 50)
    private String chargeType;  // FEE, TAX, PENALTY, INTEREST

    @Column(length = 500)
    private String description;

    // Either fixed amount OR percentage, not both
    @Column(precision = 19, scale = 2)
    private BigDecimal fixedAmount;

    @Column(precision = 5, scale = 2)
    private BigDecimal percentageRate;

    @NotNull(message = "Frequency is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ChargeFrequency frequency;

    @Column(length = 200)
    private String applicableTransactionTypes;  // Comma-separated transaction types

    @Column
    @Builder.Default
    private Boolean waivable = false;

    @Column(precision = 19, scale = 2)
    private BigDecimal minCharge;

    @Column(precision = 19, scale = 2)
    private BigDecimal maxCharge;

    @Column
    @Builder.Default
    private Boolean active = true;
}
