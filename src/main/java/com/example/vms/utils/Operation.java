package com.example.vms.utils;

/**
 * Represents an operation to be performed in the virtual memory simulation.
 * The operations can be of three types: Allocate, Load, and Store.
 * This class provides methods to create and handle these operations.
 */
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
}
