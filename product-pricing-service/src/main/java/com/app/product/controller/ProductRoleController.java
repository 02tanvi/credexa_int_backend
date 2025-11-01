package com.app.product.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.product.dto.ApiResponse;
import com.app.product.dto.ProductRoleRequest;
import com.app.product.dto.ProductRoleResponse;
import com.app.product.service.ProductRoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/products/{productId}/roles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Roles", description = "APIs for managing product role configurations")
public class ProductRoleController {

    private final ProductRoleService roleService;

    @PostMapping
    @Operation(summary = "Add role to product", description = "Adds a new allowed role configuration to a product")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Role added successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ApiResponse<ProductRoleResponse>> addRole(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Valid @RequestBody ProductRoleRequest request) {
        
        log.info("REST: Adding role to product {}", productId);
        ProductRoleResponse response = roleService.addRole(productId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Role added successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all roles for product", description = "Retrieves all role configurations for a product")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Roles retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<ProductRoleResponse>>> getRolesByProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        
        log.info("REST: Fetching roles for product {}", productId);
        List<ProductRoleResponse> response = roleService.getRolesByProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/type/{roleType}")
    @Operation(summary = "Get roles by type", description = "Retrieves role configurations of a specific type")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Roles retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<ProductRoleResponse>>> getRolesByType(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Role Type") @PathVariable String roleType) {
        
        log.info("REST: Fetching roles of type {} for product {}", roleType, productId);
        List<ProductRoleResponse> response = roleService.getRolesByType(productId, roleType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Roles Management", description = "Direct role management APIs")
class RoleManagementController {

    private final ProductRoleService roleService;

    @GetMapping("/{roleId}")
    @Operation(summary = "Get role by ID", description = "Retrieves a specific role configuration by its ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Role retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Role not found")
    })
    public ResponseEntity<ApiResponse<ProductRoleResponse>> getRoleById(
            @Parameter(description = "Role ID") @PathVariable Long roleId) {
        
        log.info("REST: Fetching role {}", roleId);
        ProductRoleResponse response = roleService.getRoleById(roleId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{roleId}")
    @Operation(summary = "Update role", description = "Updates an existing role configuration")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Role updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Role not found")
    })
    public ResponseEntity<ApiResponse<ProductRoleResponse>> updateRole(
            @Parameter(description = "Role ID") @PathVariable Long roleId,
            @Valid @RequestBody ProductRoleRequest request) {
        
        log.info("REST: Updating role {}", roleId);
        ProductRoleResponse response = roleService.updateRole(roleId, request);
        return ResponseEntity.ok(ApiResponse.success("Role updated successfully", response));
    }

    @DeleteMapping("/{roleId}")
    @Operation(summary = "Delete role", description = "Deletes a role configuration from a product")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Role deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Role not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteRole(
            @Parameter(description = "Role ID") @PathVariable Long roleId) {
        
        log.info("REST: Deleting role {}", roleId);
        roleService.deleteRole(roleId);
        return ResponseEntity.ok(ApiResponse.success("Role deleted successfully", null));
    }
}
