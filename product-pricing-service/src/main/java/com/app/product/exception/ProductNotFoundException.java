package com.app.product.exception;

public class ProductNotFoundException extends RuntimeException {
    
    public ProductNotFoundException(String message) {
        super(message);
    }
    
    public ProductNotFoundException(Long productId) {
        super("Product not found with ID: " + productId);
    }
    
    public ProductNotFoundException(String field, String value) {
        super(String.format("Product not found with %s: %s", field, value));
    }
}
