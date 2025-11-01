package com.app.product.entity;

import com.app.product.enums.TransactionType;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Product Transaction Type Entity
 * Defines allowed transaction types for a product
 */
@Entity
@Table(name = "product_transaction_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductTransactionType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TransactionType transactionType;

    @Column
    @Builder.Default
    private Boolean allowed = true;

    @Column
    @Builder.Default
    private Boolean requiresApproval = false;

    @Column(length = 500)
    private String description;

    @Column(length = 200)
    private String validationRules;  // JSON or comma-separated rules
}
