package com.example.vms.model;

public class Results {
    public static int tlbHit = 0, tlbMiss = 0, pageTableHit = 0, pageTableMiss = 0;
    public static int tlbAccesses = 0, pageTableAccesses = 0;
    public static int diskRead = 0, diskWrite = 0, pageEviction = 0;

    public static double getTLBHitRate() {
        return (double) tlbHit / tlbAccesses * 100;
    }

    public static double getTLBMissRate() {
        return (double) tlbMiss / tlbAccesses * 100;
    }

    public static double getPageTableHitRate() {
        return (double) pageTableHit / pageTableAccesses * 100;
    }

    public static double getPageTableMissRate() {
        return (double) pageTableMiss / pageTableAccesses * 100;
    }
    public static void reset(){
        tlbHit = 0;
        tlbMiss = 0;
        pageTableHit = 0;
        pageTableMiss = 0;
        diskRead = 0;
        diskWrite = 0;
        pageEviction = 0;
        tlbAccesses = 0;
        pageTableAccesses = 0;
    }
}
