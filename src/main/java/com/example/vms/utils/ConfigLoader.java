package com.example.vms.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class responsible for loading configuration files.
 */
public class ConfigLoader {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Loads all available configuration files from a directory.
     * @param configDir the directory where configuration files are stored
     * @return a list of configuration file names
     * @throws IOException if there's an error reading the directory or files
     */
    public static List<String> loadConfigFiles(String configDir) throws IOException {
        File directory = new File(configDir);
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IOException("Directory not found: " + configDir);
        }
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            return Arrays.stream(files).map(File::getName).collect(Collectors.toList());
        } else {
            throw new IOException("No configuration files found in: " + configDir);
        }
    }

    /**
     * Loads a configuration from the specified JSON file.
     * @param configFile the path to the configuration file
     * @return the loaded SimulationConfig object
     * @throws IOException if there's an error while reading the file
     */
    public static SimulationConfig loadConfigFromFile(String configFile) throws IOException {
        File file = new File(configFile);
        return objectMapper.readValue(file, SimulationConfig.class);
    }

    @GetMapping("/getConfigFiles")
    @ResponseBody
    public List<String> getConfigFiles() {
        File folder = new File("src/main/resources/configurations");
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));
        return files != null ? Arrays.stream(files).map(File::getName).collect(Collectors.toList()) : new ArrayList<>();
    }

}
