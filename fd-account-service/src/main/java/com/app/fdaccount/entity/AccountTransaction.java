package com.app.fdaccount.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.app.fdaccount.enums.TransactionType;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Account Transaction Entity
 * Represents all transactions on an FD account
 */
@Entity
@Table(name = "account_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private FdAccount account;

    @Column(unique = true, nullable = false, length = 50)
    private String transactionReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionType transactionType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @Column(nullable = false)
    private LocalDate valueDate;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String performedBy;

    // Balances after transaction
    @Column(precision = 19, scale = 2)
    private BigDecimal principalBalanceAfter;

    @Column(precision = 19, scale = 2)
    private BigDecimal interestBalanceAfter;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalBalanceAfter;

    // Reversal information
    @Column(nullable = false)
    @Builder.Default
    private Boolean isReversed = false;

    @Column
    private Long reversalTransactionId;

    @Column
    private LocalDateTime reversalDate;

    @Column(length = 500)
    private String reversalReason;

    // Related transaction (for reversals, renewals, etc.)
    @Column
    private Long relatedTransactionId;

    // Audit
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
