package com.app.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private String fileName;
    private byte[] fileData;
    private String contentType;
    private LocalDateTime generatedAt;
    private Integer recordCount;
}