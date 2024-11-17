package com.example.vms.model;

import com.example.vms.utils.LogResults;

/**
 * Results class is responsible for tracking various statistics related to TLB and page table operations.
 * This includes hits, misses, disk accesses, and eviction events.
 */
public class Results {
    public static int tlbHit = 0, tlbMiss = 0, pageTableHit = 0, pageTableMiss = 0;
    public static int tlbAccesses = 0, pageTableAccesses = 0;
    public static int diskRead = 0, diskWrite = 0, pageEviction = 0;

    /**
     * Calculates the TLB hit rate as a percentage.
     * @return The TLB hit rate as a percentage. If there are no TLB accesses, returns 0.0.
     */
    public static double getTLBHitRate() {
        if (tlbAccesses == 0) return 0.0; // Prevent division by zero
        return (double) tlbHit / tlbAccesses * 100;
    }

    /**
     * Calculates the TLB miss rate as a percentage.
     * @return The TLB miss rate as a percentage. If there are no TLB accesses, returns 0.0.
     */
    public static double getTLBMissRate() {
        if (tlbAccesses == 0) return 0.0; // Prevent division by zero
        return (double) tlbMiss / tlbAccesses * 100;
    }

    /**
     * Calculates the page table hit rate as a percentage.
     * @return The page table hit rate as a percentage. If there are no page table accesses, returns 0.0.
     */
    public static double getPageTableHitRate() {
        if (pageTableAccesses == 0) return 0.0; // Prevent division by zero
        return (double) pageTableHit / pageTableAccesses * 100;
    }

    /**
     * Calculates the page table miss rate as a percentage.
     * @return The page table miss rate as a percentage. If there are no page table accesses, returns 0.0.
     */
    public static double getPageTableMissRate() {
        if (pageTableAccesses == 0) return 0.0; // Prevent division by zero
        return (double) pageTableMiss / pageTableAccesses * 100;
    }

    /**
     * Resets all statistics to zero. This can be used to reset the counters for new simulation runs.
     */
    public static void reset() {
        tlbHit = 0;
        tlbMiss = 0;
        pageTableHit = 0;
        pageTableMiss = 0;
        diskRead = 0;
        diskWrite = 0;
        pageEviction = 0;
        tlbAccesses = 0;
        pageTableAccesses = 0;
    }

    /**
     * Logs the current statistics
     */
    public static void logStats() {
        LogResults.log("\nTLB Hits: " + Results.tlbHit + " TlB Misses: " + Results.tlbMiss + " TLB Accesses: " + Results.tlbAccesses);
        LogResults.log("Page table hits: " + Results.pageTableHit + " Page table misses: " + Results.pageTableMiss + " Page table accesses: " + Results.pageTableAccesses);
        LogResults.log("TLB Hit Rate: " + getTLBHitRate() + "%");
        LogResults.log("TLB Miss Rate: " + getTLBMissRate() + "%");
        LogResults.log("Page Table Hit Rate: " + getPageTableHitRate() + "%");
        LogResults.log("Page Table Miss Rate: " + getPageTableMissRate() + "%");
        LogResults.log("Disk Reads: " + diskRead);
        LogResults.log("Disk Writes: " + diskWrite);
        LogResults.log("Page Evictions: " + pageEviction + '\n');
    }
}
