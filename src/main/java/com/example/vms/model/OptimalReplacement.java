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
    private List<Integer> futureAccesses;  //  future memory access list
    private final Map<Integer, Integer> activePages; // Active pages and the current step at which they were last accessed
    private int currentStep; // The current simulation step (used to track page accesses)

    /**
     * Constructs the OptimalReplacement algorithm with initial values.
     */
    public OptimalReplacement() {
        this.futureAccesses = new ArrayList<>();
        this.activePages = new HashMap<>();
        this.currentStep = 0;
        LogResults.log("Optimal Replacement algorithm initialized.");
    }

    /**
     * Sets the future memory accesses for the simulation.
     *
     * @param futureAccesses A list of page numbers to be accessed in order.
     */
    public void setFutureAccesses(List<Integer> futureAccesses) {
        this.futureAccesses = new ArrayList<>(futureAccesses);
        LogResults.log("Future accesses updated.");
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
            int nextUse = getNextUse(vpn);
            // Determine which page has the furthest access time
            if (nextUse > furthestUse) {
                furthestUse = nextUse;
                victimVpn = vpn;
            }
        }

        if (victimVpn != -1) {
            activePages.remove(victimVpn); // Remove evicted page
            LogResults.log("Evicted VPN " + victimVpn + ". Next use: " +
                    (furthestUse == Integer.MAX_VALUE ? "Never" : "Step " + furthestUse));
            return victimVpn;
        }

        LogResults.log("Failed to select a page for eviction.");
        return -1;
    }

    /**
     * Determines when a page will be accessed next.
     *
     * @param vpn The virtual page number to check for future access.
     * @return The next step the page will be accessed, or Integer.MAX_VALUE if it won't be accessed.
     */
    private int getNextUse(int vpn) {
        // Find the first occurrence of this VPN after the current step
        for (int i = currentStep; i < futureAccesses.size(); i++) {
            if (futureAccesses.get(i) == vpn) {
                return i;
            }
        }
        LogResults.log("VPN " + vpn + " will not be accessed again.");
        return Integer.MAX_VALUE; // Page won't be accessed again
    }

    /**
     * Adds a page to the active pages list at the current step.
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
     *
     * @param vpn The Virtual Page Number (VPN) of the page being accessed.
     */
    @Override
    public void updatePageAccess(int vpn) {
        if (!activePages.containsKey(vpn)) {
            LogResults.log("VPN " + vpn + " not found in active pages. Adding it.");
        }
        activePages.put(vpn, currentStep);  // Update the access time for the page
        LogResults.log("Updated access time for VPN " + vpn + " to step " + currentStep);
        currentStep++;
    }

    /**
     * Finds the page in the TLB that should be evicted based on the Optimal Replacement policy.
     * @param tlbPages A set of VPNs representing the pages currently in the TLB.
     * @return The VPN of the page to evict, or -1 if no valid page is found.
     */
    public int getTLBOptimalPage(Set<Integer> tlbPages) {
        int victimVpn = -1;
        int furthestUse = -1;

        for (int vpn : tlbPages) {
            int nextUse = getNextUse(vpn);
            // Select the page with the furthest future access
            if (nextUse > furthestUse) {
                furthestUse = nextUse;
                victimVpn = vpn;
            }
        }

        if (victimVpn != -1) {
            LogResults.log("Selected VPN " + victimVpn + " for TLB eviction based on Optimal Replacement (furthest use at step " + furthestUse + ").");
        }
        return victimVpn;
    }
}