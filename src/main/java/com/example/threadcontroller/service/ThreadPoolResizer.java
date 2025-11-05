package com.example.threadcontroller.service;

import com.example.threadcontroller.model.ThreadController;
import com.example.threadcontroller.model.ThreadPoolConfiguration;
import com.example.threadcontroller.model.ThreadType;
import com.example.threadcontroller.util.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Service for resizing thread pools dynamically.
 */
public class ThreadPoolResizer {
    private static final Logger logger = new Logger(ThreadPoolResizer.class);

    private ThreadController controller;
    private ExecutorService executorService;

    /**
     * Constructor.
     *
     * @param controller the thread controller
     * @param executorService the executor service
     */
    public ThreadPoolResizer(ThreadController controller, ExecutorService executorService) {
        this.controller = controller;
        this.executorService = executorService;
        logger.info("Created ThreadPoolResizer for controller: {}", controller);
    }

    /**
     * Resizes the thread pool to the specified size.
     *
     * @param newSize the new pool size
     * @throws IllegalArgumentException if the new size is invalid
     * @throws IllegalStateException if the controller has been shut down
     */
    public void resizePool(int newSize) {
        if (newSize <= 0) {
            throw new IllegalArgumentException("Pool size must be positive");
        }

        if (controller.isShutdown()) {
            throw new IllegalStateException("Cannot resize pool: ThreadController has been shut down");
        }

        ThreadPoolConfiguration config = controller.getConfiguration();
        int oldCoreSize = config.getCorePoolSize();
        int oldMaxSize = config.getMaximumPoolSize();

        // Update configuration
        config.setCorePoolSize(newSize);
        config.setMaximumPoolSize(newSize);
        controller.setConfiguration(config);

        // For platform threads, we may need to adjust the executor
        if (controller.getThreadType() == ThreadType.PLATFORM && executorService instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executorService;
            threadPoolExecutor.setCorePoolSize(newSize);
            threadPoolExecutor.setMaximumPoolSize(newSize);
        }

        logger.info("Resized thread pool from core={}, max={} to core={}, max={}",
                   oldCoreSize, oldMaxSize, newSize, newSize);
    }

    /**
     * Increases the pool size by the specified amount.
     *
     * @param increment the amount to increase the pool size by
     */
    public void increasePoolSize(int increment) {
        if (increment <= 0) {
            throw new IllegalArgumentException("Increment must be positive");
        }

        ThreadPoolConfiguration config = controller.getConfiguration();
        int currentSize = config.getCorePoolSize();
        int newSize = currentSize + increment;

        resizePool(newSize);
        logger.info("Increased pool size by {} from {} to {}", increment, currentSize, newSize);
    }

    /**
     * Decreases the pool size by the specified amount.
     *
     * @param decrement the amount to decrease the pool size by
     */
    public void decreasePoolSize(int decrement) {
        if (decrement <= 0) {
            throw new IllegalArgumentException("Decrement must be positive");
        }

        ThreadPoolConfiguration config = controller.getConfiguration();
        int currentSize = config.getCorePoolSize();
        int newSize = Math.max(1, currentSize - decrement); // Ensure at least 1 thread

        resizePool(newSize);
        logger.info("Decreased pool size by {} from {} to {}", decrement, currentSize, newSize);
    }

    /**
     * Gets the current pool size.
     *
     * @return the current pool size
     */
    public int getCurrentPoolSize() {
        return controller.getConfiguration().getCorePoolSize();
    }
}