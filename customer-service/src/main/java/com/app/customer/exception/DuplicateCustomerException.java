package com.app.customer.exception;

/**
 * Exception thrown when there's a duplicate customer entry
 */
public class DuplicateCustomerException extends RuntimeException {
    public DuplicateCustomerException(String message) {
        super(message);
    }
}
