package com.example.threadcontroller.service;

import com.example.threadcontroller.util.Logger;
import com.example.threadcontroller.util.ShutdownException;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Guard for preventing task submissions after shutdown has been initiated.
 */
public class TaskSubmissionGuard {
    private static final Logger logger = new Logger(TaskSubmissionGuard.class);

    private final AtomicBoolean shutdownInitiated;
    private final String componentName;

    /**
     * Constructor.
     *
     * @param componentName the name of the component being guarded
     */
    public TaskSubmissionGuard(String componentName) {
        this.shutdownInitiated = new AtomicBoolean(false);
        this.componentName = componentName;
        logger.info("Created TaskSubmissionGuard for component: {}", componentName);
    }

    /**
     * Checks if task submission is allowed and throws an exception if not.
     *
     * @throws ShutdownException if shutdown has been initiated
     */
    public void checkSubmissionAllowed() throws ShutdownException {
        if (shutdownInitiated.get()) {
            String message = String.format("Cannot submit task: %s has been shut down", componentName);
            logger.warn(message);
            throw new ShutdownException(message);
        }
    }

    /**
     * Checks if task submission is allowed without throwing an exception.
     *
     * @return true if submission is allowed, false otherwise
     */
    public boolean isSubmissionAllowed() {
        boolean allowed = !shutdownInitiated.get();
        if (!allowed) {
            logger.debug("Task submission is not allowed for component: {}", componentName);
        }
        return allowed;
    }

    /**
     * Initiates the shutdown guard.
     */
    public void initiateShutdown() {
        boolean wasShutdown = shutdownInitiated.getAndSet(true);
        if (!wasShutdown) {
            logger.info("Shutdown initiated for component: {}", componentName);
        } else {
            logger.debug("Shutdown already initiated for component: {}", componentName);
        }
    }

    /**
     * Checks if shutdown has been initiated.
     *
     * @return true if shutdown has been initiated, false otherwise
     */
    public boolean isShutdownInitiated() {
        return shutdownInitiated.get();
    }

    /**
     * Resets the shutdown guard (for testing purposes).
     */
    public void reset() {
        boolean wasShutdown = shutdownInitiated.getAndSet(false);
        if (wasShutdown) {
            logger.info("Reset shutdown guard for component: {}", componentName);
        }
    }

    /**
     * Gets the component name.
     *
     * @return the component name
     */
    public String getComponentName() {
        return componentName;
    }
}