package com.example.vms.model;

import java.util.List;
import java.util.Random;

public class NRUReplacement implements ReplacementAlgorithm {
    private PageTable pageTable = null;
    private final Random random = new Random();

    public NRUReplacement(PageTable pageTable) {
        this.pageTable = pageTable;
    }

    public NRUReplacement() {
    }

    @Override
    public int evictPage() {
        List<PageTableEntry> entries = pageTable.getEntries();
        for (int i = 0; i < 4; i++) {
            for (PageTableEntry entry : entries) {
                int classNumber = (entry.isReferenced() ? 2 : 0) + (entry.isDirty() ? 1 : 0);
                if (classNumber == i) {
                    pageTable.setReferenced(entry.getFrameNumber(), false); // reset reference bit for future
                    return entry.getFrameNumber();
                }
            }
        }
        return entries.get(random.nextInt(entries.size())).getFrameNumber(); // fallback if no page found
    }

    public void addPage(int vpn) {
        // Set the referenced bit for the newly added page
        PageTableEntry entry = pageTable.getEntry(vpn);
        if (entry != null) {
            entry.setRefBit(true); // Mark this page as recently accessed
        }
    }
}
