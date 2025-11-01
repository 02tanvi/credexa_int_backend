package com.app.product.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.product.dto.ProductBalanceTypeRequest;
import com.app.product.dto.ProductBalanceTypeResponse;
import com.app.product.entity.Product;
import com.app.product.entity.ProductBalanceType;
import com.app.product.exception.ProductNotFoundException;
import com.app.product.repository.ProductBalanceTypeRepository;
import com.app.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductBalanceTypeService {

    private final ProductBalanceTypeRepository balanceTypeRepository;
    private final ProductRepository productRepository;

    @Transactional
    public ProductBalanceTypeResponse addBalanceType(Long productId, ProductBalanceTypeRequest request) {
        log.info("Adding balance type to product {}: {}", productId, request.getBalanceType());
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));
        
        ProductBalanceType balType = ProductBalanceType.builder()
                .product(product)
                .balanceType(request.getBalanceType())
                .description(request.getDescription())
                .build();
        
        balType = balanceTypeRepository.save(balType);
        return toResponse(balType);
    }

    public List<ProductBalanceTypeResponse> getBalanceTypesByProduct(Long productId) {
        log.info("Fetching balance types for product {}", productId);
        return balanceTypeRepository.findByProductId(productId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProductBalanceTypeResponse getById(Long id) {
        log.info("Fetching balance type by ID: {}", id);
        ProductBalanceType balType = balanceTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Balance type not found with ID: " + id));
        return toResponse(balType);
    }

    @Transactional
    public ProductBalanceTypeResponse update(Long id, ProductBalanceTypeRequest request) {
        log.info("Updating balance type {}", id);
        
        ProductBalanceType balType = balanceTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Balance type not found with ID: " + id));
        
        if (request.getDescription() != null) balType.setDescription(request.getDescription());
        if (request.getBalanceType() != null) balType.setBalanceType(request.getBalanceType());
        
        balType = balanceTypeRepository.save(balType);
        return toResponse(balType);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting balance type {}", id);
        balanceTypeRepository.deleteById(id);
    }

    private ProductBalanceTypeResponse toResponse(ProductBalanceType balType) {
        return ProductBalanceTypeResponse.builder()
                .id(balType.getId())
                .balanceType(balType.getBalanceType())
                .description(balType.getDescription())
                .build();
    }
}
