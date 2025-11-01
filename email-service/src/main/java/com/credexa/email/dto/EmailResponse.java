package com.credexa.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailResponse {

    private String emailId;  // Azure message ID
    private String status;
    private String message;
    private String recipient;
}
