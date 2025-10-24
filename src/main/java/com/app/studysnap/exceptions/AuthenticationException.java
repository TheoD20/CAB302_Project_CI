package com.app.studysnap.exceptions;

/**
 * Thrown when authentication fails.
 * <p>Use for wrong credentials or wrong auth provider.</p>
 */
public class AuthenticationException extends AppException {
    /**
     * Creates a new {@code AuthenticationException} with a detail message.
     * @param message human-readable explanation of the authentication failure
     */
    public AuthenticationException(String message) {
        super(message);
    }

    /**
     * Creates a new {@code AuthenticationException} with a detail message and root cause.
     * @param message human-readable explanation of the authentication failure
     * @param cause underlying cause (e.g., provider SDK error)
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}