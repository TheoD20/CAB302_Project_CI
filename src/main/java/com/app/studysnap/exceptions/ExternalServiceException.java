package com.app.studysnap.exceptions;

/**
 * Thrown when an external service call fails.
 * <p>Use for OAuth/network/API errors (e.g., Google).</p>
 */
public class ExternalServiceException extends AppException {

    /**
     * Creates a new {@code ExternalServiceException} with a detail message and root cause.
     * @param message human-readable explanation of the authentication failure
     */
    public ExternalServiceException(String message) {
        super(message);
    }

    /**
     * Creates a new {@code ExternalServiceException} with a detail message and root cause.
     * @param message human-readable explanation of the authentication failure
     * @param cause underlying cause (e.g., provider SDK error)
     */
    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}