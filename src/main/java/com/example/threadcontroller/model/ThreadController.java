package com.example.threadcontroller.model;

import com.example.threadcontroller.util.Logger;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * The main controller that manages thread pools and task execution.
 */
public class ThreadController {
    private static final Logger logger = new Logger(ThreadController.class);

    private ThreadPoolConfiguration configuration;
    private volatile boolean isShutdown;
    private AtomicInteger submittedTasks;
    private AtomicInteger completedTasks;

    /**
     * Default constructor.
     */
    public ThreadController() {
        this.configuration = new ThreadPoolConfiguration();
        this.isShutdown = false;
        this.submittedTasks = new AtomicInteger(0);
        this.completedTasks = new AtomicInteger(0);
        logger.info("Created new ThreadController with default configuration");
    }

    /**
     * Constructor with configuration.
     *
     * @param configuration the thread pool configuration
     */
    public ThreadController(ThreadPoolConfiguration configuration) {
        this.configuration = configuration;
        this.isShutdown = false;
        this.submittedTasks = new AtomicInteger(0);
        this.completedTasks = new AtomicInteger(0);
        logger.info("Created new ThreadController with configuration: {}", configuration);
    }

    // Getters and setters

    public ThreadPoolConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ThreadPoolConfiguration configuration) {
        this.configuration = configuration;
    }

    public boolean isShutdown() {
        return isShutdown;
    }

    public void setShutdown(boolean shutdown) {
        isShutdown = shutdown;
        if (shutdown) {
            logger.info("ThreadController shutdown initiated");
        }
    }

    public int getSubmittedTasks() {
        return submittedTasks.get();
    }

    public int getCompletedTasks() {
        return completedTasks.get();
    }

    public ThreadType getThreadType() {
        return configuration.getThreadType();
    }

    public int getPoolSize() {
        return configuration.getCorePoolSize();
    }

    /**
     * Increments the submitted tasks counter.
     *
     * @return the new value
     */
    public int incrementSubmittedTasks() {
        return submittedTasks.incrementAndGet();
    }

    /**
     * Increments the completed tasks counter.
     *
     * @return the new value
     */
    public int incrementCompletedTasks() {
        return completedTasks.incrementAndGet();
    }

    @Override
    public String toString() {
        return "ThreadController{" +
                "configuration=" + configuration +
                ", isShutdown=" + isShutdown +
                ", submittedTasks=" + submittedTasks +
                ", completedTasks=" + completedTasks +
                '}';
    }
}