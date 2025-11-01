package com.app.fdaccount.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.app.fdaccount.enums.AccountStatus;
import com.app.fdaccount.enums.MaturityInstruction;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FD Account Entity
 * Represents a Fixed Deposit account with all its attributes
 */
@Entity
@Table(name = "fd_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FdAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Account Identification
    @Column(unique = true, nullable = false, length = 20)
    private String accountNumber;

    @Column(unique = true, length = 34)
    private String ibanNumber;

    @Column(nullable = false, length = 100)
    private String accountName;

    // Product Information
    @Column(nullable = false, length = 50)
    private String productCode;

    @Column(length = 100)
    private String productName;

    // Account Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    // Financial Details
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal principalAmount;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(precision = 5, scale = 2)
    private BigDecimal customInterestRate;

    @Column(nullable = false)
    private Integer termMonths;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal maturityAmount;

    // Dates
    @Column(nullable = false)
    private LocalDate effectiveDate;

    @Column(nullable = false)
    private LocalDate maturityDate;

    @Column
    private LocalDate closureDate;

    // Account Settings
    @Column(length = 20)
    private String interestCalculationMethod; // SIMPLE, COMPOUND

    @Column(length = 20)
    private String interestPayoutFrequency; // MONTHLY, QUARTERLY, MATURITY

    @Column(nullable = false)
    private Boolean autoRenewal;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private MaturityInstruction maturityInstruction;

    @Column(length = 50)
    private String maturityTransferAccount; // Account to transfer on maturity

    // Branch/Location
    @Column(length = 20)
    private String branchCode;

    @Column(length = 100)
    private String branchName;

    // TDS
    @Column(nullable = false)
    private Boolean tdsApplicable;

    @Column(precision = 5, scale = 2)
    private BigDecimal tdsRate;

    // Remarks
    @Column(length = 500)
    private String remarks;

    // Audit Fields
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(length = 100)
    private String createdBy;

    @Column(length = 100)
    private String updatedBy;

    // Relationships
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AccountRole> roles = new ArrayList<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AccountTransaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AccountBalance> balances = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (autoRenewal == null) {
            autoRenewal = false;
        }
        if (tdsApplicable == null) {
            tdsApplicable = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public void addRole(AccountRole role) {
        roles.add(role);
        role.setAccount(this);
    }

    public void removeRole(AccountRole role) {
        roles.remove(role);
        role.setAccount(null);
    }

    public void addTransaction(AccountTransaction transaction) {
        transactions.add(transaction);
        transaction.setAccount(this);
    }

    public void addBalance(AccountBalance balance) {
        balances.add(balance);
        balance.setAccount(this);
    }
}
