package com.example.threadcontroller.service;

import com.example.threadcontroller.model.Task;
import com.example.threadcontroller.model.ThreadPoolConfiguration;
import com.example.threadcontroller.model.ThreadType;
import com.example.threadcontroller.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Unit tests for task services.
 */
public class TaskServiceTest {

    private ThreadControllerService threadControllerService;
    private TaskSubmissionService taskSubmissionService;
    private TaskExecutionService taskExecutionService;
    private TaskQueueManager taskQueueManager;

    @BeforeEach
    void setUp() {
        ThreadPoolConfiguration config = new ThreadPoolConfiguration();
        config.setCorePoolSize(5);
        config.setMaximumPoolSize(5);
        config.setThreadType(ThreadType.PLATFORM);

        threadControllerService = new ThreadControllerService(config);
        taskQueueManager = new TaskQueueManager();
        taskExecutionService = new TaskExecutionService(new com.example.threadcontroller.util.MetricsCollector());
    }

    @AfterEach
    void tearDown() {
        if (threadControllerService != null) {
            threadControllerService.shutdown();
        }
        if (taskQueueManager != null) {
            taskQueueManager.shutdown();
        }
    }

    @Test
    void testTaskSubmissionServiceCreation() {
        assertNotNull(threadControllerService);
        assertFalse(threadControllerService.isTerminated());
    }

    @Test
    void testSubmitRunnableTask() throws Exception {
        Runnable task = TestUtils.createSimpleTask("test-task", 100);

        CompletableFuture<Void> future = threadControllerService.submitTask(task);
        assertNotNull(future);

        // Wait for completion
        future.get(5, TimeUnit.SECONDS);

        // Verify the task completed successfully
        assertTrue(future.isDone());
        assertFalse(future.isCancelled());
    }

    @Test
    void testSubmitCallableTask() throws Exception {
        java.util.concurrent.Callable<String> task = TestUtils.createSimpleCallable("test-callable", 100, "result");

        CompletableFuture<String> future = threadControllerService.submitTask(task);
        assertNotNull(future);

        // Wait for completion
        String result = future.get(5, TimeUnit.SECONDS);

        // Verify the task completed successfully
        assertTrue(future.isDone());
        assertFalse(future.isCancelled());
        assertEquals("result", result);
    }

    @Test
    void testSubmitTaskAfterShutdown() {
        threadControllerService.shutdown();

        Runnable task = TestUtils.createSimpleTask("test-task", 100);

        assertThrows(com.example.threadcontroller.util.ShutdownException.class, () -> {
            threadControllerService.submitTask(task);
        });
    }

    @Test
    void testTaskExecutionService() throws Exception {
        Task taskModel = new Task();
        Runnable task = TestUtils.createSimpleTask("execution-test", 50);

        CompletableFuture<Void> future = taskExecutionService.executeTask(task, taskModel);
        assertNotNull(future);

        // Wait for completion
        future.get(5, TimeUnit.SECONDS);

        // Verify the task completed successfully
        assertTrue(future.isDone());
        assertEquals(com.example.threadcontroller.model.TaskStatus.COMPLETED, taskModel.getStatus());
        assertNotNull(taskModel.getStartTime());
        assertNotNull(taskModel.getCompletionTime());
    }

    @Test
    void testTaskExecutionServiceWithCallable() throws Exception {
        Task taskModel = new Task();
        java.util.concurrent.Callable<Integer> task = TestUtils.createSimpleCallable("callable-execution-test", 50, 42);

        CompletableFuture<Integer> future = taskExecutionService.executeTask(task, taskModel);
        assertNotNull(future);

        // Wait for completion
        Integer result = future.get(5, TimeUnit.SECONDS);

        // Verify the task completed successfully
        assertTrue(future.isDone());
        assertEquals(com.example.threadcontroller.model.TaskStatus.COMPLETED, taskModel.getStatus());
        assertNotNull(taskModel.getStartTime());
        assertNotNull(taskModel.getCompletionTime());
        assertEquals(42, result);
        assertEquals(42, taskModel.getResult());
    }

    @Test
    void testTaskQueueManager() throws InterruptedException {
        TaskQueueManager queueManager = new TaskQueueManager(10);

        Task task1 = new Task();
        Task task2 = new Task();

        // Add tasks to queue
        assertTrue(queueManager.addTask(task1));
        assertTrue(queueManager.addTask(task2));

        assertEquals(2, queueManager.getQueueSize());
        assertEquals(2, queueManager.getQueuedTasks());
        assertEquals(0, queueManager.getProcessedTasks());

        // Take tasks from queue
        Task takenTask1 = queueManager.takeTask();
        Task takenTask2 = queueManager.takeTask();

        assertNotNull(takenTask1);
        assertNotNull(takenTask2);
        assertEquals(0, queueManager.getQueueSize());
        assertEquals(0, queueManager.getQueuedTasks());
        assertEquals(2, queueManager.getProcessedTasks());
    }

    @Test
    void testTaskQueueManagerWithTimeout() throws InterruptedException {
        TaskQueueManager queueManager = new TaskQueueManager(10);

        // Poll with timeout when queue is empty
        Task task = queueManager.pollTask(100, TimeUnit.MILLISECONDS);
        assertNull(task);

        // Add a task and poll it
        Task newTask = new Task();
        assertTrue(queueManager.addTask(newTask));

        Task polledTask = queueManager.pollTask(100, TimeUnit.MILLISECONDS);
        assertNotNull(polledTask);
        assertEquals(newTask.getId(), polledTask.getId());
    }

    @Test
    void testTaskAdapter() {
        Runnable runnable = () -> System.out.println("Test runnable");
        Task taskModel = TaskAdapter.fromRunnable(runnable);

        assertNotNull(taskModel);
        assertNotNull(taskModel.getId());
        assertEquals(com.example.threadcontroller.model.TaskStatus.PENDING, taskModel.getStatus());

        // Test wrapping
        Runnable wrapped = TaskAdapter.wrapRunnable(taskModel, runnable);
        assertNotNull(wrapped);
    }

    @Test
    void testTaskResultHandler() throws Exception {
        CompletableFuture<String> future = CompletableFuture.completedFuture("test-result");
        Task taskModel = new Task();

        // Handle result
        TaskResultHandler.handleResult(future, taskModel);

        // Wait a bit for the handler to process
        Thread.sleep(100);

        assertEquals("test-result", taskModel.getResult());
    }

    @Test
    void testTaskResultHandlerWithException() throws Exception {
        CompletableFuture<String> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Test exception"));

        Task taskModel = new Task();

        // Handle result
        TaskResultHandler.handleResult(future, taskModel);

        // Wait a bit for the handler to process
        Thread.sleep(100);

        assertNotNull(taskModel.getException());
        assertTrue(taskModel.getException() instanceof RuntimeException);
        assertEquals("Test exception", taskModel.getException().getMessage());
    }

    @Test
    void testTaskSubmissionValidator() {
        // Valid parameters
        assertDoesNotThrow(() -> {
            TaskSubmissionValidator.validateTaskSubmission("test-task", 1000, 5);
        });

        // Invalid task name
        assertThrows(IllegalArgumentException.class, () -> {
            TaskSubmissionValidator.validateTaskSubmission("", 1000, 5);
        });

        // Invalid task duration
        assertThrows(IllegalArgumentException.class, () -> {
            TaskSubmissionValidator.validateTaskSubmission("test-task", -1, 5);
        });

        // Invalid pool size
        assertThrows(IllegalArgumentException.class, () -> {
            TaskSubmissionValidator.validateTaskSubmission("test-task", 1000, 0);
        });
    }
}