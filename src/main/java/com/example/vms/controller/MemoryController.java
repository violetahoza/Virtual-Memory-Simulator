package com.example.vms.controller;

import com.example.vms.model.*;
import com.example.vms.utils.ConfigLoader;
import com.example.vms.utils.LogResults;
import com.example.vms.utils.Operation;
import com.example.vms.utils.SimulationConfig;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller that manages the simulation of a virtual memory system, including page replacement algorithms,
 * memory management, and simulation state reset.
 */
@Controller
@SessionAttributes({"address", "storeData"})
public class MemoryController {
    @Autowired
    private HttpSession session;
    private MemoryManager memoryManager;
    private String replacementAlgorithm = "FIFO";  // Store current algorithm for display
    private int virtualAddressWidth;
    private int pageSize;
    private int tlbSize;
    private int physicalMemorySize;
    private int secondaryMemorySize;
    private int virtualMemorySize;
    private int pageTableSize;
    private PageTable pageTable = null;
    private List<Integer> futureAccesses = new ArrayList<>(); // List of future memory accesses
    public static List<String> logMessages = new ArrayList<>(); // List to store log messages

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
                algorithm = new LRUReplacement(pageTable);
                break;
            case "NRU":
                algorithm = new NRUReplacement(pageTable);
                break;
            case "Optimal":
                algorithm = new OptimalReplacement();
                ((OptimalReplacement) algorithm).setFutureAccesses(futureAccesses);
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
                this.replacementAlgorithm = replacementAlgorithm;
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
        model.addAttribute("replacementAlgorithm", replacementAlgorithm);
        model.addAttribute("virtualMemorySize", virtualMemorySize);
        model.addAttribute("pageTableSize", pageTableSize);

        if (replacementAlgorithm.equalsIgnoreCase("Optimal")) {
            OptimalReplacement optimalReplacement = new OptimalReplacement();
            //OptimalReplacement optimalReplacement = (OptimalReplacement) memoryManager.getReplacementAlgorithm();
            memoryManager.getPageTable().updateFutureAccesses(optimalReplacement); // Update future accesses for the page table
        }

        // Memory data
        model.addAttribute("tlbEntries", memoryManager.getTlb().getEntries());
        model.addAttribute("mainMemory", memoryManager.getMainMemory().getMemory());
        model.addAttribute("diskEntries", memoryManager.getSecondaryStorage().getDisk());
        model.addAttribute("pageTableEntries", memoryManager.getPageTable().getPageTableContents());
        model.addAttribute("logMessages", logMessages);

        // Add default values for attributes that might not be set yet
        if (!model.containsAttribute("address"))
            model.addAttribute("address", 0);
//        if (!model.containsAttribute("storeOffset"))
//            model.addAttribute("storeOffset", 0);
//        if (!model.containsAttribute("storeVPN"))
//            model.addAttribute("storeVPN", 0);
        if (!model.containsAttribute("storeData"))
            model.addAttribute("storeData", 0);

        // Load configuration files from the configurations folder
        File configDirectory = new File("src/main/resources/configurations");
        if (configDirectory.exists() && configDirectory.isDirectory()) {
            String[] configFiles = configDirectory.list((dir, name) -> name.endsWith(".json")); // JSON config files
            if (configFiles != null) {
                model.addAttribute("configFiles", Arrays.asList(configFiles));
            }
        }

        return "index";
    }

    /**
     * Configures the simulation with the specified parameters and updates the memory manager.
     //* @param configFile the path to the configuration file
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
            //@RequestParam(value = "configFile", required = false) String configFile,
            @RequestParam(value = "virtualAddressWidth", required = false) Integer virtualAddressWidth,
            @RequestParam(value = "pageSize", required = false) int pageSize,
            @RequestParam(value = "tlbSize", required = false) int tlbSize,
            @RequestParam(value = "physicalMemorySize", required = false) int physicalMemorySize,
            @RequestParam(value = "secondaryMemorySize", required = false) int secondaryMemorySize,
            @RequestParam(value = "replacementAlgorithm", required = false) String replacementAlgorithm,
            Model model) {
        Results.logStats();
        logMessages.clear();
        Results.reset();

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
     * Loads the configuration from a selected file and initializes the memory manager.
     * @param configFile The name of the configuration file to load.
     * @param model The model to hold attributes for rendering on the view.
     * @return A redirection to the main page after configuration loading.
     */
    @GetMapping("/loadConfig")
    public String loadConfiguration(@RequestParam(value = "configFile", required = false) String configFile, Model model) {
        Results.logStats();
        logMessages.clear();
        Results.reset();
        if (configFile == null || configFile.isEmpty()) {
            logMessages.add("Error: Missing configFile parameter");
            return "redirect:/";
        }
        try {
            SimulationConfig selectedConfig = ConfigLoader.loadConfigFromFile("src/main/resources/configurations/" + configFile);
            if (selectedConfig != null) {
                if(selectedConfig.getReplacementAlgorithm().equalsIgnoreCase("Optimal"))
                {
                    // Parse futureAccesses from the configuration
                    List<Integer> configFutureAccesses = selectedConfig.getFutureAccesses();
                    futureAccesses = configFutureAccesses;
                }
                // Initialize the memory manager with the selected configuration
                initializeMemoryManager(
                        selectedConfig.getVirtualAddressWidth(),
                        selectedConfig.getPageSize(),
                        selectedConfig.getTlbSize(),
                        selectedConfig.getPhysicalMemorySize(),
                        selectedConfig.getSecondaryMemorySize(),
                        selectedConfig.getReplacementAlgorithm()
                );
                // Update the model attributes for rendering
                model.addAttribute("virtualAddressWidth", selectedConfig.getVirtualAddressWidth());
                model.addAttribute("pageSize", selectedConfig.getPageSize());
                model.addAttribute("tlbSize", selectedConfig.getTlbSize());
                model.addAttribute("physicalMemorySize", selectedConfig.getPhysicalMemorySize());
                model.addAttribute("secondaryMemorySize", selectedConfig.getSecondaryMemorySize());
                model.addAttribute("replacementAlgorithm", selectedConfig.getReplacementAlgorithm());
                model.addAttribute("virtualMemorySize", virtualMemorySize);
                model.addAttribute("pageTableSize", pageTableSize);
                // Store the operations list and initialize the current step in the session
                session.setAttribute("operations", selectedConfig.getOperations());
                session.setAttribute("currentStep", 0);  // Set the starting point for operations
                logMessages.add("Configuration loaded successfully from " + configFile);
            } else {
                logMessages.add("Error: Configuration file not found or invalid");
            }
        } catch (Exception e) {
            logMessages.add("Error loading configuration: " + e.getMessage());
        }
        return "redirect:/"; // Redirect back to the main page
    }

    /**
     * Executes the next operation in the sequence (Allocate, Load, Store).
     * Updates the current step in the session and handles page eviction if needed.
     * @param model The model to hold log messages and updates for rendering on the view.
     * @return A redirection to the main page after performing the operation.
     */
    @GetMapping("/nextOperation")
    public String nextOperation(Model model, RedirectAttributes redirectAttributes) {
        logMessages.clear();
        // Get operations and current step from the session
        List<Operation> operations = (List<Operation>) session.getAttribute("operations");
        Integer currentStep = (Integer) session.getAttribute("currentStep");
        // If operations are completed
        if (operations == null || currentStep == null || currentStep >= operations.size()) {
            logMessages.add("All operations are completed.");
            model.addAttribute("logMessages", logMessages);
            redirectAttributes.addFlashAttribute("highlightVpn", -1);
            redirectAttributes.addFlashAttribute("highlightOffset", -1);
            redirectAttributes.addFlashAttribute("operationType", null);
            redirectAttributes.addFlashAttribute("highlightAddress", -1);
            return "redirect:/";  // Redirect after all operations
        }
        // Get the current operation to perform
        Operation operation = operations.get(currentStep);

        redirectAttributes.addFlashAttribute("highlightVpn", operation.getVpn());
        redirectAttributes.addFlashAttribute("highlightOffset", operation.getOffset());
        redirectAttributes.addFlashAttribute("operationType", operation.getType());
        redirectAttributes.addFlashAttribute("highlightAddress", operation.getAddress());

        // Execute the operation
        switch (operation.getType()) {
            case "Allocate":
                memoryManager.allocatePage(operation.getVpn());
                // logMessages.add("Allocated page: " + operation.getVpn());
                break;
            case "Load":
                memoryManager.load(operation.getAddress());
                // logMessages.add("Loaded address: " + operation.getAddress());
                break;
            case "Store":
                int address = operation.getVpn() * pageSize + operation.getOffset();
                memoryManager.store(address, operation.getData());
                // logMessages.add("Stored data " + operation.getData() + " at address: " + address);
                break;
            default:
                logMessages.add("Unknown operation type: " + operation.getType());
        }
        // Update the current step in the session
        session.setAttribute("currentStep", currentStep + 1);
        // Add log messages to the model for the view
        model.addAttribute("logMessages", logMessages);
        return "redirect:/";  // Redirect back to the main page
    }

    /**
     * This endpoint previews the next operation in the sequence based on the current step.
     *
     * The method retrieves the current operation from the session attributes and provides a preview
     * of the next operation. If the current step is null, exceeds the size of the operations list,
     * or the operations list is not available, it returns a response indicating that all operations
     * are completed. Otherwise, it returns details of the next operation, including the VPN, offset,
     * operation type, address, and data.
     *
     * @return A map containing information about the next operation, including:
     *         - "completed": a boolean indicating whether all operations are completed (true if done, false if there is a next operation)
     *         - "vpn": the VPN value of the next operation (if available)
     *         - "offset": the offset value of the next operation (if available)
     *         - "type": the type of the next operation (if available)
     *         - "address": the address of the next operation (if available)
     *         - "data": the data associated with the next operation (if available)
     *
     *         If no operations are left to preview, the map will contain only the "completed" key set to true.
     */
    @GetMapping("/api/preview-next-operation")
    @ResponseBody
    public Map<String, Object> previewNextOperation() {
        Map<String, Object> preview = new HashMap<>();

        List<Operation> operations = (List<Operation>) session.getAttribute("operations");
        Integer currentStep = (Integer) session.getAttribute("currentStep");

        if (operations == null || currentStep == null || currentStep >= operations.size()) {
            preview.put("completed", true);
            return preview;
        }

        Operation nextOperation = operations.get(currentStep);
        preview.put("completed", false);
        preview.put("vpn", nextOperation.getVpn());
        preview.put("offset", nextOperation.getOffset());
        preview.put("type", nextOperation.getType());
        preview.put("address", nextOperation.getAddress());
        preview.put("data", nextOperation.getData());

        return preview;
    }

    /**
     * Simulates loading data from a virtual address into memory.
     * @param address the address to load from
     * @param model the model to add attributes to for the view
     * @return the view name to redirect to
     */
    @PostMapping("/load")
    public String loadAddress(@RequestParam("address") int address, Model model) {
        logMessages.clear();
        // Simulate loading the address
        memoryManager.load(address);
        // Add the address to the model to display
        model.addAttribute("address", address);
        // LogResults.log("Loaded address: " + address);
        return "redirect:/";
    }

    /**
     * Simulates storing data at a specified virtual address.
     * @param address the virtual address to store data at
     * @param data the data to store
     * @param model the model to add attributes to for the view
     * @return the view name to redirect to
     */
    @PostMapping("/store")
    public String storeAddress(@RequestParam("address") int address, @RequestParam("data") int data, Model model) {
        logMessages.clear();
        // Simulate storing the data at the given address
        //int address = vpn * pageSize + offset;
        memoryManager.store(address, data);

        // Add the address and data to the model to display
        model.addAttribute("address", address);
        model.addAttribute("storeData", data);
        // LogResults.log("Stored data " + data + " at address: " + address);
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
        logMessages.clear();
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
        //memoryManager.printMemoryContents();
        Results.logStats();
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
        logMessages.clear();

        // Add the default values to the model to reset the fields
        model.addAttribute("virtualAddressWidth", virtualAddressWidth);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("tlbSize", tlbSize);
        model.addAttribute("physicalMemorySize", physicalMemorySize);
        model.addAttribute("secondaryMemorySize", secondaryMemorySize);
        model.addAttribute("replacementAlgorithm", "FIFO");
        model.addAttribute("address", 0);
        model.addAttribute("storeData", 0);
        model.addAttribute("virtualMemorySize", virtualMemorySize);
        model.addAttribute("pageTableSize", pageTableSize);
        model.addAttribute("allocatePage", pageNumber);
        model.addAttribute("logMessages", logMessages);

        // Log the reset action
        LogResults.log("\nSimulation reset.\n");

        // Redirect to the home page after resetting
        return "redirect:/";
    }

    /**
     * Handles an HTTP GET request to retrieve memory-related data.
     * The response includes information about the TLB, main memory, disk, and page table contents.
     * @return A map containing:
     *         - "tlbEntries": The current contents of the Translation Lookaside Buffer.
     *         - "mainMemory": The contents of the main memory.
     *         - "diskEntries": The contents of the secondary storage.
     *         - "pageTableEntries": The current entries in the page table.
     */
    @GetMapping("/memoryData")
    @ResponseBody
    public Map<String, Object> getMemoryData() {
        Map<String, Object> data = new HashMap<>();
        data.put("tlbEntries", memoryManager.getTlb().getTLBContents());
        data.put("mainMemory", memoryManager.getMainMemory().getMemory());
        data.put("diskEntries", memoryManager.getSecondaryStorage().getDisk());
        data.put("pageTableEntries", memoryManager.getPageTable().getPageTableContents());

        return data;
    }

    /**
     * Handles an HTTP POST request to clear all log messages stored in the application.
     * After clearing the messages, the user is redirected to the index page.
     * @return A redirect string to the index page ("/").
     */
    @PostMapping("/clearMessages")
    public String clearMessages() {
        logMessages.clear(); // Clear the log messages
        return "redirect:/"; // Redirect to the index page
    }
}
