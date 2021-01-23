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
        try {
            if (System.getProperties().containsKey("c2mon.daq.demo-config") && System.getProperty("c2mon.daq.demo-config").equals("true")) {
                daqConfigFileStream = new ClassPathResource("demo_config.yaml").getInputStream();
            } else {
                File daqConfigFile = new File("daq-config.yaml");
                daqConfigFileStream = new FileInputStream(daqConfigFile);
            }
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
        try {
            Yaml yaml = new Yaml(new Constructor(ConfigurationFile.class));
            configurationFile = yaml.load(daqConfigFileStream);
        } catch (Exception e) {
            configurationFile = new ConfigurationFile();
            log.error("Could not parse the yaml config to a valid ConfigurationFile object. " +
                    "There is something wrong in your config file. Only the configuration already on the c2mon " +
                    "server will be used. Nothing is updated.", e);
        }
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
