package com.example.vms.model;

import com.example.vms.utils.LogResults;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of the Least Recently Used (LRU) page replacement algorithm.
 * This algorithm evicts the page that has been accessed least recently.
 * It maintains a map of page numbers to their access timestamps to determine LRU.
 */
public class LRUReplacement implements ReplacementAlgorithm {
    private final Map<Integer, Long> pageAccessTime;  // tracks the access time for each page
    private long accessCounter; // incremental counter to simulate access timestamps
    private PageTable pageTable; // reference to the page table

    /**
     * Constructs the LRUReplacement algorithm with a reference to the page table.
     * This allows the algorithm to synchronize access times with the page table entries.
     * @param pageTable The page table used by the memory manager.
     */
    public LRUReplacement(PageTable pageTable) {
        this.pageAccessTime = new LinkedHashMap<>();
        this.pageTable = pageTable;
        this.accessCounter = 0;
        LogResults.log("LRU replacement algorithm initialized");
    }

    /**
     * Evicts the least recently used page from the set of tracked pages.
     * @return The VPN (Virtual Page Number) of the evicted page, or -1 if no page exists.
     */
    @Override
    public int evictPage() {
        if (pageAccessTime.isEmpty()) {
            LogResults.log("No pages to evict.");
            return -1; // Indicating no pages to evict
        }
        int lruPage = getLRUPage();
        if (lruPage != -1) {
            pageAccessTime.remove(lruPage);
            LogResults.log("Evicted page with VPN " + lruPage + " as it was the least recently used.");
        }
        return lruPage;
    }

    /**
     * Adds a page to the LRU tracking structure or updates its access time if it already exists.
     * @param vpn The Virtual Page Number (VPN) of the page to be added or updated.
     */
    @Override
    public void addPage(int vpn) {
        updatePageAccess(vpn);
        // LogResults.log("Added page with VPN " + vpn);
    }

    /**
     * Updates the access time of a page to the current access counter value.
     * This method is called whenever the page is accessed.
     * @param vpn The Virtual Page Number (VPN) of the page to update.
     */
    @Override
    public void updatePageAccess(int vpn) {
        // Update the access time of the page to the current access counter
        long currentAccessTime = ++accessCounter;
        pageAccessTime.put(vpn, currentAccessTime);
        if (pageTable != null) {
            pageTable.updateAccessTime(vpn, currentAccessTime);
        }
        LogResults.log("Updated access for VPN " + vpn + " with access time " + accessCounter);
    }

    /**
     * Returns the VPN of the least recently used page without modifying the structure.
     * @return VPN of the LRU page, or -1 if no pages are present.
     */
    public int getLRUPage() {
        if (pageAccessTime.isEmpty()) {
            LogResults.log("No pages to retrieve as LRU.");
            return -1;
        }
        int lruPage = -1;
        long oldestAccess = Long.MAX_VALUE;
        // Find the page with the oldest access time
        for (Map.Entry<Integer, Long> entry : pageAccessTime.entrySet()) {
            if (entry.getValue() < oldestAccess) {
                oldestAccess = entry.getValue();
                lruPage = entry.getKey();
            }
        }
        LogResults.log("LRU page retrieved: VPN " + lruPage + " with access time " + oldestAccess);
        return lruPage;
    }
    /**
     * Returns the least recently used page among a set of valid pages.
     * @param validPages A set of VPNs representing the pages currently in the TLB.
     * @return The VPN of the least recently used page in the TLB, or -1 if none are found.
     */
    public int getTLBLRUPage(Set<Integer> validPages) {
        int lruPage = -1;
        long oldestAccess = Long.MAX_VALUE;

        for (Map.Entry<Integer, Long> entry : pageAccessTime.entrySet()) {
            int vpn = entry.getKey();
            long accessTime = entry.getValue();

            // Only consider pages in the validPages set
            if (validPages.contains(vpn) && accessTime < oldestAccess) {
                oldestAccess = accessTime;
                lruPage = vpn;
            }
        }
//        if (lruPage != -1) {
//            LogResults.log("TLB-specific LRU page retrieved: VPN " + lruPage + " with access time " + oldestAccess);
//        } else {
//            LogResults.log("No valid TLB pages found for LRU selection.");
//        }
        return lruPage;
    }
}
