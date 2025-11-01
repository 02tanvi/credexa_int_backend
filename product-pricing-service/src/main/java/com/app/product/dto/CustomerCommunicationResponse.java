package com.app.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerCommunicationResponse {
    
    private Long id;
    private Long productId;
    private String communicationType;
    private String event;
    private String template;
    private String subject;
    private Boolean mandatory;
    private Boolean active;
}
