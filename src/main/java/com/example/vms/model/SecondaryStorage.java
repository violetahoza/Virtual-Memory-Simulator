package com.example.vms.model;

import com.example.vms.utils.LogResults;

import java.util.HashMap;
import java.util.Map;

/**
 * This class simulates secondary storage for pages in a virtual memory system.
 * It provides functionality for storing and loading pages, checking if a page exists,
 * and retrieving the contents of secondary storage with virtual addresses.
 */
public class SecondaryStorage {

    private Map<Integer, Page> disk; // Simulate disk storage for pages (vpn -> page)
    private int pageSize; // Size of each page in memory (used for address calculations)

    /**
     * Constructs a new instance of SecondaryStorage.
     * @param maxPages The maximum number of pages the secondary storage can hold.
     * @param pageSize The size of each page in the system.
     */
    public SecondaryStorage(int maxPages, int pageSize) {
        disk = new HashMap<>();
        this.pageSize = pageSize;
        LogResults.log("Secondary storage initialized with max pages: " + maxPages + " and page size: " + pageSize);
    }

    /**
     * Stores a page in secondary storage using a virtual page number (VPN).
     * @param vpn The virtual page number of the page to be stored.
     * @param page The page to be stored.
     */
    public void store(int vpn, Page page) {
        disk.put(vpn, page); // Store the page at the given virtual page number (VPN)
        LogResults.log("Page with VPN " + vpn + " stored successfully into secondary storage.");
    }

    /**
     * Loads a page from secondary storage using its virtual page number (VPN).
     * @param vpn The virtual page number of the page to be loaded.
     * @return The loaded page, or null if the page is not found.
     */
    public Page load(int vpn) {
        // Check if the page exists in secondary storage
        if (!disk.containsKey(vpn)) {
            LogResults.log("Error: Invalid VPN " + vpn + ". Page not found in secondary storage.");
            return null; // If page doesn't exist, return null
        }
        // If the page exists, load it
        LogResults.log("Page with VPN " + vpn + " loaded successfully from secondary storage.");
        return disk.get(vpn); // Return the page associated with the VPN
    }

    /**
     * Prints the contents of the secondary storage to the log.
     * This includes details about each page and its contents.
     */
    public void printContents() {
        //LogResults.log("Printing contents of secondary storage...");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("Disk contents:\n----------------------\n");

        // Iterate through each page in secondary storage and append its contents to the log
        for (Map.Entry<Integer, Page> entry : disk.entrySet()) {
            logBuilder.append("Page ").append(entry.getKey()).append(":\n");
            logBuilder.append(entry.getValue().printContents()); // Print contents of the individual page
        }

        logBuilder.append("----------------------");
        LogResults.log(logBuilder.toString()); // Log the final content of secondary storage
    }

    public Map<Integer, Page> getDisk() {
        return new HashMap<>(disk);
    }

//    public void removePage(int vpn){
//        disk.remove(vpn);
//        LogResults.log("Page with VPN " + vpn + " removed from secondary storage.");
//    }
//    /**
//     * Checks if a page exists in secondary storage based on its virtual page number (VPN).
//     * @param vpn The virtual page number to check.
//     * @return true if the page exists in secondary storage, false otherwise.
//     */
//    public boolean containsPage(int vpn) {
//        LogResults.log("Checking if page with VPN " + vpn + " exists in secondary storage.");
//
//        // Check if the disk contains a page with the given VPN
//        boolean contains = disk.containsKey(vpn);
//
//        // Log if the page exists or not
//        if (contains) {
//            LogResults.log("Page with VPN " + vpn + " exists in secondary storage.");
//        } else {
//            LogResults.log("Page with VPN " + vpn + " does not exist in secondary storage.");
//        }
//        return contains; // Return true if the page exists, false otherwise
//    }
//    /**
//     * Retrieves the contents of secondary storage, including the virtual addresses and their associated offsets.
//     * @return A map representing the virtual addresses and the data stored at those addresses in secondary storage.
//     */
//    public Map<Integer, Map<Integer, Integer>> getDiskContents() {
//        //LogResults.log("Retrieving disk contents...");
//
//        // Create a map to store disk contents with virtual addresses
//        Map<Integer, Map<Integer, Integer>> diskCopy = new HashMap<>();
//
//        // Iterate over each entry (VPN and associated page) in the disk map
//        for (Map.Entry<Integer, Page> entry : disk.entrySet()) {
//            Map<Integer, Integer> pageCopy = entry.getValue().getPageContents(); // Get the contents of the page
//            int virtualPageNumber = entry.getKey(); // Get the virtual page number
//            Map<Integer, Integer> pageCopyWithOffsets = new HashMap<>(); // Prepare a map for virtual addresses
//
//            // Convert page contents to virtual addresses by calculating the address = VPN * pageSize + offset
//            for (Map.Entry<Integer, Integer> pageEntry : pageCopy.entrySet()) {
//                int offset = pageEntry.getKey();
//                int address = virtualPageNumber * pageSize + offset; // Calculate the virtual address
//                pageCopyWithOffsets.put(address, pageEntry.getValue()); // Store the address with the value
//            }
//
//            diskCopy.put(virtualPageNumber, pageCopyWithOffsets); // Add the page copy with virtual addresses to diskCopy
//        }
//
//        LogResults.log("Disk contents retrieved successfully.");
//        return diskCopy; // Return the map with virtual addresses
//    }
}
