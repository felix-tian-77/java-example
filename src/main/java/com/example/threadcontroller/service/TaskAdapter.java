package com.example.threadcontroller.service;

import com.example.threadcontroller.model.Task;
import com.example.threadcontroller.util.Logger;

import java.util.concurrent.Callable;

/**
 * Adapter for converting between different task types.
 */
public class TaskAdapter {
    private static final Logger logger = new Logger(TaskAdapter.class);

    /**
     * Converts a Runnable to a Task model.
     *
     * @param runnable the runnable to convert
     * @return a Task model
     */
    public static Task fromRunnable(Runnable runnable) {
        Task task = new Task();
        logger.debug("Created Task model from Runnable");
        return task;
    }

    /**
     * Converts a Callable to a Task model.
     *
     * @param callable the callable to convert
     * @param <T> the result type
     * @return a Task model
     */
    public static <T> Task fromCallable(Callable<T> callable) {
        Task task = new Task();
        logger.debug("Created Task model from Callable");
        return task;
    }

    /**
     * Wraps a Runnable with task tracking.
     *
     * @param taskModel the task model for tracking
     * @param runnable the runnable to wrap
     * @return a wrapped runnable
     */
    public static Runnable wrapRunnable(Task taskModel, Runnable runnable) {
        return () -> {
            try {
                logger.debug("Executing wrapped Runnable task with ID: {}", taskModel.getId());
                runnable.run();
                logger.debug("Wrapped Runnable task with ID: {} completed", taskModel.getId());
            } catch (Exception e) {
                logger.error("Wrapped Runnable task with ID: {} failed with exception: {}",
                           taskModel.getId(), e.getMessage(), e);
                throw e;
            }
        };
    }

    /**
     * Wraps a Callable with task tracking.
     *
     * @param taskModel the task model for tracking
     * @param callable the callable to wrap
     * @param <T> the result type
     * @return a wrapped callable
     */
    public static <T> Callable<T> wrapCallable(Task taskModel, Callable<T> callable) {
        return () -> {
            try {
                logger.debug("Executing wrapped Callable task with ID: {}", taskModel.getId());
                T result = callable.call();
                logger.debug("Wrapped Callable task with ID: {} completed", taskModel.getId());
                return result;
            } catch (Exception e) {
                logger.error("Wrapped Callable task with ID: {} failed with exception: {}",
                           taskModel.getId(), e.getMessage(), e);
                throw e;
            }
        };
    }

    /**
     * Creates a Runnable from a Task model.
     *
     * @param taskModel the task model
     * @param action the action to perform
     * @return a Runnable
     */
    public static Runnable toRunnable(Task taskModel, Runnable action) {
        return () -> {
            try {
                logger.debug("Converting Task model to Runnable with ID: {}", taskModel.getId());
                action.run();
                logger.debug("Converted Runnable with ID: {} completed", taskModel.getId());
            } catch (Exception e) {
                logger.error("Converted Runnable with ID: {} failed with exception: {}",
                           taskModel.getId(), e.getMessage(), e);
                throw e;
            }
        };
    }

    /**
     * Creates a Callable from a Task model.
     *
     * @param taskModel the task model
     * @param action the action to perform
     * @param <T> the result type
     * @return a Callable
     */
    public static <T> Callable<T> toCallable(Task taskModel, Callable<T> action) {
        return () -> {
            try {
                logger.debug("Converting Task model to Callable with ID: {}", taskModel.getId());
                T result = action.call();
                logger.debug("Converted Callable with ID: {} completed", taskModel.getId());
                return result;
            } catch (Exception e) {
                logger.error("Converted Callable with ID: {} failed with exception: {}",
                           taskModel.getId(), e.getMessage(), e);
                throw e;
            }
        };
    }
}