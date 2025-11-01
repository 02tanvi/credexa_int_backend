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
import com.app.product.dto.CustomerCommunicationRequest;
import com.app.product.dto.CustomerCommunicationResponse;
import com.app.product.service.CustomerCommunicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for managing customer communication configurations
 */
@RestController
@RequestMapping("/products/{productId}/communications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Communications", description = "APIs for managing customer communication configurations")
public class CustomerCommunicationController {

    private final CustomerCommunicationService communicationService;

    @PostMapping
    @Operation(summary = "Add communication config", description = "Adds a communication configuration to a product")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Communication added successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ApiResponse<CustomerCommunicationResponse>> addCommunication(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Valid @RequestBody CustomerCommunicationRequest request) {
        
        log.info("REST: Adding communication to product {}", productId);
        CustomerCommunicationResponse response = communicationService.addCommunication(productId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Communication added successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all communications", description = "Retrieves all communication configurations for a product")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Communications retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<CustomerCommunicationResponse>>> getCommunications(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        
        log.info("REST: Fetching communications for product {}", productId);
        List<CustomerCommunicationResponse> response = communicationService.getCommunicationsByProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get communications by type", description = "Retrieves communications of a specific type")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Communications retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<CustomerCommunicationResponse>>> getCommunicationsByType(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Communication Type (EMAIL/SMS/PUSH/LETTER)") @PathVariable String type) {
        
        log.info("REST: Fetching communications of type {} for product {}", type, productId);
        List<CustomerCommunicationResponse> response = communicationService.getCommunicationsByType(productId, type);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/event/{event}")
    @Operation(summary = "Get communications by event", description = "Retrieves communications for a specific event")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Communications retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<CustomerCommunicationResponse>>> getCommunicationsByEvent(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Event name") @PathVariable String event) {
        
        log.info("REST: Fetching communications for event {} on product {}", event, productId);
        List<CustomerCommunicationResponse> response = communicationService.getCommunicationsByEvent(productId, event);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

@RestController
@RequestMapping("/communications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Communications Management", description = "Direct communication management APIs")
class CommunicationManagementController {

    private final CustomerCommunicationService communicationService;

    @GetMapping("/{id}")
    @Operation(summary = "Get communication by ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Communication retrieved successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Communication not found")
    })
    public ResponseEntity<ApiResponse<CustomerCommunicationResponse>> getById(
            @Parameter(description = "Communication ID") @PathVariable Long id) {
        
        log.info("REST: Fetching communication {}", id);
        CustomerCommunicationResponse response = communicationService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update communication")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Communication updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Communication not found")
    })
    public ResponseEntity<ApiResponse<CustomerCommunicationResponse>> update(
            @Parameter(description = "Communication ID") @PathVariable Long id,
            @Valid @RequestBody CustomerCommunicationRequest request) {
        
        log.info("REST: Updating communication {}", id);
        CustomerCommunicationResponse response = communicationService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Communication updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete communication")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Communication deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Communication not found")
    })
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Communication ID") @PathVariable Long id) {
        
        log.info("REST: Deleting communication {}", id);
        communicationService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Communication deleted successfully", null));
    }
}
