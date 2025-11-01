package com.app.fdaccount.dto;

import com.app.fdaccount.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for searching accounts with various criteria
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchAccountRequest {

    private String accountNumber;
    private String accountName;
    private String productCode;
    private Long customerId;
    private AccountStatus status;
    private String branchCode;
    private LocalDate effectiveDateFrom;
    private LocalDate effectiveDateTo;
    private LocalDate maturityDateFrom;
    private LocalDate maturityDateTo;
    
    // Pagination
    @Builder.Default
    private Integer page = 0;
    
    @Builder.Default
    private Integer size = 20;
    
    // Sorting
    @Builder.Default
    private String sortBy = "createdAt";
    
    @Builder.Default
    private String sortDirection = "DESC";
}
