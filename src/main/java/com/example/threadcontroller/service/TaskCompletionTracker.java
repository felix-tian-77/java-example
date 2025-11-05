package com.example.threadcontroller.service;

import com.example.threadcontroller.util.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracker for monitoring task completion during shutdown.
 */
public class TaskCompletionTracker {
    private static final Logger logger = new Logger(TaskCompletionTracker.class);

    private final ConcurrentMap<String, TaskStatus> taskStatuses;
    private final AtomicInteger submittedTasks;
    private final AtomicInteger completedTasks;
    private final AtomicInteger failedTasks;
    private final AtomicBoolean shutdownInitiated;
    private final Object lock = new Object();

    /**
     * Default constructor.
     */
    public TaskCompletionTracker() {
        this.taskStatuses = new ConcurrentHashMap<>();
        this.submittedTasks = new AtomicInteger(0);
        this.completedTasks = new AtomicInteger(0);
        this.failedTasks = new AtomicInteger(0);
        this.shutdownInitiated = new AtomicBoolean(false);
        logger.info("Created TaskCompletionTracker");
    }

    /**
     * Registers a task for tracking.
     *
     * @param taskId the task ID
     */
    public void registerTask(String taskId) {
        synchronized (lock) {
            if (shutdownInitiated.get()) {
                logger.warn("Cannot register task {} - shutdown has been initiated", taskId);
                return;
            }

            taskStatuses.put(taskId, TaskStatus.RUNNING);
            submittedTasks.incrementAndGet();
            logger.debug("Registered task for tracking: {}", taskId);
        }
    }

    /**
     * Marks a task as completed.
     *
     * @param taskId the task ID
     */
    public void markTaskCompleted(String taskId) {
        TaskStatus previousStatus = taskStatuses.put(taskId, TaskStatus.COMPLETED);
        completedTasks.incrementAndGet();
        logger.debug("Marked task as completed: {}. Previous status: {}", taskId, previousStatus);
    }

    /**
     * Marks a task as failed.
     *
     * @param taskId the task ID
     * @param errorMessage the error message
     */
    public void markTaskFailed(String taskId, String errorMessage) {
        taskStatuses.put(taskId, TaskStatus.FAILED);
        failedTasks.incrementAndGet();
        logger.debug("Marked task as failed: {}. Error: {}", taskId, errorMessage);
    }

    /**
     * Gets the status of a task.
     *
     * @param taskId the task ID
     * @return the task status, or null if not found
     */
    public TaskStatus getTaskStatus(String taskId) {
        return taskStatuses.get(taskId);
    }

    /**
     * Gets the number of submitted tasks.
     *
     * @return the number of submitted tasks
     */
    public int getSubmittedTasks() {
        return submittedTasks.get();
    }

    /**
     * Gets the number of completed tasks.
     *
     * @return the number of completed tasks
     */
    public int getCompletedTasks() {
        return completedTasks.get();
    }

    /**
     * Gets the number of failed tasks.
     *
     * @return the number of failed tasks
     */
    public int getFailedTasks() {
        return failedTasks.get();
    }

    /**
     * Gets the number of running tasks.
     *
     * @return the number of running tasks
     */
    public int getRunningTasks() {
        return (int) taskStatuses.values().stream()
                .filter(status -> status == TaskStatus.RUNNING)
                .count();
    }

    /**
     * Checks if all tasks have completed (either successfully or with failure).
     *
     * @return true if all tasks have completed, false otherwise
     */
    public boolean areAllTasksCompleted() {
        int totalCompleted = completedTasks.get() + failedTasks.get();
        return totalCompleted == submittedTasks.get();
    }

    /**
     * Gets a summary of task completion.
     *
     * @return a summary string
     */
    public String getCompletionSummary() {
        return String.format("Tasks - Submitted: %d, Completed: %d, Failed: %d, Running: %d",
                submittedTasks.get(), completedTasks.get(), failedTasks.get(), getRunningTasks());
    }

    /**
     * Sets the shutdown initiated flag.
     *
     * @param initiated true if shutdown has been initiated
     */
    public void setShutdownInitiated(boolean initiated) {
        shutdownInitiated.set(initiated);
        logger.info("Shutdown initiated flag set to: {}", initiated);
    }

    /**
     * Checks if shutdown has been initiated.
     *
     * @return true if shutdown has been initiated, false otherwise
     */
    public boolean isShutdownInitiated() {
        return shutdownInitiated.get();
    }

    /**
     * Clears all task tracking data.
     */
    public void clear() {
        synchronized (lock) {
            taskStatuses.clear();
            submittedTasks.set(0);
            completedTasks.set(0);
            failedTasks.set(0);
            logger.info("Cleared all task tracking data");
        }
    }

    /**
     * Task status enumeration.
     */
    public enum TaskStatus {
        RUNNING,
        COMPLETED,
        FAILED
    }
}