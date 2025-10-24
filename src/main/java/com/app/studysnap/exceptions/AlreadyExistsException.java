package com.app.studysnap.exceptions;

/**
 * Thrown when creating a resource that already exists.
 * <p>Use for duplicate email/username, etc.</p>
 */
public class AlreadyExistsException extends AppException {
    /**
     * Creates a new {@code AlreadyExistsException} with a detail message.
     * @param message human-readable explanation of the duplication conflict
     */
    public AlreadyExistsException(String message) {
        super(message);
    }

    /**
     * Creates a new {@code AlreadyExistsException} with a detail message and root cause.
     * @param message human-readable explanation of the duplication conflict
     * @param cause underlying cause (e.g., a database unique-constraint exception)
     */
    public AlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}