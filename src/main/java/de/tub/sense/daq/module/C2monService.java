package de.tub.sense.daq.module;

import cern.c2mon.client.core.service.ConfigurationService;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.tag.AliveTag;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author maxmeyer
 * @created 14/12/2020 - 11:34
 * @project DAQConfigLoader
 */

@Slf4j
@Service
public class C2monService {

    private final ConfigurationService configurationService;

    public C2monService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    /**
     * Create a process on C2mon server
     * @param processName of the process
     */
    public void createProcess(String processName) {
        if (processExists(processName)) {
            throw new IllegalArgumentException("Process with name " + processName + " not found.");
        } else {
            ConfigurationReport report = configurationService.createProcess(processName);
            log.info("Received configuration report for process {}, with status {}, with status description {}.", report.getName(), report.getStatus().toString(), report.getStatusDescription());
        }
    }

    /**
     * Remove a process from the C2mon server
     * @param processName of the process
     */
    public void removeProcess(String processName) {
        if(processExists(processName)) {
            configurationService.removeProcess(processName);
        } else {
            throw new IllegalArgumentException("No process with name " + processName + " found.");
        }
    }

    public void createEquipment(String equipmentName, String processName, String handlerClassName, int aliveTagInterval) {
        Equipment equipmentToCreate = Equipment.create(equipmentName, handlerClassName)
                .aliveTag(AliveTag.create(equipmentName + ":ALIVE").build(), aliveTagInterval)
                .build();

        configurationService.createEquipment(processName, equipmentToCreate);
    }

    public void createEquipment(String equipmentName, String processName, String handlerClassName) {
        configurationService.createEquipment(equipmentName, processName, handlerClassName);
    }

    /**
     * Checks if a process already exists on the C2mon server
     * @param processName you want to check
     * @return true if process exists false if not
     */
    public boolean processExists(@NonNull String processName) {
        return configurationService.getProcessNames().stream().anyMatch(process -> process.getProcessName().equals(processName));
    }

    /**
     * Get the XML configuration for a process name
     * @param processName of the process
     * @return XML configuration as String
     */
    public String getProcessConfigurationFor(@NonNull String processName) {
        return configurationService.getProcessXml(processName);
    }
}
