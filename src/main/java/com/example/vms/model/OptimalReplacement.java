package com.example.vms.model;

import com.example.vms.utils.LogResults;

import java.util.*;

/**
 * Optimal Replacement algorithm implementation. This algorithm selects the page to evict
 * based on the page that will be used the furthest in the future. This is ideal but cannot be
 * implemented directly in practice, as it requires knowledge of future memory accesses.
 * The algorithm evicts the page that is not needed for the longest period of time.
 */
public class OptimalReplacement implements ReplacementAlgorithm {
    private final Map<Integer, List<Integer>> futureReferences;  // Future memory access pattern for each page
    private final Map<Integer, Integer> activePages; // Active pages and the current step at which they were last accessed
    private int currentStep; // The current simulation step (used to track page accesses)

    /**
     * Constructs the OptimalReplacement algorithm with initial values.
     */
    public OptimalReplacement() {
        this.futureReferences = new HashMap<>();  // Initialize with an empty map
        this.activePages = new HashMap<>();
        this.currentStep = 0;
        LogResults.log("Optimal Replacement algorithm initialized.");
    }

    /**
     * Sets the future memory access references for each page in the system.
     * This method must be called before the eviction process to provide the
     * future memory access patterns needed for the optimal algorithm.
     *
     * @param futureReferences A map containing the future access times for each page.
     */
    public void setFutureReferences(Map<Integer, List<Integer>> futureReferences) {
        this.futureReferences.clear();
        // Validate and sort future references for each page
        futureReferences.forEach((vpn, accesses) -> {
            List<Integer> sortedAccesses = new ArrayList<>(accesses);
            Collections.sort(sortedAccesses);
            this.futureReferences.put(vpn, sortedAccesses);
        });
        LogResults.log("Future references updated and validated.");
    }

    /**
     * Selects a page to evict based on the Optimal Replacement strategy.
     * The page that will be accessed furthest in the future (or not at all) is evicted.
     *
     * @return The Virtual Page Number (VPN) of the evicted page.
     */
    @Override
    public int evictPage() {
        if (activePages.isEmpty()) {
            LogResults.log("No pages to evict.");
            return -1;  // Return -1 to indicate no page to evict
        }
        int victimVpn = -1;
        int furthestUse = -1;
        // Iterate over active pages and find the one with the furthest future access
        for (int vpn : activePages.keySet()) {
            List<Integer> futureAccesses = futureReferences.getOrDefault(vpn, Collections.emptyList());
            if (futureAccesses == null || futureAccesses.isEmpty()) {
                // If no future access, this page is the least desirable and should be evicted
                futureAccesses = Arrays.asList(Integer.MAX_VALUE); // Ensure it gets evicted
            }
            int nextUse = getNextUse(futureAccesses, currentStep);
            // Determine which page has the furthest access time
            if (nextUse > furthestUse) {
                furthestUse = nextUse;
                victimVpn = vpn;
            }
        }
        if (victimVpn != -1) {
            activePages.remove(victimVpn); // Remove the evicted page from active pages
            LogResults.log("Evicted VPN " + victimVpn + " with furthest future use at step " + furthestUse);
            return victimVpn;
        }
        //throw new IllegalStateException("Failed to select a page for eviction.");
        LogResults.log("Failed to select a page for eviction.");
        return -1;
    }

    /**
     * Determines when a page will be accessed next, based on its future access list.
     * If the page will not be accessed again, it returns Integer.MAX_VALUE.
     *
     * @param accessTimes The list of future access times for a page.
     * @param currentStep The current simulation step.
     * @return The next step the page will be accessed, or Integer.MAX_VALUE if it won't be accessed.
     */
    private int getNextUse(List<Integer> accessTimes, int currentStep) {
        if (accessTimes == null || accessTimes.isEmpty()) {
            // If accessTimes is null or empty, return Integer.MAX_VALUE to indicate that the page is not needed in the future
            return Integer.MAX_VALUE;
        }
        // Return the first access time after the current step, or MAX_VALUE if none
        return accessTimes.stream()
                .filter(time -> time > currentStep)
                .findFirst()
                .orElse(Integer.MAX_VALUE);
    }

    /**
     * Adds a page to the active pages list at the current step.
     * This is called when a page is accessed or newly loaded into memory.
     *
     * @param vpn The Virtual Page Number (VPN) of the page being added.
     */
    @Override
    public void addPage(int vpn) {
        activePages.put(vpn, currentStep);
        LogResults.log("Added VPN " + vpn + " to active pages at step " + currentStep);
    }

    /**
     * Updates the current access time for a page.
     * This is called when a page is accessed.
     *
     * @param vpn The Virtual Page Number (VPN) of the page being accessed.
     */
    @Override
    public void updatePageAccess(int vpn) {
        currentStep++;
        activePages.put(vpn, currentStep);  // Update the access time for the page
        LogResults.log("Updated access time for VPN " + vpn + " to step " + currentStep);
    }

    /**
     * Finds the page in the TLB that should be evicted based on the Optimal Replacement policy.
     * @param tlbPages A set of VPNs representing the pages currently in the TLB.
     * @return The VPN of the page to evict, or -1 if no valid page is found.
     */
    public int getTLBOptimalPage(Set<Integer> tlbPages) {
        int victimVpn = -1;
        int furthestUse = -1;
        for (int vpn : activePages.keySet()) {
            if (tlbPages.contains(vpn)) { // Only consider pages in the TLB
                List<Integer> futureAccesses = futureReferences.get(vpn);
                if (futureAccesses == null || futureAccesses.isEmpty()) {
                    futureAccesses = Arrays.asList(Integer.MAX_VALUE); // Page with no future access
                }
                int nextUse = getNextUse(futureAccesses, currentStep);
                // Select the page with the furthest future access
                if (nextUse > furthestUse) {
                    furthestUse = nextUse;
                    victimVpn = vpn;
                }
            }
        }
        if (victimVpn != -1) {
            LogResults.log("Selected VPN " + victimVpn + " for TLB eviction based on Optimal Replacement (furthest use at step " + furthestUse + ").");
        }
        return victimVpn;
    }
}
