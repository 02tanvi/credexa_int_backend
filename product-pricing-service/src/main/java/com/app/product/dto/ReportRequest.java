package com.app.product.dto;

import com.app.product.enums.ReportFormat;
import com.app.product.enums.ReportType;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
public class ReportRequest {
    private ReportType reportType;
    private ReportFormat format;
    private Map<String, Object> filters;
    private LocalDate startDate;
    private LocalDate endDate;
}