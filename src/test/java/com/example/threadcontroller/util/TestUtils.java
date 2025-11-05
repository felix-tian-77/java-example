package com.example.threadcontroller.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for testing thread controller functionality.
 */
public class TestUtils {

    /**
     * Creates a simple Runnable task for testing.
     *
     * @param taskName the name of the task
     * @param duration the duration in milliseconds to sleep
     * @return a Runnable task
     */
    public static Runnable createSimpleTask(String taskName, long duration) {
        return () -> {
            try {
                System.out.println("Starting task: " + taskName);
                Thread.sleep(duration);
                System.out.println("Completed task: " + taskName);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * Creates a simple Callable task for testing.
     *
     * @param taskName the name of the task
     * @param duration the duration in milliseconds to sleep
     * @param result   the result to return
     * @param <T>      the type of the result
     * @return a Callable task
     */
    public static <T> java.util.concurrent.Callable<T> createSimpleCallable(String taskName, long duration, T result) {
        return () -> {
            System.out.println("Starting callable task: " + taskName);
            Thread.sleep(duration);
            System.out.println("Completed callable task: " + taskName);
            return result;
        };
    }

    /**
     * Creates a task that throws an exception.
     *
     * @param taskName the name of the task
     * @param exceptionMessage the message for the exception
     * @return a Runnable task that throws an exception
     */
    public static Runnable createFailingTask(String taskName, String exceptionMessage) {
        return () -> {
            System.out.println("Starting failing task: " + taskName);
            throw new RuntimeException(exceptionMessage);
        };
    }

    /**
     * Waits for a CompletableFuture to complete with a timeout.
     *
     * @param future  the CompletableFuture to wait for
     * @param timeout the timeout value
     * @param unit    the timeout unit
     * @param <T>     the type of the result
     * @return the result of the CompletableFuture
     * @throws Exception if the CompletableFuture completes with an exception or times out
     */
    public static <T> T waitForCompletion(CompletableFuture<T> future, long timeout, TimeUnit unit) throws Exception {
        return future.get(timeout, unit);
    }

    /**
     * Creates a latch that can be used to coordinate between threads in tests.
     *
     * @param count the count for the latch
     * @return a CountDownLatch
     */
    public static CountDownLatch createLatch(int count) {
        return new CountDownLatch(count);
    }
}