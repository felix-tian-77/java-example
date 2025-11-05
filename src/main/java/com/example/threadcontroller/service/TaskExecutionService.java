package com.example.threadcontroller.service;

import com.example.threadcontroller.model.Task;
import com.example.threadcontroller.model.TaskStatus;
import com.example.threadcontroller.util.Logger;
import com.example.threadcontroller.util.MetricsCollector;
import com.example.threadcontroller.util.TaskExecutionException;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Service for executing tasks in the thread controller.
 */
public class TaskExecutionService {
    private static final Logger logger = new Logger(TaskExecutionService.class);
    private final MetricsCollector metricsCollector;

    /**
     * Constructor.
     *
     * @param metricsCollector the metrics collector to use for tracking execution metrics
     */
    public TaskExecutionService(MetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
        logger.info("Created TaskExecutionService");
    }

    /**
     * Executes a Runnable task and tracks its execution.
     *
     * @param task the task to execute
     * @param taskModel the task model for tracking
     * @return a CompletableFuture representing the task execution
     */
    public CompletableFuture<Void> executeTask(Runnable task, Task taskModel) {
        logger.info("Executing Runnable task with ID: {}", taskModel.getId());
        metricsCollector.recordTaskSubmission("Runnable");

        taskModel.setStartTime(Instant.now());
        taskModel.setStatus(TaskStatus.RUNNING);

        long startTime = System.currentTimeMillis();

        return CompletableFuture.runAsync(() -> {
            try {
                logger.debug("Running Runnable task with ID: {}", taskModel.getId());
                task.run();

                long executionTime = System.currentTimeMillis() - startTime;
                taskModel.setCompletionTime(Instant.now());
                taskModel.setStatus(TaskStatus.COMPLETED);

                metricsCollector.recordTaskCompletion(executionTime);
                logger.debug("Runnable task with ID: {} completed successfully in {}ms",
                           taskModel.getId(), executionTime);
            } catch (Exception e) {
                long executionTime = System.currentTimeMillis() - startTime;
                taskModel.setCompletionTime(Instant.now());
                taskModel.setStatus(TaskStatus.FAILED);
                taskModel.setException(e);

                metricsCollector.recordTaskFailure();
                logger.error("Runnable task with ID: {} failed after {}ms with exception: {}",
                           taskModel.getId(), executionTime, e.getMessage(), e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Executes a Callable task and tracks its execution.
     *
     * @param task the task to execute
     * @param taskModel the task model for tracking
     * @param <T> the result type
     * @return a CompletableFuture representing the task execution
     */
    public <T> CompletableFuture<T> executeTask(java.util.concurrent.Callable<T> task, Task taskModel) {
        logger.info("Executing Callable task with ID: {}", taskModel.getId());
        metricsCollector.recordTaskSubmission("Callable");

        taskModel.setStartTime(Instant.now());
        taskModel.setStatus(TaskStatus.RUNNING);

        long startTime = System.currentTimeMillis();

        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Running Callable task with ID: {}", taskModel.getId());
                T result = task.call();

                long executionTime = System.currentTimeMillis() - startTime;
                taskModel.setCompletionTime(Instant.now());
                taskModel.setStatus(TaskStatus.COMPLETED);
                taskModel.setResult(result);

                metricsCollector.recordTaskCompletion(executionTime);
                logger.debug("Callable task with ID: {} completed successfully in {}ms",
                           taskModel.getId(), executionTime);
                return result;
            } catch (Exception e) {
                long executionTime = System.currentTimeMillis() - startTime;
                taskModel.setCompletionTime(Instant.now());
                taskModel.setStatus(TaskStatus.FAILED);
                taskModel.setException(e);

                metricsCollector.recordTaskFailure();
                logger.error("Callable task with ID: {} failed after {}ms with exception: {}",
                           taskModel.getId(), executionTime, e.getMessage(), e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Executes a task with the specified executor service.
     *
     * @param task the task to execute
     * @param taskModel the task model for tracking
     * @param executorService the executor service to use
     * @return a CompletableFuture representing the task execution
     */
    public CompletableFuture<Void> executeTask(Runnable task, Task taskModel, ExecutorService executorService) {
        logger.info("Executing Runnable task with ID: {} using custom executor", taskModel.getId());
        metricsCollector.recordTaskSubmission("Runnable");

        taskModel.setStartTime(Instant.now());
        taskModel.setStatus(TaskStatus.RUNNING);

        long startTime = System.currentTimeMillis();

        return CompletableFuture.runAsync(() -> {
            try {
                logger.debug("Running Runnable task with ID: {} using custom executor", taskModel.getId());
                task.run();

                long executionTime = System.currentTimeMillis() - startTime;
                taskModel.setCompletionTime(Instant.now());
                taskModel.setStatus(TaskStatus.COMPLETED);

                metricsCollector.recordTaskCompletion(executionTime);
                logger.debug("Runnable task with ID: {} completed successfully in {}ms using custom executor",
                           taskModel.getId(), executionTime);
            } catch (Exception e) {
                long executionTime = System.currentTimeMillis() - startTime;
                taskModel.setCompletionTime(Instant.now());
                taskModel.setStatus(TaskStatus.FAILED);
                taskModel.setException(e);

                metricsCollector.recordTaskFailure();
                logger.error("Runnable task with ID: {} failed after {}ms with exception: {} using custom executor",
                           taskModel.getId(), executionTime, e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    /**
     * Executes a callable task with the specified executor service.
     *
     * @param task the task to execute
     * @param taskModel the task model for tracking
     * @param executorService the executor service to use
     * @param <T> the result type
     * @return a CompletableFuture representing the task execution
     */
    public <T> CompletableFuture<T> executeTask(java.util.concurrent.Callable<T> task, Task taskModel, ExecutorService executorService) {
        logger.info("Executing Callable task with ID: {} using custom executor", taskModel.getId());
        metricsCollector.recordTaskSubmission("Callable");

        taskModel.setStartTime(Instant.now());
        taskModel.setStatus(TaskStatus.RUNNING);

        long startTime = System.currentTimeMillis();

        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Running Callable task with ID: {} using custom executor", taskModel.getId());
                T result = task.call();

                long executionTime = System.currentTimeMillis() - startTime;
                taskModel.setCompletionTime(Instant.now());
                taskModel.setStatus(TaskStatus.COMPLETED);
                taskModel.setResult(result);

                metricsCollector.recordTaskCompletion(executionTime);
                logger.debug("Callable task with ID: {} completed successfully in {}ms using custom executor",
                           taskModel.getId(), executionTime);
                return result;
            } catch (Exception e) {
                long executionTime = System.currentTimeMillis() - startTime;
                taskModel.setCompletionTime(Instant.now());
                taskModel.setStatus(TaskStatus.FAILED);
                taskModel.setException(e);

                metricsCollector.recordTaskFailure();
                logger.error("Callable task with ID: {} failed after {}ms with exception: {} using custom executor",
                           taskModel.getId(), executionTime, e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }, executorService);
    }
}