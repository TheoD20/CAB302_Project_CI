package com.app.studysnap.exceptions;

/**
 * Thrown when authentication fails.
 * <p>Use for wrong credentials or wrong auth provider.</p>
 */
public class AuthenticationException extends AppException {
    public AuthenticationException(String message) { super(message); }
    public AuthenticationException(String message, Throwable cause) { super(message, cause); }
}