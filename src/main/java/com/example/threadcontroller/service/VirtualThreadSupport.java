package com.example.threadcontroller.service;

import com.example.threadcontroller.util.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Support class for virtual thread operations.
 */
public class VirtualThreadSupport {
    private static final Logger logger = new Logger(VirtualThreadSupport.class);

    /**
     * Checks if virtual threads are supported in the current JVM.
     *
     * @return true if virtual threads are supported, false otherwise
     */
    public static boolean isVirtualThreadSupported() {
        try {
            // Try to create a virtual thread to check if it's supported
            Thread.Builder builder = Thread.ofVirtual();
            builder.name("test-virtual-thread");
            logger.debug("Virtual thread support is available");
            return true;
        } catch (UnsupportedOperationException e) {
            logger.debug("Virtual thread support is not available: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Creates a virtual thread executor service.
     *
     * @param namePrefix the prefix for thread names
     * @return the virtual thread executor service
     */
    public static ExecutorService createVirtualThreadExecutor(String namePrefix) {
        if (!isVirtualThreadSupported()) {
            throw new UnsupportedOperationException("Virtual threads are not supported in this JVM");
        }

        ThreadFactory threadFactory = Thread.ofVirtual().name(namePrefix, 0).factory();
        ExecutorService executor = Executors.newThreadPerTaskExecutor(threadFactory);
        logger.info("Created virtual thread executor with name prefix: {}", namePrefix);
        return executor;
    }

    /**
     * Creates a virtual thread with the specified name and task.
     *
     * @param name the name for the thread
     * @param task the task to execute
     * @return the created virtual thread
     */
    public static Thread createVirtualThread(String name, Runnable task) {
        if (!isVirtualThreadSupported()) {
            throw new UnsupportedOperationException("Virtual threads are not supported in this JVM");
        }

        Thread thread = Thread.ofVirtual().name(name).unstarted(task);
        logger.debug("Created virtual thread with name: {}", name);
        return thread;
    }

    /**
     * Starts a virtual thread with the specified name and task.
     *
     * @param name the name for the thread
     * @param task the task to execute
     * @return the started virtual thread
     */
    public static Thread startVirtualThread(String name, Runnable task) {
        if (!isVirtualThreadSupported()) {
            throw new UnsupportedOperationException("Virtual threads are not supported in this JVM");
        }

        Thread thread = Thread.ofVirtual().name(name).start(task);
        logger.debug("Started virtual thread with name: {}", name);
        return thread;
    }
}