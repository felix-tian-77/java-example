package com.example.threadcontroller.service;

import com.example.threadcontroller.model.Task;
import com.example.threadcontroller.util.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manager for task queue operations.
 */
public class TaskQueueManager {
    private static final Logger logger = new Logger(TaskQueueManager.class);

    private final BlockingQueue<Task> taskQueue;
    private final AtomicInteger queuedTasks;
    private final AtomicInteger processedTasks;
    private volatile boolean isShutdown;

    /**
     * Default constructor with unbounded queue.
     */
    public TaskQueueManager() {
        this.taskQueue = new LinkedBlockingQueue<>();
        this.queuedTasks = new AtomicInteger(0);
        this.processedTasks = new AtomicInteger(0);
        this.isShutdown = false;
        logger.info("Created TaskQueueManager with unbounded queue");
    }

    /**
     * Constructor with bounded queue capacity.
     *
     * @param capacity the maximum capacity of the queue
     */
    public TaskQueueManager(int capacity) {
        this.taskQueue = new LinkedBlockingQueue<>(capacity);
        this.queuedTasks = new AtomicInteger(0);
        this.processedTasks = new AtomicInteger(0);
        this.isShutdown = false;
        logger.info("Created TaskQueueManager with bounded queue of capacity: {}", capacity);
    }

    /**
     * Adds a task to the queue.
     *
     * @param task the task to add
     * @return true if the task was added successfully, false otherwise
     * @throws IllegalStateException if the manager has been shut down
     */
    public boolean addTask(Task task) {
        if (isShutdown) {
            throw new IllegalStateException("Cannot add task: TaskQueueManager has been shut down");
        }

        boolean added = taskQueue.offer(task);
        if (added) {
            queuedTasks.incrementAndGet();
            logger.debug("Added task with ID: {} to queue. Queue size: {}", task.getId(), taskQueue.size());
        } else {
            logger.warn("Failed to add task with ID: {} to queue. Queue may be full.", task.getId());
        }
        return added;
    }

    /**
     * Takes a task from the queue, blocking until one is available.
     *
     * @return the next task in the queue
     * @throws InterruptedException if the thread is interrupted while waiting
     * @throws IllegalStateException if the manager has been shut down
     */
    public Task takeTask() throws InterruptedException {
        if (isShutdown) {
            throw new IllegalStateException("Cannot take task: TaskQueueManager has been shut down");
        }

        Task task = taskQueue.take();
        processedTasks.incrementAndGet();
        queuedTasks.decrementAndGet();
        logger.debug("Took task with ID: {} from queue. Remaining queue size: {}", task.getId(), taskQueue.size());
        return task;
    }

    /**
     * Polls for a task from the queue with a timeout.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout
     * @return the next task in the queue, or null if the timeout expires
     * @throws InterruptedException if the thread is interrupted while waiting
     * @throws IllegalStateException if the manager has been shut down
     */
    public Task pollTask(long timeout, TimeUnit unit) throws InterruptedException {
        if (isShutdown) {
            throw new IllegalStateException("Cannot poll task: TaskQueueManager has been shut down");
        }

        Task task = taskQueue.poll(timeout, unit);
        if (task != null) {
            processedTasks.incrementAndGet();
            queuedTasks.decrementAndGet();
            logger.debug("Polled task with ID: {} from queue. Remaining queue size: {}", task.getId(), taskQueue.size());
        }
        return task;
    }

    /**
     * Gets the current size of the queue.
     *
     * @return the number of tasks in the queue
     */
    public int getQueueSize() {
        return taskQueue.size();
    }

    /**
     * Gets the number of queued tasks.
     *
     * @return the number of queued tasks
     */
    public int getQueuedTasks() {
        return queuedTasks.get();
    }

    /**
     * Gets the number of processed tasks.
     *
     * @return the number of processed tasks
     */
    public int getProcessedTasks() {
        return processedTasks.get();
    }

    /**
     * Checks if the queue is empty.
     *
     * @return true if the queue is empty, false otherwise
     */
    public boolean isEmpty() {
        return taskQueue.isEmpty();
    }

    /**
     * Shuts down the task queue manager.
     */
    public void shutdown() {
        isShutdown = true;
        taskQueue.clear();
        logger.info("TaskQueueManager shutdown initiated. Cleared queue of {} tasks", taskQueue.size());
    }

    /**
     * Checks if the manager has been shut down.
     *
     * @return true if shut down, false otherwise
     */
    public boolean isShutdown() {
        return isShutdown;
    }

    /**
     * Gets the remaining capacity of the queue.
     *
     * @return the remaining capacity
     */
    public int remainingCapacity() {
        return taskQueue.remainingCapacity();
    }
}