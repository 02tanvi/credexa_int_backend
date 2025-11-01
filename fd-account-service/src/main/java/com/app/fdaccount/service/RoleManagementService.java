package com.app.fdaccount.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.fdaccount.dto.AccountRoleRequest;
import com.app.fdaccount.dto.RoleResponse;
import com.app.fdaccount.dto.external.CustomerDto;
import com.app.fdaccount.entity.AccountRole;
import com.app.fdaccount.entity.FdAccount;
import com.app.fdaccount.repository.AccountRoleRepository;
import com.app.fdaccount.repository.FdAccountRepository;
import com.app.fdaccount.service.integration.CustomerServiceClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing account roles (owners, co-owners, nominees, etc.)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleManagementService {

    private final FdAccountRepository accountRepository;
    private final AccountRoleRepository roleRepository;
    private final CustomerServiceClient customerServiceClient;

    /**
     * Add role to an account
     */
    @Transactional
    public RoleResponse addRole(String accountNumber, AccountRoleRequest request) {
        log.info("Adding role {} for customer {} to account {}", 
                request.getRoleType(), request.getCustomerId(), accountNumber);

        // 1. Find account
        FdAccount account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

        // 2. Validate customer exists
        CustomerDto customer = customerServiceClient.getCustomerById(request.getCustomerId());

        // 3. Check if customer already has a role on this account
        boolean alreadyExists = roleRepository.isCustomerAssociatedWithAccount(
                account.getId(), request.getCustomerId());
        
        if (alreadyExists) {
            throw new IllegalStateException(
                    String.format("Customer %d already has a role on account %s", 
                            request.getCustomerId(), accountNumber));
        }

        // 4. Validate ownership percentage
        validateOwnershipPercentage(account, request);

        // 5. Create role
        AccountRole role = AccountRole.builder()
                .customerId(request.getCustomerId())
                .customerName(request.getCustomerName() != null ? 
                        request.getCustomerName() : customer.getCustomerName())
                .roleType(request.getRoleType())
                .ownershipPercentage(request.getOwnershipPercentage())
                .isPrimary(request.getIsPrimary())
                .isActive(true)
                .remarks(request.getRemarks())
                .build();

        account.addRole(role);

        // 6. If this is set as primary, unset other primary roles
        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            account.getRoles().forEach(r -> {
                if (!r.equals(role) && Boolean.TRUE.equals(r.getIsPrimary())) {
                    r.setIsPrimary(false);
                    log.info("Unset primary flag from role: {}", r.getId());
                }
            });
        }

        // 7. Save
        FdAccount savedAccount = accountRepository.save(account);
        AccountRole savedRole = savedAccount.getRoles().stream()
                .filter(r -> r.getCustomerId().equals(request.getCustomerId()))
                .findFirst()
                .orElseThrow();

        log.info("✅ Added role: {} for customer {} on account {}", 
                savedRole.getRoleType(), savedRole.getCustomerId(), accountNumber);

        return mapToRoleResponse(savedRole);
    }

    /**
     * Update role
     */
    @Transactional
    public RoleResponse updateRole(Long roleId, AccountRoleRequest request) {
        log.info("Updating role: {}", roleId);

        // 1. Find role
        AccountRole role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));

        FdAccount account = role.getAccount();

        // 2. Validate ownership percentage if changed
        if (request.getOwnershipPercentage() != null && 
            !request.getOwnershipPercentage().equals(role.getOwnershipPercentage())) {
            validateOwnershipPercentageUpdate(account, role, request.getOwnershipPercentage());
        }

        // 3. Update fields
        if (request.getOwnershipPercentage() != null) {
            role.setOwnershipPercentage(request.getOwnershipPercentage());
        }
        if (request.getIsPrimary() != null) {
            role.setIsPrimary(request.getIsPrimary());
            
            // If setting as primary, unset others
            if (Boolean.TRUE.equals(request.getIsPrimary())) {
                account.getRoles().forEach(r -> {
                    if (!r.getId().equals(roleId) && Boolean.TRUE.equals(r.getIsPrimary())) {
                        r.setIsPrimary(false);
                    }
                });
            }
        }
        if (request.getIsActive() != null) {
            role.setIsActive(request.getIsActive());
        }
        if (request.getRemarks() != null) {
            role.setRemarks(request.getRemarks());
        }

        // 4. Save
        AccountRole savedRole = roleRepository.save(role);

        log.info("✅ Updated role: {}", roleId);

        return mapToRoleResponse(savedRole);
    }

    /**
     * Remove role (soft delete - set inactive)
     */
    @Transactional
    public void removeRole(Long roleId) {
        log.info("Removing role: {}", roleId);

        // 1. Find role
        AccountRole role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleId));

        FdAccount account = role.getAccount();

        // 2. Check if it's the only active owner
        long activeOwnerCount = account.getRoles().stream()
                .filter(r -> r.getIsActive() && 
                        (r.getRoleType() == com.app.fdaccount.enums.RoleType.OWNER || 
                         r.getRoleType() == com.app.fdaccount.enums.RoleType.CO_OWNER))
                .count();

        if (activeOwnerCount <= 1 && Boolean.TRUE.equals(role.getIsActive()) &&
            (role.getRoleType() == com.app.fdaccount.enums.RoleType.OWNER || 
             role.getRoleType() == com.app.fdaccount.enums.RoleType.CO_OWNER)) {
            throw new IllegalStateException("Cannot remove the only active owner from account");
        }

        // 3. Set inactive
        role.setIsActive(false);
        roleRepository.save(role);

        log.info("✅ Removed role: {}", roleId);
    }

    /**
     * Get all roles for an account
     */
    @Transactional(readOnly = true)
    public List<RoleResponse> getAccountRoles(String accountNumber) {
        FdAccount account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

        return roleRepository.findByAccountId(account.getId()).stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get active roles for an account
     */
    @Transactional(readOnly = true)
    public List<RoleResponse> getActiveAccountRoles(String accountNumber) {
        FdAccount account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

        return roleRepository.findActiveRolesByAccountId(account.getId()).stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get roles for a customer
     */
    @Transactional(readOnly = true)
    public List<RoleResponse> getCustomerRoles(Long customerId) {
        return roleRepository.findByCustomerIdAndIsActive(customerId, true).stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Validate ownership percentage
     */
    private void validateOwnershipPercentage(FdAccount account, AccountRoleRequest request) {
        if (request.getOwnershipPercentage() == null) {
            return; // Not required for all role types
        }

        // Calculate total ownership percentage
        BigDecimal totalPercentage = account.getRoles().stream()
                .filter(AccountRole::getIsActive)
                .map(AccountRole::getOwnershipPercentage)
                .filter(p -> p != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal newTotal = totalPercentage.add(request.getOwnershipPercentage());

        if (newTotal.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException(
                    String.format("Total ownership percentage would exceed 100%% (current: %.2f%%, adding: %.2f%%)", 
                            totalPercentage, request.getOwnershipPercentage()));
        }
    }

    /**
     * Validate ownership percentage update
     */
    private void validateOwnershipPercentageUpdate(FdAccount account, AccountRole roleToUpdate, BigDecimal newPercentage) {
        // Calculate total ownership excluding the role being updated
        BigDecimal totalPercentage = account.getRoles().stream()
                .filter(AccountRole::getIsActive)
                .filter(r -> !r.getId().equals(roleToUpdate.getId()))
                .map(AccountRole::getOwnershipPercentage)
                .filter(p -> p != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal newTotal = totalPercentage.add(newPercentage);

        if (newTotal.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException(
                    String.format("Total ownership percentage would exceed 100%% (other roles: %.2f%%, new: %.2f%%)", 
                            totalPercentage, newPercentage));
        }
    }

    /**
     * Map entity to response DTO
     */
    private RoleResponse mapToRoleResponse(AccountRole role) {
        return RoleResponse.builder()
                .id(role.getId())
                .customerId(role.getCustomerId())
                .customerName(role.getCustomerName())
                .roleType(role.getRoleType())
                .ownershipPercentage(role.getOwnershipPercentage())
                .isPrimary(role.getIsPrimary())
                .isActive(role.getIsActive())
                .remarks(role.getRemarks())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
}
