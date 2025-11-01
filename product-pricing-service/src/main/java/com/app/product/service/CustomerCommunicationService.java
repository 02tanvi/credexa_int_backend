package com.app.product.service;

import com.app.product.dto.CustomerCommunicationRequest;
import com.app.product.dto.CustomerCommunicationResponse;
import com.app.product.entity.CustomerCommunication;
import com.app.product.entity.Product;
import com.app.product.exception.ResourceNotFoundException;
import com.app.product.repository.CustomerCommunicationRepository;
import com.app.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing customer communication configurations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerCommunicationService {

    private final CustomerCommunicationRepository communicationRepository;
    private final ProductRepository productRepository;

    /**
     * Add communication configuration to a product
     */
    public CustomerCommunicationResponse addCommunication(Long productId, CustomerCommunicationRequest request) {
        log.info("Adding communication to product {}: type={}, event={}", 
                productId, request.getCommunicationType(), request.getEvent());
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        CustomerCommunication communication = CustomerCommunication.builder()
                .product(product)
                .communicationType(request.getCommunicationType())
                .event(request.getEvent())
                .template(request.getTemplate())
                .subject(request.getSubject())
                .content(request.getContent())
                .mandatory(request.getMandatory() != null ? request.getMandatory() : false)
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        CustomerCommunication saved = communicationRepository.save(communication);
        log.info("Communication added successfully with id {}", saved.getId());
        
        return toResponse(saved);
    }

    /**
     * Get all communications for a product
     */
    @Transactional(readOnly = true)
    public List<CustomerCommunicationResponse> getCommunicationsByProduct(Long productId) {
        log.info("Fetching communications for product {}", productId);
        
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        return communicationRepository.findByProductId(productId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get communication by ID
     */
    @Transactional(readOnly = true)
    public CustomerCommunicationResponse getById(Long id) {
        log.info("Fetching communication with id {}", id);
        
        CustomerCommunication communication = communicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Communication not found with id: " + id));
        
        return toResponse(communication);
    }

    /**
     * Get communications by type
     */
    @Transactional(readOnly = true)
    public List<CustomerCommunicationResponse> getCommunicationsByType(Long productId, String type) {
        log.info("Fetching communications of type {} for product {}", type, productId);
        
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        return communicationRepository.findByProductIdAndType(productId, type).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get communications by event
     */
    @Transactional(readOnly = true)
    public List<CustomerCommunicationResponse> getCommunicationsByEvent(Long productId, String event) {
        log.info("Fetching communications for event {} on product {}", event, productId);
        
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        return communicationRepository.findByProductIdAndEvent(productId, event).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update communication configuration
     */
    public CustomerCommunicationResponse update(Long id, CustomerCommunicationRequest request) {
        log.info("Updating communication {}", id);
        
        CustomerCommunication communication = communicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Communication not found with id: " + id));

        communication.setCommunicationType(request.getCommunicationType());
        communication.setEvent(request.getEvent());
        communication.setTemplate(request.getTemplate());
        communication.setSubject(request.getSubject());
        communication.setContent(request.getContent());
        communication.setMandatory(request.getMandatory() != null ? request.getMandatory() : false);
        communication.setActive(request.getActive() != null ? request.getActive() : true);

        CustomerCommunication updated = communicationRepository.save(communication);
        log.info("Communication {} updated successfully", id);
        
        return toResponse(updated);
    }

    /**
     * Delete communication configuration
     */
    public void delete(Long id) {
        log.info("Deleting communication {}", id);
        
        if (!communicationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Communication not found with id: " + id);
        }

        communicationRepository.deleteById(id);
        log.info("Communication {} deleted successfully", id);
    }

    /**
     * Convert entity to response DTO
     */
    private CustomerCommunicationResponse toResponse(CustomerCommunication communication) {
        return CustomerCommunicationResponse.builder()
                .id(communication.getId())
                .productId(communication.getProduct().getId())
                .communicationType(communication.getCommunicationType())
                .event(communication.getEvent())
                .template(communication.getTemplate())
                .subject(communication.getSubject())
                .mandatory(communication.getMandatory())
                .active(communication.getActive())
                .build();
    }
}
