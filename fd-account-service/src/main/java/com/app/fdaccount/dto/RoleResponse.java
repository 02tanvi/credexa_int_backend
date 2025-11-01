package com.app.fdaccount.dto;

import com.app.fdaccount.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for account role details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private RoleType roleType;
    private BigDecimal ownershipPercentage;
    private Boolean isPrimary;
    private Boolean isActive;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
