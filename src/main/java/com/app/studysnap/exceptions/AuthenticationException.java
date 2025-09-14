package com.app.studysnap.exceptions;

// Thrown when authentication fails (bad credentials, wrong provider, etc.).
public class AuthenticationException extends AppException {
    public AuthenticationException(String message) { super(message); }
    public AuthenticationException(String message, Throwable cause) { super(message, cause); }
}