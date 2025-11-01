package com.app.product.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.app.product.entity.ProductCharge;

@Repository
public interface ProductChargeRepository extends JpaRepository<ProductCharge, Long> {

    /**
     * Find all charges for a product
     */
    List<ProductCharge> findByProductId(Long productId);

    /**
     * Find active charges for a product
     */
    @Query("SELECT c FROM ProductCharge c WHERE c.product.id = :productId AND c.active = true")
    List<ProductCharge> findActiveChargesByProductId(@Param("productId") Long productId);

    /**
     * Find charges by type for a product
     */
    @Query("SELECT c FROM ProductCharge c WHERE c.product.id = :productId AND c.chargeType = :chargeType")
    List<ProductCharge> findByProductIdAndChargeType(
        @Param("productId") Long productId,
        @Param("chargeType") String chargeType
    );
}
