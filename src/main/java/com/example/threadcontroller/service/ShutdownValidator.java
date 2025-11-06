package com.example.threadcontroller.service;

import com.example.threadcontroller.util.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Validator for shutdown parameters.
 */
public class ShutdownValidator {
    private static final Logger logger = new Logger(ShutdownValidator.class);

    /**
     * Validates shutdown timeout parameters.
     *
     * @param shutdownTimeout the shutdown timeout
     * @param terminationTimeout the termination timeout
     * @param unit the time unit
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public static void validateShutdownTimeouts(long shutdownTimeout, long terminationTimeout, TimeUnit unit) {
        if (shutdownTimeout < 0) {
            throw new IllegalArgumentException("Shutdown timeout must be non-negative");
        }

        if (terminationTimeout < 0) {
            throw new IllegalArgumentException("Termination timeout must be non-negative");
        }

        if (unit == null) {
            throw new IllegalArgumentException("Time unit cannot be null");
        }

        logger.debug("Shutdown timeouts validated successfully: shutdown={} {}, termination={} {}",
                   shutdownTimeout, unit, terminationTimeout, unit);
    }

    /**
     * Validates a single timeout parameter.
     *
     * @param timeout the timeout value
     * @param unit the time unit
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public static void validateTimeout(long timeout, TimeUnit unit) {
        if (timeout < 0) {
            throw new IllegalArgumentException("Timeout must be non-negative");
        }

        if (unit == null) {
            throw new IllegalArgumentException("Time unit cannot be null");
        }

        logger.debug("Timeout validated successfully: {} {}", timeout, unit);
    }

    /**
     * Validates shutdown service parameters.
     *
     * @param threadController the thread controller
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public static void validateShutdownService(
            com.example.threadcontroller.model.ThreadController threadController) {
        if (threadController == null) {
            throw new IllegalArgumentException("Thread controller cannot be null");
        }

        logger.debug("Shutdown service parameters validated successfully");
    }

    /**
     * Validates graceful shutdown handler parameters.
     *
     * @param threadController the thread controller
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public static void validateGracefulShutdownHandler(
            com.example.threadcontroller.model.ThreadController threadController) {
        if (threadController == null) {
            throw new IllegalArgumentException("Thread controller cannot be null");
        }

        logger.debug("Graceful shutdown handler parameters validated successfully");
    }

    /**
     * Validates that shutdown has not already been initiated.
     *
     * @param shutdownInitiated flag indicating if shutdown has been initiated
     * @throws IllegalStateException if shutdown has already been initiated
     */
    public static void validateShutdownNotInitiated(boolean shutdownInitiated) {
        if (shutdownInitiated) {
            throw new IllegalStateException("Shutdown has already been initiated");
        }

        logger.debug("Verified shutdown has not been initiated");
    }
}