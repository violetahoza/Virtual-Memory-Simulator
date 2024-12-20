package com.example.vms.model;

import com.example.vms.utils.LogResults;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

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
            // Get the set of VPNs currently in the TLB
            Set<Integer> currentTLBEntries = entries.keySet();
            if(evictionAlgorithm instanceof LRUReplacement) {
                // Ask LRUReplacement for the least recently used page in the TLB
                int lruVpn = ((LRUReplacement) evictionAlgorithm).getTLBLRUPage(currentTLBEntries);
                if (lruVpn != -1 && entries.containsKey(lruVpn)) {
                    entries.remove(lruVpn); // Remove the entry from the TLB
                    LogResults.log("Evicted VPN " + lruVpn + " from TLB based on TLB-specific LRU algorithm");
                } else {
                    LogResults.log("No valid entry found for eviction in TLB. Evicting the oldest TLB entry.");
                    entries.remove(getFirstEntryVPN());
                }
            }
            else if (evictionAlgorithm instanceof NRUReplacement) {
                // Ask NRUReplacement for the least desirable page in the TLB
                int nruVpn = ((NRUReplacement) evictionAlgorithm).getTLBNRUPage(currentTLBEntries);
                if (nruVpn != -1 && entries.containsKey(nruVpn)) {
                    entries.remove(nruVpn); // Remove the entry from the TLB
                    LogResults.log("Evicted VPN " + nruVpn + " from TLB based on NRU algorithm.");
                } else {
                    LogResults.log("No valid entry found for eviction in TLB. Evicting the oldest TLB entry.");
                    entries.remove(getFirstEntryVPN());                }
            }
            else if (evictionAlgorithm instanceof FIFOReplacement) {
                // For FIFO, evict the first entry directly without using the eviction algorithm
                int fifoVpn = getFirstEntryVPN();
                if (fifoVpn != -1) {
                    entries.remove(fifoVpn); // Remove the entry from the TLB
                    LogResults.log("Evicted VPN " + fifoVpn + " from TLB based on FIFO algorithm.");
                } else {
                    LogResults.log("No valid entry found for FIFO eviction in TLB.");
                }
            }
            else if(evictionAlgorithm instanceof OptimalReplacement) {
                int optimalVpn = ((OptimalReplacement) evictionAlgorithm).getTLBOptimalPage(currentTLBEntries);
                if (optimalVpn != -1 && entries.containsKey(optimalVpn)) {
                    entries.remove(optimalVpn); // Remove the entry from the TLB
                    // LogResults.log("Evicted VPN " + optimalVpn + " from TLB based on Optimal Replacement.");
                } else {
                    LogResults.log("No valid entry found for eviction in TLB. Evicting the oldest TLB entry.");
                    entries.remove(getFirstEntryVPN());                }
            }
        }
        // Add the new entry
        entries.put(vpn, new PageTableEntry(entry.getFrameNumber(), entry.isValid(), entry.isDirty(), entry.isReferenced(), entry.isDiskPage(), entry.getAccessTime(), entry.getNextAccess()));
        // evictionAlgorithm.updatePageAccess(vpn); // Add the page to the eviction algorithm
        LogResults.log("Added VPN " + vpn + " to TLB");
    }

    /**
     * Retrieves and removes the first VPN in the LinkedHashMap (FIFO order).
     * @return The VPN of the first entry, or null if the TLB is empty.
     */
    private int getFirstEntryVPN() {
        if (entries.isEmpty()) {
            LogResults.log("TLB is empty, no entry to evict.");
            return -1;
        }
        // LinkedHashMap maintains insertion order, so the first key is the oldest
        return entries.keySet().iterator().next();
    }

    /**
     * Looks up the frame number for a given VPN in the TLB.
     * @param vpn The virtual page number (VPN).
     * @return The frame number if found, or -1 if there is a TLB miss.
     */
    public int lookup(int vpn) {
        PageTableEntry entry = entries.get(vpn);
        if (entry != null && entry.isValid()) {
            //evictionAlgorithm.updatePageAccess(vpn); // Update LRU for the accessed VPN
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
            PageTableEntry newEntry = new PageTableEntry(e.getValue().getFrameNumber(), e.getValue().isValid(), e.getValue().isDirty(), e.getValue().isReferenced(), e.getValue().isDiskPage(), e.getValue().getAccessTime(), e.getValue().getNextAccess());
            tlbCopy.put(e.getKey(), newEntry);
        }
        //LogResults.log("Copied TLB contents");
        return tlbCopy;
    }

    public Map<Integer, PageTableEntry> getEntries() {
        return new LinkedHashMap<>(entries); // Return a copy of the entries
    }

     /**
     * Retrieves the page table entry for a given VPN if it exists and is valid.
     * @param vpn The virtual page number (VPN).
     * @return The corresponding page table entry, or null if not found or invalid.
     */
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

    /**
     * Checks if a given VPN is present in the TLB.
     * @param vpn The virtual page number (VPN).
     * @return True if the VPN is present, false otherwise.
     */
    public boolean containsEntry(int vpn) {
        boolean contains = entries.containsKey(vpn);
        // LogResults.log("TLB contains VPN " + vpn + ": " + contains);
        return contains;
    }
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
