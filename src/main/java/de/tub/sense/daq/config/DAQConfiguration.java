package de.tub.sense.daq.config;

import de.tub.sense.daq.config.file.ConfigurationFile;
import org.springframework.context.annotation.Configuration;

/**
 * @author maxmeyer
 * @created 13/12/2020 - 14:29
 * @project DAQConfigLoader
 */

@Configuration
public class DAQConfiguration {

    private final ConfigurationParser parser;

    public DAQConfiguration(ConfigurationParser parser) {
        this.parser = parser;
    }

    public ConfigurationFile getConfiguration() {
        return parser.getConfigurationFile();
    }


}
