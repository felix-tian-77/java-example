package com.example.threadcontroller.model;

import com.example.threadcontroller.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for thread controller core models.
 */
public class ThreadControllerModelTest {

    private ThreadPoolConfiguration config;
    private ThreadController controller;
    private Task task;

    @BeforeEach
    void setUp() {
        config = new ThreadPoolConfiguration();
        controller = new ThreadController(config);
        task = new Task();
    }

    @Test
    void testThreadTypeEnumValues() {
        assertEquals(2, ThreadType.values().length);
        assertNotNull(ThreadType.PLATFORM);
        assertNotNull(ThreadType.VIRTUAL);
    }

    @Test
    void testTaskStatusEnumValues() {
        assertEquals(4, TaskStatus.values().length);
        assertNotNull(TaskStatus.PENDING);
        assertNotNull(TaskStatus.RUNNING);
        assertNotNull(TaskStatus.COMPLETED);
        assertNotNull(TaskStatus.FAILED);
    }

    @Test
    void testThreadPoolConfigurationDefaultValues() {
        assertEquals(10, config.getCorePoolSize());
        assertEquals(20, config.getMaximumPoolSize());
        assertEquals(60, config.getKeepAliveTime());
        assertEquals(TimeUnit.SECONDS, config.getTimeUnit());
        assertEquals(ThreadType.PLATFORM, config.getThreadType());
    }

    @Test
    void testThreadPoolConfigurationConstructor() {
        ThreadPoolConfiguration customConfig = new ThreadPoolConfiguration(
            5, 15, 30, TimeUnit.MINUTES, ThreadType.VIRTUAL);

        assertEquals(5, customConfig.getCorePoolSize());
        assertEquals(15, customConfig.getMaximumPoolSize());
        assertEquals(30, customConfig.getKeepAliveTime());
        assertEquals(TimeUnit.MINUTES, customConfig.getTimeUnit());
        assertEquals(ThreadType.VIRTUAL, customConfig.getThreadType());
    }

    @Test
    void testThreadPoolConfigurationValidation() {
        // Valid configuration
        assertDoesNotThrow(() -> config.validate());

        // Invalid core pool size
        config.setCorePoolSize(-1);
        assertThrows(IllegalArgumentException.class, () -> config.validate());
        config.setCorePoolSize(10); // Reset to valid value

        // Invalid maximum pool size
        config.setMaximumPoolSize(5); // Less than core pool size
        assertThrows(IllegalArgumentException.class, () -> config.validate());
        config.setMaximumPoolSize(20); // Reset to valid value

        // Invalid keep alive time
        config.setKeepAliveTime(-1);
        assertThrows(IllegalArgumentException.class, () -> config.validate());
        config.setKeepAliveTime(60); // Reset to valid value

        // Null time unit
        config.setTimeUnit(null);
        assertThrows(IllegalArgumentException.class, () -> config.validate());
        config.setTimeUnit(TimeUnit.SECONDS); // Reset to valid value

        // Null thread type
        config.setThreadType(null);
        assertThrows(IllegalArgumentException.class, () -> config.validate());
    }

    @Test
    void testTaskInitialState() {
        assertNotNull(task.getId());
        assertNotNull(task.getSubmissionTime());
        assertNull(task.getStartTime());
        assertNull(task.getCompletionTime());
        assertEquals(TaskStatus.PENDING, task.getStatus());
        assertNull(task.getResult());
        assertNull(task.getException());
    }

    @Test
    void testTaskStateTransitions() {
        // PENDING -> RUNNING
        task.setStatus(TaskStatus.RUNNING);
        assertEquals(TaskStatus.RUNNING, task.getStatus());

        // RUNNING -> COMPLETED
        task.setStatus(TaskStatus.COMPLETED);
        assertEquals(TaskStatus.COMPLETED, task.getStatus());

        // RUNNING -> FAILED
        Task failedTask = new Task();
        failedTask.setStatus(TaskStatus.RUNNING);
        failedTask.setStatus(TaskStatus.FAILED);
        assertEquals(TaskStatus.FAILED, failedTask.getStatus());
    }

    @Test
    void testInvalidTaskStateTransitions() {
        task.setStatus(TaskStatus.RUNNING);
        task.setStatus(TaskStatus.COMPLETED);

        // Cannot transition from COMPLETED to any other state
        assertThrows(IllegalArgumentException.class, () -> task.setStatus(TaskStatus.RUNNING));
        assertThrows(IllegalArgumentException.class, () -> task.setStatus(TaskStatus.PENDING));
        assertThrows(IllegalArgumentException.class, () -> task.setStatus(TaskStatus.FAILED));
    }

    @Test
    void testTaskEquality() {
        Task task1 = new Task("test-id");
        Task task2 = new Task("test-id");
        Task task3 = new Task("different-id");

        assertEquals(task1, task2);
        assertNotEquals(task1, task3);
        assertEquals(task1.hashCode(), task2.hashCode());
    }

    @Test
    void testThreadControllerInitialState() {
        assertEquals(config, controller.getConfiguration());
        assertFalse(controller.isShutdown());
        assertEquals(0, controller.getSubmittedTasks());
        assertEquals(0, controller.getCompletedTasks());
        assertEquals(ThreadType.PLATFORM, controller.getThreadType());
        assertEquals(10, controller.getPoolSize());
    }

    @Test
    void testThreadControllerCounters() {
        assertEquals(0, controller.getSubmittedTasks());
        assertEquals(1, controller.incrementSubmittedTasks());
        assertEquals(1, controller.getSubmittedTasks());

        assertEquals(0, controller.getCompletedTasks());
        assertEquals(1, controller.incrementCompletedTasks());
        assertEquals(1, controller.getCompletedTasks());
    }

    @Test
    void testThreadControllerShutdown() {
        assertFalse(controller.isShutdown());
        controller.setShutdown(true);
        assertTrue(controller.isShutdown());
        controller.setShutdown(false);
        assertFalse(controller.isShutdown());
    }
}