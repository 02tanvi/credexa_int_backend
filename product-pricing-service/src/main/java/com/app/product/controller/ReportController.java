package com.app.product.controller;

import com.app.product.dto.ReportRequest;
import com.app.product.dto.ReportResponse;
import com.app.product.enums.ReportFormat;
import com.app.product.enums.ReportType;
import com.app.product.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reports", description = "APIs for generating product and pricing reports")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/generate")
    @Operation(summary = "Generate report")
    public ResponseEntity<ByteArrayResource> generateReport(@Valid @RequestBody ReportRequest request) {
        log.info("REST: Generating report - Type: {}, Format: {}", request.getReportType(), request.getFormat());
        
        ReportResponse report = reportService.generateReport(request);
        ByteArrayResource resource = new ByteArrayResource(report.getFileData());
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + report.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(report.getContentType()))
                .contentLength(report.getFileData().length)
                .body(resource);
    }

    @GetMapping("/active-products")
    @Operation(summary = "Generate active products report")
    public ResponseEntity<ByteArrayResource> generateActiveProductsReport(
            @RequestParam(defaultValue = "EXCEL") String format) {
        
        log.info("REST: Generating active products report - Format: {}", format);
        
        ReportRequest request = new ReportRequest();
        request.setReportType(ReportType.ACTIVE_PRODUCTS);
        request.setFormat(ReportFormat.valueOf(format.toUpperCase()));
        
        return generateReport(request);
    }

    @GetMapping("/products/{productId}/interest-rates")
    @Operation(summary = "Generate interest rate report for product")
    public ResponseEntity<ByteArrayResource> generateInterestRateReport(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "EXCEL") String format) {
        
        log.info("REST: Generating interest rate report - Product: {}, Format: {}", productId, format);
        
        ReportRequest request = new ReportRequest();
        request.setReportType(ReportType.INTEREST_RATES_BY_PRODUCT);
        request.setFormat(ReportFormat.valueOf(format.toUpperCase()));
        request.setFilters(Map.of("productId", productId));
        
        return generateReport(request);
    }
}