package com.app.studysnap.exceptions;

/**
 * Thrown when a requested resource is not found.
 * <p>Use for missing users, quizzes, or lookups by ID/email.</p>
 */
public class ResourceNotFoundException extends AppException {
    public ResourceNotFoundException(String message) { super(message); }
    public ResourceNotFoundException(String message, Throwable cause) { super(message, cause); }
}