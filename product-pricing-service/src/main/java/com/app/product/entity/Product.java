package com.app.product.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.app.product.enums.ProductStatus;
import com.app.product.enums.ProductType;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Main Product Entity with Complete Validation
 */
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==================== BASIC DETAILS ====================
    
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 200, message = "Product name must be between 3 and 200 characters")
    @Column(nullable = false, length = 200)
    private String productName;
    
    @NotBlank(message = "Product code is required")
    @Size(min = 3, max = 50, message = "Product code must be between 3 and 50 characters")
    @Pattern(regexp = "^FD-[A-Z]+-\\d{3}$", message = "Product code must follow format: FD-XXX-001")
    @Column(nullable = false, unique = true, length = 50)
    private String productCode;
    
    @NotNull(message = "Product type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProductType productType;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(length = 1000)
    private String description;
    
    @NotNull(message = "Effective date is required")
    @Column(nullable = false)
    private LocalDate effectiveDate;
    
    @Column
    private LocalDate endDate;
    
    @NotBlank(message = "Bank/Branch code is required")
    @Size(min = 2, max = 50, message = "Bank/Branch code must be between 2 and 50 characters")
    @Column(nullable = false, length = 50)
    private String bankBranchCode;
    
    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be 3 uppercase letters (e.g., INR, USD)")
    @Column(nullable = false, length = 3)
    private String currencyCode;
    
    @NotNull(message = "Product status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProductStatus status = ProductStatus.DRAFT;
    
    // ==================== TERM-RELATED RULES ====================
    
    @DecimalMin(value = "0.01", message = "Minimum term must be greater than 0")
    @DecimalMax(value = "1200.00", message = "Minimum term cannot exceed 1200 months")
    @Digits(integer = 8, fraction = 2, message = "Invalid term format")
    @Column(precision = 10, scale = 2)
    private BigDecimal minTermMonths;
    
    @DecimalMin(value = "0.01", message = "Maximum term must be greater than 0")
    @DecimalMax(value = "1200.00", message = "Maximum term cannot exceed 1200 months")
    @Digits(integer = 8, fraction = 2, message = "Invalid term format")
    @Column(precision = 10, scale = 2)
    private BigDecimal maxTermMonths;
    
    // ==================== AMOUNT-RELATED RULES ====================
    
    @NotNull(message = "Minimum amount is required")
    @DecimalMin(value = "0.01", message = "Minimum amount must be greater than 0")
    @Digits(integer = 17, fraction = 2, message = "Invalid amount format")
    @Column(precision = 19, scale = 2)
    private BigDecimal minAmount;
    
    @DecimalMin(value = "0.01", message = "Maximum amount must be greater than 0")
    @Digits(integer = 17, fraction = 2, message = "Invalid amount format")
    @Column(precision = 19, scale = 2)
    private BigDecimal maxAmount;
    
    @DecimalMin(value = "0.00", message = "Minimum balance cannot be negative")
    @Digits(integer = 17, fraction = 2, message = "Invalid amount format")
    @Column(precision = 19, scale = 2)
    private BigDecimal minBalanceRequired;
    
    // ==================== INTEREST/RATE RULES ====================
    
    @DecimalMin(value = "0.00", message = "Base interest rate cannot be negative")
    @DecimalMax(value = "100.00", message = "Base interest rate cannot exceed 100%")
    @Digits(integer = 3, fraction = 2, message = "Invalid interest rate format (max 2 decimal places)")
    @Column(precision = 5, scale = 2)
    private BigDecimal baseInterestRate;
    
    @Size(max = 50, message = "Interest calculation method cannot exceed 50 characters")
    @Column(length = 50)
    private String interestCalculationMethod;
    
    @Size(max = 50, message = "Interest payout frequency cannot exceed 50 characters")
    @Column(length = 50)
    private String interestPayoutFrequency;
    
    // ==================== FLAGS AND BOOLEAN RULES ====================
    
    @Column
    @Builder.Default
    private Boolean prematureWithdrawalAllowed = false;
    
    @Column
    @Builder.Default
    private Boolean partialWithdrawalAllowed = false;
    
    @Column
    @Builder.Default
    private Boolean loanAgainstDepositAllowed = false;
    
    @Column
    @Builder.Default
    private Boolean autoRenewalAllowed = false;
    
    @Column
    @Builder.Default
    private Boolean nomineeAllowed = true;
    
    @Column
    @Builder.Default
    private Boolean jointAccountAllowed = true;
    
    // ==================== TAX-RELATED ====================
    
    @DecimalMin(value = "0.00", message = "TDS rate cannot be negative")
    @DecimalMax(value = "100.00", message = "TDS rate cannot exceed 100%")
    @Digits(integer = 3, fraction = 2, message = "Invalid TDS rate format")
    @Column(precision = 5, scale = 2)
    private BigDecimal tdsRate;
    
    @Column
    @Builder.Default
    private Boolean tdsApplicable = true;
    
    // ==================== RELATIONSHIPS ====================
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductRole> allowedRoles = new ArrayList<>();
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductCharge> charges = new ArrayList<>();
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InterestRateMatrix> interestRateMatrix = new ArrayList<>();
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductTransactionType> transactionTypes = new ArrayList<>();
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductBalanceType> balanceTypes = new ArrayList<>();
    
    // ==================== AUDIT FIELDS ====================
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @Column(length = 100)
    private String createdBy;
    
    @Column(length = 100)
    private String updatedBy;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // ==================== CROSS-FIELD VALIDATIONS ====================
    
    /**
     * Validate that max amount is greater than min amount
     */
    @AssertTrue(message = "Maximum amount must be greater than minimum amount")
    public boolean isMaxAmountValid() {
        if (maxAmount == null || minAmount == null) {
            return true; // Skip validation if either is null
        }
        return maxAmount.compareTo(minAmount) > 0;
    }
    
    /**
     * Validate that max term is greater than min term
     */
    @AssertTrue(message = "Maximum term must be greater than minimum term")
    public boolean isMaxTermValid() {
        if (maxTermMonths == null || minTermMonths == null) {
            return true;
        }
        return maxTermMonths.compareTo(minTermMonths) > 0;
    }
    
    /**
     * Validate that end date is after effective date
     */
    @AssertTrue(message = "End date must be after effective date")
    public boolean isEndDateValid() {
        if (endDate == null || effectiveDate == null) {
            return true;
        }
        return endDate.isAfter(effectiveDate);
    }
    
    /**
     * Validate that if TDS is applicable, TDS rate must be provided
     */
    @AssertTrue(message = "TDS rate is required when TDS is applicable")
    public boolean isTdsRateValid() {
        if (tdsApplicable == null || !tdsApplicable) {
            return true; // TDS not applicable, skip validation
        }
        return tdsRate != null && tdsRate.compareTo(BigDecimal.ZERO) > 0;
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Check if product is active on a given date
     */
    public boolean isActiveOn(LocalDate date) {
        if (status != ProductStatus.ACTIVE) {
            return false;
        }
        if (effectiveDate.isAfter(date)) {
            return false;
        }
        if (endDate != null && endDate.isBefore(date)) {
            return false;
        }
        return true;
    }
    
    /**
     * Check if product is currently active
     */
    public boolean isCurrentlyActive() {
        return isActiveOn(LocalDate.now());
    }
    
    /**
     * Add allowed role to product
     */
    public void addRole(ProductRole role) {
        allowedRoles.add(role);
        role.setProduct(this);
    }
    
    /**
     * Add charge to product
     */
    public void addCharge(ProductCharge charge) {
        charges.add(charge);
        charge.setProduct(this);
    }
    
    /**
     * Add interest rate slab to matrix
     */
    public void addInterestRateSlab(InterestRateMatrix slab) {
        interestRateMatrix.add(slab);
        slab.setProduct(this);
    }
}