package com.app.studysnap.exceptions;

/**
 * Thrown when creating a resource that already exists.
 * <p>Use for duplicate email/username, etc.</p>
 */
public class AlreadyExistsException extends AppException {
    public AlreadyExistsException(String message) { super(message); }
    public AlreadyExistsException(String message, Throwable cause) { super(message, cause); }
}