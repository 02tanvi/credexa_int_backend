package com.app.customer.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.customer.dto.CreateCustomerRequest;
import com.app.customer.dto.Customer360Response;
import com.app.customer.dto.CustomerClassificationResponse;
import com.app.customer.dto.CustomerResponse;
import com.app.customer.dto.UpdateCustomerRequest;
import com.app.customer.service.CustomerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for customer operations
 */
@RestController
@RequestMapping  // Empty mapping since context-path is already /api/customer
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Management", description = "APIs for managing customer information")
@SecurityRequirement(name = "Bearer Authentication")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @Operation(summary = "Create new customer", description = "Create a new customer profile. Regular users can only create for themselves, admins can create for any user.")
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request,
            Authentication authentication) {
        
        String authenticatedUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
        
        log.info("User '{}' (Admin: {}) creating customer profile", 
                authenticatedUsername, isAdmin);
        
        CustomerResponse response = customerService.createCustomer(request, authenticatedUsername, isAdmin);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID", description = "Retrieve customer details by customer ID")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        log.info("Received request to get customer by ID: {}", id);
        CustomerResponse response = customerService.getCustomerById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get customer by user ID", description = "Retrieve customer details by user ID from login-service")
    public ResponseEntity<CustomerResponse> getCustomerByUserId(@PathVariable Long userId) {
        log.info("Received request to get customer by user ID: {}", userId);
        CustomerResponse response = customerService.getCustomerByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer", description = "Update customer information. Regular users can only update their own profile, admins can update any profile.")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerRequest request,
            Authentication authentication) {
        
        String authenticatedUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
        
        log.info("User '{}' (Admin: {}) updating customer ID: {}", authenticatedUsername, isAdmin, id);
        CustomerResponse response = customerService.updateCustomer(id, request, authenticatedUsername, isAdmin);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/classification")
    @Operation(summary = "Get customer classification", description = "Get customer classification for FD rate determination")
    public ResponseEntity<CustomerClassificationResponse> getCustomerClassification(@PathVariable Long id) {
        log.info("Received request to get classification for customer ID: {}", id);
        CustomerClassificationResponse response = customerService.getCustomerClassification(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/360-view")
    @Operation(summary = "Get 360-degree customer view", description = "Get comprehensive customer overview including FD accounts")
    public ResponseEntity<Customer360Response> getCustomer360View(@PathVariable Long id) {
        log.info("Received request to get 360-degree view for customer ID: {}", id);
        Customer360Response response = customerService.getCustomer360View(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the customer service is running", security = {})
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Customer Service is UP");
    }
}
