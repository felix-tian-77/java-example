package com.example.threadcontroller.util;

/**
 * Base exception class for thread controller related exceptions.
 */
public class ThreadControllerException extends Exception {

    /**
     * Constructs a new exception with null as its detail message.
     */
    public ThreadControllerException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message
     */
    public ThreadControllerException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public ThreadControllerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified cause.
     *
     * @param cause the cause of the exception
     */
    public ThreadControllerException(Throwable cause) {
        super(cause);
    }
}