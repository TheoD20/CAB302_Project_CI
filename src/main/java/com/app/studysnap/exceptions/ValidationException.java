package com.app.studysnap.exceptions;

/**
 * Thrown when input fails validation.
 * <p>Use for blank/invalid fields or bad formats.</p>
 */
public class ValidationException extends AppException {
    public ValidationException(String message) { super(message); }
    public ValidationException(String message, Throwable cause) { super(message, cause); }
}