package com.app.product.service;

import com.app.product.dto.InterestRateMatrixResponse;
import com.app.product.dto.ProductSummaryResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class ExcelReportGenerator {

    public byte[] generateProductListReport(List<ProductSummaryResponse> products) {
        log.info("Generating Excel report for {} products", products.size());

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Product List");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Product ID", "Product Code", "Product Name", "Type", "Status", 
                              "Min Amount", "Max Amount", "Min Term", "Max Term", "Effective Date"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate data rows
            int rowNum = 1;
            for (ProductSummaryResponse product : products) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(product.getProductId());
                row.createCell(1).setCellValue(product.getProductCode());
                row.createCell(2).setCellValue(product.getProductName());
                row.createCell(3).setCellValue(product.getProductType().toString());
                row.createCell(4).setCellValue(product.getStatus().toString());
                row.createCell(5).setCellValue(product.getMinAmount() != null ? product.getMinAmount().doubleValue() : 0);
                row.createCell(6).setCellValue(product.getMaxAmount() != null ? product.getMaxAmount().doubleValue() : 0);
                row.createCell(7).setCellValue(product.getMinTermMonths() != null ? product.getMinTermMonths() : 0);
                row.createCell(8).setCellValue(product.getMaxTermMonths() != null ? product.getMaxTermMonths() : 0);
                row.createCell(9).setCellValue(product.getEffectiveDate() != null ? product.getEffectiveDate().toString() : "");

                for (int i = 0; i < 10; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error generating Excel report", e);
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    public byte[] generateInterestRateReport(Long productId, List<InterestRateMatrixResponse> rates) {
        log.info("Generating Excel report for interest rates - Product ID: {}", productId);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Interest Rates");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Min Amount", "Max Amount", "Min Term", "Max Term", 
                              "Interest Rate", "Additional Rate", "Total Rate", 
                              "Classification", "Effective Date", "End Date"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate data rows
            int rowNum = 1;
            for (InterestRateMatrixResponse rate : rates) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(rate.getId());
                row.createCell(1).setCellValue(rate.getMinAmount() != null ? rate.getMinAmount().doubleValue() : 0);
                row.createCell(2).setCellValue(rate.getMaxAmount() != null ? rate.getMaxAmount().doubleValue() : 0);
                row.createCell(3).setCellValue(rate.getMinTermMonths() != null ? rate.getMinTermMonths() : 0);
                row.createCell(4).setCellValue(rate.getMaxTermMonths() != null ? rate.getMaxTermMonths() : 0);
                row.createCell(5).setCellValue(rate.getInterestRate() != null ? rate.getInterestRate().doubleValue() : 0);
                row.createCell(6).setCellValue(rate.getAdditionalRate() != null ? rate.getAdditionalRate().doubleValue() : 0);
                row.createCell(7).setCellValue(rate.getTotalRate() != null ? rate.getTotalRate().doubleValue() : 0);
                row.createCell(8).setCellValue(rate.getCustomerClassification() != null ? rate.getCustomerClassification() : "");
                row.createCell(9).setCellValue(rate.getEffectiveDate() != null ? rate.getEffectiveDate().toString() : "");
                row.createCell(10).setCellValue(rate.getEndDate() != null ? rate.getEndDate().toString() : "");

                for (int i = 0; i < 11; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error generating interest rate Excel report", e);
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}