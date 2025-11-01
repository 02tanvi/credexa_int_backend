package com.app.fdaccount.dto;

import com.app.fdaccount.enums.RoleType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for adding a role (customer) to an account
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRoleRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotBlank(message = "Customer name is required")
    @Size(max = 100, message = "Customer name cannot exceed 100 characters")
    private String customerName;

    @NotNull(message = "Role type is required")
    private RoleType roleType;

    @DecimalMin(value = "0.00", message = "Ownership percentage cannot be negative")
    @DecimalMax(value = "100.00", message = "Ownership percentage cannot exceed 100%")
    @Digits(integer = 3, fraction = 2, message = "Invalid ownership percentage format")
    private BigDecimal ownershipPercentage;

    @Builder.Default
    private Boolean isPrimary = false;

    @Builder.Default
    private Boolean isActive = true;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;
}
