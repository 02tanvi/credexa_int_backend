package com.app.product.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.app.product.dto.InterestRateMatrixResponse;
import com.app.product.entity.InterestRateMatrix;
import com.app.product.mapper.ProductMapper;
import com.app.product.repository.InterestRateMatrixRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for interest rate calculations and matrix operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InterestRateService {

    private final InterestRateMatrixRepository interestRateMatrixRepository;
    private final ProductMapper productMapper;

    /**
     * Get all interest rate slabs for a product
     * Cached by product ID
     */
    @Cacheable(value = "interestRates", key = "#productId")
    public List<InterestRateMatrixResponse> getInterestRatesForProduct(Long productId) {
        log.info("🔍 CACHE MISS - Fetching interest rates for product {} from database", productId);
        log.info("Fetching interest rates for product ID: {}", productId);
        return interestRateMatrixRepository.findByProductId(productId).stream()
                .map(productMapper::toInterestRateResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get active interest rates for a product on a specific date
     */
    public List<InterestRateMatrixResponse> getActiveRatesOnDate(Long productId, LocalDate date) {
        log.info("Fetching active rates for product {} on date {}", productId, date);
        return interestRateMatrixRepository.findActiveRatesOnDate(productId, date).stream()
                .map(productMapper::toInterestRateResponse)
                .collect(Collectors.toList());
    }

    /**
     * Find applicable interest rate for given criteria
     * Returns the best (highest) applicable rate
     */
    public Optional<InterestRateMatrixResponse> findApplicableRate(
            Long productId,
            BigDecimal amount,
            Integer termMonths,
            String customerClassification) {
        
        log.info("Finding applicable rate - Product: {}, Amount: {}, Term: {} months, Classification: {}",
                productId, amount, termMonths, customerClassification);
        
        LocalDate currentDate = LocalDate.now();
        
        Optional<InterestRateMatrix> bestRate = interestRateMatrixRepository.findBestApplicableRate(
                productId, amount, termMonths, customerClassification, currentDate
        );
        
        return bestRate.map(rate -> {
            log.info("Found applicable rate: {}% (total: {}%)", 
                    rate.getInterestRate(), rate.getTotalRate());
            return productMapper.toInterestRateResponse(rate);
        });
    }

    /**
     * Calculate effective interest rate for given parameters
     * Considers base rate + matrix rate
     */
    public BigDecimal calculateEffectiveRate(
            Long productId,
            BigDecimal baseRate,
            BigDecimal amount,
            Integer termMonths,
            String customerClassification) {
        
        log.info("Calculating effective rate for product {}", productId);
        
        Optional<InterestRateMatrixResponse> applicableRate = findApplicableRate(
                productId, amount, termMonths, customerClassification
        );
        
        if (applicableRate.isPresent()) {
            BigDecimal matrixRate = applicableRate.get().getTotalRate();
            BigDecimal effectiveRate = matrixRate != null ? matrixRate : baseRate;
            log.info("Effective rate calculated: {}%", effectiveRate);
            return effectiveRate;
        }
        
        log.info("No matrix rate found, using base rate: {}%", baseRate);
        return baseRate;
    }
}