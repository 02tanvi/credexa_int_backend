package com.app.product.dto;

import com.app.product.enums.RoleType;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for product role configuration
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRoleRequest {

    @NotNull(message = "Role type is required")
    private RoleType roleType;

    private Boolean mandatory;
    private Integer minCount;
    private Integer maxCount;
    private String description;
}
