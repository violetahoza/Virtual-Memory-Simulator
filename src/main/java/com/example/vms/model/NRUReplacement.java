package com.example.vms.model;

import com.example.vms.utils.LogResults;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * NRU (Not Recently Used) Replacement algorithm implementation. This algorithm selects a victim page
 * based on the R (referenced) and M (modified) bits of the page entries in the page table.
 * The page is categorized into one of four classes (0-3) depending on the status of these bits,
 * and the algorithm tries to evict a page from the lowest class.
 */
public class NRUReplacement implements ReplacementAlgorithm {
    private final PageTable pageTable;  // The page table that holds all the page entries.
    private final Random random;        // Random object to select a random page if necessary.
    private final List<Integer> activePages; // List of active pages to be managed by NRU.

    /**
     * Constructs an NRUReplacement algorithm with a reference to the page table.
     *
     * @param pageTable The page table that contains the page entries.
     */
    public NRUReplacement(PageTable pageTable) {
        this.pageTable = pageTable;
        this.random = new Random();
        this.activePages = new ArrayList<>();
        LogResults.log("NRU Replacement algorithm initialized with page table.");
    }

    /**
     * Selects a page to evict based on the NRU algorithm.
     * Pages are classified into 4 categories (0-3) based on the R and M bits.
     * The eviction is performed from the lowest class first.
     * If no pages can be found in any class, a random page is selected.
     *
     * @return The VPN (Virtual Page Number) of the evicted page or -1 if no page was evicted.
     */
    @Override
    public int evictPage() {
        if (activePages.isEmpty()) {
            LogResults.log("No pages to evict.");
            return -1; // Return -1 to indicate no page to evict
        }
        // Try to find pages in each class (0-3) based on R and M bits
        for (int classNum = 0; classNum < 4; classNum++) {
            List<Integer> classPages = new ArrayList<>();
            for (int vpn : activePages) {
                PageTableEntry entry = pageTable.getEntry(vpn);
                if (entry != null) {
                    int pageClass = getPageClass(entry);
                    if (pageClass == classNum) {
                        classPages.add(vpn);
                    }
                }
            }
            if (!classPages.isEmpty()) {
                // Randomly select a victim from the class
                int victimIndex = random.nextInt(classPages.size());
                int victimVpn = classPages.get(victimIndex);
                activePages.remove(Integer.valueOf(victimVpn)); // Remove the evicted page from active list
                LogResults.log("Evicted page with VPN " + victimVpn + " from class " + classNum);
                return victimVpn; // Return the VPN of the evicted page
            }
            LogResults.log("No pages found in class " + classNum + ".");
        }
        // If no pages found in any class, pick a random page
        int randomIndex = random.nextInt(activePages.size());
        int victimVpn = activePages.get(randomIndex);
        activePages.remove(randomIndex); // Remove the evicted page from active list
        LogResults.log("No page found in any class. Evicted random page with VPN " + victimVpn);
        return victimVpn; // Return the VPN of the evicted page
    }

    /**
     * Determines the class of a page based on its R (referenced) and M (modified) bits.
     * Class 0: Not referenced, Not modified -> highest priority for eviction
     * Class 1: Not referenced, Modified
     * Class 2: Referenced, Not modified
     * Class 3: Referenced, Modified -> lowest priority for eviction
     *
     * @param entry The page table entry representing the page.
     * @return The class of the page (0-3).
     */
    private int getPageClass(PageTableEntry entry) {
        // Return the class based on the referenced and modified bits
        return (entry.isReferenced() ? 2 : 0) + (entry.isDirty() ? 1 : 0);
    }

    /**
     * Adds a new page to the active list of pages, and updates its access status.
     * If the page is not already in the active list, it will be added.
     *
     * @param vpn The Virtual Page Number (VPN) of the page to be added.
     */
    @Override
    public void addPage(int vpn) {
        if (!activePages.contains(vpn)) {
            activePages.add(vpn);
            LogResults.log("Added VPN " + vpn + " to active pages.");
        }
        updatePageAccess(vpn);
    }

    /**
     * Updates the page access status by setting the referenced bit to true.
     * This is done when a page is accessed in the NRU algorithm.
     *
     * @param vpn The Virtual Page Number (VPN) of the page that was accessed.
     */
    @Override
    public void updatePageAccess(int vpn) {
        PageTableEntry entry = pageTable.getEntry(vpn);
        if (entry != null) {
            entry.setRefBit(true);
            // LogResults.log("Updated referenced bit for VPN " + vpn);
        }
    }

    /**
     * Periodically reset referenced bits to maintain proper NRU behavior.
     */
    public void resetReferencedBits() {
        for (int vpn : activePages) {
            PageTableEntry entry = pageTable.getEntry(vpn);
            if (entry != null) {
                entry.setRefBit(false); // Reset the referenced bit
            }
        }
        LogResults.log("Reset referenced bits for all active pages.");
    }
}
