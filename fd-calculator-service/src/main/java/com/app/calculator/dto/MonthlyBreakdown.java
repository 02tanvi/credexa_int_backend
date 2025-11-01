package com.app.calculator.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Monthly breakdown of FD calculations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Monthly interest and balance breakdown")
public class MonthlyBreakdown {
    
    @Schema(description = "Month number", example = "1")
    private Integer month;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Month end date", example = "2025-02-20")
    private LocalDate date;
    
    @Schema(description = "Opening balance", example = "100000.00")
    private BigDecimal openingBalance;
    
    @Schema(description = "Interest earned in this month", example = "647.00")
    private BigDecimal interestEarned;
    
    @Schema(description = "Closing balance", example = "100647.00")
    private BigDecimal closingBalance;
    
    @Schema(description = "Cumulative interest earned", example = "647.00")
    private BigDecimal cumulativeInterest;
}
