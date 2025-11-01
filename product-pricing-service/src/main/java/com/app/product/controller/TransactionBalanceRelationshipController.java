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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.product.dto.ApiResponse;
import com.app.product.dto.TransactionBalanceRelationshipRequest;
import com.app.product.dto.TransactionBalanceRelationshipResponse;
import com.app.product.enums.BalanceType;
import com.app.product.enums.TransactionType;
import com.app.product.service.TransactionBalanceRelationshipService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for managing transaction to balance relationships
 */
@RestController
@RequestMapping("/transaction-balance-relationships")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transaction-Balance Relationships", description = "APIs for managing transaction to balance type mappings")
public class TransactionBalanceRelationshipController {

    private final TransactionBalanceRelationshipService relationshipService;

    @PostMapping
    @Operation(summary = "Create relationship", description = "Creates a new transaction-balance relationship")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Relationship created successfully")
    })
    public ResponseEntity<ApiResponse<TransactionBalanceRelationshipResponse>> create(
            @Valid @RequestBody TransactionBalanceRelationshipRequest request) {
        
        log.info("REST: Creating transaction-balance relationship");
        TransactionBalanceRelationshipResponse response = relationshipService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Relationship created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all relationships", description = "Retrieves all transaction-balance relationships")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Relationships retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<TransactionBalanceRelationshipResponse>>> getAll() {
        
        log.info("REST: Fetching all relationships");
        List<TransactionBalanceRelationshipResponse> response = relationshipService.getAll();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get relationship by ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Relationship retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Relationship not found")
    })
    public ResponseEntity<ApiResponse<TransactionBalanceRelationshipResponse>> getById(
            @Parameter(description = "Relationship ID") @PathVariable Long id) {
        
        log.info("REST: Fetching relationship {}", id);
        TransactionBalanceRelationshipResponse response = relationshipService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/transaction/{transactionType}")
    @Operation(summary = "Get by transaction type", description = "Retrieves relationships for a transaction type")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Relationships retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<TransactionBalanceRelationshipResponse>>> getByTransactionType(
            @Parameter(description = "Transaction Type") @PathVariable TransactionType transactionType) {
        
        log.info("REST: Fetching relationships for transaction type {}", transactionType);
        List<TransactionBalanceRelationshipResponse> response = relationshipService.getByTransactionType(transactionType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/balance/{balanceType}")
    @Operation(summary = "Get by balance type", description = "Retrieves relationships for a balance type")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Relationships retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<TransactionBalanceRelationshipResponse>>> getByBalanceType(
            @Parameter(description = "Balance Type") @PathVariable BalanceType balanceType) {
        
        log.info("REST: Fetching relationships for balance type {}", balanceType);
        List<TransactionBalanceRelationshipResponse> response = relationshipService.getByBalanceType(balanceType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/lookup")
    @Operation(summary = "Lookup specific relationship", description = "Finds relationship by transaction and balance type")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Relationship found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Relationship not found")
    })
    public ResponseEntity<ApiResponse<TransactionBalanceRelationshipResponse>> getByTransactionAndBalance(
            @Parameter(description = "Transaction Type") @RequestParam TransactionType transactionType,
            @Parameter(description = "Balance Type") @RequestParam BalanceType balanceType) {
        
        log.info("REST: Looking up relationship for {}→{}", transactionType, balanceType);
        TransactionBalanceRelationshipResponse response = relationshipService.getByTransactionAndBalance(transactionType, balanceType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active relationships", description = "Retrieves all active relationships")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Active relationships retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<TransactionBalanceRelationshipResponse>>> getAllActive() {
        
        log.info("REST: Fetching all active relationships");
        List<TransactionBalanceRelationshipResponse> response = relationshipService.getAllActive();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update relationship")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Relationship updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Relationship not found")
    })
    public ResponseEntity<ApiResponse<TransactionBalanceRelationshipResponse>> update(
            @Parameter(description = "Relationship ID") @PathVariable Long id,
            @Valid @RequestBody TransactionBalanceRelationshipRequest request) {
        
        log.info("REST: Updating relationship {}", id);
        TransactionBalanceRelationshipResponse response = relationshipService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Relationship updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete relationship")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Relationship deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Relationship not found")
    })
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Relationship ID") @PathVariable Long id) {
        
        log.info("REST: Deleting relationship {}", id);
        relationshipService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Relationship deleted successfully", null));
    }
}
