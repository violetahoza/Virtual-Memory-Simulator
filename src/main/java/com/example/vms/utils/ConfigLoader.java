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
 * Class responsible for loading configuration files and fetching available configuration file names.
 * Provides methods for loading configuration files in JSON format and listing available configuration files.
 */
public class ConfigLoader {
    // Jackson ObjectMapper for reading and writing JSON
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Loads a configuration from the specified JSON file.
     * This method deserializes the contents of the JSON configuration file into a {@link SimulationConfig} object.
     *
     * @param configFile the path to the configuration file to load
     * @return the loaded {@link SimulationConfig} object
     * @throws IOException if there is an error reading the file or parsing the JSON
     */
    public static SimulationConfig loadConfigFromFile(String configFile) throws IOException {
        // Create a File object for the given configuration file
        File file = new File(configFile);

        // Deserialize the file content into a SimulationConfig object using Jackson's ObjectMapper
        return objectMapper.readValue(file, SimulationConfig.class);
    }

    /**
     * Retrieves a list of available configuration files in the configurations folder.
     * This method searches for all files with the ".json" extension in the "src/main/resources/configurations" directory.
     *
     * @return a list of filenames (with ".json" extension) of configuration files available in the folder
     */
    @GetMapping("/getConfigFiles")
    @ResponseBody
    public List<String> getConfigFiles() {
        // Define the folder containing the configuration files
        File folder = new File("src/main/resources/configurations");

        // Get the list of files in the folder that have a ".json" extension
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));

        // If files exist, return the list of filenames, else return an empty list
        return files != null ? Arrays.stream(files).map(File::getName).collect(Collectors.toList()) : new ArrayList<>();
    }
}
