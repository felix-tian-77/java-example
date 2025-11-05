package com.example.threadcontroller.util;

/**
 * Exception thrown when a task fails during execution.
 */
public class TaskExecutionException extends ThreadControllerException {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message
     */
    public TaskExecutionException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public TaskExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}