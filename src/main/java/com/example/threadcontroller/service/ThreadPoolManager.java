package com.example.threadcontroller.service;

import com.example.threadcontroller.model.ThreadController;
import com.example.threadcontroller.model.ThreadPoolConfiguration;
import com.example.threadcontroller.model.ThreadType;
import com.example.threadcontroller.util.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manager for thread pool operations.
 */
public class ThreadPoolManager {
    private static final Logger logger = new Logger(ThreadPoolManager.class);

    private ThreadController controller;
    private ExecutorService executorService;
    private final Object lock = new Object();

    /**
     * Constructor.
     *
     * @param controller the thread controller
     */
    public ThreadPoolManager(ThreadController controller) {
        this.controller = controller;
        logger.info("Created ThreadPoolManager for controller: {}", controller);
    }

    /**
     * Initializes the executor service based on the controller configuration.
     */
    public void initializeExecutorService() {
        synchronized (lock) {
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
            }

            ThreadPoolConfiguration config = controller.getConfiguration();
            ThreadType threadType = config.getThreadType();
            int corePoolSize = config.getCorePoolSize();

            if (threadType == ThreadType.VIRTUAL) {
                executorService = createVirtualThreadExecutor();
                logger.info("Initialized virtual thread executor with core pool size: {}", corePoolSize);
            } else {
                executorService = createPlatformThreadExecutor(corePoolSize);
                logger.info("Initialized platform thread executor with core pool size: {}", corePoolSize);
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
                thread.setDaemon(false);
                return thread;
            }
        };
        return Executors.newFixedThreadPool(poolSize, threadFactory);
    }

    /**
     * Gets the executor service.
     *
     * @return the executor service
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Shuts down the executor service.
     */
    public void shutdown() {
        synchronized (lock) {
            if (executorService != null) {
                executorService.shutdown();
                logger.info("Executor service shutdown initiated");
            }
        }
    }

    /**
     * Checks if the executor service has been terminated.
     *
     * @return true if terminated, false otherwise
     */
    public boolean isTerminated() {
        return executorService == null || executorService.isTerminated();
    }
}