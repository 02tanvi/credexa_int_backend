package com.app.product.service;

import com.app.product.dto.InterestRateMatrixResponse;
import com.app.product.dto.ProductSummaryResponse;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

@Component
@Slf4j
public class CsvReportGenerator {

    public byte[] generateProductListReport(List<ProductSummaryResponse> products) {
        log.info("Generating CSV report for {} products", products.size());

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream))) {

            // Write header
            String[] header = {"Product ID", "Product Code", "Product Name", "Type", "Status", 
                             "Min Amount", "Max Amount", "Min Term", "Max Term", "Effective Date"};
            writer.writeNext(header);

            // Write data
            for (ProductSummaryResponse product : products) {
                String[] row = {
                    product.getProductId().toString(),
                    product.getProductCode(),
                    product.getProductName(),
                    product.getProductType().toString(),
                    product.getStatus().toString(),
                    product.getMinAmount() != null ? product.getMinAmount().toString() : "",
                    product.getMaxAmount() != null ? product.getMaxAmount().toString() : "",
                    product.getMinTermMonths() != null ? product.getMinTermMonths().toString() : "",
                    product.getMaxTermMonths() != null ? product.getMaxTermMonths().toString() : "",
                    product.getEffectiveDate() != null ? product.getEffectiveDate().toString() : ""
                };
                writer.writeNext(row);
            }

            writer.flush();
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error generating CSV report", e);
            throw new RuntimeException("Failed to generate CSV report", e);
        }
    }

    public byte[] generateInterestRateReport(Long productId, List<InterestRateMatrixResponse> rates) {
        log.info("Generating CSV report for interest rates - Product ID: {}", productId);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream))) {

            // Write header
            String[] header = {"ID", "Min Amount", "Max Amount", "Min Term", "Max Term", 
                             "Interest Rate", "Additional Rate", "Total Rate", 
                             "Classification", "Effective Date", "End Date"};
            writer.writeNext(header);

            // Write data
            for (InterestRateMatrixResponse rate : rates) {
                String[] row = {
                    rate.getId().toString(),
                    rate.getMinAmount() != null ? rate.getMinAmount().toString() : "",
                    rate.getMaxAmount() != null ? rate.getMaxAmount().toString() : "",
                    rate.getMinTermMonths() != null ? rate.getMinTermMonths().toString() : "",
                    rate.getMaxTermMonths() != null ? rate.getMaxTermMonths().toString() : "",
                    rate.getInterestRate() != null ? rate.getInterestRate().toString() : "",
                    rate.getAdditionalRate() != null ? rate.getAdditionalRate().toString() : "",
                    rate.getTotalRate() != null ? rate.getTotalRate().toString() : "",
                    rate.getCustomerClassification() != null ? rate.getCustomerClassification() : "",
                    rate.getEffectiveDate() != null ? rate.getEffectiveDate().toString() : "",
                    rate.getEndDate() != null ? rate.getEndDate().toString() : ""
                };
                writer.writeNext(row);
            }

            writer.flush();
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error generating CSV report", e);
            throw new RuntimeException("Failed to generate CSV report", e);
        }
    }
}