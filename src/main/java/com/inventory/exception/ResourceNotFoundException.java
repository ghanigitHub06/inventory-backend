package com.inventory.exception;

// RuntimeException — no need to declare it in method signatures
// Spring's @Transactional rolls back on RuntimeException by default
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}