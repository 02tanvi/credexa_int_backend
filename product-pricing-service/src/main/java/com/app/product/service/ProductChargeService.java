package com.app.product.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.product.dto.ProductChargeRequest;
import com.app.product.dto.ProductChargeResponse;
import com.app.product.entity.Product;
import com.app.product.entity.ProductCharge;
import com.app.product.exception.ProductNotFoundException;
import com.app.product.repository.ProductChargeRepository;
import com.app.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductChargeService {

    private final ProductChargeRepository chargeRepository;
    private final ProductRepository productRepository;

    /**
     * Add a charge to a product
     */
    @Transactional
    public ProductChargeResponse addCharge(Long productId, ProductChargeRequest request) {
        log.info("Adding charge to product {}: {}", productId, request.getChargeName());
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));
        
        ProductCharge charge = ProductCharge.builder()
                .product(product)
                .chargeName(request.getChargeName())
                .chargeType(request.getChargeType())
                .description(request.getDescription())
                .fixedAmount(request.getFixedAmount())
                .percentageRate(request.getPercentageRate())
                .frequency(request.getFrequency())
                .waivable(request.getWaivable())
                .build();
        
        charge = chargeRepository.save(charge);
        return toResponse(charge);
    }

    /**
     * Get all charges for a product
     */
    public List<ProductChargeResponse> getChargesByProduct(Long productId) {
        log.info("Fetching charges for product {}", productId);
        return chargeRepository.findByProductId(productId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get charge by ID
     */
    public ProductChargeResponse getChargeById(Long chargeId) {
        log.info("Fetching charge by ID: {}", chargeId);
        ProductCharge charge = chargeRepository.findById(chargeId)
                .orElseThrow(() -> new RuntimeException("Charge not found with ID: " + chargeId));
        return toResponse(charge);
    }

    /**
     * Update a charge
     */
    @Transactional
    public ProductChargeResponse updateCharge(Long chargeId, ProductChargeRequest request) {
        log.info("Updating charge {}", chargeId);
        
        ProductCharge charge = chargeRepository.findById(chargeId)
                .orElseThrow(() -> new RuntimeException("Charge not found with ID: " + chargeId));
        
        if (request.getChargeName() != null) charge.setChargeName(request.getChargeName());
        if (request.getChargeType() != null) charge.setChargeType(request.getChargeType());
        if (request.getDescription() != null) charge.setDescription(request.getDescription());
        if (request.getFixedAmount() != null) charge.setFixedAmount(request.getFixedAmount());
        if (request.getPercentageRate() != null) charge.setPercentageRate(request.getPercentageRate());
        if (request.getFrequency() != null) charge.setFrequency(request.getFrequency());
        if (request.getWaivable() != null) charge.setWaivable(request.getWaivable());
        
        charge = chargeRepository.save(charge);
        return toResponse(charge);
    }

    /**
     * Delete a charge
     */
    @Transactional
    public void deleteCharge(Long chargeId) {
        log.info("Deleting charge {}", chargeId);
        chargeRepository.deleteById(chargeId);
    }

    /**
     * Get charges by type
     */
    public List<ProductChargeResponse> getChargesByType(Long productId, String chargeType) {
        log.info("Fetching charges of type {} for product {}", chargeType, productId);
        return chargeRepository.findByProductIdAndChargeType(productId, chargeType).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ProductChargeResponse toResponse(ProductCharge charge) {
        return ProductChargeResponse.builder()
                .id(charge.getId())
                .chargeName(charge.getChargeName())
                .chargeType(charge.getChargeType())
                .description(charge.getDescription())
                .fixedAmount(charge.getFixedAmount())
                .percentageRate(charge.getPercentageRate())
                .frequency(charge.getFrequency())
                .waivable(charge.getWaivable())
                .active(true)
                .build();
    }
}
