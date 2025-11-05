package com.example.threadcontroller.util;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility class for collecting and reporting metrics.
 */
public class MetricsCollector {
    private static final Logger logger = new Logger(MetricsCollector.class);

    private final AtomicInteger submittedTasks = new AtomicInteger(0);
    private final AtomicInteger completedTasks = new AtomicInteger(0);
    private final AtomicInteger failedTasks = new AtomicInteger(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    private final ConcurrentHashMap<String, AtomicInteger> taskTypeCounters = new ConcurrentHashMap<>();

    private volatile Instant startTime;

    /**
     * Default constructor.
     */
    public MetricsCollector() {
        this.startTime = Instant.now();
        logger.info("MetricsCollector initialized");
    }

    /**
     * Records a task submission.
     *
     * @param taskType the type of task
     */
    public void recordTaskSubmission(String taskType) {
        submittedTasks.incrementAndGet();
        taskTypeCounters.computeIfAbsent(taskType, k -> new AtomicInteger(0)).incrementAndGet();
        logger.debug("Recorded task submission. Task type: {}, Total submitted: {}", taskType, submittedTasks.get());
    }

    /**
     * Records a task completion.
     *
     * @param executionTime the execution time in milliseconds
     */
    public void recordTaskCompletion(long executionTime) {
        completedTasks.incrementAndGet();
        totalExecutionTime.addAndGet(executionTime);
        logger.debug("Recorded task completion. Execution time: {}ms, Total completed: {}", executionTime, completedTasks.get());
    }

    /**
     * Records a task failure.
     */
    public void recordTaskFailure() {
        failedTasks.incrementAndGet();
        logger.debug("Recorded task failure. Total failed: {}", failedTasks.get());
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
     * Gets the average execution time.
     *
     * @return the average execution time in milliseconds
     */
    public double getAverageExecutionTime() {
        int completed = completedTasks.get();
        if (completed == 0) {
            return 0.0;
        }
        return (double) totalExecutionTime.get() / completed;
    }

    /**
     * Gets the uptime in seconds.
     *
     * @return the uptime in seconds
     */
    public long getUptimeSeconds() {
        return java.time.Duration.between(startTime, Instant.now()).getSeconds();
    }

    /**
     * Gets a report of the current metrics.
     *
     * @return a formatted metrics report
     */
    public String getMetricsReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Thread Controller Metrics Report ===\n");
        report.append("Uptime: ").append(getUptimeSeconds()).append(" seconds\n");
        report.append("Submitted tasks: ").append(submittedTasks.get()).append("\n");
        report.append("Completed tasks: ").append(completedTasks.get()).append("\n");
        report.append("Failed tasks: ").append(failedTasks.get()).append("\n");
        report.append("Average execution time: ").append(String.format("%.2f", getAverageExecutionTime())).append(" ms\n");
        report.append("Task type distribution:\n");

        taskTypeCounters.forEach((type, counter) -> {
            report.append("  ").append(type).append(": ").append(counter.get()).append("\n");
        });

        return report.toString();
    }

    /**
     * Resets all metrics.
     */
    public void reset() {
        submittedTasks.set(0);
        completedTasks.set(0);
        failedTasks.set(0);
        totalExecutionTime.set(0);
        taskTypeCounters.clear();
        startTime = Instant.now();
        logger.info("Metrics reset");
    }
}