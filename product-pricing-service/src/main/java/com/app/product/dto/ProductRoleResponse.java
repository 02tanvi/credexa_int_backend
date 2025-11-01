package com.app.product.dto;

import com.app.product.enums.RoleType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRoleResponse {
    
    private Long id;
    private RoleType roleType;
    private Boolean mandatory;
    private Integer minCount;
    private Integer maxCount;
    private String description;
}
