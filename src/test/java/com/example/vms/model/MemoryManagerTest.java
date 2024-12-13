package com.example.vms.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        memoryManager.allocatePage(0); // Allocate virtual page 0
        memoryManager.load(0); // Load virtual address 0
        memoryManager.allocatePage(1); // Allocate virtual page 1
        memoryManager.load(16); // Load virtual address 16
        memoryManager.allocatePage(2); // Allocate virtual page 2
        memoryManager.load(32); // Load virtual address 32
        // Check if the pages are loaded correctly
        assertNotNull(memoryManager.getMainMemory().getPage(0));
        assertNotNull(memoryManager.getMainMemory().getPage(1));
        assertNotNull(memoryManager.getMainMemory().getPage(2));
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
        fifoMemoryManager.load(32);
        fifoMemoryManager.allocatePage(3);
        fifoMemoryManager.load(48);

        // Allocate and load a new page, which should trigger eviction (FIFO policy)
        fifoMemoryManager.allocatePage(4);
        fifoMemoryManager.load(64); // Loading page 4 should evict page 0 (VPN 0)

        // Check if the first page (VPN 0) was evicted
        boolean vpn0InMemory = fifoMemoryManager.getPageTable().isValid(0);
        assertFalse(vpn0InMemory, "Expected VPN 0 to be evicted under FIFO policy.");

        // Check if the correct page (VPN 1) was evicted next
        fifoMemoryManager.allocatePage(5);
        fifoMemoryManager.load(80); // Loading page 5 should evict page 1
        boolean vpn1InMemory = fifoMemoryManager.getPageTable().isValid(1);
        assertFalse(vpn1InMemory, "Expected VPN 1 to be evicted under FIFO policy.");
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
        assertTrue(lruMemoryManager.getPageTable().isValid(4), "VPN 4 should be in memory.");
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

    @Test
    void testOptimalReplacement() {
        // Initialize MemoryManager with Optimal replacement algorithm
        int virtualAddressWidth = 10; // 1024 virtual addresses
        int tlbSize = 4;
        int pageSize = 16; // 16 bytes per page
        int physicalMemorySize = 64; // 64 bytes of physical memory (4 pages)
        int diskSize = 256; // 256 bytes of disk space
        List<Integer> futureAccesses = Arrays.asList(0, 1, 2, 3, 4, 1, 2, 0, 4, 5); // Future VPN accesses
        ReplacementAlgorithm replacementAlgorithm = new OptimalReplacement();
        ((OptimalReplacement) replacementAlgorithm).setFutureAccesses(futureAccesses);

        MemoryManager optimalMemoryManager = new MemoryManager(virtualAddressWidth, tlbSize, pageSize, physicalMemorySize, diskSize, replacementAlgorithm);

        // Allocate and load pages
        optimalMemoryManager.allocatePage(0);
        optimalMemoryManager.load(0 * pageSize); // VPN 0
        optimalMemoryManager.allocatePage(1);
        optimalMemoryManager.load(1 * pageSize); // VPN 1
        optimalMemoryManager.allocatePage(2);
        optimalMemoryManager.load(2 * pageSize); // VPN 2
        optimalMemoryManager.allocatePage(3);
        optimalMemoryManager.load(3 * pageSize); // VPN 3

        // At this point, physical memory is full

        // Allocate and load a new page, which should trigger eviction
        optimalMemoryManager.allocatePage(4);
        optimalMemoryManager.load(4 * pageSize); // VPN 4

        // Check that the correct page (VPN 0) was evicted, as it will not be used again for the longest time
        boolean vpn0InMemory = optimalMemoryManager.getPageTable().isValid(0);
        assertFalse(vpn0InMemory, "Expected VPN 0 to be evicted under Optimal Replacement policy.");

        // Additional checks to confirm the state of memory
        assertTrue(optimalMemoryManager.getPageTable().isValid(4), "VPN 4 should be in memory.");
        assertTrue(optimalMemoryManager.getPageTable().isValid(1), "VPN 1 should still be in memory.");
        assertTrue(optimalMemoryManager.getPageTable().isValid(2), "VPN 2 should still be in memory.");
        assertTrue(optimalMemoryManager.getPageTable().isValid(3), "VPN 3 should still be in memory.");

        // Allocate and load another page, which should trigger eviction of VPN 1 as it is least used in the future
        optimalMemoryManager.allocatePage(5);
        optimalMemoryManager.load(5 * pageSize); // VPN 5
        boolean vpn1InMemory = optimalMemoryManager.getPageTable().isValid(1);
        assertFalse(vpn1InMemory, "Expected VPN 1 to be evicted under Optimal Replacement policy.");
    }
}