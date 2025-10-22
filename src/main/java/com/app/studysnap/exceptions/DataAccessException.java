package com.app.studysnap.exceptions;

/**
 * Thrown for DAO/database failures.
 * <p>Wrap lower-level SQL/IO errors in the data layer.</p>
 */
public class DataAccessException extends AppException {
    public DataAccessException(String message) { super(message); }
    public DataAccessException(String message, Throwable cause) { super(message, cause); }
}