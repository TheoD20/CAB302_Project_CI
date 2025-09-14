package com.app.studysnap.exceptions;

// Base exception for all errors.
public class AppException extends RuntimeException {
    public AppException(String message) { super(message); }
    public AppException(String message, Throwable cause) { super(message, cause); }
}
