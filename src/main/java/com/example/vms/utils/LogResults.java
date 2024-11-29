package com.example.vms.utils;

import com.example.vms.controller.MemoryController;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A utility class for logging messages to a file and maintaining the log within the simulation.
 * This class writes messages to a log file ("log.txt") and also adds them to the `logMessages` list in the `MemoryController` class.
 */
public class LogResults {
    public static final File fileInit = new File("log.txt"); //define  a file to store logs
    public static FileWriter file = null; // FileWriter to write logs to the file

    // Static block to initialize the FileWriter and create the log file, because I want the initialization to happen only one time + thread safety
    static {
        try {
            file = new FileWriter("log.txt");
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Logs a message to the log file and the MemoryController's logMessages list.
     * This method ensures thread-safety by synchronizing access to the log file and the logMessages list.
     * @param message the message to be logged
     */
    public static synchronized void log(String message) {
        try {
            file.write(message + '\n'); // write the message to the log file
            file.flush();
            MemoryController.logMessages.add(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the FileWriter and releases any resources associated with it.
     * This method should be called when logging is complete to ensure resources are properly cleaned up.
     */
    public static synchronized void close() {
        try {
            if(file != null)
                file.close(); // close the FileWriter
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
