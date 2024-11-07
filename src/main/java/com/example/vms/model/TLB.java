package com.example.vms.model;

import com.example.vms.utils.LogResults;

import java.util.LinkedHashMap;
import java.util.Map;

public class TLB {
    private LinkedHashMap<Integer, PageTableEntry> entries; // Maps VPNs to page table entries
    private int maxSize; // max nr of entries in the TLB

    public TLB(int size) {
        this.entries = new LinkedHashMap<>();
        this.maxSize = size;
    }

    // Retrieves an entry for a given VPN if it is valid.
    public PageTableEntry getEntry(int vpn) {
        PageTableEntry entry = entries.get(vpn);
        if(entry != null && entry.isValid())
            return entry;
        return null;
    }

    public void addEntry(int vpn, PageTableEntry entry) {
        // Check if the TLB is already full
        if (entries.size() >= maxSize) {
            // Remove the first entry (the oldest entry)
            Map.Entry<Integer, PageTableEntry> firstEntry = entries.entrySet().iterator().next();
            entries.remove(firstEntry.getKey());
        }
        // Add the new entry
        entries.put(vpn, entry);
    }

    public int lookup(int vpn) {
        PageTableEntry entry = entries.get(vpn);
        if (entry != null && entry.isValid()) {
            entry.setRefBit(true); // Set the reference bit
            return entry.getFrameNumber(); // TLB hit
        }
        return -1; // TLB miss
    }

    public void removeEntry(int vpn) {
        entries.remove(vpn);
    }
    public boolean contains(int vpn) {
        if (entries.containsKey(vpn))
            return true;
        return false;
    }
    public boolean isFull(){
        if(entries.size() == maxSize)
            return true;
        return false;
    }
    public boolean isDirty(int vpn){
        PageTableEntry entry = entries.get(vpn);
        return entry != null && entry.isDirty();
    }
    public boolean isReferenced(int vpn){
        PageTableEntry entry = entries.get(vpn);
        return entry != null && entry.isReferenced();
    }
    public boolean isValid(int vpn){
        PageTableEntry entry = entries.get(vpn);
        return entry != null && entry.isValid();
    }
    public int getCorrespondingVPN(int ppn) {
        for (Map.Entry<Integer, PageTableEntry> entry : entries.entrySet()) {
            if (entry.getValue().getFrameNumber() == ppn) {
                return entry.getKey();
            }
        }
        return -1;
    }
    public void setDirty(int vpn, boolean dirty) {
        PageTableEntry entry = entries.get(vpn);
        if (entry != null) {
            entry.setDirtyBit(dirty);
        }
    }
    public void setReferenced(int vpn, boolean referenced) {
        PageTableEntry entry = entries.get(vpn);
        if (entry != null)
            entry.setRefBit(referenced);
    }
    public void setValid(int vpn, boolean valid) {
        PageTableEntry entry = entries.get(vpn);
        if (entry != null) {
            entry.setValidBit(false);
        }
        if(valid == false)
            entries.get(vpn).setFrameNumber(-1);
    }

    // Provides a copy of the TLB contents to prevent direct modification.
    public Map<Integer, PageTableEntry> getTLBContents() {
        Map<Integer, PageTableEntry> tlbCopy = new LinkedHashMap<>();
        for (Map.Entry<Integer, PageTableEntry> e: entries.entrySet()) {
            PageTableEntry newEntry = new PageTableEntry(e.getValue().getFrameNumber(), e.getValue().isValid(), e.getValue().isDirty(), e.getValue().isReferenced());
            tlbCopy.put(e.getKey(), newEntry);
        }
        return tlbCopy;
    }

    public void printContents() {
        LogResults.log("TLB contents:\n" + "----------------------");
        for (Map.Entry<Integer, PageTableEntry> e: entries.entrySet()) {
            LogResults.log(e.getKey() + ": " + e.getValue());
        }
        LogResults.log("----------------------");
    }
}
