package com.example.threadcontroller.model;

/**
 * Enumeration of task execution states.
 */
public enum TaskStatus {
    /**
     * Task has been submitted but not yet started.
     */
    PENDING,

    /**
     * Task is currently executing.
     */
    RUNNING,

    /**
     * Task completed successfully.
     */
    COMPLETED,

    /**
     * Task failed with an exception.
     */
    FAILED
}