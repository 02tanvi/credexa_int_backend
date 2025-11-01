package com.app.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.app.product.entity.Product;
import com.app.product.entity.ProductTransactionType;
import com.app.product.enums.TransactionType;

@Repository
public interface ProductTransactionTypeRepository extends JpaRepository<ProductTransactionType, Long> {

    /**
     * Find all transaction types for a product
     */
    List<ProductTransactionType> findByProductId(Long productId);

    /**
     * Find a specific transaction type for a product
     */
    Optional<ProductTransactionType> findByProductIdAndTransactionType(
        Long productId, 
        TransactionType transactionType
    );

    /**
     * Find all allowed transaction types for a product
     */
    @Query("SELECT t FROM ProductTransactionType t WHERE t.product.id = :productId AND t.allowed = true")
    List<ProductTransactionType> findAllowedTransactionsByProductId(@Param("productId") Long productId);
    
    long countByProduct(Product product);
}
