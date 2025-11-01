package com.app.customer.exception;

/**
 * Exception thrown when a user tries to access/modify a customer profile they don't own
 */
public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
