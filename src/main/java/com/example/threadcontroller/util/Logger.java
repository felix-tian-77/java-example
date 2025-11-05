package com.example.threadcontroller.util;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Simple structured logger for the thread controller system.
 */
public class Logger {
    private final String className;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_INSTANT;

    /**
     * Constructs a logger for the specified class.
     *
     * @param clazz the class to create a logger for
     */
    public Logger(Class<?> clazz) {
        this.className = clazz.getSimpleName();
    }

    /**
     * Logs an informational message.
     *
     * @param message the message to log
     */
    public void info(String message) {
        log("INFO", message, null);
    }

    /**
     * Logs an informational message with parameters.
     *
     * @param message the message to log
     * @param params  the parameters to include in the log
     */
    public void info(String message, Object... params) {
        log("INFO", formatMessage(message, params), null);
    }

    /**
     * Logs a warning message.
     *
     * @param message the message to log
     */
    public void warn(String message) {
        log("WARN", message, null);
    }

    /**
     * Logs a warning message with parameters.
     *
     * @param message the message to log
     * @param params  the parameters to include in the log
     */
    public void warn(String message, Object... params) {
        log("WARN", formatMessage(message, params), null);
    }

    /**
     * Logs an error message.
     *
     * @param message the message to log
     */
    public void error(String message) {
        log("ERROR", message, null);
    }

    /**
     * Logs an error message with parameters.
     *
     * @param message the message to log
     * @param params  the parameters to include in the log
     */
    public void error(String message, Object... params) {
        log("ERROR", formatMessage(message, params), null);
    }

    /**
     * Logs an error message with an exception.
     *
     * @param message the message to log
     * @param throwable the exception to log
     */
    public void error(String message, Throwable throwable) {
        log("ERROR", message, throwable);
    }

    /**
     * Logs a debug message.
     *
     * @param message the message to log
     */
    public void debug(String message) {
        log("DEBUG", message, null);
    }

    /**
     * Logs a debug message with parameters.
     *
     * @param message the message to log
     * @param params  the parameters to include in the log
     */
    public void debug(String message, Object... params) {
        log("DEBUG", formatMessage(message, params), null);
    }

    /**
     * Internal method to log messages with a specific level.
     *
     * @param level     the log level
     * @param message   the message to log
     * @param throwable an optional throwable to log
     */
    private void log(String level, String message, Throwable throwable) {
        String timestamp = FORMATTER.format(Instant.now());
        String threadName = Thread.currentThread().getName();

        System.out.printf("[%s] [%s] [%s] [%s] %s%n",
                timestamp, threadName, level, className, message);

        if (throwable != null) {
            throwable.printStackTrace(System.out);
        }
    }

    /**
     * Formats a message with parameters.
     *
     * @param message the message template
     * @param params  the parameters to insert
     * @return the formatted message
     */
    private String formatMessage(String message, Object... params) {
        if (params == null || params.length == 0) {
            return message;
        }

        String formatted = message;
        for (Object param : params) {
            formatted = formatted.replaceFirst("\\{\\}", String.valueOf(param));
        }
        return formatted;
    }
}