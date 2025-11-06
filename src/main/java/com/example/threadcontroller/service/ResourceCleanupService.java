package com.example.threadcontroller.service;

import com.example.threadcontroller.util.Logger;

import java.io.Closeable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Service for cleaning up resources during shutdown.
 */
public class ResourceCleanupService {
    private static final Logger logger = new Logger(ResourceCleanupService.class);

    private final ConcurrentMap<String, Closeable> managedResources;

    /**
     * Default constructor.
     */
    public ResourceCleanupService() {
        this.managedResources = new ConcurrentHashMap<>();
        logger.info("Created ResourceCleanupService");
    }

    /**
     * Registers a resource for cleanup.
     *
     * @param name the name of the resource
     * @param resource the resource to register
     */
    public void registerResource(String name, Closeable resource) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Resource name cannot be null or empty");
        }
        if (resource == null) {
            throw new IllegalArgumentException("Resource cannot be null");
        }

        managedResources.put(name, resource);
        logger.debug("Registered resource for cleanup: {}", name);
    }

    /**
     * Unregisters a resource from cleanup.
     *
     * @param name the name of the resource
     * @return the unregistered resource, or null if not found
     */
    public Closeable unregisterResource(String name) {
        Closeable resource = managedResources.remove(name);
        if (resource != null) {
            logger.debug("Unregistered resource from cleanup: {}", name);
        }
        return resource;
    }

    /**
     * Cleans up a specific resource.
     *
     * @param name the name of the resource
     * @return true if the resource was cleaned up, false if not found
     */
    public boolean cleanupResource(String name) {
        Closeable resource = managedResources.remove(name);
        if (resource != null) {
            try {
                resource.close();
                logger.info("Cleaned up resource: {}", name);
                return true;
            } catch (Exception e) {
                logger.error("Failed to clean up resource: {} - {}", name, e.getMessage(), e);
                return false;
            }
        }
        logger.warn("Resource not found for cleanup: {}", name);
        return false;
    }

    /**
     * Cleans up all registered resources.
     *
     * @return the number of resources successfully cleaned up
     */
    public int cleanupAll() {
        int cleanedCount = 0;
        logger.info("Cleaning up {} registered resources", managedResources.size());

        for (String name : managedResources.keySet()) {
            if (cleanupResource(name)) {
                cleanedCount++;
            }
        }

        logger.info("Cleaned up {} out of {} resources", cleanedCount, managedResources.size());
        return cleanedCount;
    }

    /**
     * Gets the number of registered resources.
     *
     * @return the number of registered resources
     */
    public int getResourceCount() {
        return managedResources.size();
    }

    /**
     * Checks if a resource is registered.
     *
     * @param name the name of the resource
     * @return true if the resource is registered, false otherwise
     */
    public boolean isResourceRegistered(String name) {
        return managedResources.containsKey(name);
    }

    /**
     * Clears all registered resources without cleaning them up.
     */
    public void clear() {
        int count = managedResources.size();
        managedResources.clear();
        logger.info("Cleared {} registered resources without cleanup", count);
    }
}