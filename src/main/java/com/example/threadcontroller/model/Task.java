package com.example.threadcontroller.model;

import com.example.threadcontroller.util.Logger;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a unit of work to be executed by the thread controller.
 */
public class Task {
    private static final Logger logger = new Logger(Task.class);

    private String id;
    private Instant submissionTime;
    private Instant startTime;
    private Instant completionTime;
    private TaskStatus status;
    private Object result;
    private Exception exception;

    /**
     * Default constructor.
     */
    public Task() {
        this.id = UUID.randomUUID().toString();
        this.submissionTime = Instant.now();
        this.status = TaskStatus.PENDING;
        logger.debug("Created new task with ID: {}", id);
    }

    /**
     * Constructor with ID.
     *
     * @param id the unique identifier for the task
     */
    public Task(String id) {
        this.id = id;
        this.submissionTime = Instant.now();
        this.status = TaskStatus.PENDING;
        logger.debug("Created new task with ID: {}", id);
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(Instant submissionTime) {
        this.submissionTime = submissionTime;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(Instant completionTime) {
        this.completionTime = completionTime;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        // Validate state transitions
        if (!isValidTransition(this.status, status)) {
            throw new IllegalArgumentException(
                String.format("Invalid state transition from %s to %s", this.status, status));
        }
        this.status = status;
        logger.debug("Task {} status changed to {}", id, status);
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    /**
     * Validates if a state transition is valid.
     *
     * @param from the current state
     * @param to   the target state
     * @return true if the transition is valid, false otherwise
     */
    private boolean isValidTransition(TaskStatus from, TaskStatus to) {
        switch (from) {
            case PENDING:
                return to == TaskStatus.RUNNING || to == TaskStatus.FAILED;
            case RUNNING:
                return to == TaskStatus.COMPLETED || to == TaskStatus.FAILED;
            case COMPLETED:
            case FAILED:
                return false; // Terminal states
            default:
                return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", submissionTime=" + submissionTime +
                ", startTime=" + startTime +
                ", completionTime=" + completionTime +
                ", status=" + status +
                '}';
    }
}