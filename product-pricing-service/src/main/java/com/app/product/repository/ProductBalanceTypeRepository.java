package com.app.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.app.product.entity.Product;
import com.app.product.entity.ProductBalanceType;
import com.app.product.enums.BalanceType;

@Repository
public interface ProductBalanceTypeRepository extends JpaRepository<ProductBalanceType, Long> {

    /**
     * Find all balance types for a product
     */
    List<ProductBalanceType> findByProductId(Long productId);

    /**
     * Find a specific balance type for a product
     */
    Optional<ProductBalanceType> findByProductIdAndBalanceType(
        Long productId, 
        BalanceType balanceType
    );

    /**
     * Find all tracked balance types for a product
     */
    @Query("SELECT b FROM ProductBalanceType b WHERE b.product.id = :productId AND b.tracked = true")
    List<ProductBalanceType> findTrackedBalancesByProductId(@Param("productId") Long productId);
    long countByProduct(Product product);
}
