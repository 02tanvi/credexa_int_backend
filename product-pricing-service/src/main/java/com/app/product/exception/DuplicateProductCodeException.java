package com.app.product.exception;

public class DuplicateProductCodeException extends RuntimeException {
    
    public DuplicateProductCodeException(String productCode) {
        super("Product with code '" + productCode + "' already exists");
    }
}
