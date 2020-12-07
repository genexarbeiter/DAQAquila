package de.tub.sense.daq.config;

import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.service.ConfigurationService;
import de.tub.sense.daq.config.file.DAQConfiguration;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author maxmeyer
 * @created 05/12/2020 - 15:43
 * @project DaqConfigurationLoader
 */

@Service
public class ConfigurationInitializer {

    private final ConfigurationService configurationService;
    private final ConfigurationParser parser;

    public ConfigurationInitializer(ConfigurationParser configurationParser) {
        this.parser = configurationParser;
        this.configurationService = C2monServiceGateway.getConfigurationService();
    }

    @PostConstruct
    private void init() {
        DAQConfiguration configuration = parser.getDaqConfiguration();

        System.out.println(configuration.toString());
        /* String hostName = "";
        String processName = "";

        if (isProcessConfigured(processName)) {
            return;
        }

        configurationService.createProcess(processName);
        configurationService.createEquipment(processName, hostName, "");
         */
    }

    private boolean isProcessConfigured(String processName) {
        return configurationService.getProcessNames().stream().anyMatch(p -> p.getProcessName().equals(processName));
    }
}
