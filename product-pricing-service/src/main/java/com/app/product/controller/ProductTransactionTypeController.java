package com.app.product.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.product.dto.ApiResponse;
import com.app.product.dto.ProductTransactionTypeRequest;
import com.app.product.dto.ProductTransactionTypeResponse;
import com.app.product.service.ProductTransactionTypeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/products/{productId}/transaction-types")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transaction Types", description = "APIs for managing product transaction types")
public class ProductTransactionTypeController {

    private final ProductTransactionTypeService transactionTypeService;

    @PostMapping
    @Operation(summary = "Add transaction type", description = "Adds a new transaction type to a product")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Transaction type added successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ApiResponse<ProductTransactionTypeResponse>> addTransactionType(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Valid @RequestBody ProductTransactionTypeRequest request) {
        
        log.info("REST: Adding transaction type to product {}", productId);
        ProductTransactionTypeResponse response = transactionTypeService.addTransactionType(productId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transaction type added successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all transaction types", description = "Retrieves all transaction types for a product")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transaction types retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<ProductTransactionTypeResponse>>> getTransactionTypes(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        
        log.info("REST: Fetching transaction types for product {}", productId);
        List<ProductTransactionTypeResponse> response = transactionTypeService.getTransactionTypesByProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

@RestController
@RequestMapping("/transaction-types")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transaction Types Management", description = "Direct transaction type management APIs")
class TransactionTypeManagementController {

    private final ProductTransactionTypeService transactionTypeService;

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction type by ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transaction type retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Transaction type not found")
    })
    public ResponseEntity<ApiResponse<ProductTransactionTypeResponse>> getById(
            @Parameter(description = "Transaction Type ID") @PathVariable Long id) {
        
        log.info("REST: Fetching transaction type {}", id);
        ProductTransactionTypeResponse response = transactionTypeService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update transaction type")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transaction type updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Transaction type not found")
    })
    public ResponseEntity<ApiResponse<ProductTransactionTypeResponse>> update(
            @Parameter(description = "Transaction Type ID") @PathVariable Long id,
            @Valid @RequestBody ProductTransactionTypeRequest request) {
        
        log.info("REST: Updating transaction type {}", id);
        ProductTransactionTypeResponse response = transactionTypeService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Transaction type updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete transaction type")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transaction type deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Transaction type not found")
    })
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Transaction Type ID") @PathVariable Long id) {
        
        log.info("REST: Deleting transaction type {}", id);
        transactionTypeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Transaction type deleted successfully", null));
    }
}
