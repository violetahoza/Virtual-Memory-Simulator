package com.example.vms.model;

/**
 * Represents an entry in a page table for virtual memory management.
 * Each entry includes information about a page's physical location,
 * validity, modification status, and reference status.
 */
public class PageTableEntry {
    private int frameNumber; // Physical page number
    private boolean validBit; // Indicates if the page is in the main memory
    private boolean dirtyBit; // Reflects the page's state (if the page from the disk was modified)
    private boolean refBit; // Set whenever a page is referenced, either for reading or for writing
    private boolean diskPage; // Indicates if the page is stored on disk
    private long accessTime; // Tracks the last access time for LRU
    private int nextAccess = Integer.MAX_VALUE; // Default to MAX_VALUE (not accessed again)

    /**
     * Default constructor that initializes an invalid page entry.
     * The frame number is set to -1 to indicate no mapping.
     */
    public PageTableEntry() {
        this.frameNumber = -1;
        this.validBit = false;
        this.dirtyBit = false;
        this.refBit = false;
        this.diskPage = false;
        this.accessTime = -1;
        this.nextAccess = Integer.MAX_VALUE;
    }

    /**
     * Parameterized constructor to initialize a page table entry with specific values.
     * @param frameNumber the physical frame number in main memory
     * @param validBit    true if the page is currently in memory, otherwise false
     * @param dirtyBit    true if the page has been modified in memory, otherwise false
     * @param refBit      true if the page has been recently referenced, otherwise false
     * @param diskPage    true if the page is stored on disk, otherwise false
     */
    public PageTableEntry(int frameNumber, boolean validBit, boolean dirtyBit, boolean refBit, boolean diskPage, long accessTime, int nextAccess) {
        this.frameNumber = frameNumber;
        this.refBit = refBit;
        this.validBit = validBit;
        this.dirtyBit = dirtyBit;
        this.diskPage = diskPage;
        this.accessTime = accessTime;
        this.nextAccess = nextAccess;
    }

    /**
     * Sets the diskPage flag indicating whether the page is stored on disk.
     * @param diskPage true if the page is stored on disk, otherwise false
     */
    public void setDiskPage(boolean diskPage) { this.diskPage = diskPage;}

    /**
     * Returns whether the page is stored on disk.
     * @return true if the page is stored on disk, otherwise false
     */
    public boolean isDiskPage() { return diskPage;}

    /**
     * Returns the physical frame number of the page.
     * @return the physical frame number
     */
    public int getFrameNumber() { return frameNumber;}

    /**
     * Returns whether the page has been modified (dirty).
     * @return true if the page is dirty (modified), otherwise false
     */
    public boolean isDirty() {
        return dirtyBit;
    }

    /**
     * Returns whether the page is currently in memory (valid).
     * @return true if the page is valid (in memory), otherwise false
     */
    public boolean isValid() {
        return validBit;
    }

    /**
     * Returns whether the page has been recently referenced.
     * @return true if the page has been referenced, otherwise false
     */
    public boolean isReferenced() {
        return refBit;
    }

    /**
     * Sets the dirty bit for the page, indicating if the page has been modified.
     * @param dirtyBit true if the page is dirty (modified), otherwise false
     */
    public void setDirtyBit(boolean dirtyBit) {
        this.dirtyBit = dirtyBit;
    }

    /**
     * Sets the physical frame number for the page.
     * @param frameNumber the new frame number to set
     */
    public void setFrameNumber(int frameNumber) {
        this.frameNumber = frameNumber;
    }

    /**
     * Sets the reference bit for the page, indicating if the page has been referenced.
     * @param refBit true if the page has been referenced, otherwise false
     */
    public void setRefBit(boolean refBit) {
        this.refBit = refBit;
    }

    /**
     * Sets the valid bit for the page, indicating if the page is in memory.
     * @param validBit true if the page is valid (in memory), otherwise false
     */
    public void setValidBit(boolean validBit) {
        this.validBit = validBit;
    }

    /**
     * Gets the last access time of the page.
     * @return The last access time as a long value.
     */
    public long getAccessTime() {
        return accessTime;
    }

    /**
     * Sets the last access time for the page.
     * @param accessTime The new access time to set.
     */
    public void setAccessTime(long accessTime) {
        this.accessTime = accessTime;
    }

    /**
     * Returns the NRU class of the page based on its referenced and dirty bits.
     * The NRU class is calculated as follows:
     * Class 0: Not Referenced, Not Dirty
     * Class 1: Not Referenced, Dirty
     * Class 2: Referenced, Not Dirty
     * Class 3: Referenced, Dirty
     *
     * @return The NRU class (0-3).
     */
    public int getNRUClass() {
        return (isReferenced() ? 2 : 0) + (isDirty() ? 1 : 0);
    }

    /**
     * Gets the next access of the page (for optimal replacement).
     * @return The next access time of a page.
     */
    public int getNextAccess() {
        return nextAccess;
    }

    /**
     * Sets the next access time for the page.
     * @param nextAccess The new access time to set.
     */
    public void setNextAccess(int nextAccess) {
        this.nextAccess = nextAccess;
    }

    /**
     * Returns a string representation of the page table entry.
     * It includes the validity, dirty status, reference status, frame number,
     * and whether the page is in storage (on disk).
     * @return a string representation of the page table entry
     */
    @Override
    public String toString() {
        return "Valid: " + validBit + " Dirty: " + dirtyBit + " Referenced: " + refBit + " Frame: " + frameNumber + " In storage: " + diskPage;
    }
}
