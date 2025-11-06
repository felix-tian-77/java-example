package com.example.threadcontroller.service;

import com.example.threadcontroller.model.ThreadController;
import com.example.threadcontroller.model.ThreadPoolConfiguration;
import com.example.threadcontroller.model.ThreadType;
import com.example.threadcontroller.util.Logger;
import com.example.threadcontroller.util.ShutdownException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for managing thread controller operations.
 */
public class ThreadControllerService {
    private static final Logger logger = new Logger(ThreadControllerService.class);

    private final ThreadController controller;
    private ExecutorService executorService;
    private final Object lock = new Object();

    /**
     * Default constructor.
     */
    public ThreadControllerService() {
        this.controller = new ThreadController();
        initializeExecutorService();
        logger.info("Created ThreadControllerService with default configuration");
    }

    /**
     * Constructor with configuration.
     *
     * @param configuration the thread pool configuration
     */
    public ThreadControllerService(ThreadPoolConfiguration configuration) {
        this.controller = new ThreadController(configuration);
        initializeExecutorService();
        logger.info("Created ThreadControllerService with configuration: {}", configuration);
    }

    /**
     * Initializes the executor service based on the controller configuration.
     */
    private void initializeExecutorService() {
        synchronized (lock) {
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
            }

            ThreadType threadType = controller.getThreadType();
            int poolSize = controller.getPoolSize();

            if (threadType == ThreadType.VIRTUAL) {
                executorService = createVirtualThreadExecutor();
                logger.info("Initialized virtual thread executor with pool size: {}", poolSize);
            } else {
                executorService = createPlatformThreadExecutor(poolSize);
                logger.info("Initialized platform thread executor with pool size: {}", poolSize);
            }
        }
    }

    /**
     * Creates a virtual thread executor.
     *
     * @return the virtual thread executor
     */
    private ExecutorService createVirtualThreadExecutor() {
        ThreadFactory threadFactory = Thread.ofVirtual().name("virtual-thread-", 0).factory();
        return Executors.newThreadPerTaskExecutor(threadFactory);
    }

    /**
     * Creates a platform thread executor.
     *
     * @param poolSize the pool size
     * @return the platform thread executor
     */
    private ExecutorService createPlatformThreadExecutor(int poolSize) {
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("platform-thread-" + counter.getAndIncrement());
                return thread;
            }
        };
        return Executors.newFixedThreadPool(poolSize, threadFactory);
    }

    /**
     * Submits a task for execution.
     *
     * @param task the task to execute
     * @return a CompletableFuture representing the task execution
     * @throws ShutdownException if the controller has been shut down
     */
    public CompletableFuture<Void> submitTask(Runnable task) throws ShutdownException {
        if (controller.isShutdown()) {
            throw new ShutdownException("Cannot submit task: ThreadController has been shut down");
        }

        controller.incrementSubmittedTasks();
        logger.info("Submitting task. Total submitted tasks: {}", controller.getSubmittedTasks());

        return CompletableFuture.runAsync(() -> {
            try {
                task.run();
                controller.incrementCompletedTasks();
                logger.info("Task completed. Total completed tasks: {}", controller.getCompletedTasks());
            } catch (Exception e) {
                logger.error("Task failed with exception: {}", e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    /**
     * Submits a callable task for execution.
     *
     * @param task the task to execute
     * @param <T>  the result type
     * @return a CompletableFuture representing the task execution
     * @throws ShutdownException if the controller has been shut down
     */
    public <T> CompletableFuture<T> submitTask(java.util.concurrent.Callable<T> task) throws ShutdownException {
        if (controller.isShutdown()) {
            throw new ShutdownException("Cannot submit task: ThreadController has been shut down");
        }

        controller.incrementSubmittedTasks();
        logger.info("Submitting callable task. Total submitted tasks: {}", controller.getSubmittedTasks());

        return CompletableFuture.supplyAsync(() -> {
            try {
                T result = task.call();
                controller.incrementCompletedTasks();
                logger.info("Callable task completed. Total completed tasks: {}", controller.getCompletedTasks());
                return result;
            } catch (Exception e) {
                logger.error("Callable task failed with exception: {}", e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    /**
     * Shuts down the thread controller gracefully.
     */
    public void shutdown() {
        synchronized (lock) {
            if (!controller.isShutdown()) {
                controller.setShutdown(true);
                logger.info("Initiating graceful shutdown");
                if (executorService != null) {
                    executorService.shutdown();
                }
            }
        }
    }

    /**
     * Checks if the thread controller has been terminated.
     *
     * @return true if terminated, false otherwise
     */
    public boolean isTerminated() {
        return controller.isShutdown() && (executorService == null || executorService.isTerminated());
    }

    /**
     * Gets the thread controller.
     *
     * @return the thread controller
     */
    public ThreadController getController() {
        return controller;
    }

    /**
     * Resizes the thread pool.
     *
     * @param newSize the new pool size
     */
    public void resizeThreadPool(int newSize) {
        synchronized (lock) {
            if (controller.isShutdown()) {
                throw new IllegalStateException("Cannot resize pool: ThreadController has been shut down");
            }

            ThreadPoolConfiguration config = controller.getConfiguration();
            config.setMaximumPoolSize(newSize);
            config.setCorePoolSize(newSize);
            controller.setConfiguration(config);

            // For platform threads, we need to recreate the executor
            if (controller.getThreadType() == ThreadType.PLATFORM) {
                initializeExecutorService();
            }

            logger.info("Resized thread pool to size: {}", newSize);
        }
    }
}