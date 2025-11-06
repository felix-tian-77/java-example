package com.example.threadcontroller.cli;

import com.example.threadcontroller.model.ThreadPoolConfiguration;
import com.example.threadcontroller.model.ThreadType;
import com.example.threadcontroller.service.ThreadControllerService;
import com.example.threadcontroller.util.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * CLI command for submitting tasks to the thread controller.
 */
@Command(name = "task-submit", description = "Submit tasks to the thread controller for execution.")
public class TaskSubmitCommand implements Callable<Integer> {
    private static final Logger logger = new Logger(TaskSubmitCommand.class);

    @Option(names = {"-t", "--thread-type"}, description = "Thread type (PLATFORM or VIRTUAL)")
    private ThreadType threadType = ThreadType.PLATFORM;

    @Option(names = {"-s", "--pool-size"}, description = "Thread pool size")
    private int poolSize = 10;

    @Option(names = {"-d", "--duration"}, description = "Task duration in milliseconds")
    private long taskDuration = 1000;

    @Parameters(index = "0", description = "Task name or description")
    private String taskName = "default-task";

    private ThreadControllerService threadControllerService;

    /**
     * Executes the task-submit command.
     *
     * @return exit code
     * @throws Exception if an error occurs
     */
    @Override
    public Integer call() throws Exception {
        logger.info("Executing task-submit command with options: name={}, type={}, poolSize={}, duration={}",
                   taskName, threadType, poolSize, taskDuration);

        // Create thread controller service
        ThreadPoolConfiguration config = new ThreadPoolConfiguration();
        config.setCorePoolSize(poolSize);
        config.setMaximumPoolSize(poolSize);
        config.setThreadType(threadType);

        threadControllerService = new ThreadControllerService(config);

        // Submit a simple task
        Runnable task = createSimpleTask(taskName, taskDuration);
        CompletableFuture<Void> future = threadControllerService.submitTask(task);

        System.out.println("Submitted task: " + taskName);
        System.out.println("Task will run for approximately " + taskDuration + " milliseconds");

        // Wait for completion (in a real application, you might not want to block)
        try {
            future.get();
            System.out.println("Task completed successfully: " + taskName);
        } catch (Exception e) {
            System.err.println("Task failed: " + taskName + " - " + e.getMessage());
            logger.error("Task failed: {} - {}", taskName, e.getMessage(), e);
            return 1;
        } finally {
            // Shutdown the thread controller
            threadControllerService.shutdown();
        }

        return 0;
    }

    /**
     * Creates a simple task for testing.
     *
     * @param taskName the name of the task
     * @param duration the duration to sleep in milliseconds
     * @return a Runnable task
     */
    private Runnable createSimpleTask(String taskName, long duration) {
        return () -> {
            try {
                System.out.println("Starting task: " + taskName);
                logger.info("Starting task: {}", taskName);
                Thread.sleep(duration);
                System.out.println("Completed task: " + taskName);
                logger.info("Completed task: {}", taskName);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Task {} was interrupted", taskName);
                throw new RuntimeException(e);
            }
        };
    }
}