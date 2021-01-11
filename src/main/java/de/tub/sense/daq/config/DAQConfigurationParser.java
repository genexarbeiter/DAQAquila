package de.tub.sense.daq.config;

import de.tub.sense.daq.config.file.ConfigurationFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author maxmeyer
 * @created 05/12/2020 - 15:43
 * @project DaqConfigurationLoader
 */

@Slf4j
@Component
public class DAQConfigurationParser {

    private InputStream daqConfigFileStream;
    private ConfigurationFile configurationFile;

    public DAQConfigurationParser() {
        loadFile();
    }


    /**
     * Load the demo config file from classpath
     */
    private void loadFile() {
        //Just for development, later file is fetched from server
        try {
            File daqConfigFile = new File("daq-config.yaml");
            daqConfigFileStream = new FileInputStream(daqConfigFile);
            //daqConfigFileStream = new ClassPathResource("demo_config2.yaml").getInputStream();
        } catch (IOException e) {
            log.error("Failed to load yaml configuration. " +
                    "How to solve it: In Docker volume root the file 'daq-config.yaml' has to be placed.", e);
            throw new RuntimeException("Could not load DAQ configuration file.");
        }
        parseConfiguration();
    }

    /**
     * Parse the configuration file to ConfigurationFile object
     */
    private void parseConfiguration() {
        Yaml yaml = new Yaml(new Constructor(ConfigurationFile.class));
        configurationFile = yaml.load(daqConfigFileStream);
    }

    /**
     * Get the loaded ConfigurationFile object
     *
     * @return the configuration
     */
    public ConfigurationFile getConfigurationFile() {
        if (configurationFile == null) {
            parseConfiguration();
        }
        return this.configurationFile;
    }



}
