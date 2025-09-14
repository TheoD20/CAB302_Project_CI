package com.app.studysnap.exceptions;

// Thrown when user input fail validation rules.
public class ValidationException extends AppException {
    public ValidationException(String message) { super(message); }
    public ValidationException(String message, Throwable cause) { super(message, cause); }
}