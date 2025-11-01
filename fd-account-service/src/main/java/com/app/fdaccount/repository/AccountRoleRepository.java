package com.app.fdaccount.repository;

import com.app.fdaccount.entity.AccountRole;
import com.app.fdaccount.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for AccountRole entity
 */
@Repository
public interface AccountRoleRepository extends JpaRepository<AccountRole, Long> {

    /**
     * Find all roles for an account
     */
    @Query("SELECT r FROM AccountRole r WHERE r.account.id = :accountId")
    List<AccountRole> findByAccountId(@Param("accountId") Long accountId);

    /**
     * Find active roles for an account
     */
    @Query("SELECT r FROM AccountRole r WHERE r.account.id = :accountId AND r.isActive = true")
    List<AccountRole> findActiveRolesByAccountId(@Param("accountId") Long accountId);

    /**
     * Find roles by customer ID
     */
    List<AccountRole> findByCustomerId(Long customerId);

    /**
     * Find active roles by customer ID
     */
    List<AccountRole> findByCustomerIdAndIsActive(Long customerId, Boolean isActive);

    /**
     * Find roles by role type
     */
    List<AccountRole> findByRoleType(RoleType roleType);

    /**
     * Find primary owner for an account
     */
    @Query("SELECT r FROM AccountRole r WHERE r.account.id = :accountId AND r.isPrimary = true AND r.isActive = true")
    Optional<AccountRole> findPrimaryOwnerByAccountId(@Param("accountId") Long accountId);

    /**
     * Find roles by account and role type
     */
    @Query("SELECT r FROM AccountRole r WHERE r.account.id = :accountId AND r.roleType = :roleType AND r.isActive = true")
    List<AccountRole> findByAccountIdAndRoleType(@Param("accountId") Long accountId, 
                                                   @Param("roleType") RoleType roleType);

    /**
     * Check if customer is associated with account
     */
    @Query("SELECT COUNT(r) > 0 FROM AccountRole r WHERE r.account.id = :accountId AND r.customerId = :customerId AND r.isActive = true")
    boolean isCustomerAssociatedWithAccount(@Param("accountId") Long accountId, 
                                            @Param("customerId") Long customerId);

    /**
     * Count active roles for an account
     */
    @Query("SELECT COUNT(r) FROM AccountRole r WHERE r.account.id = :accountId AND r.isActive = true")
    long countActiveRolesByAccountId(@Param("accountId") Long accountId);
}
