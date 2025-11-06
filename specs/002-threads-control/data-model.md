# Data Model: Thread Controller System

## Entities

### ThreadController

The main controller that manages thread pools and task execution.

**Attributes**:
- `executorService`: ExecutorService - The underlying thread pool executor
- `isShutdown`: boolean - Flag indicating if shutdown has been initiated
- `threadType`: ThreadType - Enum indicating whether using platform or virtual threads
- `poolSize`: int - Current size of the thread pool
- `submittedTasks`: AtomicInteger - Counter for submitted tasks
- `completedTasks`: AtomicInteger - Counter for completed tasks

**Validation Rules**:
- Pool size must be positive
- Cannot submit tasks after shutdown initiated
- Thread type must be either PLATFORM or VIRTUAL

### Task

Represents a unit of work to be executed by the thread controller.

**Attributes**:
- `id`: String - Unique identifier for the task
- `submissionTime`: Instant - Timestamp when task was submitted
- `startTime`: Instant - Timestamp when task started execution
- `completionTime`: Instant - Timestamp when task completed
- `status`: TaskStatus - Current status of the task (PENDING, RUNNING, COMPLETED, FAILED)
- `result`: Object - Result of task execution (if any)
- `exception`: Exception - Exception thrown during execution (if any)

**Validation Rules**:
- Task ID must be unique
- Status transitions must follow valid state machine (PENDING → RUNNING → COMPLETED/FAILED)

### ThreadPoolConfiguration

Configuration parameters for the thread pool.

**Attributes**:
- `corePoolSize`: int - Number of core threads to keep alive
- `maximumPoolSize`: int - Maximum number of threads allowed
- `keepAliveTime`: long - Time to keep excess threads alive
- `timeUnit`: TimeUnit - Time unit for keepAliveTime
- `threadType`: ThreadType - Type of threads to use (PLATFORM or VIRTUAL)

**Validation Rules**:
- Core pool size must be >= 0
- Maximum pool size must be >= core pool size
- Keep alive time must be >= 0

## Enums

### ThreadType

Enumeration of supported thread types.

**Values**:
- `PLATFORM`: Traditional OS threads
- `VIRTUAL`: Virtual threads (requires Java 21+)

### TaskStatus

Enumeration of task execution states.

**Values**:
- `PENDING`: Task has been submitted but not yet started
- `RUNNING`: Task is currently executing
- `COMPLETED`: Task completed successfully
- `FAILED`: Task failed with an exception

## Relationships

- One-to-One: ThreadController to ThreadPoolConfiguration (each controller has one configuration)
- One-to-Many: ThreadController to Task (each controller manages multiple tasks)
- Task contains references to its result and any exception that occurred

## State Transitions

### Task States
1. **PENDING**: Initial state when task is submitted
2. **RUNNING**: Task is being executed by a thread
3. **COMPLETED**: Task finished successfully
4. **FAILED**: Task terminated due to an exception

### ThreadController States
1. **ACTIVE**: Controller is accepting and executing tasks
2. **SHUTDOWN_INITIATED**: No new tasks accepted, waiting for existing tasks to complete
3. **TERMINATED**: All tasks completed, resources released

## Data Persistence

### In-Memory Storage
- ThreadController instances are not persisted
- Task information is kept in memory only during execution
- Statistics are ephemeral and reset on application restart

### Thread Safety
- All shared data structures use thread-safe collections
- Atomic operations for counters and flags
- Proper synchronization for state transitions