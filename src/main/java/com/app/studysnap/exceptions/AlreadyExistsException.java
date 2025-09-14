package com.app.studysnap.exceptions;

// Thrown when creating object conflicts with existing instance (existing email).
public class AlreadyExistsException extends AppException {
    public AlreadyExistsException(String message) { super(message); }
    public AlreadyExistsException(String message, Throwable cause) { super(message, cause); }
}