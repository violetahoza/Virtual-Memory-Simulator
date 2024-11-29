package com.example.vms.model;

/**
 * Interface representing the various page replacement algorithms.
 * Implementing classes will define the behavior for managing page replacements in the memory.
 */
public interface ReplacementAlgorithm {
    int evictPage(); // Decide which page to evict
    void addPage(int vpn); // Add a new page to track
    void updatePageAccess(int vpn); // Update on page access (for algorithms like LRU)
}
