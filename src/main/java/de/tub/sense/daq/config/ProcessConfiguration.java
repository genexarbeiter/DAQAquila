package de.tub.sense.daq.config;

import de.tub.sense.daq.config.xml.ProcessConfigurationFile;
import org.springframework.context.annotation.Configuration;

/**
 * @author maxmeyer
 * @created 22/12/2020 - 12:07
 * @project DAQConfigLoader
 */

@Configuration
public class ProcessConfiguration {

    private final ProcessConfigurationParser parser;
    private boolean loaded = false;

    public ProcessConfiguration(ProcessConfigurationParser parser) {
        this.parser = parser;
    }

    public void init(String xml) {
        parser.parseConfiguration(xml);
        loaded = true;
    }

    public ProcessConfigurationFile getConfig() {
        return parser.getProcessConfigurationFile();
    }

    public boolean isLoaded() {
        return loaded;
    }
}
