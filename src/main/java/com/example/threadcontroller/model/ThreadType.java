package com.example.threadcontroller.model;

/**
 * Enumeration of supported thread types.
 */
public enum ThreadType {
    /**
     * Traditional OS threads.
     */
    PLATFORM,

    /**
     * Virtual threads (requires Java 21+).
     */
    VIRTUAL
}