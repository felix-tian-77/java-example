# Quick Start Guide: Thread Controller System

## Prerequisites
- OpenJDK 25 or later
- Maven 3.8+
- Git

## Building the Project
```bash
# Clone the repository
git clone &lt;repository-url&gt;
cd thread-controller

# Build the project
mvn clean install
```

## Running the Application
```bash
# Create a thread controller
java -jar target/thread-controller.jar create --pool-size 10 --thread-type VIRTUAL

# Submit a task
java -jar target/thread-controller.jar submit-task --controller-id "controller-1" --task-name "example-task" --task-data '{"action":"sleep","duration":5000}'

# Get controller status
java -jar target/thread-controller.jar get-status --controller-id "controller-1" --include-tasks

# Shutdown controller gracefully
java -jar target/thread-controller.jar shutdown --controller-id "controller-1" --timeout 30
```

## Example Usage

### Basic Thread Controller Creation
```java
// Create a thread controller with 10 virtual threads
ThreadController controller = ThreadController.builder()
    .poolSize(10)
    .threadType(ThreadType.VIRTUAL)
    .build();

// Submit a simple task
CompletableFuture&lt;String&gt; future = controller.submitTask(() -&gt; {
    // Simulate some work
    Thread.sleep(1000);
    return "Task completed";
});

// Get the result (blocking)
String result = future.get();
```

### I/O-Bound Task Example
```java
// Virtual threads are ideal for I/O-bound tasks
ThreadController controller = ThreadController.builder()
    .poolSize(100) // Can handle many virtual threads efficiently
    .threadType(ThreadType.VIRTUAL)
    .build();

// Submit multiple I/O tasks
List&lt;CompletableFuture&lt;String&gt;&gt; futures = new ArrayList&lt;&gt;();
for (int i = 0; i &lt; 50; i++) {
    final int taskId = i;
    CompletableFuture&lt;String&gt; future = controller.submitTask(() -&gt; {
        // Simulate I/O operation (e.g., HTTP request, database query)
        simulateNetworkCall();
        return "Task " + taskId + " completed";
    });
    futures.add(future);
}

// Wait for all tasks to complete
CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
```

### Graceful Shutdown
```java
// Submit some long-running tasks
for (int i = 0; i &lt; 5; i++) {
    controller.submitTask(() -&gt; {
        Thread.sleep(10000); // 10-second task
        return "Long task completed";
    });
}

// Initiate graceful shutdown
controller.shutdown();

// Wait for completion (non-blocking check)
while (!controller.isTerminated()) {
    System.out.println("Waiting for tasks to complete...");
    Thread.sleep(1000);
}

System.out.println("All tasks completed and resources released");
```

## Running Tests
```bash
# Run all tests
mvn test

# Run unit tests only
mvn test -Dtest.include="**/*Test.java"

# Run integration tests only
mvn test -Dtest.include="**/*IntegrationTest.java"

# Generate test coverage report
mvn jacoco:report
```

## Project Structure
```
src/main/java/
├── model/          # Data models (ThreadController, Task, etc.)
├── service/        # Business logic implementations
├── cli/            # Command-line interface handlers
└── util/           # Utility classes

src/test/java/
├── contract/       # Contract tests for APIs
├── integration/    # Integration tests
└── unit/           # Unit tests
```

## Configuration
The thread controller can be configured with various parameters:

```bash
# Create controller with custom configuration
java -jar target/thread-controller.jar create \
  --pool-size 20 \
  --thread-type PLATFORM \
  --keep-alive 30 \
  --time-unit SECONDS
```

## Best Practices

1. **Use Virtual Threads for I/O-bound Tasks**: Virtual threads are lightweight and perfect for I/O operations
2. **Use Platform Threads for CPU-intensive Tasks**: Platform threads are better for CPU-bound work
3. **Always Shutdown Gracefully**: Ensure proper resource cleanup by calling shutdown()
4. **Monitor Task Progress**: Use status queries to track task execution
5. **Handle Exceptions Properly**: Implement proper error handling in your tasks

## Performance Considerations

- Virtual threads have minimal memory overhead (few hundred bytes vs. megabytes for platform threads)
- Thread creation is much faster with virtual threads
- Context switching is more efficient with virtual threads
- For CPU-bound tasks, stick to platform threads and appropriate pool sizing