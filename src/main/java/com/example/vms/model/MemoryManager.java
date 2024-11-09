package com.example.vms.model;

import com.example.vms.utils.LogResults;

public class MemoryManager {
    private TLB tlb;
    private PageTable pageTable;
    private MainMemory mainMemory;
    private SecondaryStorage secondaryStorage;
    private ReplacementAlgorithm replacementAlgorithm;
    private int pageSize, virtualAddressWidth, virtualMemorySize;

    public MemoryManager(int virtualAddressWidth, int tlbSize, int pageSize, int physicalMemorySize, ReplacementAlgorithm replacementAlgorithm) {
        if (pageSize <= 0 || virtualAddressWidth <= 0 || physicalMemorySize <= 0 || tlbSize < 0) {
            throw new IllegalArgumentException("Memory configuration parameters must be positive.");
        }

        this.pageSize = pageSize;
        this.virtualAddressWidth = virtualAddressWidth;
        this.virtualMemorySize = (int) Math.pow(2, virtualAddressWidth);
        int totalPages = (int) (virtualMemorySize / pageSize);
        int nrFrames = physicalMemorySize / pageSize;
        if (totalPages == 0 || nrFrames == 0) {
            throw new IllegalArgumentException("Page size and physical memory size are too small for configuration.");
        }

        this.tlb = new TLB(tlbSize);
        this.replacementAlgorithm = replacementAlgorithm;
        this.pageTable = new PageTable(totalPages);
        this.mainMemory = new MainMemory(nrFrames, pageSize);
        int maxDiskPages = totalPages - nrFrames;
        this.secondaryStorage = new SecondaryStorage(maxDiskPages, pageSize);
    }

    public void load(int virtualAddress) {
        int vpn = virtualAddress / pageSize;
        int offset = virtualAddress % pageSize;
        Address virtualAddr = new Address(vpn, offset);
        LogResults.log("Access request for virtual address: " + virtualAddr.printAddress("Virtual"));

        int ppn = tlb.lookup(vpn);
        if (ppn != -1) {
            LogResults.log("TLB hit! Physical page number: " + ppn);
            Results.tlbHit++;
            loadFromMemory(new Address(ppn, offset));
        } else {
            LogResults.log("TLB miss for virtual page nr: " + vpn);
            Results.tlbMiss++;
            handlePageTableLookup(vpn, offset);
        }
    }

    public void store(int virtualAddress, int data) {
        int vpn = virtualAddress / pageSize;
        int offset = virtualAddress % pageSize;
        Address virtualAddr = new Address(vpn, offset);
        LogResults.log("Storing data to: " + virtualAddr.printAddress("Virtual") + " . Data stored: " + data);

        int ppn = tlb.lookup(vpn);
        if (ppn != -1) {
            LogResults.log("TLB hit! Physical page number: " + ppn);
            Results.tlbHit++;
            storeToMemory(new Address(ppn, offset), data);
        } else {
            LogResults.log("TLB miss for virtual page number: " + vpn);
            Results.tlbMiss++;
            handlePageTableLookupForStore(vpn, offset, data);
        }
    }

    private void handlePageTableLookup(int vpn, int offset) {
        PageTableEntry entry = pageTable.getEntry(vpn);
        if (entry != null) {
            LogResults.log("Page table hit! Physical page number: " + entry.getFrameNumber() + " for virtual page number: " + vpn);
            Results.pageTableHit++;
            tlb.addEntry(vpn, entry);
            loadFromMemory(new Address(entry.getFrameNumber(), offset));
        } else {
            LogResults.log("Page table miss for virtual page number: " + vpn);
            Results.pageTableMiss++;
            handlePageFault(vpn);
            loadFromMemory(new Address(pageTable.getPhysicalPageNumber(vpn), offset));
        }
    }

    private void handlePageTableLookupForStore(int vpn, int offset, int data) {
        PageTableEntry entry = pageTable.getEntry(vpn);
        if (entry != null) {
            LogResults.log("Page table hit! Physical page number: " + entry.getFrameNumber() + " for virtual page number: " + vpn);
            Results.pageTableHit++;
            tlb.addEntry(vpn, entry);
            storeToMemory(new Address(entry.getFrameNumber(), offset), data);
        } else {
            LogResults.log("Page table miss for virtual page number: " + vpn);
            Results.pageTableMiss++;
            handlePageFault(vpn);
            storeToMemory(new Address(pageTable.getPhysicalPageNumber(vpn), offset), data);
        }
    }

    private void handlePageFault(int vpn) {
        LogResults.log("Page fault for virtual page number: " + vpn + ". Loading page from disk.");
        Page page = secondaryStorage.load(vpn);
        int newFrame;

        if (mainMemory.isFull()) {
            int evictVpn = replacementAlgorithm.evictPage();
            PageTableEntry evictedEntry = pageTable.getEntry(evictVpn);
            if (evictedEntry != null) {
                int evictedFrame = evictedEntry.getFrameNumber();
                LogResults.log("Evicting physical page number: " + evictedFrame + " for virtual page number: " + evictVpn);
                if (evictedEntry.isDirty()) {
                    LogResults.log("Evicted page is dirty. Writing back to disk.");
                    secondaryStorage.store(evictVpn, mainMemory.getPage(evictedFrame));
                    Results.diskWrite++;
                }
                mainMemory.removePage(evictedFrame);
                pageTable.setValid(evictVpn, false);
                tlb.removeEntry(evictVpn);
                newFrame = evictedFrame;
                Results.pageEviction++;
            } else {
                throw new IllegalStateException("Failed to evict a page");
            }
        } else {
            newFrame = mainMemory.loadPageIntoMemory(page);
        }

        if (newFrame == -1) {
            throw new IllegalStateException("Failed to allocate frame for new page");
        }

        mainMemory.removePage(newFrame);
        mainMemory.getMemory().put(newFrame, page);
        pageTable.addEntry(vpn, newFrame);
        replacementAlgorithm.addPage(vpn);
        Results.diskRead++;
        LogResults.log("Loaded virtual page number: " + vpn + " into physical frame number: " + newFrame);
        PageTableEntry newEntry = new PageTableEntry(newFrame, true, false, true);
        tlb.addEntry(vpn, newEntry);
    }

    public MemoryManager() {
        this.tlb = new TLB(0);
        this.pageTable = new PageTable(0);
        this.mainMemory = new MainMemory(0, 0);
        this.secondaryStorage = new SecondaryStorage(0, 0);
        Results.reset();
        LogResults.log("Memory Manager reset to initial state.");
    }

    private void loadFromMemory(Address physicalAddress) {
        int data = mainMemory.load(physicalAddress);
        PageTableEntry entry = pageTable.getEntry(physicalAddress.getPageNumber());
        if (entry != null) {
            entry.setRefBit(true);
        }
        LogResults.log("Loaded data: " + data + " from: " + physicalAddress.printAddress("Physical"));
    }

    private void storeToMemory(Address physicalAddress, int data) {
        mainMemory.store(physicalAddress, data);
        PageTableEntry entry = pageTable.getEntry(physicalAddress.getPageNumber());
        if (entry != null) {
            entry.setDirtyBit(true);
            entry.setRefBit(true);
        }
        LogResults.log("Stored data: " + data + " to: " + physicalAddress.printAddress("Physical"));
    }

    public void printMemoryContents() {
        mainMemory.printContents();
        secondaryStorage.printContents();
        tlb.printContents();
        pageTable.printContents();
    }
}
