package com.app.customer.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.app.customer.entity.Customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for customer response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {

    private Long id;
    private Long userId;
    private String username;
    private String fullName;
    private String mobileNumber;
    private String email;
    private String panNumber;
    private String aadharNumber;
    private LocalDate dateOfBirth;
    private Customer.Gender gender;
    private Customer.CustomerClassification classification;
    private Customer.KycStatus kycStatus;

    // Address details
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;
    private String country;

    // Financial details
    private Boolean isActive;
    private String accountNumber;
    private String ifscCode;

    // Preferences
    private String preferredLanguage;
    private String preferredCurrency;

    // Communication preferences
    private Boolean emailNotifications;
    private Boolean smsNotifications;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert Customer entity to CustomerResponse DTO
     */
    public static CustomerResponse fromEntity(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .userId(customer.getUserId())
                .username(customer.getUsername())
                .fullName(customer.getFullName())
                .mobileNumber(customer.getMobileNumber())
                .email(customer.getEmail())
                .panNumber(customer.getPanNumber())
                .aadharNumber(customer.getAadharNumber())
                .dateOfBirth(customer.getDateOfBirth())
                .gender(customer.getGender())
                .classification(customer.getClassification())
                .kycStatus(customer.getKycStatus())
                .addressLine1(customer.getAddressLine1())
                .addressLine2(customer.getAddressLine2())
                .city(customer.getCity())
                .state(customer.getState())
                .pincode(customer.getPincode())
                .country(customer.getCountry())
                .isActive(customer.getIsActive())
                .accountNumber(customer.getAccountNumber())
                .ifscCode(customer.getIfscCode())
                .preferredLanguage(customer.getPreferredLanguage())
                .preferredCurrency(customer.getPreferredCurrency())
                .emailNotifications(customer.getEmailNotifications())
                .smsNotifications(customer.getSmsNotifications())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}
