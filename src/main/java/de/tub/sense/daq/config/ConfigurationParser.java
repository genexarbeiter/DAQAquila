package de.tub.sense.daq.config;

import de.tub.sense.daq.config.file.DAQConfiguration;
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
    private DAQConfiguration daqConfiguration;

    public ConfigurationParser() {
        loadFile();
    }


    private void loadFile() {
        //Just for development, later file is fetched from server
        try {
            daqConfigFileStream = new ClassPathResource("demo_config.yaml").getInputStream();
        } catch (IOException e) {
            log.warn("Failed to load demo configuration", e);
        }
        parseConfiguration();
    }

    private void parseConfiguration() {
        //Yaml yaml = new Yaml(new Constructor(DAQConfiguration.class));
        //daqConfiguration = yaml.load(daqConfigFileStream);
    }

    public DAQConfiguration getDaqConfiguration() {
        if(daqConfiguration == null) {
            parseConfiguration();
        }
        return this.daqConfiguration;
    }


}
