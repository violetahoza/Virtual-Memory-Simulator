package com.example.vms.controller;

import com.example.vms.model.*;
import com.example.vms.utils.LogResults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller that manages the simulation of a virtual memory system, including page replacement algorithms,
 * memory management, and simulation state reset.
 */
@Controller
@SessionAttributes({"loadAddress", "storeAddress", "storeData"})
public class MemoryController {

    private MemoryManager memoryManager;
    private String currentReplacementAlgorithm = "FIFO";  // Store current algorithm for display
    private int virtualAddressWidth;
    private int pageSize;
    private int tlbSize;
    private int physicalMemorySize;
    private int secondaryMemorySize;
    private int virtualMemorySize;
    private int pageTableSize;
    private PageTable pageTable = null;

    /**
     * Default constructor that initializes the memory manager with default values.
     */
    public MemoryController() {
        initializeMemoryManager(0, 0, 0, 0, 0, "FIFO");
    }

    /**
     * Initializes the memory manager with the given configuration parameters.
     * Sets up the memory manager with the specified replacement algorithm and memory sizes.
     * @param virtualAddressWidth the width of the virtual address
     * @param pageSize the size of a single page
     * @param tlbSize the size of the Translation Lookaside Buffer
     * @param physicalMemorySize the size of the physical memory
     * @param diskSize the size of the secondary memory
     * @param replacementAlgorithm the page replacement algorithm to use (FIFO, LRU, Optimal)
     */
    private void initializeMemoryManager(int virtualAddressWidth, int pageSize, int tlbSize,
                                         int physicalMemorySize, int diskSize, String replacementAlgorithm) {
        // Determine the replacement algorithm
        ReplacementAlgorithm algorithm;
        switch (replacementAlgorithm) {
            case "LRU":
                algorithm = new LRUReplacement();
                break;
            case "NRU":
                algorithm = new NRUReplacement(pageTable);
            case "Optimal":
                algorithm = new OptimalReplacement();
                break;
            case "FIFO":
            default:
                int maxSize = 0;
                if(pageSize != 0)
                    maxSize = physicalMemorySize / pageSize;
                algorithm = new FIFOReplacement(maxSize);
                break;
        }

        // Initialize memory manager with the provided configuration, or use default if no configuration is provided
        if (virtualAddressWidth == 0 && pageSize == 0 && tlbSize == 0 && physicalMemorySize == 0) {
            memoryManager = new MemoryManager();
        } else {
            if (pageSize != 0 && virtualAddressWidth != 0) {
                // Store configuration parameters
                this.virtualAddressWidth = virtualAddressWidth;
                this.pageSize = pageSize;
                this.tlbSize = tlbSize;
                this.physicalMemorySize = physicalMemorySize;
                this.currentReplacementAlgorithm = replacementAlgorithm;
                this.secondaryMemorySize = diskSize;
                this.virtualMemorySize = (int) Math.pow(2, virtualAddressWidth);
                this.pageTableSize = (int) (virtualMemorySize / pageSize);

                // Log the configuration values
//                LogResults.log("Configured with virtual memory size: " + virtualMemorySize);
//                LogResults.log("Configured with page table size: " + pageTableSize);
//                LogResults.log("Configured with number of physical frames: " + physicalMemorySize / pageSize);
//                LogResults.log("Configured with number of virtual pages: " + virtualMemorySize / pageSize);

                // Initialize memory manager with the calculated parameters
                memoryManager = new MemoryManager(virtualAddressWidth, tlbSize, pageSize, physicalMemorySize, diskSize, algorithm);
            }
        }
    }

    /**
     * The home page of the simulation that displays the current status and configuration of the memory system.
     * @param model the model to add attributes to for the view
     * @return the view name to render
     */
    @GetMapping("/")
    public String index(Model model) {
        // Add simulation results and configuration settings to the model
        model.addAttribute("tlbHit", Results.getTLBHitRate());
        model.addAttribute("tlbMiss", Results.getTLBMissRate());
        model.addAttribute("pageTableHit", Results.getPageTableHitRate());
        model.addAttribute("pageTableMiss", Results.getPageTableMissRate());
        model.addAttribute("diskRead", Results.diskRead);
        model.addAttribute("diskWrite", Results.diskWrite);
        model.addAttribute("pageEviction", Results.pageEviction);

        // Add configuration settings to the model
        model.addAttribute("virtualAddressWidth", virtualAddressWidth);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("tlbSize", tlbSize);
        model.addAttribute("physicalMemorySize", physicalMemorySize);
        model.addAttribute("secondaryMemorySize", secondaryMemorySize);
        model.addAttribute("replacementAlgorithm", currentReplacementAlgorithm);
        model.addAttribute("virtualMemorySize", virtualMemorySize);
        model.addAttribute("pageTableSize", pageTableSize);

        // Add default values for attributes that might not be set yet
        if (!model.containsAttribute("loadAddress")) {
            model.addAttribute("loadAddress", 0);
        }
        if (!model.containsAttribute("storeAddress")) {
            model.addAttribute("storeAddress", 0);
        }
        if (!model.containsAttribute("storeData")) {
            model.addAttribute("storeData", 0);
        }

        return "index";
    }

    /**
     * Configures the simulation with the specified parameters and updates the memory manager.
     * @param virtualAddressWidth the virtual address width
     * @param pageSize the page size
     * @param tlbSize the TLB size
     * @param physicalMemorySize the physical memory size
     * @param secondaryMemorySize the secondary memory size
     * @param replacementAlgorithm the page replacement algorithm to use
     * @param model the model to add attributes to for the view
     * @return the view name to redirect to
     */
    @PostMapping("/configure")
    public String configureSimulation(
            @RequestParam("virtualAddressWidth") int virtualAddressWidth,
            @RequestParam("pageSize") int pageSize,
            @RequestParam("tlbSize") int tlbSize,
            @RequestParam("physicalMemorySize") int physicalMemorySize,
            @RequestParam("secondaryMemorySize") int secondaryMemorySize,
            @RequestParam("replacementAlgorithm") String replacementAlgorithm,
            Model model) {

        // Initialize MemoryManager with user-configured parameters
        initializeMemoryManager(virtualAddressWidth, pageSize, tlbSize, physicalMemorySize, secondaryMemorySize, replacementAlgorithm);

        // Update the model with the latest values to retain them in the form fields
        model.addAttribute("virtualAddressWidth", virtualAddressWidth);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("tlbSize", tlbSize);
        model.addAttribute("physicalMemorySize", physicalMemorySize);
        model.addAttribute("secondaryMemorySize", secondaryMemorySize);
        model.addAttribute("replacementAlgorithm", replacementAlgorithm);
        model.addAttribute("virtualMemorySize", virtualMemorySize);
        model.addAttribute("pageTableSize", pageTableSize);

        LogResults.log("\nSimulation configured with virtualAddressWidth: " + virtualAddressWidth +
                ", pageSize: " + pageSize + ", tlbSize: " + tlbSize +
                ", physicalMemorySize: " + physicalMemorySize + " secondaryMemorySize: " + secondaryMemorySize + '\n');

        return "redirect:/";
    }

    /**
     * Simulates loading data from a virtual address into memory.
     * @param address the address to load from
     * @param model the model to add attributes to for the view
     * @return the view name to redirect to
     */
    @PostMapping("/load")
    public String loadAddress(@RequestParam("loadAddress") int address, Model model) {
        // Simulate loading the address
        memoryManager.load(address);

        // Add the address to the model to display
        model.addAttribute("loadAddress", address);
        //LogResults.log("Loaded address: " + address);
        return "redirect:/";
    }

    /**
     * Simulates storing data at a specified virtual address.
     * @param address the address to store data at
     * @param data the data to store
     * @param model the model to add attributes to for the view
     * @return the view name to redirect to
     */
    @PostMapping("/store")
    public String storeAddress(@RequestParam("storeAddress") int address, @RequestParam("data") int data, Model model) {
        // Simulate storing the data at the given address
        memoryManager.store(address, data);

        // Add the address and data to the model to display
        model.addAttribute("storeAddress", address);
        model.addAttribute("storeData", data);
        //LogResults.log("Stored data " + data + " at address: " + address);
        return "redirect:/";
    }

    /**
     * Simulates allocating a page in memory.
     * @param pageNumber the page number to allocate
     * @param model the model to add attributes to for the view
     * @return the view name to redirect to
     */
    @PostMapping("/allocate")
    public String allocatePage(@RequestParam("allocatePage") int pageNumber, Model model) {
        // Simulate page allocation
        memoryManager.allocatePage(pageNumber);

        // Add the allocated page number to the model
        model.addAttribute("allocatePage", pageNumber);
        //LogResults.log("Allocated page number: " + pageNumber);
        return "redirect:/";
    }

    /**
     * Resets the simulation and clears all memory manager states.
     * @param model the model to reset attributes
     * @param status the session status to clear the session
     * @param redirectAttributes the attributes to redirect to the home page
     * @return the view name to redirect to
     */
    @PostMapping("/reset")
    public String resetSimulation(Model model, SessionStatus status, RedirectAttributes redirectAttributes) {
        // Print memory contents for debugging purposes
        memoryManager.printMemoryContents();

        // Reset the results and memory manager state
        Results.reset();
        memoryManager = new MemoryManager();
        status.setComplete();

        // Reset configuration variables to default values
        virtualAddressWidth = 0;
        virtualMemorySize = 0;
        pageTableSize = 0;
        tlbSize = 0;
        physicalMemorySize = 0;
        secondaryMemorySize = 0;
        pageSize = 0;
        int pageNumber = 0;

        // Add the default values to the model to reset the fields
        model.addAttribute("virtualAddressWidth", virtualAddressWidth);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("tlbSize", tlbSize);
        model.addAttribute("physicalMemorySize", physicalMemorySize);
        model.addAttribute("secondaryMemorySize", secondaryMemorySize);
        model.addAttribute("replacementAlgorithm", "FIFO");
        model.addAttribute("loadAddress", 0);
        model.addAttribute("storeAddress", 0);
        model.addAttribute("storeData", 0);
        model.addAttribute("virtualMemorySize", virtualMemorySize);
        model.addAttribute("pageTableSize", pageTableSize);
        model.addAttribute("allocatePage", pageNumber);

        // Log the reset action
        LogResults.log("\nSimulation reset.\n");

        // Redirect to the home page after resetting
        return "redirect:/";
    }
}
