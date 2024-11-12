package com.example.vms.controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VirtualMemorySimulatorApplication {
    public static void main(String[] args) {
//        int tlbSize = 4;
//        int physicalMemorySize = 128;
//        int diskSize = 256;
//        int pageSize = 16;
//        int virtualAddressWidth = 10;
//
//        ReplacementAlgorithm replacementAlgorithm = new FIFOReplacement();
//        MemoryManager memoryManager = new MemoryManager(virtualAddressWidth, tlbSize, pageSize, physicalMemorySize, diskSize, replacementAlgorithm);
//
//        Scanner scanner = new Scanner(System.in);
//        boolean running = true;
//
//        while (running) {
//            System.out.println("\nMemory Management Simulator");
//            System.out.println("1. Load from virtual address");
//            System.out.println("2. Store to virtual address");
//            System.out.println("3. Print memory contents");
//            System.out.println("4. Display simulation results");
//            System.out.println("5. Exit");
//            System.out.print("Enter your choice: ");
//            int choice = scanner.nextInt();
//
//            switch (choice) {
//                case 1:
//                    System.out.print("Enter virtual address to load: ");
//                    int loadAddress = scanner.nextInt();
//                    memoryManager.load(loadAddress);
//                    break;
//                case 2:
//                    System.out.print("Enter virtual address to store: ");
//                    int storeAddress = scanner.nextInt();
//                    System.out.print("Enter data to store: ");
//                    int data = scanner.nextInt();
//                    memoryManager.store(storeAddress, data);
//                    break;
//                case 3:
//                    memoryManager.printMemoryContents();
//                    break;
//                case 4:
//                    System.out.println("Simulation Results:");
//                    System.out.println("TLB hits: " + Results.tlbHit);
//                    System.out.println("TLB misses: " + Results.tlbMiss);
//                    System.out.println("Page table hits: " + Results.pageTableHit);
//                    System.out.println("Page table misses: " + Results.pageTableMiss);
//                    System.out.println("Disk reads: " + Results.diskRead);
//                    System.out.println("Disk writes: " + Results.diskWrite);
//                    System.out.println("Pages evicted: " + Results.pageEviction);
//                    break;
//                case 5:
//                    running = false;
//                    System.out.println("Exiting the simulator.");
//                    break;
//                default:
//                    System.out.println("Invalid choice. Please try again.");
//            }
//        }
//
//        scanner.close();

        SpringApplication.run(VirtualMemorySimulatorApplication.class, args);
    }
}
