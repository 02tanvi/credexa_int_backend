package com.app.fdaccount.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.fdaccount.dto.PrematureWithdrawalInquiryRequest;
import com.app.fdaccount.dto.PrematureWithdrawalInquiryResponse;
import com.app.fdaccount.dto.TransactionRequest;
import com.app.fdaccount.dto.TransactionResponse;
import com.app.fdaccount.service.PrematureWithdrawalService;
import com.app.fdaccount.service.TransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for Transaction operations
 */
@Slf4j
@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "APIs for managing FD account transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final PrematureWithdrawalService prematureWithdrawalService;

    /**
     * Create a new transaction
     */
    @PostMapping
    @Operation(summary = "Create Transaction",
               description = "Create a new transaction on an FD account")
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionRequest request) {
        
        log.info("REST: Creating transaction for account: {}", request.getAccountNumber());
        TransactionResponse response = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Reverse a transaction
     */
    @PostMapping("/{transactionReference}/reverse")
    @Operation(summary = "Reverse Transaction",
               description = "Reverse a transaction by its reference number")
    public ResponseEntity<TransactionResponse> reverseTransaction(
            @PathVariable String transactionReference,
            @Parameter(description = "Reason for reversal")
            @RequestParam String reason,
            @Parameter(description = "User performing the reversal")
            @RequestParam String performedBy) {
        
        log.info("REST: Reversing transaction: {}", transactionReference);
        TransactionResponse response = transactionService.reverseTransaction(
                transactionReference, reason, performedBy);
        return ResponseEntity.ok(response);
    }

    /**
     * Get transaction by reference
     */
    @GetMapping("/{transactionReference}")
    @Operation(summary = "Get Transaction",
               description = "Get transaction details by reference number")
    public ResponseEntity<TransactionResponse> getTransaction(
            @PathVariable String transactionReference) {
        
        log.info("REST: Fetching transaction: {}", transactionReference);
        TransactionResponse response = transactionService.getTransactionByReference(transactionReference);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all transactions for an account
     */
    @GetMapping("/account/{accountNumber}")
    @Operation(summary = "Get Account Transactions",
               description = "Get all transactions for a specific account")
    public ResponseEntity<List<TransactionResponse>> getAccountTransactions(
            @PathVariable String accountNumber) {
        
        log.info("REST: Fetching transactions for account: {}", accountNumber);
        List<TransactionResponse> response = transactionService.getAccountTransactions(accountNumber);
        return ResponseEntity.ok(response);
    }

    /**
     * Get transactions with pagination
     */
    @GetMapping("/account/{accountNumber}/paged")
    @Operation(summary = "Get Account Transactions (Paged)",
               description = "Get transactions for an account with pagination")
    public ResponseEntity<Page<TransactionResponse>> getAccountTransactionsPaged(
            @PathVariable String accountNumber,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("REST: Fetching paged transactions for account: {} (page: {}, size: {})",
                accountNumber, page, size);
        Page<TransactionResponse> response = transactionService.getAccountTransactionsPaged(
                accountNumber, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Inquire about premature withdrawal
     */
    @PostMapping("/premature-withdrawal/inquire")
    @Operation(summary = "Premature Withdrawal Inquiry",
               description = "Get details about premature withdrawal including penalty and net amount")
    public ResponseEntity<PrematureWithdrawalInquiryResponse> inquirePrematureWithdrawal(
            @Valid @RequestBody PrematureWithdrawalInquiryRequest request) {
        
        log.info("REST: Premature withdrawal inquiry for account: {}", request.getAccountNumber());
        PrematureWithdrawalInquiryResponse response = 
                prematureWithdrawalService.inquirePrematureWithdrawal(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Process premature withdrawal
     */
    @PostMapping("/premature-withdrawal/process")
    @Operation(summary = "Process Premature Withdrawal",
               description = "Process premature withdrawal for an FD account with penalty")
    public ResponseEntity<TransactionResponse> processPrematureWithdrawal(
            @Parameter(description = "Account number")
            @RequestParam String accountNumber,
            @Parameter(description = "Withdrawal date")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate withdrawalDate,
            @Parameter(description = "User performing the withdrawal")
            @RequestParam String performedBy,
            @Parameter(description = "Additional remarks")
            @RequestParam(required = false) String remarks) {
        
        log.info("REST: Processing premature withdrawal for account: {}", accountNumber);
        TransactionResponse response = prematureWithdrawalService.processPrematureWithdrawal(
                accountNumber, withdrawalDate, performedBy, remarks);
        return ResponseEntity.ok(response);
    }
}
