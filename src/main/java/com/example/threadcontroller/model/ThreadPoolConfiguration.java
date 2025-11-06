package com.example.threadcontroller.model;

import java.util.concurrent.TimeUnit;
import java.util.Objects;

/**
 * Configuration parameters for the thread pool.
 */
public class ThreadPoolConfiguration {
    private int corePoolSize;
    private int maximumPoolSize;
    private long keepAliveTime;
    private TimeUnit timeUnit;
    private ThreadType threadType;

    /**
     * Default constructor.
     */
    public ThreadPoolConfiguration() {
        // Default values
        this.corePoolSize = 10;
        this.maximumPoolSize = 20;
        this.keepAliveTime = 60;
        this.timeUnit = TimeUnit.SECONDS;
        this.threadType = ThreadType.PLATFORM;
    }

    /**
     * Constructor with all parameters.
     *
     * @param corePoolSize    Number of core threads to keep alive
     * @param maximumPoolSize Maximum number of threads allowed
     * @param keepAliveTime   Time to keep excess threads alive
     * @param timeUnit        Time unit for keepAliveTime
     * @param threadType      Type of threads to use (PLATFORM or VIRTUAL)
     */
    public ThreadPoolConfiguration(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit timeUnit, ThreadType threadType) {
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.threadType = threadType;
        validate();
    }

    /**
     * Validates the configuration parameters.
     *
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public void validate() {
        if (corePoolSize < 0) {
            throw new IllegalArgumentException("Core pool size must be >= 0");
        }
        if (maximumPoolSize < corePoolSize) {
            throw new IllegalArgumentException("Maximum pool size must be >= core pool size");
        }
        if (keepAliveTime < 0) {
            throw new IllegalArgumentException("Keep alive time must be >= 0");
        }
        if (timeUnit == null) {
            throw new IllegalArgumentException("Time unit cannot be null");
        }
        if (threadType == null) {
            throw new IllegalArgumentException("Thread type cannot be null");
        }
    }

    // Getters and setters

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
        validate();
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
        validate();
    }

    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
        validate();
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        validate();
    }

    public ThreadType getThreadType() {
        return threadType;
    }

    public void setThreadType(ThreadType threadType) {
        this.threadType = threadType;
        validate();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThreadPoolConfiguration that = (ThreadPoolConfiguration) o;
        return corePoolSize == that.corePoolSize &&
                maximumPoolSize == that.maximumPoolSize &&
                keepAliveTime == that.keepAliveTime &&
                Objects.equals(timeUnit, that.timeUnit) &&
                threadType == that.threadType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(corePoolSize, maximumPoolSize, keepAliveTime, timeUnit, threadType);
    }

    @Override
    public String toString() {
        return "ThreadPoolConfiguration{" +
                "corePoolSize=" + corePoolSize +
                ", maximumPoolSize=" + maximumPoolSize +
                ", keepAliveTime=" + keepAliveTime +
                ", timeUnit=" + timeUnit +
                ", threadType=" + threadType +
                '}';
    }
}