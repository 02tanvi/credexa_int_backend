package com.app.calculator.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for customer details from customer-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNumber;
    
    private String customerClassification;
    private Integer age;
    
    private Boolean active;
}
