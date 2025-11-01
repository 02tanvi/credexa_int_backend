package com.app.customer.dto;

import java.time.LocalDate;

import com.app.customer.entity.Customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating customer information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCustomerRequest {

    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be 10 digits")
    private String mobileNumber;

    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]$", message = "Invalid PAN format")
    private String panNumber;

    @Pattern(regexp = "^[0-9]{12}$", message = "Aadhar number must be 12 digits")
    private String aadharNumber;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private Customer.Gender gender;

    private Customer.CustomerClassification classification;

    private Customer.KycStatus kycStatus;

    // Address details
    @Size(max = 255)
    private String addressLine1;

    @Size(max = 255)
    private String addressLine2;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be 6 digits")
    private String pincode;

    @Size(max = 100)
    private String country;

    // Financial details
    private Boolean isActive;

    @Size(max = 50)
    private String accountNumber;

    @Size(max = 20)
    private String ifscCode;

    // Preferences
    @Size(max = 10)
    private String preferredLanguage;

    @Size(max = 10)
    private String preferredCurrency;

    // Communication preferences
    private Boolean emailNotifications;
    private Boolean smsNotifications;
}
