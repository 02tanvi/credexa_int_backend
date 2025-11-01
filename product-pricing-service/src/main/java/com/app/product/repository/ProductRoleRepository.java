package com.app.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.app.product.entity.ProductRole;
import com.app.product.enums.RoleType;

@Repository
public interface ProductRoleRepository extends JpaRepository<ProductRole, Long> {

    /**
     * Find all roles for a product
     */
    List<ProductRole> findByProductId(Long productId);

    /**
     * Find a specific role type for a product
     */
    Optional<ProductRole> findByProductIdAndRoleType(Long productId, RoleType roleType);

    /**
     * Find all mandatory roles for a product
     */
    @Query("SELECT r FROM ProductRole r WHERE r.product.id = :productId AND r.mandatory = true")
    List<ProductRole> findMandatoryRolesByProductId(@Param("productId") Long productId);
}
