package com.app.calculator.service;

import com.app.calculator.dto.CalculationResponse;
import com.app.calculator.dto.MonthlyBreakdown;
import com.app.calculator.enums.TenureUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for Simple Interest calculations
 * Formula: M = P + (P × r × t / 100)
 */
@Service
@Slf4j
public class SimpleInterestCalculator {
    
    /**
     * Calculate simple interest
     * @param principal Principal amount
     * @param rate Annual interest rate (%)
     * @param tenure Tenure value
     * @param tenureUnit Unit of tenure (DAYS, MONTHS, YEARS)
     * @return Interest earned
     */
    public BigDecimal calculateInterest(BigDecimal principal, BigDecimal rate, 
                                       int tenure, TenureUnit tenureUnit) {
        log.debug("Calculating simple interest: P={}, R={}, T={} {}", 
                 principal, rate, tenure, tenureUnit);
        
        double tenureInYears = tenureUnit.toYears(tenure);
        
        // Formula: Interest = (P × r × t) / 100
        BigDecimal interest = principal
            .multiply(rate)
            .multiply(BigDecimal.valueOf(tenureInYears))
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        log.debug("Calculated simple interest: {}", interest);
        return interest;
    }
    
    /**
     * Calculate maturity amount with simple interest
     * @param principal Principal amount
     * @param rate Annual interest rate (%)
     * @param tenure Tenure value
     * @param tenureUnit Unit of tenure
     * @param tdsRate TDS rate (%)
     * @return Maturity amount after TDS
     */
    public BigDecimal calculateMaturityAmount(BigDecimal principal, BigDecimal rate,
                                             int tenure, TenureUnit tenureUnit,
                                             BigDecimal tdsRate) {
        BigDecimal interest = calculateInterest(principal, rate, tenure, tenureUnit);
        BigDecimal tds = calculateTDS(interest, tdsRate);
        BigDecimal netInterest = interest.subtract(tds);
        
        return principal.add(netInterest);
    }
    
    /**
     * Calculate TDS on interest
     */
    public BigDecimal calculateTDS(BigDecimal interest, BigDecimal tdsRate) {
        if (tdsRate == null || tdsRate.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return interest
            .multiply(tdsRate)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
    
    /**
     * Generate monthly breakdown for simple interest
     */
    public List<MonthlyBreakdown> generateMonthlyBreakdown(BigDecimal principal, 
                                                           BigDecimal annualRate,
                                                           int tenureMonths,
                                                           LocalDate startDate) {
        List<MonthlyBreakdown> breakdown = new ArrayList<>();
        
        // Monthly rate = Annual rate / 12
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), 6, RoundingMode.HALF_UP);
        
        BigDecimal cumulativeInterest = BigDecimal.ZERO;
        
        for (int month = 1; month <= tenureMonths; month++) {
            LocalDate monthEndDate = startDate.plusMonths(month);
            
            // Simple interest for one month: P × (r/12) / 100
            BigDecimal monthlyInterest = principal.multiply(monthlyRate);
            monthlyInterest = monthlyInterest.setScale(2, RoundingMode.HALF_UP);
            
            cumulativeInterest = cumulativeInterest.add(monthlyInterest);
            
            BigDecimal closingBalance = principal.add(cumulativeInterest);
            
            MonthlyBreakdown entry = MonthlyBreakdown.builder()
                .month(month)
                .date(monthEndDate)
                .openingBalance(principal)
                .interestEarned(monthlyInterest)
                .closingBalance(closingBalance)
                .cumulativeInterest(cumulativeInterest)
                .build();
            
            breakdown.add(entry);
        }
        
        return breakdown;
    }
}
