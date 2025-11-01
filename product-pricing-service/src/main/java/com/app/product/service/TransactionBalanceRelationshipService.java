package com.app.product.service;

import com.app.product.dto.TransactionBalanceRelationshipRequest;
import com.app.product.dto.TransactionBalanceRelationshipResponse;
import com.app.product.entity.TransactionBalanceRelationship;
import com.app.product.enums.BalanceType;
import com.app.product.enums.TransactionType;
import com.app.product.exception.ResourceNotFoundException;
import com.app.product.repository.TransactionBalanceRelationshipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing transaction to balance relationships
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransactionBalanceRelationshipService {

    private final TransactionBalanceRelationshipRepository relationshipRepository;

    /**
     * Create a new transaction-balance relationship
     */
    public TransactionBalanceRelationshipResponse create(TransactionBalanceRelationshipRequest request) {
        log.info("Creating transaction-balance relationship: {}→{} ({})", 
                request.getTransactionType(), request.getBalanceType(), request.getImpactType());
        
        TransactionBalanceRelationship relationship = TransactionBalanceRelationship.builder()
                .transactionType(request.getTransactionType())
                .balanceType(request.getBalanceType())
                .impactType(request.getImpactType())
                .description(request.getDescription())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        TransactionBalanceRelationship saved = relationshipRepository.save(relationship);
        log.info("Relationship created successfully with id {}", saved.getId());
        
        return toResponse(saved);
    }

    /**
     * Get all relationships
     */
    @Transactional(readOnly = true)
    public List<TransactionBalanceRelationshipResponse> getAll() {
        log.info("Fetching all transaction-balance relationships");
        
        return relationshipRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get relationship by ID
     */
    @Transactional(readOnly = true)
    public TransactionBalanceRelationshipResponse getById(Long id) {
        log.info("Fetching relationship with id {}", id);
        
        TransactionBalanceRelationship relationship = relationshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relationship not found with id: " + id));
        
        return toResponse(relationship);
    }

    /**
     * Get relationships by transaction type
     */
    @Transactional(readOnly = true)
    public List<TransactionBalanceRelationshipResponse> getByTransactionType(TransactionType transactionType) {
        log.info("Fetching relationships for transaction type {}", transactionType);
        
        return relationshipRepository.findByTransactionType(transactionType).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get relationships by balance type
     */
    @Transactional(readOnly = true)
    public List<TransactionBalanceRelationshipResponse> getByBalanceType(BalanceType balanceType) {
        log.info("Fetching relationships for balance type {}", balanceType);
        
        return relationshipRepository.findByBalanceType(balanceType).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get relationship by transaction and balance type
     */
    @Transactional(readOnly = true)
    public TransactionBalanceRelationshipResponse getByTransactionAndBalance(
            TransactionType transactionType, BalanceType balanceType) {
        log.info("Fetching relationship for {}→{}", transactionType, balanceType);
        
        TransactionBalanceRelationship relationship = relationshipRepository
                .findByTransactionTypeAndBalanceType(transactionType, balanceType)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Relationship not found for " + transactionType + "→" + balanceType));
        
        return toResponse(relationship);
    }

    /**
     * Get all active relationships
     */
    @Transactional(readOnly = true)
    public List<TransactionBalanceRelationshipResponse> getAllActive() {
        log.info("Fetching all active relationships");
        
        return relationshipRepository.findAllActive().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update relationship
     */
    public TransactionBalanceRelationshipResponse update(Long id, TransactionBalanceRelationshipRequest request) {
        log.info("Updating relationship {}", id);
        
        TransactionBalanceRelationship relationship = relationshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Relationship not found with id: " + id));

        relationship.setTransactionType(request.getTransactionType());
        relationship.setBalanceType(request.getBalanceType());
        relationship.setImpactType(request.getImpactType());
        relationship.setDescription(request.getDescription());
        relationship.setActive(request.getActive() != null ? request.getActive() : true);

        TransactionBalanceRelationship updated = relationshipRepository.save(relationship);
        log.info("Relationship {} updated successfully", id);
        
        return toResponse(updated);
    }

    /**
     * Delete relationship
     */
    public void delete(Long id) {
        log.info("Deleting relationship {}", id);
        
        if (!relationshipRepository.existsById(id)) {
            throw new ResourceNotFoundException("Relationship not found with id: " + id);
        }

        relationshipRepository.deleteById(id);
        log.info("Relationship {} deleted successfully", id);
    }

    /**
     * Convert entity to response DTO
     */
    private TransactionBalanceRelationshipResponse toResponse(TransactionBalanceRelationship relationship) {
        return TransactionBalanceRelationshipResponse.builder()
                .id(relationship.getId())
                .transactionType(relationship.getTransactionType())
                .balanceType(relationship.getBalanceType())
                .impactType(relationship.getImpactType())
                .description(relationship.getDescription())
                .active(relationship.getActive())
                .build();
    }
}
