package com.app.product.service;

import com.app.product.dto.*;
import com.app.product.enums.ProductStatus;
import com.app.product.enums.ProductType;
import com.app.product.enums.ReportFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ProductService productService;
    private final InterestRateService interestRateService;
    private final ExcelReportGenerator excelGenerator;
    private final CsvReportGenerator csvGenerator;

    public ReportResponse generateReport(ReportRequest request) {
        log.info("Generating report: Type={}, Format={}", request.getReportType(), request.getFormat());

        byte[] reportData;
        String fileName;

        switch (request.getReportType()) {
            case PRODUCT_LIST_FILTERED:
                reportData = generateFilteredProductReport(request);
                fileName = generateFileName("Product_List_Filtered", request.getFormat());
                break;

            case ACTIVE_PRODUCTS:
                reportData = generateActiveProductsReport(request);
                fileName = generateFileName("Active_Products", request.getFormat());
                break;

            case INTEREST_RATES_BY_PRODUCT:
                reportData = generateInterestRateReport(request);
                fileName = generateFileName("Interest_Rates", request.getFormat());
                break;

            default:
                throw new IllegalArgumentException("Unsupported report type: " + request.getReportType());
        }

        return ReportResponse.builder()
                .fileName(fileName)
                .fileData(reportData)
                .contentType(getContentType(request.getFormat()))
                .generatedAt(LocalDateTime.now())
                .recordCount(0)
                .build();
    }

    private byte[] generateFilteredProductReport(ReportRequest request) {
        ProductSearchCriteria criteria = mapFiltersToSearchCriteria(request.getFilters());
        ProductListResponse products = productService.searchProducts(criteria);

        if (request.getFormat() == ReportFormat.EXCEL) {
            return excelGenerator.generateProductListReport(products.getProducts());
        } else {
            return csvGenerator.generateProductListReport(products.getProducts());
        }
    }

    private byte[] generateActiveProductsReport(ReportRequest request) {
        List<ProductSummaryResponse> activeProducts = productService.getActiveProducts();

        if (request.getFormat() == ReportFormat.EXCEL) {
            return excelGenerator.generateProductListReport(activeProducts);
        } else {
            return csvGenerator.generateProductListReport(activeProducts);
        }
    }

    private byte[] generateInterestRateReport(ReportRequest request) {
        Long productId = (Long) request.getFilters().get("productId");
        List<InterestRateMatrixResponse> rates = interestRateService.getInterestRatesForProduct(productId);

        if (request.getFormat() == ReportFormat.EXCEL) {
            return excelGenerator.generateInterestRateReport(productId, rates);
        } else {
            return csvGenerator.generateInterestRateReport(productId, rates);
        }
    }

    private String generateFileName(String reportName, ReportFormat format) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String extension = format == ReportFormat.EXCEL ? ".xlsx" : ".csv";
        return reportName + "_" + timestamp + extension;
    }

    private String getContentType(ReportFormat format) {
        return format == ReportFormat.EXCEL 
                ? "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                : "text/csv";
    }

    private ProductSearchCriteria mapFiltersToSearchCriteria(Map<String, Object> filters) {
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        if (filters != null) {
            criteria.setProductName((String) filters.get("productName"));
            criteria.setProductCode((String) filters.get("productCode"));
            if (filters.get("productType") != null) {
                criteria.setProductType(ProductType.valueOf(filters.get("productType").toString()));
            }
            if (filters.get("status") != null) {
                criteria.setStatus(ProductStatus.valueOf(filters.get("status").toString()));
            }
        }
        criteria.setPage(0);
        criteria.setSize(1000);
        criteria.setSortBy("createdAt");
        criteria.setSortDirection("DESC");
        return criteria;
    }
}