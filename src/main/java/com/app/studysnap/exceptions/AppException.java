package com.app.studysnap.exceptions;

/**
 * Base runtime exception for StudySnap.
 * <p>Extend this for all app-specific errors.</p>
 */
public class AppException extends RuntimeException {
    public AppException(String message) { super(message); }
    public AppException(String message, Throwable cause) { super(message, cause); }
}
