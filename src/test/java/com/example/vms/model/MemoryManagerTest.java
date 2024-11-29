package com.example.vms.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MemoryManagerTest {
    private MemoryManager memoryManager;

    @BeforeEach
    void setUp() {
        // Initialize MemoryManager with some test parameters
        int virtualAddressWidth = 10; // 1024 virtual addresses
        int tlbSize = 4;
        int pageSize = 16; // 16 bytes per page
        int physicalMemorySize = 128; // 128 bytes of physical memory
        int diskSize = 256; // 256 bytes of disk space
        ReplacementAlgorithm replacementAlgorithm = new FIFOReplacement(4);

        memoryManager = new MemoryManager(virtualAddressWidth, tlbSize, pageSize, physicalMemorySize, diskSize, replacementAlgorithm);
    }

    @Test
    void testLoadValidAddress() {
        // Load a page into memory
        memoryManager.allocatePage(0); // Allocate virtual page 0
        memoryManager.load(0); // Load virtual address 0

        // Check if the page is loaded correctly
        assertNotNull(memoryManager.getMainMemory().getPage(0));
    }

    @Test
    void testLoadInvalidAddress() {
        // Attempt to load an invalid address
        memoryManager.load(-1); // Invalid address
        // No assertion needed, just ensure it doesn't throw an exception
    }

    @Test
    void testStoreData() {
        // Allocate and load a page
        memoryManager.allocatePage(0);
        memoryManager.load(0);

        // Store data at a valid address
        memoryManager.store(0, 42); // Store value 42 at virtual address 0

        // Verify that the data is stored correctly
        int data = memoryManager.getMainMemory().load(new Address(0, 0)); // Load from physical address
        assertEquals(42, data);
    }

    @Test
    void testFIFOReplacement() {
        // Initialize MemoryManager with FIFO replacement
        int virtualAddressWidth = 10; // 1024 virtual addresses
        int tlbSize = 4;
        int pageSize = 16; // 16 bytes per page
        int physicalMemorySize = 64; // 64 bytes of physical memory (4 pages)
        int diskSize = 256; // 256 bytes of disk space
        ReplacementAlgorithm replacementAlgorithm = new FIFOReplacement(4);

        MemoryManager fifoMemoryManager = new MemoryManager(virtualAddressWidth, tlbSize, pageSize, physicalMemorySize, diskSize, replacementAlgorithm);

        // Allocate and load 4 pages (filling physical memory)
        fifoMemoryManager.allocatePage(0);
        fifoMemoryManager.load(0);
        fifoMemoryManager.allocatePage(1);
        fifoMemoryManager.load(16);
        fifoMemoryManager.allocatePage(2);
        fifoMemoryManager.store(32, 42);
        fifoMemoryManager.allocatePage(3);
        fifoMemoryManager.load(48);

        // Allocate and load a new page, which should trigger eviction (FIFO policy)
        fifoMemoryManager.allocatePage(4);
        fifoMemoryManager.load(64); // Loading page 4 should evict page 0 (VPN 0)

        // Check if the first page (VPN 0) was evicted
        boolean vpn0InMemory = fifoMemoryManager.getPageTable().isValid(0);
        //System.out.println(vpn0InMemory);
        assertFalse(vpn0InMemory, "Expected VPN 0 to be evicted under FIFO policy.");

        // Additional assertions to confirm the current memory state
        assertTrue(fifoMemoryManager.getPageTable().isValid(4), "VPN 4 should be in memory.");
        assertTrue(fifoMemoryManager.getPageTable().isValid(1), "VPN 1 should still be in memory.");
        assertTrue(fifoMemoryManager.getPageTable().isValid(2), "VPN 2 should still be in memory.");
        assertTrue(fifoMemoryManager.getPageTable().isValid(3), "VPN 3 should still be in memory.");
    }


    @Test
    void testLRUReplacement() {
        // Initialize MemoryManager with LRU replacement
        int virtualAddressWidth = 10; // 1024 virtual addresses
        int tlbSize = 4;
        int pageSize = 16; // 16 bytes per page
        int physicalMemorySize = 64; // 64 bytes of physical memory (4 pages)
        int diskSize = 256; // 256 bytes of disk space
        ReplacementAlgorithm replacementAlgorithm = new LRUReplacement();

        MemoryManager lruMemoryManager = new MemoryManager(virtualAddressWidth, tlbSize, pageSize, physicalMemorySize, diskSize, replacementAlgorithm);

        // Allocate and load pages
        lruMemoryManager.allocatePage(0);
        lruMemoryManager.allocatePage(1);
        lruMemoryManager.allocatePage(2);
        lruMemoryManager.allocatePage(3);

        // Load pages into memory
        lruMemoryManager.load(0);
        lruMemoryManager.load(16);
        lruMemoryManager.load(32);
        lruMemoryManager.load(48);

        // Now allocate one more page, which should trigger eviction
        lruMemoryManager.allocatePage(4);
        lruMemoryManager.load(64); // Load the new page to trigger eviction
        // Check if the least recently used page (VPN 0) was evicted
        assertFalse(lruMemoryManager.getPageTable().isValid(0), "Expected VPN 0 to be evicted.");
    }

    @Test
    void testNRUReplacement() {
        // Initialize MemoryManager with NRU replacement
        int virtualAddressWidth = 10; // 1024 virtual addresses
        int tlbSize = 4;
        int pageSize = 16; // 16 bytes per page
        int physicalMemorySize = 64; // 64 bytes of physical memory (4 pages)
        int diskSize = 256; // 256 bytes of disk space
        PageTable pageTable = new PageTable(virtualAddressWidth / pageSize);
        ReplacementAlgorithm replacementAlgorithm = new NRUReplacement(pageTable);

        MemoryManager nruMemoryManager = new MemoryManager(virtualAddressWidth, tlbSize, pageSize, physicalMemorySize, diskSize, replacementAlgorithm);

        // Allocate and load pages
        nruMemoryManager.allocatePage(0); // VPN 0
        nruMemoryManager.allocatePage(1); // VPN 1
        nruMemoryManager.allocatePage(2); // VPN 2
        nruMemoryManager.allocatePage(3); // VPN 3

        // Load pages into memory
        nruMemoryManager.load(0);
        nruMemoryManager.load(1);

        // Simulate accesses to update reference bits
        nruMemoryManager.load(0); // Access VPN 0
        nruMemoryManager.load(1); // Access VPN 1

        // Now allocate one more page, which should trigger eviction
        nruMemoryManager.allocatePage(4);
        nruMemoryManager.load(4);// This should evict either VPN 2 or VPN 3

        // Check the state of the page table to see if VPN 2 or VPN 3 was evicted
        PageTableEntry entry2 = pageTable.getEntry(2);
        PageTableEntry entry3 = pageTable.getEntry(3);

        // Assert that at least one of the pages (VPN 2 or VPN 3) is not valid (evicted)
        assertTrue(entry2 == null || !entry2.isValid(), "Expected VPN 2 to be evicted.");
        assertTrue(entry3 == null || !entry3.isValid(), "Expected VPN 3 to be evicted.");
    }
}