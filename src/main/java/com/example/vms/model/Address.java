package com.example.vms.model;

/**
 * Represents an address in memory, which can be used as a virtual or physical address.
 * The address contains a page/frame number and an offset.
 */
public class Address {
    private int pageNumber; // represents vpn / ppn
    private int offset;  // offset within the page/frame

    public Address(int pageNumber, int offset) {
        this.pageNumber = pageNumber;
        this.offset = offset;
    }

    public int getPageNumber() {
        return pageNumber;
    } // return the page or frame number of the address
    public int getOffset() {
        return offset;
    } // return the offset within the page or frame
    public void setPageNumber(int pageNumber) {this.pageNumber = pageNumber;} // set the page or frame number of the address
    public void setOffset(int offset) {
        this.offset = offset;
    } // set the offset within the page or frame
    public String printAddress(String addressType) {
        if (addressType.equalsIgnoreCase("Virtual"))
            return "Virtual page number: " + pageNumber + " Offset: " + offset;
        else if (addressType.equalsIgnoreCase("Physical"))
            return "Frame number: " + pageNumber + " Offset: " + offset;
        else
            return "Invalid address type";
    }
}
