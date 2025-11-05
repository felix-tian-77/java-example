package com.example.threadcontroller.util;

/**
 * Exception thrown when attempting to submit tasks after shutdown has been initiated.
 */
public class ShutdownException extends ThreadControllerException {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message
     */
    public ShutdownException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public ShutdownException(String message, Throwable cause) {
        super(message, cause);
    }
}