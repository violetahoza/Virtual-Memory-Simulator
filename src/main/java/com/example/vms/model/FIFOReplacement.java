package com.example.vms.model;

import java.util.LinkedList;
import java.util.Queue;

public class FIFOReplacement implements ReplacementAlgorithm {
    private Queue<Integer> pageQueue; // stores VPNs in the order they were added

    public FIFOReplacement() {
        this.pageQueue = new LinkedList<>();
    }

    @Override
    public int evictPage() {
        if (pageQueue.isEmpty()) {
            throw new IllegalStateException("No pages to evict, the queue is empty.");
        }
        return pageQueue.poll(); // remove and return the first inserted page
    }

    @Override
    public void addPage(int vpn) {
        if (!pageQueue.contains(vpn)) {
            pageQueue.offer(vpn);
        }
    }
}

