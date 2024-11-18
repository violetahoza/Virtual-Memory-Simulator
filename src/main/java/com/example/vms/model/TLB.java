package com.example.vms.model;

import com.example.vms.utils.LogResults;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a Translation Lookaside Buffer (TLB) that stores mappings of VPNs to page table entries.
 * It uses an eviction algorithm to manage the size of the TLB when it is full.
 */
public class TLB {
    private LinkedHashMap<Integer, PageTableEntry> entries; // Maps VPNs to page table entries
    private int maxSize; // Maximum number of entries in the TLB
    private ReplacementAlgorithm evictionAlgorithm; // The eviction algorithm to use

    /**
     * Initializes the TLB with a specified size and eviction algorithm.
     * @param size The maximum number of entries in the TLB.
     * @param evictionAlgorithm The algorithm used to decide which entry to evict when the TLB is full.
     */
    public TLB(int size, ReplacementAlgorithm evictionAlgorithm) {
        this.entries = new LinkedHashMap<>();
        this.maxSize = size;
        this.evictionAlgorithm = evictionAlgorithm;
        LogResults.log("TLB initialized with size: " + size + " and eviction algorithm: " + evictionAlgorithm.getClass().getSimpleName());
    }

    /**
     * Adds a new entry to the TLB. Evicts an entry if necessary based on the eviction algorithm.
     * @param vpn The virtual page number (VPN).
     * @param entry The page table entry to add.
     */
    public void addEntry(int vpn, PageTableEntry entry) {
        // Evict an entry if the TLB is full
        if (isFull()) {
            int evictedVpn = evictionAlgorithm.evictPage();
            if (entries.containsKey(evictedVpn)) {
                entries.remove(evictedVpn); // Remove the evicted entry from the TLB
            }
            LogResults.log("Evicted VPN " + evictedVpn + " from TLB based on eviction algorithm");
        }
        // Add the new entry
        entries.put(vpn, new PageTableEntry(entry.getFrameNumber(), entry.isValid(), entry.isDirty(), entry.isReferenced(), entry.isDiskPage()));
        evictionAlgorithm.addPage(vpn); // Add the page to the eviction algorithm
        LogResults.log("Added VPN " + vpn + " to TLB: " + entry);
    }

    /**
     * Looks up the frame number for a given VPN in the TLB.
     * @param vpn The virtual page number (VPN).
     * @return The frame number if found, or -1 if there is a TLB miss.
     */
    public int lookup(int vpn) {
        PageTableEntry entry = entries.get(vpn);
        if (entry != null && entry.isValid()) {
            evictionAlgorithm.updatePageAccess(vpn); // Update LRU for the accessed VPN
            entry.setRefBit(true); // Set the reference bit
            //LogResults.log("TLB hit for VPN " + vpn + ": Frame " + entry.getFrameNumber());
            return entry.getFrameNumber(); // TLB hit
        }
        //LogResults.log("TLB miss for VPN " + vpn);
        return -1; // TLB miss
    }

    /**
     * Removes a given VPN from the TLB.
     * @param vpn The virtual page number (VPN) to remove.
     */
    public void removeEntry(int vpn) {
        entries.remove(vpn);
        LogResults.log("Removed VPN " + vpn + " from TLB");
    }

    /**
     * Checks if the TLB is full.
     * @return True if the TLB is full, false otherwise.
     */
    public boolean isFull() {
        boolean full = entries.size() == maxSize;
        //LogResults.log("TLB full: " + full);
        return full;
    }

    /**
     * Prints the current contents of the TLB to the log.
     */
    public void printContents() {
        LogResults.log("TLB contents:\n----------------------");
        for (Map.Entry<Integer, PageTableEntry> e : entries.entrySet()) {
            LogResults.log(e.getKey() + ": " + e.getValue());
        }
        LogResults.log("----------------------");
    }

    /**
     * Provides a copy of the current TLB contents, to avoid direct modification of the internal structure.
     * @return A map representing the TLB contents.
     */
    public Map<Integer, PageTableEntry> getTLBContents() {
        Map<Integer, PageTableEntry> tlbCopy = new LinkedHashMap<>();
        for (Map.Entry<Integer, PageTableEntry> e : entries.entrySet()) {
            PageTableEntry newEntry = new PageTableEntry(e.getValue().getFrameNumber(), e.getValue().isValid(), e.getValue().isDirty(), e.getValue().isReferenced(), e.getValue().isDiskPage());
            tlbCopy.put(e.getKey(), newEntry);
        }
        //LogResults.log("Copied TLB contents");
        return tlbCopy;
    }

    public Map<Integer, PageTableEntry> getEntries() {
        return new LinkedHashMap<>(entries); // Return a copy of the entries
    }

    /**
     //     * Retrieves the page table entry for a given VPN if it exists and is valid.
     //     * @param vpn The virtual page number (VPN).
     //     * @return The corresponding page table entry, or null if not found or invalid.
     //     */
    public PageTableEntry getEntry(int vpn) {
        PageTableEntry entry = entries.get(vpn);
        if (entry != null && entry.isValid()) {
            entry.setRefBit(true); // Mark the entry as referenced
            //evictionAlgorithm.updatePageAccess(vpn); // Update the eviction algorithm state
            //LogResults.log("TLB hit for VPN " + vpn + ": " + entry);
            return entry;
        }
        //LogResults.log("TLB miss for VPN " + vpn);
        return null;
    }

//    /**
//     * Checks if a given VPN is present in the TLB.
//     * @param vpn The virtual page number (VPN).
//     * @return True if the VPN is present, false otherwise.
//     */
//    public boolean containsEntry(int vpn) {
//        boolean contains = entries.containsKey(vpn);
//        LogResults.log("TLB contains VPN " + vpn + ": " + contains);
//        return contains;
//    }
//    /**
//     * Checks if the page entry for the given VPN is dirty (modified).
//     * @param vpn The virtual page number (VPN).
//     * @return True if the page is dirty, false otherwise.
//     */
//    public boolean isDirty(int vpn) {
//        PageTableEntry entry = entries.get(vpn);
//        boolean dirty = entry != null && entry.isDirty();
//        //LogResults.log("VPN " + vpn + " dirty: " + dirty);
//        return dirty;
//    }
//
//    /**
//     * Checks if the page entry for the given VPN has been referenced.
//     * @param vpn The virtual page number (VPN).
//     * @return True if the page has been referenced, false otherwise.
//     */
//    public boolean isReferenced(int vpn) {
//        PageTableEntry entry = entries.get(vpn);
//        boolean referenced = entry != null && entry.isReferenced();
//        //LogResults.log("VPN " + vpn + " referenced: " + referenced);
//        return referenced;
//    }
//
//    /**
//     * Checks if the page entry for the given VPN is valid.
//     * @param vpn The virtual page number (VPN).
//     * @return True if the page is valid, false otherwise.
//     */
//    public boolean isValid(int vpn) {
//        PageTableEntry entry = entries.get(vpn);
//        boolean valid = entry != null && entry.isValid();
//        //LogResults.log("VPN " + vpn + " valid: " + valid);
//        return valid;
//    }
//
//    /**
//     * Finds the corresponding VPN for a given physical page number (PPN).
//     * @param ppn The physical page number (PPN).
//     * @return The corresponding VPN, or -1 if not found.
//     */
//    public int getCorrespondingVPN(int ppn) {
//        for (Map.Entry<Integer, PageTableEntry> entry : entries.entrySet()) {
//            if (entry.getValue().getFrameNumber() == ppn) {
//                LogResults.log("Found corresponding VPN for PPN " + ppn + ": " + entry.getKey());
//                return entry.getKey();
//            }
//        }
//        LogResults.log("No corresponding VPN found for PPN " + ppn);
//        return -1;
//    }
//
//    /**
//     * Sets the dirty bit for a given VPN.
//     * @param vpn The virtual page number (VPN).
//     * @param dirty The new value for the dirty bit (true if the page has been modified).
//     */
//    public void setDirty(int vpn, boolean dirty) {
//        PageTableEntry entry = entries.get(vpn);
//        if (entry != null) {
//            entry.setDirtyBit(dirty);
//            //LogResults.log("Set dirty bit for VPN " + vpn + ": " + dirty);
//        }
//    }
//
//    /**
//     * Sets the reference bit for a given VPN.
//     * @param vpn The virtual page number (VPN).
//     * @param referenced The new value for the reference bit (true if the page has been accessed).
//     */
//    public void setReferenced(int vpn, boolean referenced) {
//        PageTableEntry entry = entries.get(vpn);
//        if (entry != null) {
//            entry.setRefBit(referenced);
//            //LogResults.log("Set reference bit for VPN " + vpn + ": " + referenced);
//        }
//    }
//
//    /**
//     * Sets the validity of the page entry for a given VPN.
//     * @param vpn The virtual page number (VPN).
//     * @param valid The new validity status (true if the page is in memory).
//     */
//    public void setValid(int vpn, boolean valid) {
//        PageTableEntry entry = entries.get(vpn);
//        if (entry != null) {
//            entry.setValidBit(valid);
//            //LogResults.log("Set valid bit for VPN " + vpn + ": " + valid);
//        }
//        if (!valid) {
//            entries.get(vpn).setFrameNumber(-1); // Invalidate the frame number when the page is marked invalid
//        }
//    }

}
