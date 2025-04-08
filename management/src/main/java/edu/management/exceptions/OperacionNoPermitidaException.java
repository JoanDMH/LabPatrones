package edu.management.exceptions;

public class OperacionNoPermitidaException extends Exception {
    public OperacionNoPermitidaException(String message) {
        super(message);
    }
    
    public OperacionNoPermitidaException(String message, Throwable cause) {
        super(message, cause);
    }
}