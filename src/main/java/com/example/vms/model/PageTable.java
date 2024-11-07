package com.example.vms.model;

import com.example.vms.utils.LogResults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a page table that maps virtual page numbers (VPN) to physical page numbers (PPN).
 * Manages page table entries with attributes for memory mapping, including validity, reference, and modification status.
 */
public class PageTable {
    private Map<Integer, PageTableEntry> pageTable; // maps vpns to ppns
    private int size; // nr of entries in the page table


    // Initializes a page table of a given size with empty entries.
    public PageTable(int size) {
        this.size = size;
        pageTable = new HashMap<>();
        for (int i = 0; i < size; i++) {
            PageTableEntry entry = new PageTableEntry();
            pageTable.put(i, entry);
        }
    }

    // Retrieves a page table entry for a given VPN if valid.
    public PageTableEntry getEntry(int vpn) {
        PageTableEntry entry = pageTable.get(vpn);
        if(entry != null && entry.isValid())
            return pageTable.get(vpn);
        return null;
    }

    public List<PageTableEntry> getEntries() {
        return new ArrayList<>(pageTable.values());
    }

    // Retrieves the physical page number associated with a VPN.
    public Integer getPhysicalPageNumber(int vpn) {
        PageTableEntry entry = pageTable.get(vpn);
        if (entry != null) {
            return entry.getFrameNumber();
        }
        return -1;
    }

    public boolean isValid(int vpn) {
        PageTableEntry entry = pageTable.get(vpn);
        return entry != null && entry.isValid();
    }
    public boolean isReferenced(int vpn) {
        PageTableEntry entry = pageTable.get(vpn);
        return entry != null && entry.isReferenced();
    }
    public boolean isDirty(int vpn) {
        PageTableEntry entry = pageTable.get(vpn);
        return entry != null && entry.isDirty();
    }

    public void setDirty(int vpn, boolean dirty) {
        PageTableEntry entry = pageTable.get(vpn);
        if (entry != null) {
            entry.setDirtyBit(dirty);
        }
    }
    public void setReferenced(int vpn, boolean referenced) {
        PageTableEntry entry = pageTable.get(vpn);
        if (entry != null)
            entry.setRefBit(referenced);
    }
    public void setValid(int vpn, boolean valid) {
        PageTableEntry entry = pageTable.get(vpn);
        if (entry != null) {
            entry.setValidBit(valid);
        }
        if(valid == false) {
            entry.setValidBit(valid);
            entry.setFrameNumber(-1);
            entry.setRefBit(false);
            entry.setDirtyBit(false);
        }
    }

    // Finds the VPN that corresponds to a given physical page number.
    public int getCorrespondingVPN(int ppn) {
        for (Map.Entry<Integer, PageTableEntry> entry : pageTable.entrySet()) {
            if (entry.getValue().getFrameNumber() == ppn) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public void addEntry(int vpn, int ppn) {
        // Check if the entry already exists
        PageTableEntry entry = pageTable.getOrDefault(vpn, new PageTableEntry());
        entry.setFrameNumber(ppn);
        entry.setValidBit(true);
        entry.setRefBit(true);
        entry.setDirtyBit(false);
        pageTable.put(vpn, entry);
    }

    public boolean contains(int vpn) {
        return pageTable.containsKey(vpn);
    }

    public void removeEntry(int vpn) {
        PageTableEntry entry = pageTable.get(vpn);
        if (entry != null) {
            entry.setFrameNumber(-1);
            entry.setRefBit(false);
            entry.setDirtyBit(false);
            entry.setValidBit(false);
            pageTable.remove(vpn);
        }
    }

    // Provides a copy of the page table's contents, maintaining the current state of each entry.
    public Map<Integer, PageTableEntry> getPageTableContents() {
        Map<Integer, PageTableEntry> pageTableCopy = new HashMap<>();
        for (Map.Entry<Integer, PageTableEntry> e: pageTable.entrySet()) {
            PageTableEntry newEntry = new PageTableEntry(e.getValue().getFrameNumber(), e.getValue().isValid(), e.getValue().isDirty(), e.getValue().isReferenced());
            pageTableCopy.put(e.getKey(), newEntry);
        }
        return pageTableCopy;
    }

    public void printContents() {
        StringBuilder logBuilder = new StringBuilder("Page table contents:\n----------------------\n");
        for (Map.Entry<Integer, PageTableEntry> e : pageTable.entrySet()) {
            logBuilder.append(e.getKey()).append(": ").append(e.getValue()).append("\n");
        }
        logBuilder.append("----------------------");
        LogResults.log(logBuilder.toString());
    }
}
