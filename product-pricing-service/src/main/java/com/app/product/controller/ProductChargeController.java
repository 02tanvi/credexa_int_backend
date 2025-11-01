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
import com.app.product.dto.ProductChargeRequest;
import com.app.product.dto.ProductChargeResponse;
import com.app.product.service.ProductChargeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/products/{productId}/charges")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Charges", description = "APIs for managing product charges and fees")
public class ProductChargeController {

    private final ProductChargeService chargeService;

    @PostMapping
    @Operation(summary = "Add charge to product", description = "Adds a new charge or fee to a product")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Charge added successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ApiResponse<ProductChargeResponse>> addCharge(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Valid @RequestBody ProductChargeRequest request) {
        
        log.info("REST: Adding charge to product {}", productId);
        ProductChargeResponse response = chargeService.addCharge(productId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Charge added successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all charges for product", description = "Retrieves all charges associated with a product")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Charges retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<ProductChargeResponse>>> getChargesByProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        
        log.info("REST: Fetching charges for product {}", productId);
        List<ProductChargeResponse> response = chargeService.getChargesByProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/type/{chargeType}")
    @Operation(summary = "Get charges by type", description = "Retrieves charges of a specific type for a product")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Charges retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<ProductChargeResponse>>> getChargesByType(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Charge Type (FEE/PENALTY)") @PathVariable String chargeType) {
        
        log.info("REST: Fetching charges of type {} for product {}", chargeType, productId);
        List<ProductChargeResponse> response = chargeService.getChargesByType(productId, chargeType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

@RestController
@RequestMapping("/charges")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Charges Management", description = "Direct charge management APIs")
class ChargeManagementController {

    private final ProductChargeService chargeService;

    @GetMapping("/{chargeId}")
    @Operation(summary = "Get charge by ID", description = "Retrieves a specific charge by its ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Charge retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Charge not found")
    })
    public ResponseEntity<ApiResponse<ProductChargeResponse>> getChargeById(
            @Parameter(description = "Charge ID") @PathVariable Long chargeId) {
        
        log.info("REST: Fetching charge {}", chargeId);
        ProductChargeResponse response = chargeService.getChargeById(chargeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{chargeId}")
    @Operation(summary = "Update charge", description = "Updates an existing charge")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Charge updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Charge not found")
    })
    public ResponseEntity<ApiResponse<ProductChargeResponse>> updateCharge(
            @Parameter(description = "Charge ID") @PathVariable Long chargeId,
            @Valid @RequestBody ProductChargeRequest request) {
        
        log.info("REST: Updating charge {}", chargeId);
        ProductChargeResponse response = chargeService.updateCharge(chargeId, request);
        return ResponseEntity.ok(ApiResponse.success("Charge updated successfully", response));
    }

    @DeleteMapping("/{chargeId}")
    @Operation(summary = "Delete charge", description = "Deletes a charge from a product")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Charge deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Charge not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteCharge(
            @Parameter(description = "Charge ID") @PathVariable Long chargeId) {
        
        log.info("REST: Deleting charge {}", chargeId);
        chargeService.deleteCharge(chargeId);
        return ResponseEntity.ok(ApiResponse.success("Charge deleted successfully", null));
    }
}
