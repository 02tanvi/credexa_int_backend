package com.app.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerCommunicationRequest {
    
    @NotBlank(message = "Communication type is required (EMAIL/SMS/PUSH/LETTER)")
    private String communicationType;
    
    @NotBlank(message = "Event is required")
    private String event;
    
    private String template;
    private String subject;
    private String content;
    private Boolean mandatory;
    private Boolean active;
}
