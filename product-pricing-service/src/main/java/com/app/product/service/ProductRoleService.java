package com.app.product.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.product.dto.ProductRoleRequest;
import com.app.product.dto.ProductRoleResponse;
import com.app.product.entity.Product;
import com.app.product.entity.ProductRole;
import com.app.product.exception.ProductNotFoundException;
import com.app.product.repository.ProductRepository;
import com.app.product.repository.ProductRoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductRoleService {

    private final ProductRoleRepository roleRepository;
    private final ProductRepository productRepository;

    @Transactional
    public ProductRoleResponse addRole(Long productId, ProductRoleRequest request) {
        log.info("Adding role to product {}: {}", productId, request.getRoleType());
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));
        
        ProductRole role = ProductRole.builder()
                .product(product)
                .roleType(request.getRoleType())
                .mandatory(request.getMandatory())
                .minCount(request.getMinCount())
                .maxCount(request.getMaxCount())
                .description(request.getDescription())
                .build();
        
        role = roleRepository.save(role);
        return toResponse(role);
    }

    public List<ProductRoleResponse> getRolesByProduct(Long productId) {
        log.info("Fetching roles for product {}", productId);
        return roleRepository.findByProductId(productId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProductRoleResponse getRoleById(Long roleId) {
        log.info("Fetching role by ID: {}", roleId);
        ProductRole role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));
        return toResponse(role);
    }

    @Transactional
    public ProductRoleResponse updateRole(Long roleId, ProductRoleRequest request) {
        log.info("Updating role {}", roleId);
        
        ProductRole role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));
        
        if (request.getRoleType() != null) role.setRoleType(request.getRoleType());
        if (request.getMandatory() != null) role.setMandatory(request.getMandatory());
        if (request.getMinCount() != null) role.setMinCount(request.getMinCount());
        if (request.getMaxCount() != null) role.setMaxCount(request.getMaxCount());
        if (request.getDescription() != null) role.setDescription(request.getDescription());
        
        role = roleRepository.save(role);
        return toResponse(role);
    }

    @Transactional
    public void deleteRole(Long roleId) {
        log.info("Deleting role {}", roleId);
        roleRepository.deleteById(roleId);
    }

    public List<ProductRoleResponse> getRolesByType(Long productId, String roleType) {
        log.info("Fetching roles of type {} for product {}", roleType, productId);
        return roleRepository.findByProductId(productId).stream()
                .filter(role -> role.getRoleType().name().equals(roleType))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ProductRoleResponse toResponse(ProductRole role) {
        return ProductRoleResponse.builder()
                .id(role.getId())
                .roleType(role.getRoleType())
                .mandatory(role.getMandatory())
                .minCount(role.getMinCount())
                .maxCount(role.getMaxCount())
                .description(role.getDescription())
                .build();
    }
}
