package com.example.threadcontroller.cli;

import com.example.threadcontroller.model.ThreadPoolConfiguration;
import com.example.threadcontroller.model.ThreadType;
import com.example.threadcontroller.service.GracefulShutdownHandler;
import com.example.threadcontroller.service.ShutdownService;
import com.example.threadcontroller.service.ThreadControllerService;
import com.example.threadcontroller.util.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * CLI command for shutting down the thread controller gracefully.
 */
@Command(name = "shutdown", description = "Shutdown the thread controller gracefully.")
public class ShutdownCommand implements Callable<Integer> {
    private static final Logger logger = new Logger(ShutdownCommand.class);

    @Option(names = {"-t", "--timeout"}, description = "Shutdown timeout in seconds")
    private long timeout = 30;

    @Option(names = {"-n", "--now"}, description = "Perform immediate shutdown instead of graceful shutdown")
    private boolean immediate = false;

    @Option(names = {"-s", "--show-status"}, description = "Show status after shutdown")
    private boolean showStatus = false;

    private ThreadControllerService threadControllerService;

    /**
     * Executes the shutdown command.
     *
     * @return exit code
     * @throws Exception if an error occurs
     */
    @Override
    public Integer call() throws Exception {
        logger.info("Executing shutdown command with options: timeout={}s, immediate={}, showStatus={}",
                   timeout, immediate, showStatus);

        // Create a thread controller service for demonstration
        ThreadPoolConfiguration config = new ThreadPoolConfiguration();
        config.setCorePoolSize(5);
        config.setMaximumPoolSize(5);
        config.setThreadType(ThreadType.PLATFORM);

        threadControllerService = new ThreadControllerService(config);

        try {
            if (immediate) {
                return performImmediateShutdown();
            } else {
                return performGracefulShutdown();
            }
        } finally {
            if (showStatus) {
                showShutdownStatus();
            }
        }
    }

    /**
     * Performs an immediate shutdown.
     *
     * @return exit code
     */
    private int performImmediateShutdown() {
        System.out.println("Initiating immediate shutdown...");
        logger.info("Initiating immediate shutdown");

        try {
            ShutdownService shutdownService = new ShutdownService(
                threadControllerService.getController(),
                null // In a real implementation, this would be the actual executor service
            );

            shutdownService.shutdownNow();
            System.out.println("Immediate shutdown completed");
            logger.info("Immediate shutdown completed");
            return 0;
        } catch (Exception e) {
            System.err.println("Immediate shutdown failed: " + e.getMessage());
            logger.error("Immediate shutdown failed: {}", e.getMessage(), e);
            return 1;
        }
    }

    /**
     * Performs a graceful shutdown.
     *
     * @return exit code
     */
    private int performGracefulShutdown() {
        System.out.println("Initiating graceful shutdown with timeout: " + timeout + " seconds...");
        logger.info("Initiating graceful shutdown with timeout: {} seconds", timeout);

        try {
            GracefulShutdownHandler shutdownHandler = new GracefulShutdownHandler(
                threadControllerService.getController(),
                null // In a real implementation, this would be the actual executor service
            );

            GracefulShutdownHandler.ShutdownResult result = shutdownHandler.shutdown(
                timeout / 2, timeout / 2, TimeUnit.SECONDS);

            if (result.isShutdownSuccess() && result.isTerminationSuccess()) {
                System.out.println("Graceful shutdown completed successfully");
                System.out.println("Cleaned up " + result.getCleanedResources() + " resources");
                logger.info("Graceful shutdown completed successfully. Cleaned resources: {}",
                           result.getCleanedResources());
                return 0;
            } else {
                System.err.println("Graceful shutdown completed with issues: " + result.getMessage());
                logger.warn("Graceful shutdown completed with issues: {}", result.getMessage());
                return 1;
            }
        } catch (Exception e) {
            System.err.println("Graceful shutdown failed: " + e.getMessage());
            logger.error("Graceful shutdown failed: {}", e.getMessage(), e);
            return 1;
        }
    }

    /**
     * Shows the shutdown status.
     */
    private void showShutdownStatus() {
        if (threadControllerService != null) {
            boolean isShutdown = threadControllerService.getController().isShutdown();
            boolean isTerminated = threadControllerService.isTerminated();

            System.out.println("=== Shutdown Status ===");
            System.out.println("Controller shutdown: " + isShutdown);
            System.out.println("Service terminated: " + isTerminated);

            logger.info("Shutdown status - Controller shutdown: {}, Service terminated: {}",
                       isShutdown, isTerminated);
        }
    }
}