package com.app.product.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.app.product.entity.Product;
import com.app.product.enums.ProductStatus;
import com.app.product.enums.ProductType;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find product by unique product code
     */
    Optional<Product> findByProductCode(String productCode);

    /**
     * Check if product code already exists
     */
    boolean existsByProductCode(String productCode);

    /**
     * Find all products by type
     */
    List<Product> findByProductType(ProductType productType);

    /**
     * Find all products by status
     */
    List<Product> findByStatus(ProductStatus status);

    /**
     * Find all active products (status = ACTIVE)
     */
    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE'")
    List<Product> findActiveProducts();

    /**
     * Find products created by a specific user
     */
    List<Product> findByCreatedBy(String createdBy);

    /**
     * Find products effective within a date range
     */
    @Query("SELECT p FROM Product p WHERE p.effectiveDate <= :endDate " +
           "AND (p.endDate IS NULL OR p.endDate >= :startDate)")
    List<Product> findByEffectiveDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Find currently active products (status=ACTIVE and date is within effective range)
     */
    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' " +
           "AND p.effectiveDate <= :currentDate " +
           "AND (p.endDate IS NULL OR p.endDate >= :currentDate)")
    List<Product> findCurrentlyActiveProducts(@Param("currentDate") LocalDate currentDate);

    /**
     * Complex search with multiple criteria
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:productName IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :productName, '%'))) AND " +
           "(:productCode IS NULL OR p.productCode = :productCode) AND " +
           "(:productType IS NULL OR p.productType = :productType) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:createdBy IS NULL OR p.createdBy = :createdBy)")
    Page<Product> searchProducts(
        @Param("productName") String productName,
        @Param("productCode") String productCode,
        @Param("productType") ProductType productType,
        @Param("status") ProductStatus status,
        @Param("createdBy") String createdBy,
        Pageable pageable
    );

    /**
     * Find products by type with pagination
     */
    Page<Product> findByProductType(ProductType productType, Pageable pageable);

    /**
     * Find products by status with pagination
     */
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
}
