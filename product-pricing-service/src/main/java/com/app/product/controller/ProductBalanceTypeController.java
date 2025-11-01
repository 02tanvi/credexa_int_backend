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
import com.app.product.dto.ProductBalanceTypeRequest;
import com.app.product.dto.ProductBalanceTypeResponse;
import com.app.product.service.ProductBalanceTypeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/products/{productId}/balance-types")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Balance Types", description = "APIs for managing product balance types")
public class ProductBalanceTypeController {

    private final ProductBalanceTypeService balanceTypeService;

    @PostMapping
    @Operation(summary = "Add balance type", description = "Adds a new balance type to a product")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Balance type added successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ApiResponse<ProductBalanceTypeResponse>> addBalanceType(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Valid @RequestBody ProductBalanceTypeRequest request) {
        
        log.info("REST: Adding balance type to product {}", productId);
        ProductBalanceTypeResponse response = balanceTypeService.addBalanceType(productId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Balance type added successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all balance types", description = "Retrieves all balance types for a product")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Balance types retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<ProductBalanceTypeResponse>>> getBalanceTypes(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        
        log.info("REST: Fetching balance types for product {}", productId);
        List<ProductBalanceTypeResponse> response = balanceTypeService.getBalanceTypesByProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

@RestController
@RequestMapping("/balance-types")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Balance Types Management", description = "Direct balance type management APIs")
class BalanceTypeManagementController {

    private final ProductBalanceTypeService balanceTypeService;

    @GetMapping("/{id}")
    @Operation(summary = "Get balance type by ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Balance type retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Balance type not found")
    })
    public ResponseEntity<ApiResponse<ProductBalanceTypeResponse>> getById(
            @Parameter(description = "Balance Type ID") @PathVariable Long id) {
        
        log.info("REST: Fetching balance type {}", id);
        ProductBalanceTypeResponse response = balanceTypeService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update balance type")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Balance type updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Balance type not found")
    })
    public ResponseEntity<ApiResponse<ProductBalanceTypeResponse>> update(
            @Parameter(description = "Balance Type ID") @PathVariable Long id,
            @Valid @RequestBody ProductBalanceTypeRequest request) {
        
        log.info("REST: Updating balance type {}", id);
        ProductBalanceTypeResponse response = balanceTypeService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Balance type updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete balance type")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Balance type deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Balance type not found")
    })
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Balance Type ID") @PathVariable Long id) {
        
        log.info("REST: Deleting balance type {}", id);
        balanceTypeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Balance type deleted successfully", null));
    }
}
