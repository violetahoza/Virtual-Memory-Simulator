Virtual Memory Simulator
Description
The Virtual Memory Simulator is an educational application designed to illustrate the key aspects of virtual memory management, specifically focusing on the paging process. This project simulates the mechanisms involved in virtual memory, including page tables, address translation, and page replacement algorithms. It serves as a practical tool for understanding how virtual memory operates in modern computer systems.

Features
Page Location Simulation: Handle memory access requests to determine if the requested page is in main memory (RAM) or needs to be fetched from secondary storage (disk), demonstrating page hits and page faults.
Address Translation: Utilize a Memory Management Unit (MMU) to translate virtual addresses into physical addresses using page tables and a Translation Lookaside Buffer (TLB).
Page Loading Simulation: Emulate loading pages into main memory during page faults, managing memory constraints effectively.
Page Replacement Mechanisms: Demonstrate various page replacement algorithms, such as FIFO (First-In-First-Out) and LRU (Least Recently Used), to manage memory when it becomes full.
User -Friendly Interface: A graphical user interface to visualize key operations like memory accesses, page faults, and page replacements.
Customizable Settings: Allow users to configure physical memory size, page size, virtual address bits, and select page replacement strategies.
Performance Metrics: Provide feedback on memory access performance, including the number of page hits, page faults, and the efficiency of different page replacement strategies.
Technologies Used
Java 11 or higher
Spring Boot
JavaScript
HTML + CSS
