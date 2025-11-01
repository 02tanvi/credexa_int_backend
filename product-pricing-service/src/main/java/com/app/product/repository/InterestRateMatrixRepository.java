package com.app.product.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.app.product.entity.InterestRateMatrix;

@Repository
public interface InterestRateMatrixRepository extends JpaRepository<InterestRateMatrix, Long> {

    /**
     * Find all rate slabs for a product
     */
    List<InterestRateMatrix> findByProductId(Long productId);

    /**
     * Find applicable interest rate for given criteria
     */
    @Query("SELECT i FROM InterestRateMatrix i WHERE i.product.id = :productId " +
           "AND (:amount IS NULL OR (i.minAmount IS NULL OR :amount >= i.minAmount) " +
           "    AND (i.maxAmount IS NULL OR :amount <= i.maxAmount)) " +
           "AND (:termMonths IS NULL OR (i.minTermMonths IS NULL OR :termMonths >= i.minTermMonths) " +
           "    AND (i.maxTermMonths IS NULL OR :termMonths <= i.maxTermMonths)) " +
           "AND (:customerClassification IS NULL OR i.customerClassification IS NULL " +
           "    OR i.customerClassification = :customerClassification) " +
           "AND i.effectiveDate <= :currentDate " +
           "AND (i.endDate IS NULL OR i.endDate >= :currentDate) " +
           "ORDER BY i.interestRate DESC")
    List<InterestRateMatrix> findApplicableRates(
        @Param("productId") Long productId,
        @Param("amount") BigDecimal amount,
        @Param("termMonths") Integer termMonths,
        @Param("customerClassification") String customerClassification,
        @Param("currentDate") LocalDate currentDate
    );

    /**
     * Find the best (highest) applicable interest rate
     */
    default Optional<InterestRateMatrix> findBestApplicableRate(
        Long productId,
        BigDecimal amount,
        Integer termMonths,
        String customerClassification,
        LocalDate currentDate
    ) {
        List<InterestRateMatrix> rates = findApplicableRates(
            productId, amount, termMonths, customerClassification, currentDate
        );
        return rates.isEmpty() ? Optional.empty() : Optional.of(rates.get(0));
    }

    /**
     * Find active rate slabs for a product on a specific date
     */
    @Query("SELECT i FROM InterestRateMatrix i WHERE i.product.id = :productId " +
           "AND i.effectiveDate <= :date " +
           "AND (i.endDate IS NULL OR i.endDate >= :date)")
    List<InterestRateMatrix> findActiveRatesOnDate(
        @Param("productId") Long productId,
        @Param("date") LocalDate date
    );
}
