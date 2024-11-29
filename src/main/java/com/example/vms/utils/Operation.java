package com.example.vms.utils;

public class Operation {
    private String type;  // Allocate, Load or Store
    private int address;  // address for load or store operations
    private int vpn;      // page number for allocate or store operations
    private int offset;   // offset within the page (relevant for store operations)
    private int data;     // data to store (relevant for store operations)

    // Getter and setter methods

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public int getVpn() {
        return vpn;
    }

    public void setVpn(int vpn) {
        this.vpn = vpn;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }

    /**
     * Allocate a virtual page (this operation only needs the vpn).
     * @param vpn The page number to allocate
     */
    public static Operation allocatePage(int vpn) {
        Operation operation = new Operation();
        operation.setType("Allocate");
        operation.setVpn(vpn);  // Only vpn is needed for allocation
        return operation;
    }

    /**
     * Load data from a given address.
     * @param address The virtual address to load from
     */
    public static Operation load(int address) {
        Operation operation = new Operation();
        operation.setType("Load");
        operation.setAddress(address);  // The address to load data from
        return operation;
    }

    /**
     * Store data at a specific offset in a given virtual page number.
     * @param vpn The virtual page number
     * @param offset The offset within the page to store data
     * @param data The data to store
     */
    public static Operation store(int vpn, int offset, int data) {
        Operation operation = new Operation();
        operation.setType("Store");
        operation.setVpn(vpn);   // The page number to store data
        operation.setOffset(offset);  // The offset to store data at
        operation.setData(data);  // The data to store at the given page and offset
        return operation;
    }
}
