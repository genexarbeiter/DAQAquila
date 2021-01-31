package de.tub.sense.daq.config;

import de.tub.sense.daq.config.file.ConfigurationFile;
import de.tub.sense.daq.config.file.GeneralSettings;
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
                daqConfigFileStream = new ClassPathResource("test_config.yaml").getInputStream();
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
            compareEnvironment();
        } catch (Exception e) {
            configurationFile = new ConfigurationFile();
            log.error("Could not parse the yaml config to a valid ConfigurationFile object. " +
                    "There is something wrong in your config file. Only the configuration already on the c2mon " +
                    "server will be used. Nothing is updated.", e);
        }
    }

    private void compareEnvironment() {
        GeneralSettings generalSettings = configurationFile.getGeneral();
        if (!System.getenv().containsKey("c2mon.daq.name")) {
            System.setProperty("c2mon.daq.name", generalSettings.getProcessName());
            log.debug("Using config process name value");
        } else {
            System.setProperty("c2mon.daq.name", System.getenv("c2mon.daq.name"));
            log.debug("Using environment process name value");
        }
        if (!System.getenv().containsKey("c2mon.client.jms.url")) {
            log.debug("Using config jms c2mon url value");
            System.setProperty("c2mon.client.jms.url", "failover:tcp://" + generalSettings.getC2monHost() + ":" + generalSettings.getC2monPort());
        } else {
            log.debug("Using environment jms c2mon url value");
            System.setProperty("c2mon.client.jms.url", System.getenv("c2mon.client.jms.url"));
        }
        if (!System.getenv().containsKey("c2mon.daq.jms.url")) {
            log.debug("Using config jms daq url value");
            System.setProperty("c2mon.daq.jms.url", "failover:tcp://" + generalSettings.getC2monHost() + ":" + generalSettings.getC2monPort());
        } else {
            log.debug("Using environment jms daq url value");
            System.setProperty("c2mon.daq.jms.url", System.getenv("c2mon.daq.jms.url"));
        }
        if (!System.getenv().containsKey("c2mon.daq.forceConfiguration")) {
            log.debug("Using config forceConfiguration value");
            System.setProperty("c2mon.daq.forceConfiguration", String.valueOf(generalSettings.isForceConfiguration()));
        } else {
            log.debug("Using environment forceConfiguration value");
            System.setProperty("c2mon.daq.forceConfiguration", System.getenv("c2mon.daq.forceConfiguration"));
        }
        if(!System.getenv().containsKey("c2mon.daq.performanceMode")) {
            log.debug("Using config performanceMode value");
            System.setProperty("c2mon.daq.performanceMode", String.valueOf(generalSettings.isPerformanceMode()));
        } else {
            log.debug("Using environment performanceMode value");
            System.setProperty("c2mon.daq.performanceMode", System.getenv("c2mon.daq.performanceMode"));
        }
    }

    /**
     * Get the loaded ConfigurationFile object
     *
     * @return the configuration
     */
    public ConfigurationFile getConfigurationFile() {
        if(daqConfigFileStream == null) {
            loadFile();
            return this.configurationFile;
        }
        if (configurationFile == null) {
            parseConfiguration();
        }
        return this.configurationFile;
    }


}
