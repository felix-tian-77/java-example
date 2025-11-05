package com.example.threadcontroller.service;

import com.example.threadcontroller.model.Task;
import com.example.threadcontroller.util.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * Handler for task result processing.
 */
public class TaskResultHandler {
    private static final Logger logger = new Logger(TaskResultHandler.class);

    /**
     * Handles the result of a CompletableFuture task.
     *
     * @param future the CompletableFuture to handle
     * @param taskModel the task model for tracking
     * @param <T> the result type
     */
    public static <T> void handleResult(CompletableFuture<T> future, Task taskModel) {
        future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                logger.error("Task with ID: {} failed with exception: {}",
                           taskModel.getId(), throwable.getMessage(), throwable);
                taskModel.setException(throwable);
            } else {
                logger.debug("Task with ID: {} completed successfully with result: {}",
                           taskModel.getId(), result);
                taskModel.setResult(result);
            }
        });
    }

    /**
     * Handles the result of a CompletableFuture task with callbacks.
     *
     * @param future the CompletableFuture to handle
     * @param onSuccess callback for successful completion
     * @param onFailure callback for failure
     * @param <T> the result type
     */
    public static <T> void handleResult(CompletableFuture<T> future,
                                      Consumer<T> onSuccess,
                                      Consumer<Throwable> onFailure) {
        future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                logger.error("Task failed with exception: {}", throwable.getMessage(), throwable);
                onFailure.accept(throwable);
            } else {
                logger.debug("Task completed successfully with result: {}", result);
                onSuccess.accept(result);
            }
        });
    }

    /**
     * Gets the result of a CompletableFuture task with a timeout.
     *
     * @param future the CompletableFuture to get the result from
     * @param timeout the timeout value
     * @param unit the timeout unit
     * @param taskModel the task model for tracking
     * @param <T> the result type
     * @return the result
     * @throws TimeoutException if the task times out
     * @throws ExecutionException if the task fails
     * @throws InterruptedException if the thread is interrupted
     */
    public static <T> T getResult(CompletableFuture<T> future, long timeout, TimeUnit unit, Task taskModel)
            throws TimeoutException, ExecutionException, InterruptedException {
        try {
            T result = future.get(timeout, unit);
            logger.debug("Task with ID: {} completed successfully with result", taskModel.getId());
            return result;
        } catch (TimeoutException e) {
            logger.warn("Task with ID: {} timed out after {} {}", taskModel.getId(), timeout, unit);
            throw e;
        } catch (ExecutionException e) {
            logger.error("Task with ID: {} failed with exception: {}", taskModel.getId(), e.getMessage(), e);
            throw e;
        } catch (InterruptedException e) {
            logger.warn("Task with ID: {} was interrupted", taskModel.getId());
            throw e;
        }
    }

    /**
     * Cancels a task if it hasn't completed yet.
     *
     * @param future the CompletableFuture to cancel
     * @param taskModel the task model for tracking
     * @return true if the task was cancelled, false otherwise
     */
    public static boolean cancelTask(CompletableFuture<?> future, Task taskModel) {
        boolean cancelled = future.cancel(true);
        if (cancelled) {
            logger.info("Task with ID: {} was cancelled", taskModel.getId());
        } else {
            logger.warn("Task with ID: {} could not be cancelled (may have already completed)", taskModel.getId());
        }
        return cancelled;
    }

    /**
     * Checks if a task has completed.
     *
     * @param future the CompletableFuture to check
     * @return true if the task has completed, false otherwise
     */
    public static boolean isTaskCompleted(CompletableFuture<?> future) {
        return future.isDone();
    }

    /**
     * Checks if a task has been cancelled.
     *
     * @param future the CompletableFuture to check
     * @return true if the task has been cancelled, false otherwise
     */
    public static boolean isTaskCancelled(CompletableFuture<?> future) {
        return future.isCancelled();
    }
}