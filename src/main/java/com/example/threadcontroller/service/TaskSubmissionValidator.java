package com.example.threadcontroller.service;

import com.example.threadcontroller.util.Logger;

/**
 * Validator for task submission parameters.
 */
public class TaskSubmissionValidator {
    private static final Logger logger = new Logger(TaskSubmissionValidator.class);

    /**
     * Validates task submission parameters.
     *
     * @param taskName the task name
     * @param taskDuration the task duration
     * @param poolSize the pool size
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public static void validateTaskSubmission(String taskName, long taskDuration, int poolSize) {
        if (taskName == null || taskName.trim().isEmpty()) {
            throw new IllegalArgumentException("Task name cannot be null or empty");
        }

        if (taskDuration < 0) {
            throw new IllegalArgumentException("Task duration must be non-negative");
        }

        if (poolSize <= 0) {
            throw new IllegalArgumentException("Pool size must be positive");
        }

        logger.debug("Task submission parameters validated successfully: name={}, duration={}, poolSize={}",
                   taskName, taskDuration, poolSize);
    }

    /**
     * Validates thread pool parameters.
     *
     * @param poolSize the pool size
     * @param threadType the thread type
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public static void validateThreadPoolParameters(int poolSize, com.example.threadcontroller.model.ThreadType threadType) {
        if (poolSize <= 0) {
            throw new IllegalArgumentException("Pool size must be positive");
        }

        if (threadType == null) {
            throw new IllegalArgumentException("Thread type cannot be null");
        }

        if (threadType == com.example.threadcontroller.model.ThreadType.VIRTUAL) {
            // Check if virtual threads are supported
            if (!VirtualThreadSupport.isVirtualThreadSupported()) {
                throw new IllegalArgumentException("Virtual threads are not supported in this JVM");
            }
        }

        logger.debug("Thread pool parameters validated successfully: poolSize={}, threadType={}",
                   poolSize, threadType);
    }

    /**
     * Validates task execution parameters.
     *
     * @param task the task to validate
     * @throws IllegalArgumentException if the task is null
     */
    public static void validateTask(Object task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }

        logger.debug("Task validated successfully: {}", task.getClass().getSimpleName());
    }

    /**
     * Validates CompletableFuture parameters.
     *
     * @param future the CompletableFuture to validate
     * @throws IllegalArgumentException if the future is null
     */
    public static void validateCompletableFuture(java.util.concurrent.CompletableFuture<?> future) {
        if (future == null) {
            throw new IllegalArgumentException("CompletableFuture cannot be null");
        }

        logger.debug("CompletableFuture validated successfully");
    }
}