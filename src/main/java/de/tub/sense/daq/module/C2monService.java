package de.tub.sense.daq.module;

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.service.ConfigurationService;
import cern.c2mon.client.core.service.TagService;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.tag.AliveTag;
import cern.c2mon.shared.client.configuration.api.tag.StatusTag;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * @author maxmeyer
 * @created 14/12/2020 - 11:34
 * @project DAQConfigLoader
 */

@Slf4j
@Service
public class C2monService {

    private final ConfigurationService configurationService;
    private final TagService tagService;

    public C2monService(ConfigurationService configurationService, TagService tagService) {
        this.configurationService = configurationService;
        this.tagService = tagService;
    }

    /**
     * Create a process on C2mon server
     *
     * @param processName of the process
     */
    public void createProcess(String processName) {
        if (processExists(processName)) {
            throw new IllegalArgumentException("Process with name " + processName + " already exists.");
        } else {
            ConfigurationReport report = configurationService.createProcess(processName);
            log.info("Received configuration report for process {}, with status {}, with status description {}.", report.getName(), report.getStatus().toString(), report.getStatusDescription());
        }
    }

    /**
     * Remove a process from the C2mon server
     *
     * @param processName of the process
     */
    public void removeProcess(String processName) {
        if (processExists(processName)) {
            configurationService.removeProcess(processName);
        } else {
            throw new IllegalArgumentException("No process with name " + processName + " found.");
        }
    }

    public void createEquipment(String equipmentName, String processName, String handlerClassName, int aliveTagInterval) {
        Equipment equipmentToCreate = Equipment.create(equipmentName, handlerClassName)
                .aliveTag(AliveTag.create(equipmentName + ":ALIVE").build(), aliveTagInterval)
                .statusTag(StatusTag.create(equipmentName + ":STATUS").build())
                .build();
        configurationService.createEquipment(processName, equipmentToCreate);
    }

    public void createEquipment(String equipmentName, String processName, String handlerClassName) {
        configurationService.createEquipment(equipmentName, processName, handlerClassName);
    }

    /**
     * Checks if a process already exists on the C2mon server
     *
     * @param processName you want to check
     * @return true if process exists false if not
     */
    public boolean processExists(@NonNull String processName) {
        return configurationService.getProcessNames().stream().anyMatch(process -> process.getProcessName().equals(processName));
    }

    /**
     * Get the XML configuration for a process name
     *
     * @param processName of the process
     * @return XML configuration as String
     */
    public String getProcessConfigurationFor(@NonNull String processName) {
        System.out.println(configurationService.getProcessXml(processName));
        return configurationService.getProcessXml(processName);
    }


    public long createDataTag(String equipmentName, String tagName, String datatype) {
        ConfigurationReport report = configurationService.createDataTag(equipmentName, equipmentName + "/" + tagName, dataTypeClass(datatype), new DataTagAddress());
        return report.getId();
    }

    public Tag getDataTag(long tagId) {
        return tagService.get(tagId);
    }

    public void updateTag(long tagId, Object value) {

    }

    public ArrayList<String> getAllProcesses() {
        ArrayList<String> processes = new ArrayList<>();
        configurationService.getProcessNames().forEach(processNameResponse -> processes.add(processNameResponse.getProcessName()));
        return processes;
    }

    private Class<?> dataTypeClass(String datatype) {
        switch (datatype) {
            case "bool":
                return Boolean.class;
            case "s8":
            case "u8":
                return Byte.class;
            case "s16":
            case "u16":
                return Short.class;
            case "u32":
            case "s32":
                return Integer.class;
            case "s64":
            case "u64":
                return Long.class;
            case "float32":
                return Float.class;
            case "float64":
                return Double.class;
            default:
                throw new IllegalArgumentException("Datatype " + datatype + " could not be converted to class");
        }
    }
}
