package com.example.vms.model;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a page in virtual memory, storing data in offset-data pairs.
 */
public class Page {
    private Map<Integer, Integer> pageContents;// Store offset-data pairs (key: offset, value: data)
    private int pageSize; // Size of the page, representing the number of offsets it can store

    /**
     * Constructor to initialize a page with a given size.
     * Each offset in the page is initialized to 0.
     *
     * @param pageSize the number of offsets the page can hold
     */
    public Page(int pageSize) {
        this.pageSize = pageSize;
        pageContents = new LinkedHashMap<>();
        for (int i = 0; i < pageSize; i++) {
            pageContents.put(i, i); // initialize page
        }
    }

    /**
     * Private constructor used for creating a copy of a page.
     * Clones the contents of an existing page into a new one.
     *
     * @param pageSize     the size of the page to copy
     * @param pageContents the content to duplicate in the new page
     */
    private Page(int pageSize, Map<Integer, Integer> pageContents) {
        this.pageSize = pageSize;
        // Create a deep copy of the page contents to ensure isolation between original and copy
        this.pageContents = new LinkedHashMap<>(pageContents);
    }

    /**
     * Checks if the provided offset is within the valid range of the page.
     *
     * @param offset the offset to validate
     * @throws IllegalArgumentException if the offset is out of the page bounds
     */
    public void checkOffsetBounds(int offset) {
        if (offset < 0 || offset >= pageSize) {
            throw new IllegalArgumentException("Offset out of bounds.");
        }
    }

    /**
     * Retrieves the data stored at a specific offset in the page.
     *
     * @param offset the offset to retrieve data from
     * @return the value stored at the given offset
     * @throws IllegalArgumentException if the offset is out of the page bounds
     */
    public int load(int offset) {
        checkOffsetBounds(offset); // Validate offset bounds
        return pageContents.get(offset);
    }

    /**
     * Stores a value at a specific offset in the page.
     *
     * @param offset the offset to store the value at
     * @param value  the value to be stored
     * @throws IllegalArgumentException if the offset is out of the page bounds
     */
    public void store(int offset, int value) { // store a value at the given offset
        checkOffsetBounds(offset); // Validate offset bounds
        pageContents.put(offset, value);
    }

    /**
     * Retrieves a copy of the current page contents.
     * The copy prevents direct modification of the original page data.
     *
     * @return a map containing the offset-data pairs of the page
     */
    public Map<Integer, Integer> getPageContents() {
        // Create a copy to protect original page contents from modification
        Map<Integer, Integer> pageCopy = new HashMap<>();
        for (Map.Entry<Integer, Integer> e : pageContents.entrySet()) {
            pageCopy.put(e.getKey(), e.getValue()); // make a copy of the page so the user cannot modify the page's actual data in memory
        }
        return pageCopy; // return a copy of the page contents
    }

    /**
     * Creates and returns a copy of the current Page object.
     * This includes both the page size and the page contents.
     *
     * @return a new Page object with identical contents to the current one
     */
    public Page getCopy() {
        return new Page(pageSize, pageContents);
    }

    /**
     * Prints the contents of the page to the console or specified log.
     * Each entry shows the offset and the corresponding stored value.
     */
    public String printContents() {
        StringBuilder output = new StringBuilder("Page contents:\n");
        // Append each offset and its value to the output string
        for (Map.Entry<Integer, Integer> e : pageContents.entrySet()) {
            output.append(e.getKey()).append(": ").append(e.getValue()).append("\n");
        }
        return output.toString();
    }
}
