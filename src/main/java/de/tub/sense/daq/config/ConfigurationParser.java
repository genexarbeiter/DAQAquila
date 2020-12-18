package de.tub.sense.daq.config;

import de.tub.sense.daq.config.file.ConfigurationFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author maxmeyer
 * @created 05/12/2020 - 15:43
 * @project DaqConfigurationLoader
 */

@Slf4j
@Component
public class ConfigurationParser {

    private InputStream daqConfigFileStream;
    private ConfigurationFile configurationFile;

    public ConfigurationParser() {
        loadFile();
    }


    /**
     * Load the demo config file from classpath
     */
    private void loadFile() {
        //Just for development, later file is fetched from server
        try {
            daqConfigFileStream = new ClassPathResource("demo_config.yaml").getInputStream();
        } catch (IOException e) {
            log.warn("Failed to load demo configuration", e);
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
