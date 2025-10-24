package com.app.studysnap.exceptions;

/**
 * Thrown when input fails validation.
 * <p>Use for blank/invalid fields or bad formats.</p>
 */
public class ValidationException extends AppException {

    /**
     * Creates a new {@code ValidationException} with a detail message and root cause.
     * @param message human-readable explanation of the authentication failure
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Creates a new {@code ValidationException} with a detail message and root cause.
     * @param message human-readable explanation of the authentication failure
     * @param cause underlying cause (e.g., provider SDK error)
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}