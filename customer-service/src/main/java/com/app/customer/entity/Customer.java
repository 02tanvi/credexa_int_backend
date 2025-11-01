package com.app.customer.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Customer entity representing customer information
 */
@Entity
@Table(name = "customers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId; // Reference to user from login-service

    @Column(nullable = false, unique = true, length = 50)
    private String username; // Username from JWT token for security validation

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 15)
    private String mobileNumber;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 20)
    private String panNumber;

    @Column(length = 20)
    private String aadharNumber;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CustomerClassification classification;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private KycStatus kycStatus;

    // Address details
    @Column(length = 255)
    private String addressLine1;

    @Column(length = 255)
    private String addressLine2;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 10)
    private String pincode;

    @Column(length = 100)
    private String country;

    // Financial details
    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(length = 50)
    private String accountNumber; // Bank account for FD operations

    @Column(length = 20)
    private String ifscCode;

    // Preferences
    @Builder.Default
    @Column(length = 10)
    private String preferredLanguage = "en";

    @Builder.Default
    @Column(length = 10)
    private String preferredCurrency = "INR";

    // Communication preferences
    @Builder.Default
    @Column(nullable = false)
    private Boolean emailNotifications = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean smsNotifications = true;

    // Metadata
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private Long createdBy;
    private Long updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Gender enum
     */
    public enum Gender {
        MALE, FEMALE, OTHER
    }

    /**
     * Customer Classification for FD rate determination
     */
    public enum CustomerClassification {
        REGULAR,    // Standard interest rates
        PREMIUM,    // 0.25% additional
        VIP,        // 0.50% additional
        SENIOR_CITIZEN, // Senior citizen benefits (0.50% additional)
        SUPER_SENIOR    // Super senior (80+) benefits (0.75% additional)
    }

    /**
     * KYC Status
     */
    public enum KycStatus {
        PENDING,
        IN_PROGRESS,
        VERIFIED,
        REJECTED,
        EXPIRED
    }
}
