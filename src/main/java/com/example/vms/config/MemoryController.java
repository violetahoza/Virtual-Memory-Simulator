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
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    public MemoryController() {
        initializeMemoryManager(0, 0, 0,  0,  "FIFO");
    }

    private void initializeMemoryManager(int virtualAddressWidth, int pageSize, int tlbSize,
                                         int physicalMemorySize, String replacementAlgorithm) {
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
        if(virtualAddressWidth == 0 && pageSize == 0 && tlbSize == 0 && physicalMemorySize == 0)
            memoryManager = new MemoryManager();
        else{
            if(pageSize != 0) {
                // Store configuration parameters
                this.virtualAddressWidth = virtualAddressWidth;
                this.pageSize = pageSize;
                this.tlbSize = tlbSize;
                this.physicalMemorySize = physicalMemorySize;
                this.currentReplacementAlgorithm = replacementAlgorithm;
                this.virtualMemorySize = (int) Math.pow(2, virtualAddressWidth);
                this.pageTableSize = (int) (virtualMemorySize / pageSize);
                int totalPages = (int) (virtualMemorySize / pageSize);
                int nrFrames = (int) (physicalMemorySize / pageSize);
                this.secondaryMemorySize = (totalPages - nrFrames) * pageSize;

                memoryManager = new MemoryManager(virtualAddressWidth, tlbSize, pageSize, physicalMemorySize, algorithm);
            }
        }
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
        model.addAttribute("physicalMemorySize", physicalMemorySize);
        model.addAttribute("secondaryMemorySize", secondaryMemorySize);
        model.addAttribute("replacementAlgorithm", currentReplacementAlgorithm);
        model.addAttribute("virtualMemorySize", virtualMemorySize);
        model.addAttribute("pageTableSize", pageTableSize);

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

    @PostMapping("/configure")
    public String configureSimulation(
            @RequestParam("virtualAddressWidth") int virtualAddressWidth,
            @RequestParam("pageSize") int pageSize,
            @RequestParam("tlbSize") int tlbSize,
            @RequestParam("physicalMemorySize") int physicalMemorySize,
            @RequestParam("replacementAlgorithm") String replacementAlgorithm,
            Model model) {

        // Initialize MemoryManager with user-configured parameters
        initializeMemoryManager(virtualAddressWidth, tlbSize, pageSize, physicalMemorySize, replacementAlgorithm);
        memoryManager.printMemoryContents();

        // Update the model with the latest values to retain them in the form fields
        model.addAttribute("virtualAddressWidth", virtualAddressWidth);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("tlbSize", tlbSize);
        model.addAttribute("physicalMemorySize", physicalMemorySize);
        model.addAttribute("secondaryMemorySize", secondaryMemorySize);
        model.addAttribute("replacementAlgorithm", replacementAlgorithm);
        model.addAttribute("virtualMemorySize", virtualMemorySize);
        model.addAttribute("pageTableSize", pageTableSize);

        return "redirect:/";
    }

    @PostMapping("/load")
    public String loadAddress(@RequestParam("loadAddress") int address, Model model) {
        memoryManager.load(address);
        model.addAttribute("loadAddress", address);
        return "redirect:/";
    }

    @PostMapping("/store")
    public String storeAddress(@RequestParam("storeAddress") int address, @RequestParam("data") int data, Model model) {
        memoryManager.store(address, data);
        model.addAttribute("storeAddress", address);
        model.addAttribute("storeData", data);
        return "redirect:/";
    }

    @PostMapping("/reset")
    public String resetSimulation(Model model, SessionStatus status, RedirectAttributes redirectAttributes) {
        // Reset results and memory manager state
        Results.reset();
        memoryManager = new MemoryManager();
        status.setComplete();

        virtualAddressWidth = 0;
        virtualMemorySize = 0;
        pageTableSize = 0;
        tlbSize = 0;
        physicalMemorySize = 0;
        secondaryMemorySize = 0;
        pageSize = 0;

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

        // Redirect to the home page after resetting
        return "redirect:/";
    }
}
