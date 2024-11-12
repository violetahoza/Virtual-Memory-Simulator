package com.example.vms.model;

import com.example.vms.utils.LogResults;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUReplacement implements ReplacementAlgorithm {
    private final Map<Integer, Long> pageAccessTime;
    private long accessCounter;

    public LRUReplacement() {
        this.pageAccessTime = new LinkedHashMap<>();
        this.accessCounter = 0;
        LogResults.log("LRU replacement algorithm initialized");
    }

    @Override
    public int evictPage() {
        if (pageAccessTime.isEmpty()) {
            LogResults.log("No pages to evict.");
            return -1; // Indicating no pages to evict
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

        // Evict the LRU page
        pageAccessTime.remove(lruPage);
        LogResults.log("Evicted page with VPN " + lruPage + " as it was the least recently used.");
        return lruPage;
    }

    @Override
    public void addPage(int vpn) {
        updatePageAccess(vpn);
        LogResults.log("Added or updated page with VPN " + vpn);
    }

    @Override
    public void updatePageAccess(int vpn) {
        // Update the access time of the page to the current access counter
        pageAccessTime.put(vpn, ++accessCounter);
        LogResults.log("Updated access for VPN " + vpn + " with access time " + accessCounter);
    }
}
