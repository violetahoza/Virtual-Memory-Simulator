package com.example.vms.utils;

import java.util.List;

public class SimulationConfig {
    private Integer virtualAddressWidth;
    private Integer pageSize;
    private Integer tlbSize;
    private Integer physicalMemorySize;
    private Integer secondaryMemorySize;
    private String replacementAlgorithm;
    private List<Operation> operations; // List of predefined operations like "load", "store", etc.

    // Getters and setters
    public int getVirtualAddressWidth() { return virtualAddressWidth; }
    public void setVirtualAddressWidth(int virtualAddressWidth) { this.virtualAddressWidth = virtualAddressWidth; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }

    public int getTlbSize() { return tlbSize; }
    public void setTlbSize(int tlbSize) { this.tlbSize = tlbSize; }

    public int getPhysicalMemorySize() { return physicalMemorySize; }
    public void setPhysicalMemorySize(int physicalMemorySize) { this.physicalMemorySize = physicalMemorySize; }

    public int getSecondaryMemorySize() { return secondaryMemorySize; }
    public void setSecondaryMemorySize(int secondaryMemorySize) { this.secondaryMemorySize = secondaryMemorySize; }

    public String getReplacementAlgorithm() { return replacementAlgorithm; }
    public void setReplacementAlgorithm(String replacementAlgorithm) { this.replacementAlgorithm = replacementAlgorithm; }

    public List<Operation> getOperations() { return operations; }
    public void setOperations(List<Operation> operations) { this.operations = operations; }
}