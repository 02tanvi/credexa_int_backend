package com.app.fdaccount.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for inquiring about premature withdrawal penalty and amount
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrematureWithdrawalInquiryRequest {

    @NotBlank(message = "Account number is required")
    @Size(max = 20, message = "Account number cannot exceed 20 characters")
    private String accountNumber;

    @NotNull(message = "Withdrawal date is required")
    private LocalDate withdrawalDate;
}
