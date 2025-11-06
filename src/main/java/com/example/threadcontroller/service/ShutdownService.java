package com.example.threadcontroller.service;

import com.example.threadcontroller.model.ThreadController;
import com.example.threadcontroller.util.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service for handling graceful shutdown of the thread controller.
 */
public class ShutdownService {
    private static final Logger logger = new Logger(ShutdownService.class);

    private final ThreadController threadController;
    private final ExecutorService executorService;
    private final ResourceCleanupService resourceCleanupService;
    private final AtomicBoolean shutdownInitiated;
    private final Object shutdownLock = new Object();

    /**
     * Constructor.
     *
     * @param threadController the thread controller to shutdown
     * @param executorService the executor service to shutdown
     */
    public ShutdownService(ThreadController threadController, ExecutorService executorService) {
        this.threadController = threadController;
        this.executorService = executorService;
        this.resourceCleanupService = new ResourceCleanupService();
        this.shutdownInitiated = new AtomicBoolean(false);
        logger.info("Created ShutdownService");
    }

    /**
     * Initiates graceful shutdown of the thread controller.
     *
     * @param timeout the timeout for waiting for tasks to complete
     * @param unit the time unit for the timeout
     * @return true if shutdown completed successfully, false if timeout occurred
     */
    public boolean shutdown(long timeout, TimeUnit unit) {
        synchronized (shutdownLock) {
            if (shutdownInitiated.get()) {
                logger.warn("Shutdown already initiated");
                return true;
            }

            logger.info("Initiating graceful shutdown with timeout: {} {}", timeout, unit);
            shutdownInitiated.set(true);

            // Mark the thread controller as shutdown
            threadController.setShutdown(true);

            // First phase: shutdown (stop accepting new tasks)
            if (executorService != null) {
                executorService.shutdown();
                logger.info("Executor service shutdown initiated");
            }

            // Second phase: await termination (wait for running tasks to complete)
            boolean terminated = awaitTermination(timeout, unit);

            // Cleanup resources
            resourceCleanupService.cleanupAll();

            logger.info("Graceful shutdown completed. Terminated: {}", terminated);
            return terminated;
        }
    }

    /**
     * Initiates immediate shutdown of the thread controller.
     */
    public void shutdownNow() {
        synchronized (shutdownLock) {
            if (shutdownInitiated.get()) {
                logger.warn("Shutdown already initiated");
                return;
            }

            logger.info("Initiating immediate shutdown");
            shutdownInitiated.set(true);

            // Mark the thread controller as shutdown
            threadController.setShutdown(true);

            // Immediate shutdown
            if (executorService != null) {
                executorService.shutdownNow();
                logger.info("Executor service shutdownNow initiated");
            }

            // Cleanup resources
            resourceCleanupService.cleanupAll();

            logger.info("Immediate shutdown completed");
        }
    }

    /**
     * Waits for the executor service to terminate.
     *
     * @param timeout the timeout for waiting
     * @param unit the time unit for the timeout
     * @return true if terminated successfully, false if timeout occurred
     */
    private boolean awaitTermination(long timeout, TimeUnit unit) {
        if (executorService == null) {
            return true;
        }

        try {
            logger.info("Awaiting termination with timeout: {} {}", timeout, unit);
            boolean terminated = executorService.awaitTermination(timeout, unit);
            if (terminated) {
                logger.info("Executor service terminated successfully");
            } else {
                logger.warn("Executor service did not terminate within timeout");
            }
            return terminated;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Interrupted while awaiting termination");
            return false;
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
     * Checks if the shutdown process has completed.
     *
     * @return true if shutdown is complete, false otherwise
     */
    public boolean isShutdownComplete() {
        if (executorService == null) {
            return shutdownInitiated.get();
        }
        return shutdownInitiated.get() && executorService.isTerminated();
    }
}