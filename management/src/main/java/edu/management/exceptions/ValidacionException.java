package edu.management.exceptions;

public class ValidacionException extends Exception {
    public ValidacionException(String message) {
        super(message);
    }
    
    public ValidacionException(String message, Throwable cause) {
        super(message, cause);
    }
}