package com.example.vms.config;

import com.example.vms.model.MemoryManager;
import com.example.vms.model.Results;
import com.example.vms.model.FIFOReplacement;
import com.example.vms.model.LRUReplacement;
import com.example.vms.model.ReplacementAlgorithm;
import com.example.vms.model.OptimalReplacement;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MemoryController {

    private MemoryManager memoryManager;
    private String currentReplacementAlgorithm = "FIFO";  // Store current algorithm for display
    private int virtualAddressWidth;
    private int pageSize;
    private int tlbSize;
    private int pageTableSize;
    private int physicalMemorySize;
    private int secondaryMemorySize;

    public MemoryController() {
        initializeMemoryManager(10, 64, 2, 4, 128, 256, "FIFO");
    }

    private void initializeMemoryManager(int virtualAddressWidth, int pageSize, int tlbSize, int pageTableSize,
                                         int physicalMemorySize, int secondaryMemorySize, String replacementAlgorithm) {
        ReplacementAlgorithm algorithm;
        switch (replacementAlgorithm) {
            case "LRU":
                algorithm = new LRUReplacement();
                break;
            case "Optimal":
                algorithm = new OptimalReplacement();
                break;
            case "FIFO":
            default:
                algorithm = new FIFOReplacement();
                break;
        }

        int nrFrames = physicalMemorySize / pageSize;
        int maxDiskPages = secondaryMemorySize / pageSize;

        // Store configuration parameters
        this.virtualAddressWidth = virtualAddressWidth;
        this.pageSize = pageSize;
        this.tlbSize = tlbSize;
        this.pageTableSize = pageTableSize;
        this.physicalMemorySize = physicalMemorySize;
        this.secondaryMemorySize = secondaryMemorySize;
        this.currentReplacementAlgorithm = replacementAlgorithm;

        memoryManager = new MemoryManager(virtualAddressWidth, tlbSize, pageTableSize, nrFrames, maxDiskPages, pageSize, algorithm);
    }

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

        model.addAttribute("virtualAddressWidth", virtualAddressWidth);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("tlbSize", tlbSize);
        model.addAttribute("pageTableSize", pageTableSize);
        model.addAttribute("physicalMemorySize", physicalMemorySize);
        model.addAttribute("secondaryMemorySize", secondaryMemorySize);
        model.addAttribute("replacementAlgorithm", currentReplacementAlgorithm);
        model.addAttribute("virtualMemorySize", (int) Math.pow(2, virtualAddressWidth));

        return "index";
    }

    @PostMapping("/configure")
    public String configureSimulation(
            @RequestParam("virtualAddressWidth") int virtualAddressWidth,
            @RequestParam("pageSize") int pageSize,
            @RequestParam("tlbSize") int tlbSize,
            @RequestParam("pageTableSize") int pageTableSize,
            @RequestParam("physicalMemorySize") int physicalMemorySize,
            @RequestParam("secondaryMemorySize") int secondaryMemorySize,
            @RequestParam("replacementAlgorithm") String replacementAlgorithm) {

        // Initialize MemoryManager with user-configured parameters
        initializeMemoryManager(virtualAddressWidth, pageSize, tlbSize, pageTableSize, physicalMemorySize, secondaryMemorySize, replacementAlgorithm);
        return "redirect:/";
    }

    @PostMapping("/load")
    public String loadAddress(@RequestParam("loadAddress") int address) {
        memoryManager.load(address);
        return "redirect:/";
    }

    @PostMapping("/store")
    public String storeAddress(@RequestParam("storeAddress") int address, @RequestParam("data") int data) {
        memoryManager.store(address, data);
        return "redirect:/";
    }

    @PostMapping("/reset")
    public String resetSimulation() {
        Results.reset();
        memoryManager = null; // Clear memory manager, requiring reconfiguration
        return "redirect:/";
    }
}
