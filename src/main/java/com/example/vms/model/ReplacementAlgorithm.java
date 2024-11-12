package com.example.vms.model;

public interface ReplacementAlgorithm {
    int evictPage(); // Decide which page to evict
    void addPage(int vpn); // Add a new page to track
    void updatePageAccess(int vpn); // Update on page access (for algorithms like LRU)
}
