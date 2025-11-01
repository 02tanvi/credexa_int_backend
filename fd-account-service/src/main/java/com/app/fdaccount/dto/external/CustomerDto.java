package com.app.fdaccount.dto.external;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for customer data from customer-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {

    private Long customerId;
    private String customerName;
    private String email;
    private String phone;
    private String panNumber;
    private String aadhaarNumber;
    private LocalDate dateOfBirth;
    private String customerType;
    private String customerClassification;
    private Boolean isActive;
    private String kycStatus;
}
