package com.app.studysnap.exceptions;

/**
 * Base runtime exception for StudySnap.
 * <p>Extend this for all app-specific errors.</p>
 */
public class AppException extends RuntimeException {
    /**
     * Creates a new {@code AppException} with a detail message.
     * @param message human-readable explanation of the error
     */
    public AppException(String message) {
        super(message);
    }

    /**
     * Creates a new {@code AppException} with a detail message and root cause.
     * @param message human-readable explanation of the error
     * @param cause underlying cause
     */
    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}
