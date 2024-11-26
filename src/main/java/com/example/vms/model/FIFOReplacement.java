package com.example.vms.model;

import com.example.vms.utils.LogResults;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * FIFOReplacement is an implementation of the ReplacementAlgorithm interface using the
 * First-In-First-Out (FIFO) page replacement policy.
 */
public class FIFOReplacement implements ReplacementAlgorithm {
    private Queue<Integer> pageQueue; // stores VPNs in the order they were added
    private int maxSize; // maximum number of pages allowed in the FIFO queue

    /**
     * Constructor for the FIFO replacement algorithm.
     * @param maxSize The maximum number of pages that can be held in the FIFO queue.
     */
    public FIFOReplacement(int maxSize) {
        this.pageQueue = new LinkedList<>();
        this.maxSize = maxSize;
        LogResults.log("FIFO replacement algorithm initialized with max size: " + maxSize);
    }

    /**
     * Evicts the page that has been in the queue the longest (FIFO policy).
     * @return The VPN of the page to be evicted, or -1 if no page is available to evict.
     */
    @Override
    public int evictPage() {
        if (pageQueue.isEmpty()) {
            LogResults.log("No page to evict");
            return -1; // Return -1 to indicate no page is available for eviction
        }
        int evictedVpn = pageQueue.poll(); // remove and return the first inserted page
        LogResults.log("Evicted VPN " + evictedVpn + " from FIFO queue");
        return evictedVpn;
    }

    /**
     * Adds a page to the FIFO queue. If the queue is full, evicts the oldest page first.
     * @param vpn The VPN of the page to be added.
     */
    @Override
    public void addPage(int vpn) {
        // If the page is already in the queue, do nothing
        if (!pageQueue.contains(vpn)) {
            // If the queue is full, evict the oldest page
            if (pageQueue.size() == maxSize) {
                evictPage();
            }
            pageQueue.offer(vpn); // add the new page to the end of the queue
            LogResults.log("Added VPN " + vpn + " to FIFO queue");
        }
    }

    /**
     * Updates page access for the FIFO algorithm. FIFO does not need to track page accesses.
     * @param vpn The VPN of the page that has been accessed.
     */
    @Override
    public void updatePageAccess(int vpn) {
        // FIFO doesn't require tracking page access, so this method does nothing.
    }
//    /**
//     * Returns the oldest page (FIFO order) that is currently in the TLB.
//     * @param tlbPages A set of VPNs representing the pages currently in the TLB.
//     * @return The VPN of the oldest page in the TLB, or -1 if no valid page is found.
//     */
//    public int getTLBFIFOPage(Set<Integer> tlbPages) {
//        for (int vpn : pageQueue) {
//            if (tlbPages.contains(vpn)) {
//                // LogResults.log("Selected VPN " + vpn + " for eviction from FIFO queue among TLB pages.");
//                return vpn; // Return the first VPN in the FIFO order that is in the TLB
//            }
//        }
//        LogResults.log("No valid TLB page found for FIFO eviction.");
//        return -1;
//    }
}
