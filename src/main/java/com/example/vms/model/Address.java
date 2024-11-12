package com.example.vms.model;

/**
 * Represents an address in memory, which can be used as a virtual or physical address.
 * The address contains a page/frame number and an offset.
 */
public class Address {
    private int pageNumber; // represents vpn / ppn
    private int offset;  // offset within the page/frame

    /**
     * Constructs an Address with the specified page/frame number and offset.
     * @param pageNumber The page or frame number of the address.
     * @param offset The offset within the page or frame.
     */
    public Address(int pageNumber, int offset) {
        this.pageNumber = pageNumber;
        this.offset = offset;
    }

    /**
     * Retrieves the page or frame number of the address.
     * @return The page or frame number.
     */
    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * Retrieves the offset within the page or frame.
     * @return The offset within the page or frame.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Sets the page or frame number of the address.
     * @param pageNumber The new page or frame number to be set.
     */
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    /**
     * Sets the offset within the page or frame.
     * @param offset The new offset to be set within the page or frame.
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Returns a string representation of the address based on its type (virtual or physical).
     * @param addressType The type of the address ("Virtual" or "Physical").
     * @return A string describing the address, or an error message if the address type is invalid.
     */
    public String printAddress(String addressType) {
        if (addressType.equalsIgnoreCase("Virtual"))
            return "Virtual page number: " + pageNumber + " Offset: " + offset;
        else if (addressType.equalsIgnoreCase("Physical"))
            return "Frame number: " + pageNumber + " Offset: " + offset;
        else
            return "Invalid address type";
    }
}
