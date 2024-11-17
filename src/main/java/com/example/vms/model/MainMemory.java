package com.example.vms.model;

import com.example.vms.utils.LogResults;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the main memory in a virtual memory system.
 * It simulates the loading, storing, and management of pages in memory,
 * providing functionality to manipulate pages and retrieve memory contents.
 */
public class MainMemory {

    private Map<Integer, Page> memory; // Stores pages mapped to frame numbers
    private int nrFrames;              // Total number of frames in memory
    private int lastFrameNr;           // Tracks the next available frame
    private int pageSize;              // Size of each page
    private Map<Integer, Integer> frameToVirtualPageMap; // Maps frame numbers to virtual page numbers

    /**
     * Constructs a new instance of MainMemory.
     * Initializes memory with the specified number of frames and page size.
     * @param nrFrames The total number of frames available in memory.
     * @param pageSize The size of each page in memory.
     */
    public MainMemory(int nrFrames, int pageSize) {
        this.nrFrames = nrFrames;
        this.pageSize = pageSize;
        this.frameToVirtualPageMap = new HashMap<>(); // Initialize frame-to-virtual page map
        this.memory = new HashMap<>(); // Start with an empty memory (no pages loaded)
        for (int i = 0; i < nrFrames; i++) {
            memory.put(i, new Page(pageSize)); // initialize memory
        }
        this.lastFrameNr = 0;
        LogResults.log("Main memory initialized with " + nrFrames + " frames, each of size " + pageSize + " bytes.");
    }

    /**
     * Loads data from memory for a specific address.
     * @param address The address from which data needs to be loaded.
     * @return The data at the specified address, or -1 if the address is invalid.
     */
    public int load(Address address) {
        //LogResults.log("Attempting to load data from physical address: " + address);
        Page page = memory.get(address.getPageNumber());
        if (page == null) {
            LogResults.log("Invalid physical address: page not found.");
            return -1;
        }
        int data = page.load(address.getOffset());
        LogResults.log("Data loaded from physical address " + address + ": " + data);
        return data;
    }

    /**
     * Stores data in memory at a specific address.
     * @param address The address at which data needs to be stored.
     * @param data The data to be stored at the specified address.
     */
    public void store(Address address, int data) {
        //LogResults.log("Attempting to store data at physical address: " + address + " with data: " + data);
        if (address.getOffset() >= pageSize) {
            LogResults.log("Error: Offset exceeds page size. Address not valid.");
            return;
        }
        Page page = memory.get(address.getPageNumber());
        if (page != null) {
            page.store(address.getOffset(), data);
            LogResults.log("Data successfully stored at physical address: " + address);
        } else {
            LogResults.log("Invalid physical address: page not found.");
        }
    }

    /**
     * Loads a page into memory at a specific frame number.
     * @param page The page to be loaded into memory.
     * @param frameNr The frame number in which the page will be stored.
     */
    public void loadPageIntoMemory(Page page, int frameNr, int vpn) {
        //LogResults.log("Loading page into frame number: " + frameNr);
        if (frameNr < 0 || frameNr >= nrFrames) {
            LogResults.log("Invalid frame number specified for loading page: " + frameNr);
            return;
        }
        memory.put(frameNr, page.getCopy());
        frameToVirtualPageMap.put(frameNr, vpn);
        lastFrameNr = Math.max(lastFrameNr, frameNr + 1); // Update last used frame if necessary
        LogResults.log("Page successfully loaded into frame " + frameNr);
    }

    /**
     * Retrieves the next available frame in memory.
     * @return The frame number of the next available frame, or -1 if no frames are available.
     */
    public int getNextAvailableFrame() {
        //LogResults.log("Searching for the next available frame in memory.");
        for (int i = 0; i < nrFrames; i++) {
            if (!memory.containsKey(i)) {
                LogResults.log("Next available frame found: " + i);
                return i;
            }
        }
        LogResults.log("No available frames found.");
        return -1; // Should not reach here if memory is not full
    }

    /**
     * Removes a page from memory at a specific frame number.
     * @param frameNumber The frame number from which the page will be removed.
     */
    public void removePage(int frameNumber) {
        // LogResults.log("Removing page from frame number: " + frameNumber);
        memory.remove(frameNumber);
        LogResults.log("Page removed from frame number " + frameNumber);
    }

    /**
     * Checks if memory is full.
     * @return true if memory is full, false otherwise.
     */
    public boolean isFull() {
        boolean full = memory.size() >= nrFrames;
        if (full) {
            LogResults.log("Memory is full.");
        } else {
            LogResults.log("Memory is not full. Current size: " + memory.size() + " / " + nrFrames);
        }
        return full;
    }

    /**
     * Retrieves the page at a specific frame number.
     * @param frameNr The frame number where the page is stored.
     * @return The page stored at the specified frame number.
     */
    public Page getPage(int frameNr) {
        LogResults.log("Retrieving page at frame number: " + frameNr);
        return memory.get(frameNr);
    }
    /**
     * Retrieves all pages from memory.
     * @return A map of all pages in memory, with frame numbers as keys.
     */
    public Map<Integer, Page> getMemory() {
        //LogResults.log("Retrieving all pages from memory.");
        return memory;
    }
    /**
     * Prints all memory contents.
     * Logs the current memory status, including each frame and its contents.
     */
    public void printContents() {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("Main memory contents (size: ").append(memory.size()).append("):\n");

        for (Map.Entry<Integer, Page> entry : memory.entrySet()) {
            int frameNumber = entry.getKey();
            int virtualPageNumber = frameToVirtualPageMap.getOrDefault(frameNumber, -1); // Retrieve virtual page number
            logBuilder.append("Frame ").append(frameNumber).append(" (Virtual Page ").append(virtualPageNumber).append("):\n");
            logBuilder.append(entry.getValue().printContents()).append("\n");
        }
        logBuilder.append("----------------------");
        LogResults.log(logBuilder.toString());
    }

//    /**
//     * Creates a copy of a given page.
//     * @param page The page to be copied.
//     * @return A copy of the given page.
//     */
//    public Page pageCopy(Page page) {
//        LogResults.log("Creating a copy of the page.");
//        return page.getCopy();
//    }

//    /**
//     * Retrieves the full memory contents, mapping frame numbers to virtual addresses and their associated data.
//     * @return A map of memory contents, with frame numbers as keys and addresses as the values.
//     */
//    public Map<Integer, Map<Integer, Integer>> getMemoryContents() {
//        //LogResults.log("Retrieving full memory contents.");
//        Map<Integer, Map<Integer, Integer>> memoryCopy = new HashMap<>();
//        for (Map.Entry<Integer, Page> entry : memory.entrySet()) {
//            int frameNumber = entry.getKey();
//            Page page = entry.getValue();
//            Map<Integer, Integer> pageContents = page.getPageContents();
//            Map<Integer, Integer> addressContents = new HashMap<>();
//
//            // Convert page contents to physical addresses
//            for (Map.Entry<Integer, Integer> pageEntry : pageContents.entrySet()) {
//                int offset = pageEntry.getKey();
//                int address = frameNumber * pageSize + offset;
//                addressContents.put(address, pageEntry.getValue());
//            }
//            memoryCopy.put(frameNumber, addressContents);
//        }
//        return memoryCopy;
//    }
}
