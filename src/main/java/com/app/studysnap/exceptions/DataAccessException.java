package com.app.studysnap.exceptions;

/**
 * Thrown for DAO/database failures.
 * <p>Wrap lower-level SQL/IO errors in the data layer.</p>
 */
public class DataAccessException extends AppException {
    /**
     * Creates a new {@code DataAccessException} with a detail message and root cause.
     * @param message human-readable explanation of the authentication failure
     */
    public DataAccessException(String message) {
        super(message);
    }

    /**
     * Creates a new {@code DataAccessException} with a detail message and root cause.
     * @param message human-readable explanation of the authentication failure
     * @param cause underlying cause (e.g., provider SDK error)
     */
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}