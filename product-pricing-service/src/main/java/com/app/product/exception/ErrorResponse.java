package com.app.product.exception;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard error response structure
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    
    private boolean success;
    private String message;
    private String error;
    private int status;
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    private String path;
    private Map<String, String> validationErrors;
}
