package com.app.fdaccount.controller;

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

import com.app.fdaccount.dto.AccountRoleRequest;
import com.app.fdaccount.dto.RoleResponse;
import com.app.fdaccount.service.RoleManagementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for Role Management operations
 */
@Slf4j
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "APIs for managing account roles (owners, nominees, etc.)")
public class RoleController {

    private final RoleManagementService roleManagementService;

    /**
     * Add a role to an account
     */
    @PostMapping("/account/{accountNumber}")
    @Operation(summary = "Add Role",
               description = "Add a customer role (owner, co-owner, nominee, etc.) to an FD account")
    public ResponseEntity<RoleResponse> addRole(
            @PathVariable String accountNumber,
            @Valid @RequestBody AccountRoleRequest request) {
        
        log.info("REST: Adding role {} for customer {} to account {}", 
                request.getRoleType(), request.getCustomerId(), accountNumber);
        RoleResponse response = roleManagementService.addRole(accountNumber, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update a role
     */
    @PutMapping("/{roleId}")
    @Operation(summary = "Update Role",
               description = "Update an existing role on an FD account")
    public ResponseEntity<RoleResponse> updateRole(
            @PathVariable Long roleId,
            @Valid @RequestBody AccountRoleRequest request) {
        
        log.info("REST: Updating role: {}", roleId);
        RoleResponse response = roleManagementService.updateRole(roleId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Remove a role (soft delete)
     */
    @DeleteMapping("/{roleId}")
    @Operation(summary = "Remove Role",
               description = "Remove (deactivate) a role from an FD account")
    public ResponseEntity<Void> removeRole(@PathVariable Long roleId) {
        log.info("REST: Removing role: {}", roleId);
        roleManagementService.removeRole(roleId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all roles for an account
     */
    @GetMapping("/account/{accountNumber}")
    @Operation(summary = "Get Account Roles",
               description = "Get all roles (including inactive) for a specific account")
    public ResponseEntity<List<RoleResponse>> getAccountRoles(
            @PathVariable String accountNumber) {
        
        log.info("REST: Fetching all roles for account: {}", accountNumber);
        List<RoleResponse> response = roleManagementService.getAccountRoles(accountNumber);
        return ResponseEntity.ok(response);
    }

    /**
     * Get active roles for an account
     */
    @GetMapping("/account/{accountNumber}/active")
    @Operation(summary = "Get Active Account Roles",
               description = "Get only active roles for a specific account")
    public ResponseEntity<List<RoleResponse>> getActiveAccountRoles(
            @PathVariable String accountNumber) {
        
        log.info("REST: Fetching active roles for account: {}", accountNumber);
        List<RoleResponse> response = roleManagementService.getActiveAccountRoles(accountNumber);
        return ResponseEntity.ok(response);
    }

    /**
     * Get roles for a customer
     */
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get Customer Roles",
               description = "Get all active roles for a specific customer across all accounts")
    public ResponseEntity<List<RoleResponse>> getCustomerRoles(
            @Parameter(description = "Customer ID")
            @PathVariable Long customerId) {
        
        log.info("REST: Fetching roles for customer: {}", customerId);
        List<RoleResponse> response = roleManagementService.getCustomerRoles(customerId);
        return ResponseEntity.ok(response);
    }
}
