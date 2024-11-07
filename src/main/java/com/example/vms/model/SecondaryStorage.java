package com.example.vms.model;

import com.example.vms.utils.LogResults;

import java.util.HashMap;
import java.util.Map;

public class SecondaryStorage {
    private Map<Integer, Page> disk; // simulate disk storage for pages (vpn -> page)
    private int pageSize;

    public SecondaryStorage(int maxPages, int pageSize) {
        disk = new HashMap<>();
        this.pageSize = pageSize;
        for (int i = 0; i < maxPages; i++) {
            disk.put(i, new Page(pageSize)); // initialize disk
        }
    }

    public void store(int vpn, Page page) { disk.put(vpn, page);}// store a page in secondary storage

    public Page load(int vpn) { // retrieve a page from the secondary storage
        if (!disk.containsKey(vpn)) {
            throw new IllegalArgumentException("Invalid VPN: Page not found.");
        }
        return disk.get(vpn);
    }

    // Returns a copy of disk contents with virtual addresses and offsets
    public Map<Integer, Map<Integer, Integer>> getDiskContents() {
        Map<Integer, Map<Integer, Integer>> diskCopy = new HashMap<>();
        for (Map.Entry<Integer, Page> e: disk.entrySet()) {
            Map<Integer, Integer> pageCopy = e.getValue().getPageContents();
            //addresses with offset
            int virtualPageNumber = e.getKey();
            Map<Integer, Integer> pageCopyWithOffsets = new HashMap<>();
            for(Map.Entry<Integer, Integer> pe: pageCopy.entrySet()) {
                int offset = pe.getKey();
                int address = virtualPageNumber * pageSize + offset;
                pageCopyWithOffsets.put(address, pe.getValue());
            }
            diskCopy.put(e.getKey(), pageCopyWithOffsets);
        }
        return diskCopy;
    }

    public void printContents() {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("Disk contents:\n----------------------\n");

        for (Map.Entry<Integer, Page> entry : disk.entrySet()) {
            logBuilder.append("Page ").append(entry.getKey()).append(":\n");
            logBuilder.append(entry.getValue().printContents());
        }

        logBuilder.append("----------------------");
        LogResults.log(logBuilder.toString());
    }
}
