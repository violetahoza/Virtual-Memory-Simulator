package com.example.vms.model;

/**
 * Represents an entry in a page table for virtual memory management.
 * Each entry includes information about a page's physical location,
 * validity, modification status, and reference status.
 */
public class PageTableEntry {
    private int frameNumber; //physical page number
    private boolean validBit; // indicates if the page is in the main memory
    private boolean dirtyBit; // reflects the page's state (if the page from the disk was modified)
    private boolean refBit; // set whenever a page is referenced, either for reading or for writing

    /**
     * Default constructor that initializes an invalid page entry.
     * Frame number is set to -1 to indicate no mapping.
     */
    public PageTableEntry() {
        this.frameNumber = -1;
        this.validBit = false;
        this.dirtyBit = false;
        this.refBit = false;
    }

    /**
     * Parameterized constructor to initialize a page table entry.
     *
     * @param frameNumber the physical frame number in main memory
     * @param validBit    true if the page is currently in memory, otherwise false
     * @param dirtyBit    true if the page has been modified in memory, otherwise false
     * @param refBit      true if the page has been recently referenced, otherwise false
     */
    public PageTableEntry(int frameNumber, boolean validBit, boolean dirtyBit, boolean refBit){
        this.frameNumber = frameNumber;
        this.refBit = refBit;
        this.validBit = validBit;
        this.dirtyBit = dirtyBit;
    }

    public int getFrameNumber() {return frameNumber;}

    public boolean isDirty() {
        return dirtyBit;
    }

    public boolean isValid() {
        return validBit;
    }

    public boolean isReferenced() {
        return refBit;
    }

    public void setDirtyBit(boolean dirtyBit) {
        this.dirtyBit = dirtyBit;
    }

    public void setFrameNumber(int frameNumber) {
        this.frameNumber = frameNumber;
    }

    public void setRefBit(boolean refBit) {
        this.refBit = refBit;
    }

    public void setValidBit(boolean validBit) {
        this.validBit = validBit;
    }

    @Override
    public String toString(){
        return "Valid: " + validBit + " Dirty: " + dirtyBit + " Referenced: " + refBit + " Frame: " + frameNumber;
    }
}
