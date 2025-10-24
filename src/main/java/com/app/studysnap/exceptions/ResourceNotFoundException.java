package com.app.studysnap.exceptions;

/**
 * Thrown when a requested resource is not found.
 * <p>Use for missing users, quizzes, or lookups by ID/email.</p>
 */
public class ResourceNotFoundException extends AppException {

    /**
     * Creates a new {@code ResourceNotFoundException} with a detail message and root cause.
     * @param message human-readable explanation of the authentication failure
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates a new {@code ResourceNotFoundException} with a detail message and root cause.
     * @param message human-readable explanation of the authentication failure
     * @param cause underlying cause (e.g., provider SDK error)
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}