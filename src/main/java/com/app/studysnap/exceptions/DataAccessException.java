package com.app.studysnap.exceptions;

// Throws for database access error
public class DataAccessException extends AppException {
    public DataAccessException(String message) { super(message); }
    public DataAccessException(String message, Throwable cause) { super(message, cause); }
}