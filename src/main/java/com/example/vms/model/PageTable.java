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
    private Map<Integer, PageTableEntry> pageTable; // Maps VPNs to PPNs (Virtual Page Numbers to Physical Page Numbers)
    private int size; // Number of entries in the page table

    /**
     * Initializes a page table with the given size, setting up entries with default values.
     * @param size The number of entries in the page table.
     */
    public PageTable(int size) {
        this.size = size;
        pageTable = new HashMap<>();
        for (int i = 0; i < size; i++) {
            PageTableEntry entry = new PageTableEntry();
            pageTable.put(i, entry); // Initialize entries with default values
        }
        LogResults.log("Page table initialized with size: " + size); // Log the initialization
    }

    /**
     * Retrieves a page table entry for a given VPN if the entry is valid.
     * @param vpn The virtual page number (VPN).
     * @return The corresponding page table entry if valid, otherwise null.
     */
    public PageTableEntry getEntry(int vpn) {
        PageTableEntry entry = pageTable.get(vpn);
        if (entry != null && entry.isValid()) {
            //LogResults.log("Page table hit for VPN " + vpn + ": " + entry); // Log a page hit
            return entry;
        }
        //LogResults.log("Page table miss for VPN " + vpn); // Log a page miss
        return null;
    }

    /**
     * Retrieves all entries in the page table as a list for easy iteration.
     * @return A list of all page table entries.
     */
    public List<PageTableEntry> getEntries() {
        //LogResults.log("Retrieving all entries from the page table."); // Log retrieval of all entries
        return new ArrayList<>(pageTable.values());
    }

    /**
     * Retrieves the physical page number (PPN) associated with a given VPN.
     * @param vpn The virtual page number (VPN).
     * @return The physical page number (PPN), or -1 if no valid mapping exists.
     */
    public Integer getPhysicalPageNumber(int vpn) {
        PageTableEntry entry = pageTable.get(vpn);
        if (entry != null) {
            //LogResults.log("Physical page number for VPN " + vpn + ": " + entry.getFrameNumber()); // Log the PPN
            return entry.getFrameNumber();
        }
        //LogResults.log("No physical page found for VPN " + vpn); // Log if no PPN is found
        return -1;
    }

    /**
     * Checks if the page entry for the given VPN is valid.
     * @param vpn The virtual page number (VPN).
     * @return True if the page entry is valid, false otherwise.
     */
    public boolean isValid(int vpn) {
        PageTableEntry entry = pageTable.get(vpn);
        boolean valid = entry != null && entry.isValid();
        //LogResults.log("VPN " + vpn + " valid: " + valid); // Log validity check result
        return valid;
    }
    public boolean isOnDisk(int vpn) {
        PageTableEntry entry = pageTable.get(vpn);
        if (entry != null) {
            return entry.isDiskPage();
        }
        return false;
    }
    public void setDiskPage(int vpn, boolean isOnDisk) {
        PageTableEntry entry = pageTable.get(vpn);
        if (entry != null) {
            entry.setDiskPage(isOnDisk);
        }
    }
    /**
     * Checks if the page entry for the given VPN has been referenced.
     * @param vpn The virtual page number (VPN).
     * @return True if the page entry has been referenced, false otherwise.
     */
    public boolean isReferenced(int vpn) {
        PageTableEntry entry = pageTable.get(vpn);
        boolean referenced = entry != null && entry.isReferenced();
        //LogResults.log("VPN " + vpn + " referenced: " + referenced); // Log reference check result
        return referenced;
    }

    /**
     * Checks if the page entry for the given VPN is dirty (modified).
     * @param vpn The virtual page number (VPN).
     * @return True if the page entry is dirty, false otherwise.
     */
    public boolean isDirty(int vpn) {
        PageTableEntry entry = pageTable.get(vpn);
        boolean dirty = entry != null && entry.isDirty();
        //LogResults.log("VPN " + vpn + " dirty: " + dirty); // Log dirty check result
        return dirty;
    }

    /**
     * Sets the dirty bit for the page entry of a given VPN.
     * @param vpn The virtual page number (VPN).
     * @param dirty The new value of the dirty bit (true if the page has been modified).
     */
    public void setDirty(int vpn, boolean dirty) {
        PageTableEntry entry = pageTable.get(vpn);
        if (entry != null) {
            entry.setDirtyBit(dirty);
            //LogResults.log("Set dirty bit for VPN " + vpn + ": " + dirty); // Log dirty bit setting
        }
    }

    /**
     * Sets the reference bit for the page entry of a given VPN.
     * @param vpn The virtual page number (VPN).
     * @param referenced The new value of the reference bit (true if the page has been accessed).
     */
    public void setReferenced(int vpn, boolean referenced) {
        PageTableEntry entry = pageTable.get(vpn);
        if (entry != null) {
            entry.setRefBit(referenced);
            //LogResults.log("Set reference bit for VPN " + vpn + ": " + referenced); // Log reference bit setting
        }
    }

    /**
     * Sets the validity of the page entry for a given VPN.
     * @param vpn The virtual page number (VPN).
     * @param valid The new validity status (true if the page is in memory).
     */
    public void setValid(int vpn, boolean valid) {
        PageTableEntry entry = pageTable.get(vpn);
        if (entry != null) {
            entry.setValidBit(valid);
            //LogResults.log("Set valid bit for VPN " + vpn + ": " + valid); // Log valid bit setting
            if (!valid) {
                entry.setFrameNumber(-1);
                entry.setRefBit(false);
                entry.setDirtyBit(false);
                //LogResults.log("Invalidated VPN " + vpn + " and reset associated bits."); // Log invalidation and reset
            }
        }
    }

    /**
     * Finds the VPN that corresponds to a given physical page number (PPN).
     * @param ppn The physical page number (PPN).
     * @return The VPN that maps to the given PPN, or -1 if no such VPN exists.
     */
    public int getCorrespondingVPN(int ppn) {
        for (Map.Entry<Integer, PageTableEntry> entry : pageTable.entrySet()) {
            if (entry.getValue().getFrameNumber() == ppn) {
                //LogResults.log("Found corresponding VPN for PPN " + ppn + ": " + entry.getKey()); // Log found VPN
                return entry.getKey();
            }
        }
        //LogResults.log("No corresponding VPN found for PPN " + ppn); // Log if no corresponding VPN is found
        return -1;
    }

    /**
     * Adds an entry for a given VPN and PPN, setting the appropriate flags (valid, referenced, and dirty).
     * @param vpn The virtual page number (VPN).
     * @param ppn The physical page number (PPN).
     */
    public void addEntry(int vpn, int ppn) {
        PageTableEntry entry = pageTable.getOrDefault(vpn, new PageTableEntry());
        entry.setFrameNumber(ppn);
        entry.setValidBit(true);
        entry.setRefBit(true);
        entry.setDirtyBit(false);
        entry.setDiskPage(false);
        pageTable.put(vpn, entry);
        //LogResults.log("Added entry for VPN " + vpn + " with PPN " + ppn); // Log entry addition
    }

    public void addEntryOnDisk(int vpn, PageTableEntry entry) {
        pageTable.put(vpn, entry);
    }

    /**
     * Checks if a given physical page number (PPN) is already in use in the page table.
     * @param ppn The physical page number (PPN).
     * @return True if the PPN is already in use, false otherwise.
     */
    public boolean contains(int ppn) {
        for (PageTableEntry entry : pageTable.values()) {
            if (entry.getFrameNumber() == ppn) {
                //LogResults.log("Page table contains PPN " + ppn); // Log presence of PPN
                return true;
            }
        }
        //LogResults.log("Page table does not contain PPN " + ppn); // Log absence of PPN
        return false;
    }

    /**
     * Removes the entry for a given VPN from the page table and resets all associated flags.
     * @param vpn The virtual page number (VPN).
     */
    public void removeEntry(int vpn) {
        PageTableEntry entry = pageTable.get(vpn);
        if (entry != null) {
            entry.setFrameNumber(-1);
            entry.setRefBit(false);
            entry.setDirtyBit(false);
            entry.setValidBit(false);
            pageTable.remove(vpn);
            //LogResults.log("Removed entry for VPN " + vpn); // Log entry removal
        }
    }

    /**
     * Returns a copy of the page table's contents, preserving the current state of each entry.
     * @return A map representing a copy of the page table.
     */
    public Map<Integer, PageTableEntry> getPageTableContents() {
        Map<Integer, PageTableEntry> pageTableCopy = new HashMap<>();
        for (Map.Entry<Integer, PageTableEntry> e: pageTable.entrySet()) {
            PageTableEntry newEntry = new PageTableEntry(
                    e.getValue().getFrameNumber(),
                    e.getValue().isValid(),
                    e.getValue().isDirty(),
                    e.getValue().isReferenced(),
                    e.getValue().isDiskPage()
            );
            pageTableCopy.put(e.getKey(), newEntry);
        }
        //LogResults.log("Page table copied."); // Log copying action
        return pageTableCopy;
    }
    public void setPPN(int vpn, int ppn) {
        PageTableEntry entry = pageTable.get(vpn);
        entry.setFrameNumber(ppn);
    }
    /**
     * Prints the current contents of the page table to the log.
     */
    public void printContents() {
        StringBuilder logBuilder = new StringBuilder("Page table contents:\n----------------------\n");
        for (Map.Entry<Integer, PageTableEntry> e : pageTable.entrySet()) {
            logBuilder.append(e.getKey()).append(": ").append(e.getValue()).append("\n");
        }
        logBuilder.append("----------------------");
        LogResults.log(logBuilder.toString()); // Log the page table contents
    }
}
