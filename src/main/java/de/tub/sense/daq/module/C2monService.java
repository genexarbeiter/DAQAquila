package de.tub.sense.daq.module;

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.service.ConfigurationService;
import cern.c2mon.client.core.service.TagService;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.tag.AliveTag;
import cern.c2mon.shared.client.configuration.api.tag.StatusTag;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.datatag.address.impl.HardwareAddressImpl;
import cern.c2mon.shared.common.datatag.address.impl.SimpleHardwareAddressImpl;
import de.tub.sense.daq.config.ProcessConfiguration;
import de.tub.sense.daq.config.xml.CommandTag;
import de.tub.sense.daq.config.xml.DataTag;
import de.tub.sense.daq.config.xml.EquipmentUnit;
import de.tub.sense.daq.old.address.data.BaseModbusTcpTagAddress;
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
    private final ProcessConfiguration processConfiguration;
    private final TagService tagService;

    public C2monService(ConfigurationService configurationService, ProcessConfiguration processConfiguration, TagService tagService) {
        this.configurationService = configurationService;
        this.processConfiguration = processConfiguration;
        this.tagService = tagService;
    }

    public void reloadProcessConfiguration(String processName) {
        if (!processExists(processName)) {
            throw new IllegalArgumentException("Process with name " + processName + " does not exist.");
        } else {
            processConfiguration.init(configurationService.getProcessXml(processName));
        }
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

    public void removeProcessEntirely(String processName) {
        if(log.isDebugEnabled()) {
            log.debug("Removing process {} entirely", processName);
        }
        if (processExists(processName)) {
            reloadProcessConfiguration(processName);
            for(EquipmentUnit equipmentUnit : processConfiguration.getConfig().getEquipmentUnits()) {
                log.debug("Removing data tags");
                for(DataTag dataTag : equipmentUnit.getDataTags()) {
                    configurationService.removeDataTagById(dataTag.getId());
                }
                log.debug("Removing command tags");
                for(CommandTag commandTag : equipmentUnit.getCommandTags()) {
                    configurationService.removeCommandTagById(commandTag.getId());
                }
                log.debug("Removing equipment tags");
                configurationService.removeEquipmentById(equipmentUnit.getId());
            }
            configurationService.removeProcessById(processConfiguration.getConfig().getProcessId());
            if(log.isDebugEnabled()) {
                log.debug("Removed process {} entirely", processName);
            }
        } else {
            throw new IllegalArgumentException("No process with name " + processName + " found.");
        }
    }

    private void removeEquipment(String equipmentName) {
        configurationService.removeEquipment(equipmentName);
    }

    public void createEquipment(String equipmentName, String processName, String handlerClassName, int aliveTagInterval, String host, long port, int unitID) {
        if(log.isDebugEnabled()) {
            log.debug("Creating equipment {} for process {} with handlerClass {}", equipmentName, processName, handlerClassName);
        }

        Equipment equipmentToCreate = Equipment.create(equipmentName, handlerClassName)
                .aliveTag(AliveTag.create(equipmentName + ":ALIVE").build(), aliveTagInterval)
                .statusTag(StatusTag.create(equipmentName + ":STATUS").build())
                .address("{\"host\":\"" + host +"\",\"port\":" + port + ",\"unitID\":"+ unitID + "}")
                .build();

        ConfigurationReport report = configurationService.createEquipment(processName, equipmentToCreate);
        for(ConfigurationElementReport elementReport : report.getElementReports()) {
            if(elementReport.isFailure()) {
                log.warn("Action {} of entity {} failed with: {}", elementReport.getAction(), elementReport.getEntity(), elementReport.getStatusMessage());
            } else if(elementReport.isSuccess() && log.isDebugEnabled()) {
                log.debug("Action {} of entity {} succeeded", elementReport.getAction(), elementReport.getEntity());
            }
        }
        if(report.getStatus().equals(ConfigConstants.Status.FAILURE)) {
            log.warn("Creating equipment failed with status description: {}", report.getStatusDescription());
        } else if(report.getStatus().equals(ConfigConstants.Status.RESTART) && log.isDebugEnabled()) {
            log.debug("Creating equipment success with status description: {}", report.getStatusDescription());
        }
    }

    public void createEquipment(String equipmentName, String processName, String handlerClassName, String host, long port, int unitId) {
        createEquipment(equipmentName, processName, handlerClassName, 100000, host, port, unitId);
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
        return configurationService.getProcessXml(processName);
    }


    public void createDataTag(String equipmentName, String tagName, String datatype, int startAddress, String addressType, int valueCount) {
        SimpleHardwareAddressImpl simpleHardwareAddress
                = new SimpleHardwareAddressImpl("{\"startAddress\":" + startAddress +",\"readValueCount\":" + valueCount + ",\"readingType\":\""+ addressType + "\"}");
        configurationService.createDataTag(equipmentName, equipmentName + "/" + tagName, dataTypeClass(datatype), new DataTagAddress(simpleHardwareAddress));
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
