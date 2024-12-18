package com.example.vms.model;

import com.example.vms.utils.LogResults;

import java.util.*;

/**
 * Manages virtual memory by simulating the interaction between TLB (Translation Lookaside Buffer),
 * page table, main memory, secondary storage, and the page replacement algorithm. It handles page
 * allocation, page faults, memory loads, stores, and evictions based on the replacement algorithm.
 */
public class MemoryManager {
    private TLB tlb;
    private PageTable pageTable;
    private MainMemory mainMemory;
    private SecondaryStorage secondaryStorage;
    private ReplacementAlgorithm replacementAlgorithm;
    private int pageSize, virtualAddressWidth, virtualMemorySize;
    private int operationCount; // Counter for memory operations
    private static final int RESET_INTERVAL = 8; // Reset every 8 operations

    /**
     * Constructs the MemoryManager with the provided memory configurations.
     * @param virtualAddressWidth The width of the virtual address space.
     * @param tlbSize The size of the TLB.
     * @param pageSize The size of each page.
     * @param physicalMemorySize The size of the physical memory.
     * @param diskSize The size of the secondary storage.
     * @param replacementAlgorithm The page replacement algorithm to use.
     */
    public MemoryManager(int virtualAddressWidth, int tlbSize, int pageSize, int physicalMemorySize, int diskSize, ReplacementAlgorithm replacementAlgorithm) {
        if (pageSize <= 0 || virtualAddressWidth <= 0 || physicalMemorySize <= 0 || tlbSize < 0) {
            LogResults.log("\nMemory configuration parameters must be positive.\n");
            return;
        }

        this.pageSize = pageSize;
        this.virtualAddressWidth = virtualAddressWidth;
        this.virtualMemorySize = (int) Math.pow(2, virtualAddressWidth);
        this.pageTable = new PageTable(virtualMemorySize / pageSize);
        if(replacementAlgorithm instanceof LRUReplacement)
            replacementAlgorithm = new LRUReplacement(pageTable);
        if(replacementAlgorithm instanceof NRUReplacement)
            replacementAlgorithm = new NRUReplacement(pageTable);
//        if(replacementAlgorithm instanceof OptimalReplacement)
//        {
//            List<Integer> futureAccesses = Arrays.asList(1, 4, 0, 1, 2, 3, 5, 6);
//            ((OptimalReplacement) replacementAlgorithm).setFutureAccesses(futureAccesses);
//        }
        this.replacementAlgorithm = replacementAlgorithm;
        this.tlb = new TLB(tlbSize, replacementAlgorithm);
        this.mainMemory = new MainMemory(physicalMemorySize / pageSize, pageSize);
        this.secondaryStorage = new SecondaryStorage(diskSize / pageSize, pageSize);
        this.operationCount = 0;
        //LogResults.log("MemoryManager initialized with given configuration.");
    }

    /**
     * Default constructor for the MemoryManager. Initializes with empty configurations.
     */
    public MemoryManager() {
        this.pageTable = new PageTable(0);
        this.mainMemory = new MainMemory(0, 0);
        this.secondaryStorage = new SecondaryStorage(0, 0);
        this.replacementAlgorithm = new FIFOReplacement(0);
        this.tlb = new TLB(0, replacementAlgorithm);
        //LogResults.log("MemoryManager initialized with default configuration.");
    }

    /**
     * Allocates a page for the specified virtual page number (VPN). This process maps the VPN
     * to a physical frame in memory and stores it in secondary storage if needed.
     * @param vpn The virtual page number to be allocated.
     */
    public void allocatePage(int vpn) {
        if (vpn < 0 || vpn >= pageTable.getEntries().size()) { // validate VPN range
            LogResults.log("Invalid VPN: " + vpn);
            return;
        }
        if (pageTable.isValid(vpn)) { // check if the VPN is already allocated
            LogResults.log("VPN " + vpn + " is already allocated.");
            return;
        }
        int freeFrame = mainMemory.getNextAvailableFrame(); // check for a free frame in main memory
        if (freeFrame != -1) { // free frame found
            LogResults.log("Allocating VPN " + vpn + " to free frame " + freeFrame);
            Page newPage = new Page(pageSize);
            mainMemory.loadPageIntoMemory(newPage, freeFrame, vpn); // load a new page into the free frame
            replacementAlgorithm.addPage(vpn);
            pageTable.addEntry(vpn, freeFrame);
            //pageTable.setDiskPage(vpn, false); // mark as no longer on disk
            //secondaryStorage.store(vpn, newPage); // backup to secondary storage
            LogResults.log("Mapped VPN " + vpn + " to frame " + freeFrame + " in main memory.");
            //LogResults.log("Backup of VPN " + vpn + " stored in secondary storage.");
        } else {
            LogResults.log("No free frames in main memory for VPN " + vpn + ". Storing directly to secondary storage.");
            Page diskPage = new Page(pageSize);
            secondaryStorage.store(vpn, diskPage);
            // Update page table for disk-only mapping
            PageTableEntry diskEntry = new PageTableEntry(-1, false, false, false, true, -1, Integer.MAX_VALUE);
            pageTable.addEntryOnDisk(vpn, diskEntry);
        }
    }

//    public void allocatePage(int vpn) {
//        if (vpn < 0 || vpn >= pageTable.getEntries().size()) { // Check if the VPN is within a valid range
//            LogResults.log("Invalid page number " + vpn);
//            return;
//        }
//        if (pageTable.isValid(vpn)) {  // Check if the VPN is already allocated
//            LogResults.log("VPN " + vpn + " is already allocated.");
//            return;
//        }
//
//        int unmappedFrame = -1; // Find the first unmapped frame in the main memory
//        for (int i = 0; i < mainMemory.getMemory().size(); i++) {
//            if (!pageTable.contains(i)) {
//                unmappedFrame = i;
//                break;
//            }
//        }
//
//        if (unmappedFrame == -1) { // no unmapped frame is found
//            LogResults.log("\nNo unmapped frames available in main memory.");
//            // Store the page directly in secondary storage
//            Page page = new Page(pageSize); // Create a new page for this virtual page
//            secondaryStorage.store(vpn, page); // Store it directly in secondary storage
//            LogResults.log("No available frames in main memory. Stored VPN " + vpn + " directly to disk\n");
//
//            // Update page table to indicate that this page is mapped to disk
//            PageTableEntry diskEntry = new PageTableEntry(-1, false, false, false, true, -1, Integer.MAX_VALUE); // Frame number -1 to indicate disk
//            //pageTable.getEntries().add(vpn, diskEntry); // Directly use the internal map to update the entry
//            pageTable.addEntryOnDisk(vpn, diskEntry);
//            pageTable.setDiskPage(vpn, true);
//        }
//
//        Page page = new Page(pageSize); // Create a new page and bring it into the unmapped frame
//        mainMemory.loadPageIntoMemory(page, unmappedFrame, vpn); // Load the page into the unmapped frame
//        replacementAlgorithm.addPage(vpn);
//        secondaryStorage.store(vpn, page); // Store a backup copy of the page in secondary storage
//        pageTable.setDiskPage(unmappedFrame, true);
//        LogResults.log("\nStored a copy of VPN " + vpn + " in secondary storage.");
//        pageTable.addEntry(vpn, unmappedFrame); // Update the page table to map the VPN to the unmapped frame
//        LogResults.log("Mapped VPN " + vpn + " to frame " + unmappedFrame + " in main memory.\n");
//    }

    /**
     * Loads data from a virtual address by performing TLB and page table lookups,
     * handling page faults if necessary.
     * @param virtualAddress The virtual address to be accessed.
     */
    public void load(int virtualAddress) {
        if (virtualAddress < 0 || virtualAddress >= virtualMemorySize) {
            LogResults.log("Invalid virtual address " + virtualAddress);
            //throw new IllegalArgumentException("Invalid virtual address " + virtualAddress);
            return;
        }
        int vpn = virtualAddress / pageSize;
        int offset = virtualAddress % pageSize;
        Address virtualAddr = new Address(vpn, offset);
        LogResults.log("\nAccess request for virtual address: " + virtualAddress + " (" + virtualAddr.printAddress("Virtual") +  ")");
        int ppn = tlb.lookup(vpn); // TLB lookup
        Results.tlbAccesses++;
        if (ppn != -1) {
            LogResults.log("TLB hit! Physical page number: " + ppn);
            Results.tlbHit++;
            loadFromMemory(new Address(ppn, offset));
            return;
        }
        LogResults.log("TLB miss for virtual page nr: " + vpn);
        Results.tlbMiss++;
        handlePageTableLookup(vpn, offset);
    }

    /**
     * Handles page table lookup for a given VPN and offset. If the page is found, it loads it.
     * Otherwise, it handles a page fault.
     * @param vpn The virtual page number.
     * @param offset The offset within the page.
     */
    private void handlePageTableLookup(int vpn, int offset) {
        PageTableEntry entry = pageTable.getEntry(vpn);
        Results.pageTableAccesses++;
        if (entry != null) {
            LogResults.log("Page table hit! Physical page number: " + entry.getFrameNumber() + " for virtual page number: " + vpn + '\n');
            Results.pageTableHit++;
            tlb.addEntry(vpn, entry);
            //replacementAlgorithm.addPage(vpn);
            loadFromMemory(new Address(entry.getFrameNumber(), offset));
            return;
        }
        LogResults.log("Page table miss for virtual page number: " + vpn);
        Results.pageTableMiss++;
        if(handlePageFault(vpn) == -1)
            return;
        else loadFromMemory(new Address(pageTable.getPhysicalPageNumber(vpn), offset));
    }

    /**
     * Stores data at a given virtual address. This function checks for TLB and page table hits.
     * If the page is not present, it handles a page fault and updates the page table.
     * @param virtualAddress The virtual address where data will be stored.
     * @param data The data to be stored.
     */
    public void store(int virtualAddress, int data) {
        if (virtualAddress < 0 || virtualAddress >= virtualMemorySize) {
            LogResults.log("Virtual address out of bounds: " + virtualAddress);
            return;
        }
        int vpn = virtualAddress / pageSize;
        int offset = virtualAddress % pageSize;
        Address virtualAddr = new Address(vpn, offset);
        LogResults.log("\nStore request for virtual address: " + virtualAddress + " (" + virtualAddr.printAddress("Virtual") + ")");
        int ppn = tlb.lookup(vpn); // TLB lookup
        Results.tlbAccesses++;
        if (ppn != -1) {
            LogResults.log("TLB hit! Physical page number: " + ppn);
            Results.tlbHit++;
            storeToMemory(new Address(ppn, offset), data);
            return;
        }
        LogResults.log("TLB miss for virtual page number: " + vpn);
        Results.tlbMiss++;
        handlePageTableLookupForStore(vpn, offset, data);
    }

    /**
     * Handles the page table lookup for store requests. If the page is found in the page table,
     * the data is stored in memory. Otherwise, a page fault is handled.
     * @param vpn The virtual page number.
     * @param offset The offset within the page.
     * @param data The data to be stored.
     */
    private void handlePageTableLookupForStore(int vpn, int offset, int data) {
        Results.pageTableAccesses++;
        PageTableEntry entry = pageTable.getEntry(vpn);
        if (entry != null) {
            LogResults.log("Page table hit! Physical page number: " + entry.getFrameNumber() + " for virtual page number: " + vpn);
            Results.pageTableHit++;
            tlb.addEntry(vpn, entry);
            //replacementAlgorithm.updatePageAccess(vpn); // Update LRU on page table hit
            storeToMemory(new Address(entry.getFrameNumber(), offset), data);
            return;
        }
        LogResults.log("Page table miss for virtual page number: " + vpn);
        Results.pageTableMiss++;
        if (handlePageFault(vpn) == -1)
            return;
        else storeToMemory(new Address(pageTable.getPhysicalPageNumber(vpn), offset), data);
    }

    /**
     * Handles page faults. If a page fault occurs, the page is loaded from secondary storage
     * and placed into the main memory, and the page table and TLB are updated.
     * @param vpn The virtual page number that caused the page fault.
     * @return The frame number where the page was loaded, or -1 if an error occurred.
     */
    private int handlePageFault(int vpn) {
        // LogResults.log("Page fault for virtual page number: " + vpn);

        // Step 1: Load the page from secondary storage
        Page page = secondaryStorage.load(vpn);
        if (page == null) {
            LogResults.log("Page fault: page not found in secondary storage.");
            return -1;
        }
        Results.diskRead++;

        // Step 2: Determine the frame where the page should be loaded
        int frameToUse;
        if (mainMemory.isFull()) {
            frameToUse = handleEviction(vpn);
            if (frameToUse == -1) {
                LogResults.log("Error during eviction: no frame could be freed.");
                return -1;
            }
        } else {
            frameToUse = mainMemory.getNextAvailableFrame();
        }

        // Step 3: Load page into the chosen frame
        mainMemory.loadPageIntoMemory(page, frameToUse, vpn);
        //secondaryStorage.removePage(vpn);
        // Step 4: Update page table and TLB with the new frame mapping
        pageTable.addEntry(vpn, frameToUse);
        pageTable.setDiskPage(vpn, true);
        PageTableEntry newEntry = pageTable.getEntry(vpn);
        tlb.addEntry(vpn, newEntry);
         replacementAlgorithm.addPage(vpn);
        // LogResults.log("Loaded VPN " + vpn + " into frame " + frameToUse);
        return frameToUse;
    }

    /**
     * Handles the eviction of a page when memory is full. The victim page is evicted based on
     * the replacement algorithm, and if the page is dirty, it is written back to secondary storage.
     * @param newVpn The VPN of the page that caused the eviction.
     * @return The frame number that was freed, or -1 if an error occurred.
     */
    private int handleEviction(int newVpn) {
        int victimVpn = replacementAlgorithm.evictPage();
        PageTableEntry victimEntry = pageTable.getEntry(victimVpn);
        if (victimEntry == null) {
            LogResults.log("Victim page not found in page table");
            return -1;
        }

        int victimFrame = victimEntry.getFrameNumber();
        LogResults.log("Evicting VPN " + victimVpn + " from frame " + victimFrame);

        if (victimEntry.isDirty()) { // Write back if dirty
            LogResults.log("Evicted page is dirty. Writing back to disk.");
            Page victimPage = mainMemory.getPage(victimFrame);
            secondaryStorage.store(victimVpn, victimPage);
            pageTable.setDiskPage(victimVpn, true);
            Results.diskWrite++;
        }

        mainMemory.removePage(victimFrame); // Clean up old entry
        pageTable.setValid(victimVpn, false);
        pageTable.setDirty(victimVpn, false);
        pageTable.setReferenced(victimVpn, false);
        pageTable.setPPN(victimVpn, -1);
        if(tlb.containsEntry(victimVpn) != false)
            tlb.removeEntry(victimVpn);
        Results.pageEviction++;
        return victimFrame;
    }

    /**
     * Loads data from memory at a given physical address.
     * @param physicalAddress The physical address to load data from.
     */
    private void loadFromMemory(Address physicalAddress) {
        int data = mainMemory.load(physicalAddress);
        int vpn = pageTable.getCorrespondingVPN(physicalAddress.getPageNumber());
        if (pageTable.getEntry(vpn) != null) {
            pageTable.getEntry(vpn).setRefBit(true);  // Mark the referenced bit, since the page has been accessed
            replacementAlgorithm.updatePageAccess(vpn);
        }
        incrementOperationCount(); // Increment operation count after a load
        int address = physicalAddress.getPageNumber() * pageSize + physicalAddress.getOffset();
        LogResults.log("Loaded data: " + data + " from physical address: " + address +  " (" + physicalAddress.printAddress("Physical") + ")" + '\n');
    }

    /**
     * Stores data to memory at a given physical address.
     * @param physicalAddress The physical address where data will be stored.
     * @param data The data to be stored.
     */
    private void storeToMemory(Address physicalAddress, int data) {
        mainMemory.store(physicalAddress, data);
        int vpn = pageTable.getCorrespondingVPN(physicalAddress.getPageNumber());
        if (pageTable.getEntry(vpn) != null) {
            pageTable.getEntry(vpn).setDirtyBit(true); // Mark as dirty (modified)
            pageTable.getEntry(vpn).setRefBit(true);  // Mark as referenced
            replacementAlgorithm.updatePageAccess(vpn);
        }
        if(tlb.getEntry(vpn) != null) {
            tlb.getEntry(vpn).setRefBit(true);
            tlb.getEntry(vpn).setDirtyBit(true);
        }
        incrementOperationCount(); // Increment operation count after a store
        int address = physicalAddress.getPageNumber() * pageSize + physicalAddress.getOffset();
        LogResults.log("Stored data: " + data + " to physical address: " + address + " (" + physicalAddress.printAddress("Physical") + ")" + '\n');
    }

    /**
     * Tracks the number of memory operations performed and resets
     * NRU replacement algorithm referenced bits after reaching a predefined interval.
     * This periodic reset ensures proper behavior for NRU replacement.
     */
    private void incrementOperationCount() {
        operationCount++;
        if (operationCount >= RESET_INTERVAL) {
            resetNRUBits(); // Reset referenced bits periodically
            operationCount = 0; // Reset the counter
        }
    }

    /**
     * Resets the referenced bits in the NRU replacement algorithm, if applicable.
     */
    public void resetNRUBits() {
        if (replacementAlgorithm instanceof NRUReplacement) {
            ((NRUReplacement) replacementAlgorithm).resetReferencedBits();
            LogResults.log("Reset referenced bits for NRU algorithm.");
        }
    }

    /**
     * Prints the contents of the main memory, secondary storage, TLB, and page table.
     */
    public void printMemoryContents() {
        mainMemory.printContents();
        secondaryStorage.printContents();
        tlb.printContents();
        pageTable.printContents();
    }

    public MainMemory getMainMemory(){ return mainMemory; } // gets the current instance of the main memory
    public TLB getTlb() { return tlb; } // gets the current instance of the TLB
    public PageTable getPageTable() { return pageTable; } // gets the current instance of the  page table
    public SecondaryStorage getSecondaryStorage() { return secondaryStorage; } // gets the current instance of the disk
    //public Object getReplacementAlgorithm() { return this.replacementAlgorithm;}

    //    /**
//     * Sets the future memory access references for the Optimal Replacement algorithm.
//     * It must be called before the eviction process to enable the Optimal Replacement algorithm to
//     * make decisions based on future memory accesses.
//     * @param futureReferences A map where the key is the Virtual Page Number (VPN) and the value is a list of future access steps.
//     */
//    public void setFutureReferences(Map<Integer, List<Integer>> futureReferences) {
//        if (replacementAlgorithm instanceof OptimalReplacement) {
//            ((OptimalReplacement) replacementAlgorithm).setFutureReferences(futureReferences);
//            LogResults.log("Future references set for optimal replacement.");
//        }
//    }
}
