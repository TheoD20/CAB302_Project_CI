package com.app.studysnap.exceptions;

// Indicates third-party API failure (network, bad response, rate limit).
public class ExternalServiceException extends AppException {
    public ExternalServiceException(String message) { super(message); }
    public ExternalServiceException(String message, Throwable cause) { super(message, cause); }
}