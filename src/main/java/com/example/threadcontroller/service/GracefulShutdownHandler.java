package com.example.threadcontroller.service;

import com.example.threadcontroller.model.ThreadController;
import com.example.threadcontroller.util.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handler for the two-phase graceful shutdown process.
 */
public class GracefulShutdownHandler {
    private static final Logger logger = new Logger(GracefulShutdownHandler.class);

    private final ThreadController threadController;
    private final ExecutorService executorService;
    private final ResourceCleanupService resourceCleanupService;
    private final TaskCompletionTracker taskCompletionTracker;
    private final AtomicBoolean shutdownInitiated;
    private final Object shutdownLock = new Object();

    /**
     * Constructor.
     *
     * @param threadController the thread controller to shutdown
     * @param executorService the executor service to shutdown
     */
    public GracefulShutdownHandler(ThreadController threadController, ExecutorService executorService) {
        this.threadController = threadController;
        this.executorService = executorService;
        this.resourceCleanupService = new ResourceCleanupService();
        this.taskCompletionTracker = new TaskCompletionTracker();
        this.shutdownInitiated = new AtomicBoolean(false);
        logger.info("Created GracefulShutdownHandler");
    }

    /**
     * Executes the two-phase graceful shutdown process.
     *
     * @param shutdownTimeout the timeout for the shutdown phase
     * @param terminationTimeout the timeout for the termination phase
     * @param unit the time unit for the timeouts
     * @return the result of the shutdown process
     */
    public ShutdownResult shutdown(long shutdownTimeout, long terminationTimeout, TimeUnit unit) {
        synchronized (shutdownLock) {
            if (shutdownInitiated.get()) {
                logger.warn("Shutdown already initiated");
                return new ShutdownResult(true, true, 0, "Shutdown already initiated");
            }

            logger.info("Initiating two-phase graceful shutdown");
            shutdownInitiated.set(true);

            long startTime = System.currentTimeMillis();

            // Phase 1: Shutdown (stop accepting new tasks)
            boolean shutdownSuccess = performShutdown(shutdownTimeout, unit);

            // Phase 2: Await Termination (wait for running tasks to complete)
            boolean terminationSuccess = performAwaitTermination(terminationTimeout, unit);

            // Phase 3: Resource Cleanup
            int cleanedResources = resourceCleanupService.cleanupAll();

            long totalTime = System.currentTimeMillis() - startTime;

            ShutdownResult result = new ShutdownResult(
                shutdownSuccess,
                terminationSuccess,
                cleanedResources,
                String.format("Shutdown completed in %d ms", totalTime)
            );

            logger.info("Two-phase graceful shutdown completed. Result: {}", result);
            return result;
        }
    }

    /**
     * Performs the shutdown phase.
     *
     * @param timeout the timeout for the shutdown
     * @param unit the time unit for the timeout
     * @return true if shutdown was successful, false otherwise
     */
    private boolean performShutdown(long timeout, TimeUnit unit) {
        logger.info("Performing shutdown phase with timeout: {} {}", timeout, unit);

        try {
            // Mark the thread controller as shutdown to prevent new task submissions
            threadController.setShutdown(true);
            taskCompletionTracker.setShutdownInitiated(true);

            // Shutdown the executor service (stop accepting new tasks)
            if (executorService != null) {
                executorService.shutdown();
                logger.info("Executor service shutdown initiated");
            }

            return true;
        } catch (Exception e) {
            logger.error("Error during shutdown phase: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Performs the await termination phase.
     *
     * @param timeout the timeout for termination
     * @param unit the time unit for the timeout
     * @return true if termination was successful, false if timeout occurred
     */
    private boolean performAwaitTermination(long timeout, TimeUnit unit) {
        if (executorService == null) {
            logger.info("No executor service to terminate");
            return true;
        }

        logger.info("Performing await termination phase with timeout: {} {}", timeout, unit);

        try {
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
     * Gets the task completion tracker.
     *
     * @return the task completion tracker
     */
    public TaskCompletionTracker getTaskCompletionTracker() {
        return taskCompletionTracker;
    }

    /**
     * Gets the resource cleanup service.
     *
     * @return the resource cleanup service
     */
    public ResourceCleanupService getResourceCleanupService() {
        return resourceCleanupService;
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
     * Result class for shutdown operations.
     */
    public static class ShutdownResult {
        private final boolean shutdownSuccess;
        private final boolean terminationSuccess;
        private final int cleanedResources;
        private final String message;

        public ShutdownResult(boolean shutdownSuccess, boolean terminationSuccess, int cleanedResources, String message) {
            this.shutdownSuccess = shutdownSuccess;
            this.terminationSuccess = terminationSuccess;
            this.cleanedResources = cleanedResources;
            this.message = message;
        }

        public boolean isShutdownSuccess() {
            return shutdownSuccess;
        }

        public boolean isTerminationSuccess() {
            return terminationSuccess;
        }

        public int getCleanedResources() {
            return cleanedResources;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "ShutdownResult{" +
                    "shutdownSuccess=" + shutdownSuccess +
                    ", terminationSuccess=" + terminationSuccess +
                    ", cleanedResources=" + cleanedResources +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}