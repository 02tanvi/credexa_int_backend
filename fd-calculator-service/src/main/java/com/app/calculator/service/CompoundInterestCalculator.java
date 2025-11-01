package com.app.calculator.service;

import com.app.calculator.dto.MonthlyBreakdown;
import com.app.calculator.enums.CompoundingFrequency;
import com.app.calculator.enums.TenureUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Compound Interest Calculator Utility
 * Provides reusable methods for computing:
 * - Compound interest
 * - Maturity amount
 * - Monthly breakdown (interest progression)
 *
 * CORRECTED:
 * - Fixed TDS logic to apply only to interest.
 * - Fixed monthly breakdown to use the correct compound interest formula.
A*/
@Service
@Slf4j
public class CompoundInterestCalculator {

    // Use a high-precision MathContext for intermediate division
    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);

    /**
     * Calculate total interest earned on a principal amount using compound interest.
     *
     * @param principal principal amount
     * @param annualRate annual interest rate (in %)
     * @param tenure duration of deposit
     * @param tenureUnit unit of tenure (months/years)
     * @param frequency compounding frequency
     * @return total interest earned
     */
    public BigDecimal calculateInterest(BigDecimal principal,
                                        BigDecimal annualRate,
                                        int tenure,
                                        TenureUnit tenureUnit,
                                        CompoundingFrequency frequency) {

        // Calculate maturity *before* TDS, then find interest
        BigDecimal maturityBeforeTds = calculateMaturityAmountBeforeTDS(principal, annualRate, tenure, tenureUnit, frequency);
        return maturityBeforeTds.subtract(principal).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates the maturity amount *before* any TDS is applied.
     * Formula: A = P * (1 + r/n)^(n*t)
     */
    public BigDecimal calculateMaturityAmountBeforeTDS(BigDecimal principal,
                                                       BigDecimal annualRate,
                                                       int tenure,
                                                       TenureUnit tenureUnit,
                                                       CompoundingFrequency frequency) {
        
        int n = frequency.getPeriodsPerYear(); // Compounding periods per year
        double t = tenureUnit.toYears(tenure); // Tenure in years
        double nt = n * t; // Total compounding periods
        
        // --- High-Precision Base Calculation ---
        // r = (rate / 100)
        BigDecimal r_decimal = annualRate.divide(BigDecimal.valueOf(100), MC);
        
        // (r / n)
        BigDecimal r_over_n = r_decimal.divide(BigDecimal.valueOf(n), MC);
        
        // base = (1 + r/n)
        BigDecimal base = BigDecimal.ONE.add(r_over_n);
        
        // --- Exponentiation ---
        // compoundFactor = (1 + r/n)^nt
        // We must use double for Math.pow() as 'nt' can be fractional
        double compoundFactor = Math.pow(base.doubleValue(), nt);
        
        // M = P * compoundFactor
        BigDecimal maturityAmount = principal.multiply(BigDecimal.valueOf(compoundFactor));
        
        return maturityAmount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate maturity amount after compounding and applying TDS.
     *
     * @param principal principal amount
     * @param annualRate annual interest rate (%)
     * @param tenure duration of deposit
     * @param tenureUnit unit of tenure (MONTHS or YEARS)
     * @param frequency compounding frequency
     * @param tdsRate tax deduction at source (%)
     * @return maturity amount after TDS
     */
    public BigDecimal calculateMaturityAmount(BigDecimal principal,
                                            BigDecimal annualRate,
                                            int tenure,
                                            TenureUnit tenureUnit,
                                            CompoundingFrequency frequency,
                                            BigDecimal tdsRate) {

        // 1. Calculate maturity *before* tax
        BigDecimal maturityBeforeTds = calculateMaturityAmountBeforeTDS(principal, annualRate, tenure, tenureUnit, frequency);
        
        // 2. Calculate total interest earned
        BigDecimal totalInterest = maturityBeforeTds.subtract(principal);

        // 3. Calculate TDS *only on the interest*
        BigDecimal tdsAmount = totalInterest.multiply(
            tdsRate.divide(BigDecimal.valueOf(100), MC)
        ).setScale(2, RoundingMode.HALF_UP);

        // 4. Final maturity is maturity before TDS, minus the tax amount
        BigDecimal maturityAfterTds = maturityBeforeTds.subtract(tdsAmount);

        return maturityAfterTds.setScale(2, RoundingMode.HALF_UP);
}

    /** 
    * Generate a detailed monthly breakdown for FD progression.
    *
    * CORRECTED: This logic now uses the compound interest formula
    * M(m) = P * (1 + r/n)^(n*t_m) where t_m = m / 12
    * This shows the true compounded value month-by-month.
    *
    * @param principal principal amount
    * @param annualRate annual interest rate (%)
    * @param totalMonths total number of months in tenure
    * @param frequency compounding frequency
    * @param startDate deposit start date
    * @return list of MonthlyBreakdown entries
    */
    public List<MonthlyBreakdown> generateMonthlyBreakdown(BigDecimal principal,
                                                           BigDecimal annualRate,
                                                           int totalMonths,
                                                           CompoundingFrequency frequency,
                                                           LocalDate startDate) {

        List<MonthlyBreakdown> breakdown = new ArrayList<>();
        
        int n = frequency.getPeriodsPerYear();

        // Calculate (1 + r/n) with high precision
        BigDecimal r_decimal = annualRate.divide(BigDecimal.valueOf(100), MC);
        BigDecimal r_over_n = r_decimal.divide(BigDecimal.valueOf(n), MC);
        BigDecimal base = BigDecimal.ONE.add(r_over_n);
        double baseDouble = base.doubleValue();

        BigDecimal openingBalance = principal;
        BigDecimal cumulativeInterest = BigDecimal.ZERO;

        for (int month = 1; month <= totalMonths; month++) {
            LocalDate monthEndDate = startDate.plusMonths(month);
            
            // Calculate total value at the end of *this* month
            // t_m = (month / 12.0)
            // nt_m = n * (month / 12.0)
            double t_m = month / 12.0;
            double nt_m = n * t_m;
            
            // factor = (1 + r/n)^(nt_m)
            double compoundFactor = Math.pow(baseDouble, nt_m);
            
            BigDecimal closingBalance = principal
                    .multiply(BigDecimal.valueOf(compoundFactor))
                    .setScale(2, RoundingMode.HALF_UP);
            
            BigDecimal newCumulativeInterest = closingBalance.subtract(principal);
            BigDecimal interestEarned = newCumulativeInterest.subtract(cumulativeInterest);
            
            MonthlyBreakdown entry = MonthlyBreakdown.builder()
                .month(month)
                .date(monthEndDate)
                .openingBalance(openingBalance)
                .interestEarned(interestEarned)
                .closingBalance(closingBalance)
                .cumulativeInterest(newCumulativeInterest)
                .build();
            
            breakdown.add(entry);
            
            // Set up for next iteration
            openingBalance = closingBalance; 
            cumulativeInterest = newCumulativeInterest;
        }
        
        log.debug("Generated {} monthly breakdown entries", breakdown.size());
            return breakdown;
  }

    // This helper is no longer needed with the new breakdown logic
   // private boolean isCompoundingMonth(int month, CompoundingFrequency frequency) { ... }
}

