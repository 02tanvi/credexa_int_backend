package com.app.product.entity;

import com.app.product.enums.BalanceType;
import com.app.product.enums.TransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Transaction to Balance Relationship Entity
 * Maps which transactions affect which balance types
 */
@Entity
@Table(name = "transaction_balance_relationships")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionBalanceRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BalanceType balanceType;

    @Column(nullable = false, length = 20)
    private String impactType;  // DEBIT, CREDIT, NO_IMPACT

    @Column(length = 500)
    private String description;

    @Column
    @Builder.Default
    private Boolean active = true;
}
