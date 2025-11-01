package com.app.product.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.product.dto.ProductTransactionTypeRequest;
import com.app.product.dto.ProductTransactionTypeResponse;
import com.app.product.entity.Product;
import com.app.product.entity.ProductTransactionType;
import com.app.product.exception.ProductNotFoundException;
import com.app.product.repository.ProductRepository;
import com.app.product.repository.ProductTransactionTypeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductTransactionTypeService {

    private final ProductTransactionTypeRepository transactionTypeRepository;
    private final ProductRepository productRepository;

    @Transactional
    public ProductTransactionTypeResponse addTransactionType(Long productId, ProductTransactionTypeRequest request) {
        log.info("Adding transaction type to product {}: {}", productId, request.getTransactionType());
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));
        
        ProductTransactionType txnType = ProductTransactionType.builder()
                .product(product)
                .transactionType(request.getTransactionType())
                .description(request.getDescription())
                .build();
        
        txnType = transactionTypeRepository.save(txnType);
        return toResponse(txnType);
    }

    public List<ProductTransactionTypeResponse> getTransactionTypesByProduct(Long productId) {
        log.info("Fetching transaction types for product {}", productId);
        return transactionTypeRepository.findByProductId(productId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProductTransactionTypeResponse getById(Long id) {
        log.info("Fetching transaction type by ID: {}", id);
        ProductTransactionType txnType = transactionTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction type not found with ID: " + id));
        return toResponse(txnType);
    }

    @Transactional
    public ProductTransactionTypeResponse update(Long id, ProductTransactionTypeRequest request) {
        log.info("Updating transaction type {}", id);
        
        ProductTransactionType txnType = transactionTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction type not found with ID: " + id));
        
        if (request.getDescription() != null) txnType.setDescription(request.getDescription());
        if (request.getTransactionType() != null) txnType.setTransactionType(request.getTransactionType());
        
        txnType = transactionTypeRepository.save(txnType);
        return toResponse(txnType);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting transaction type {}", id);
        transactionTypeRepository.deleteById(id);
    }

    private ProductTransactionTypeResponse toResponse(ProductTransactionType txnType) {
        return ProductTransactionTypeResponse.builder()
                .id(txnType.getId())
                .transactionType(txnType.getTransactionType())
                .description(txnType.getDescription())
                .build();
    }
}
