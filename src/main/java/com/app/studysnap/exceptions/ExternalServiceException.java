package com.app.studysnap.exceptions;

/**
 * Thrown when an external service call fails.
 * <p>Use for OAuth/network/API errors (e.g., Google).</p>
 */
public class ExternalServiceException extends AppException {
    public ExternalServiceException(String message) { super(message); }
    public ExternalServiceException(String message, Throwable cause) { super(message, cause); }
}