package com.example.threadcontroller.service;

import com.example.threadcontroller.model.Task;
import com.example.threadcontroller.util.Logger;
import com.example.threadcontroller.util.ShutdownException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Service for submitting tasks to the thread controller.
 */
public class TaskSubmissionService {
    private static final Logger logger = new Logger(TaskSubmissionService.class);

    private final ExecutorService executorService;
    private volatile boolean isShutdown;

    /**
     * Constructor.
     *
     * @param executorService the executor service to use for task execution
     */
    public TaskSubmissionService(ExecutorService executorService) {
        this.executorService = executorService;
        this.isShutdown = false;
        logger.info("Created TaskSubmissionService");
    }

    /**
     * Submits a Runnable task for execution.
     *
     * @param task the task to execute
     * @return a CompletableFuture representing the task execution
     * @throws ShutdownException if the service has been shut down
     */
    public CompletableFuture<Void> submitTask(Runnable task) throws ShutdownException {
        if (isShutdown) {
            throw new ShutdownException("Cannot submit task: TaskSubmissionService has been shut down");
        }

        logger.info("Submitting Runnable task");
        return CompletableFuture.runAsync(() -> {
            try {
                logger.debug("Executing Runnable task");
                task.run();
                logger.debug("Runnable task completed successfully");
            } catch (Exception e) {
                logger.error("Runnable task failed with exception: {}", e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    /**
     * Submits a Callable task for execution.
     *
     * @param task the task to execute
     * @param <T>  the result type
     * @return a CompletableFuture representing the task execution
     * @throws ShutdownException if the service has been shut down
     */
    public <T> CompletableFuture<T> submitTask(java.util.concurrent.Callable<T> task) throws ShutdownException {
        if (isShutdown) {
            throw new ShutdownException("Cannot submit task: TaskSubmissionService has been shut down");
        }

        logger.info("Submitting Callable task");
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Executing Callable task");
                T result = task.call();
                logger.debug("Callable task completed successfully");
                return result;
            } catch (Exception e) {
                logger.error("Callable task failed with exception: {}", e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    /**
     * Submits a task with a corresponding Task model for tracking.
     *
     * @param taskModel the task model for tracking
     * @param task      the task to execute
     * @return a CompletableFuture representing the task execution
     * @throws ShutdownException if the service has been shut down
     */
    public CompletableFuture<Void> submitTask(Task taskModel, Runnable task) throws ShutdownException {
        if (isShutdown) {
            throw new ShutdownException("Cannot submit task: TaskSubmissionService has been shut down");
        }

        logger.info("Submitting Runnable task with model ID: {}", taskModel.getId());
        taskModel.setStatus(com.example.threadcontroller.model.TaskStatus.RUNNING);

        return CompletableFuture.runAsync(() -> {
            try {
                logger.debug("Executing Runnable task with model ID: {}", taskModel.getId());
                task.run();
                taskModel.setStatus(com.example.threadcontroller.model.TaskStatus.COMPLETED);
                logger.debug("Runnable task with model ID: {} completed successfully", taskModel.getId());
            } catch (Exception e) {
                taskModel.setStatus(com.example.threadcontroller.model.TaskStatus.FAILED);
                taskModel.setException(e);
                logger.error("Runnable task with model ID: {} failed with exception: {}",
                           taskModel.getId(), e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    /**
     * Submits a callable task with a corresponding Task model for tracking.
     *
     * @param taskModel the task model for tracking
     * @param task      the task to execute
     * @param <T>       the result type
     * @return a CompletableFuture representing the task execution
     * @throws ShutdownException if the service has been shut down
     */
    public <T> CompletableFuture<T> submitTask(Task taskModel, java.util.concurrent.Callable<T> task) throws ShutdownException {
        if (isShutdown) {
            throw new ShutdownException("Cannot submit task: TaskSubmissionService has been shut down");
        }

        logger.info("Submitting Callable task with model ID: {}", taskModel.getId());
        taskModel.setStatus(com.example.threadcontroller.model.TaskStatus.RUNNING);

        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Executing Callable task with model ID: {}", taskModel.getId());
                T result = task.call();
                taskModel.setStatus(com.example.threadcontroller.model.TaskStatus.COMPLETED);
                taskModel.setResult(result);
                logger.debug("Callable task with model ID: {} completed successfully", taskModel.getId());
                return result;
            } catch (Exception e) {
                taskModel.setStatus(com.example.threadcontroller.model.TaskStatus.FAILED);
                taskModel.setException(e);
                logger.error("Callable task with model ID: {} failed with exception: {}",
                           taskModel.getId(), e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    /**
     * Shuts down the task submission service.
     */
    public void shutdown() {
        isShutdown = true;
        logger.info("TaskSubmissionService shutdown initiated");
    }

    /**
     * Checks if the service has been shut down.
     *
     * @return true if shut down, false otherwise
     */
    public boolean isShutdown() {
        return isShutdown;
    }
}