package com.example.threadcontroller.service;

import com.example.threadcontroller.model.ThreadController;
import com.example.threadcontroller.model.ThreadPoolConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for shutdown services.
 */
public class ShutdownServiceTest {

    private ThreadController threadController;
    private ExecutorService executorService;
    private ShutdownService shutdownService;
    private GracefulShutdownHandler gracefulShutdownHandler;

    @BeforeEach
    void setUp() {
        ThreadPoolConfiguration config = new ThreadPoolConfiguration();
        threadController = new ThreadController(config);
        executorService = Executors.newFixedThreadPool(2);
        shutdownService = new ShutdownService(threadController, executorService);
        gracefulShutdownHandler = new GracefulShutdownHandler(threadController, executorService);
    }

    @AfterEach
    void tearDown() {
        if (executorService != null && !executorService.isTerminated()) {
            executorService.shutdownNow();
        }
    }

    @Test
    void testShutdownServiceCreation() {
        assertNotNull(shutdownService);
        assertFalse(shutdownService.isShutdownInitiated());
        assertFalse(shutdownService.isShutdownComplete());
    }

    @Test
    void testGracefulShutdownHandlerCreation() {
        assertNotNull(gracefulShutdownHandler);
        assertNotNull(gracefulShutdownHandler.getTaskCompletionTracker());
        assertNotNull(gracefulShutdownHandler.getResourceCleanupService());
    }

    @Test
    void testShutdownServiceShutdown() {
        assertFalse(shutdownService.isShutdownInitiated());

        // Perform shutdown
        boolean result = shutdownService.shutdown(5, TimeUnit.SECONDS);

        assertTrue(shutdownService.isShutdownInitiated());
        // Note: The result may vary depending on whether there were actual tasks running
        assertTrue(threadController.isShutdown());
    }

    @Test
    void testShutdownServiceShutdownNow() {
        assertFalse(shutdownService.isShutdownInitiated());

        // Perform immediate shutdown
        shutdownService.shutdownNow();

        assertTrue(shutdownService.isShutdownInitiated());
        assertTrue(threadController.isShutdown());
    }

    @Test
    void testDoubleShutdown() {
        // First shutdown
        boolean firstResult = shutdownService.shutdown(1, TimeUnit.SECONDS);
        assertTrue(shutdownService.isShutdownInitiated());

        // Second shutdown should not fail
        boolean secondResult = shutdownService.shutdown(1, TimeUnit.SECONDS);
        assertTrue(shutdownService.isShutdownInitiated());
    }

    @Test
    void testGracefulShutdown() {
        GracefulShutdownHandler.ShutdownResult result = gracefulShutdownHandler.shutdown(
            2, 2, TimeUnit.SECONDS);

        assertNotNull(result);
        // The shutdown should succeed since there are no tasks running
        assertTrue(result.isShutdownSuccess());
    }

    @Test
    void testTaskCompletionTracker() {
        TaskCompletionTracker tracker = new TaskCompletionTracker();

        assertFalse(tracker.isShutdownInitiated());
        assertEquals(0, tracker.getSubmittedTasks());
        assertEquals(0, tracker.getCompletedTasks());
        assertEquals(0, tracker.getFailedTasks());

        // Register a task
        tracker.registerTask("task-1");
        assertEquals(1, tracker.getSubmittedTasks());

        // Mark task as completed
        tracker.markTaskCompleted("task-1");
        assertEquals(1, tracker.getCompletedTasks());
        assertEquals(TaskCompletionTracker.TaskStatus.COMPLETED, tracker.getTaskStatus("task-1"));

        // Register another task and mark as failed
        tracker.registerTask("task-2");
        tracker.markTaskFailed("task-2", "Test error");
        assertEquals(1, tracker.getFailedTasks());
        assertEquals(TaskCompletionTracker.TaskStatus.FAILED, tracker.getTaskStatus("task-2"));

        // Check completion summary
        String summary = tracker.getCompletionSummary();
        assertNotNull(summary);
        assertTrue(summary.contains("Submitted: 2"));
        assertTrue(summary.contains("Completed: 1"));
        assertTrue(summary.contains("Failed: 1"));
    }

    @Test
    void testTaskSubmissionGuard() {
        TaskSubmissionGuard guard = new TaskSubmissionGuard("TestComponent");

        // Initially, submission should be allowed
        assertTrue(guard.isSubmissionAllowed());
        assertDoesNotThrow(() -> guard.checkSubmissionAllowed());

        // After shutdown initiation, submission should be blocked
        guard.initiateShutdown();
        assertFalse(guard.isSubmissionAllowed());

        assertThrows(com.example.threadcontroller.util.ShutdownException.class, () -> {
            guard.checkSubmissionAllowed();
        });

        // Test reset
        guard.reset();
        assertTrue(guard.isSubmissionAllowed());
    }

    @Test
    void testResourceCleanupService() {
        ResourceCleanupService cleanupService = new ResourceCleanupService();

        assertEquals(0, cleanupService.getResourceCount());

        // Register a resource
        java.io.Closeable resource = () -> System.out.println("Closed");
        cleanupService.registerResource("test-resource", resource);

        assertEquals(1, cleanupService.getResourceCount());
        assertTrue(cleanupService.isResourceRegistered("test-resource"));

        // Unregister the resource
        java.io.Closeable unregistered = cleanupService.unregisterResource("test-resource");
        assertNotNull(unregistered);
        assertEquals(0, cleanupService.getResourceCount());

        // Try to unregister non-existent resource
        java.io.Closeable nonExistent = cleanupService.unregisterResource("non-existent");
        assertNull(nonExistent);
    }

    @Test
    void testShutdownValidator() {
        // Valid parameters
        assertDoesNotThrow(() -> {
            ShutdownValidator.validateShutdownTimeouts(5, 5, TimeUnit.SECONDS);
        });

        assertDoesNotThrow(() -> {
            ShutdownValidator.validateTimeout(10, TimeUnit.SECONDS);
        });

        // Invalid shutdown timeout
        assertThrows(IllegalArgumentException.class, () -> {
            ShutdownValidator.validateShutdownTimeouts(-1, 5, TimeUnit.SECONDS);
        });

        // Invalid termination timeout
        assertThrows(IllegalArgumentException.class, () -> {
            ShutdownValidator.validateShutdownTimeouts(5, -1, TimeUnit.SECONDS);
        });

        // Null time unit
        assertThrows(IllegalArgumentException.class, () -> {
            ShutdownValidator.validateTimeout(5, null);
        });
    }

    @Test
    void testTaskCompletionTrackerShutdownBehavior() {
        TaskCompletionTracker tracker = new TaskCompletionTracker();
        tracker.setShutdownInitiated(true);

        // After shutdown, registering tasks should not fail but should log a warning
        tracker.registerTask("task-1");
        // The task should not be registered
        assertNull(tracker.getTaskStatus("task-1"));
    }
}