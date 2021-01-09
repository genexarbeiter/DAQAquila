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
import cern.c2mon.shared.common.datatag.address.impl.SimpleHardwareAddressImpl;
import de.tub.sense.daq.config.DAQConfigurationService;
import de.tub.sense.daq.config.xml.CommandTag;
import de.tub.sense.daq.config.xml.DataTag;
import de.tub.sense.daq.config.xml.EquipmentUnit;
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

    //C2mon services
    private final ConfigurationService configurationService;
    private final TagService tagService;

    //Custom services
    private final DAQConfigurationService daqConfigurationService;

    public C2monService(ConfigurationService configurationService, TagService tagService, DAQConfigurationService daqConfigurationService) {
        this.configurationService = configurationService;
        this.tagService = tagService;
        this.daqConfigurationService = daqConfigurationService;
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
            if(log.isDebugEnabled()) {
                log.debug("Received configuration report for process {}, with status {}, with status description {}.", report.getName(), report.getStatus().toString(), report.getStatusDescription());
            }
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

    /**
     * Remove a process and its equipments
     * @param processName of the process
     */
    public void removeProcessEntirely(String processName) {
        if (log.isDebugEnabled()) {
            log.debug("Removing process {} entirely", processName);
        }
        if (processExists(processName)) {
            for (EquipmentUnit equipmentUnit : daqConfigurationService.getEquipmentUnits()) {
                log.debug("Removing data tags");
                for (DataTag dataTag : equipmentUnit.getDataTags()) {
                    configurationService.removeDataTagById(dataTag.getId());
                }
                log.debug("Removing command tags");
                for (CommandTag commandTag : equipmentUnit.getCommandTags()) {
                    configurationService.removeCommandTagById(commandTag.getId());
                }
                log.debug("Removing equipment tags");
                configurationService.removeEquipmentById(equipmentUnit.getId());
            }
            configurationService.removeProcess(processName);
            if (log.isDebugEnabled()) {
                log.debug("Removed process {} entirely", processName);
            }
        } else {
            throw new IllegalArgumentException("No process with name " + processName + " found.");
        }
    }

    /**
     * Create a equipment for an existing process
     * @param equipmentName of the equipment
     * @param processName of the process
     * @param handlerClassName of the message handler for the equipment
     * @param aliveTagInterval to send an alive tag in millis
     * @param host of the equipment
     * @param port of the equipment
     * @param unitId of the equipment
     */
    public void createEquipment(String equipmentName, String processName, String handlerClassName, int aliveTagInterval, String host, long port, int unitId) {
        if (log.isDebugEnabled()) {
            log.debug("Creating equipment {} for process {} with handlerClass {}", equipmentName, processName, handlerClassName);
        }
        Equipment equipmentToCreate = Equipment.create(equipmentName, handlerClassName)
                .aliveTag(AliveTag.create(equipmentName + ":ALIVE").build(), aliveTagInterval)
                .statusTag(StatusTag.create(equipmentName + ":STATUS").build())
                .address("{\"host\":\"" + host + "\",\"port\":" + port + ",\"unitID\":" + unitId + "}")
                .build();

        ConfigurationReport report = configurationService.createEquipment(processName, equipmentToCreate);
        for (ConfigurationElementReport elementReport : report.getElementReports()) {
            if (elementReport.isFailure()) {
                log.warn("Action {} of entity {} failed with: {}", elementReport.getAction(), elementReport.getEntity(), elementReport.getStatusMessage());
            } else if (elementReport.isSuccess() && log.isDebugEnabled()) {
                log.debug("Action {} of entity {} succeeded", elementReport.getAction(), elementReport.getEntity());
            }
        }
        if (report.getStatus().equals(ConfigConstants.Status.FAILURE)) {
            log.warn("Creating equipment failed with status description: {}", report.getStatusDescription());
        } else if (report.getStatus().equals(ConfigConstants.Status.RESTART) && log.isDebugEnabled()) {
            log.debug("Creating equipment success with status description: {}", report.getStatusDescription());
        }
    }

    /**
     * Create a equipment for an existing process
     * @param equipmentName of the equipment
     * @param processName of the process
     * @param handlerClassName of the message handler for the equipment
     * @param host of the equipment
     * @param port of the equipment
     * @param unitId of the equipment
     */
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

    /**
     * Create a data tag for a given equipment
     * @param equipmentName of the equipment
     * @param tagName of the tag
     * @param datatype of the tag
     * @param startAddress of the related register
     * @param addressType of the related register
     * @param valueCount of the related register
     */
    public void createDataTag(String equipmentName, String tagName, String datatype, int startAddress, String addressType, int valueCount) {
        SimpleHardwareAddressImpl simpleHardwareAddress
                = new SimpleHardwareAddressImpl("{\"startAddress\":" + startAddress + ",\"readValueCount\":" + valueCount + ",\"readingType\":\"" + addressType + "\"}");
        configurationService.createDataTag(equipmentName, equipmentName + "/" + tagName, dataTypeClass(datatype), new DataTagAddress(simpleHardwareAddress));
    }

    /**
     * Get a data tag from its id
     * @param tagId of the tag
     * @return corresponding data tag
     */
    public Tag getDataTag(long tagId) {
        return tagService.get(tagId);
    }

    /**
     * Get the names of all running processes on the C2mon
     * @return list of process names
     */
    public ArrayList<String> getAllProcesses() {
        ArrayList<String> processes = new ArrayList<>();
        configurationService.getProcessNames().forEach(processNameResponse -> processes.add(processNameResponse.getProcessName()));
        return processes;
    }

    /**
     * Convert a data type string to the corresponding data type class
     * @param datatype as string
     * @return datatype as class
     */
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

    //TODO to allow force configuration, the yaml config has to be changed
    public void forceConfiguration(String processName) {
        if (processExists(processName)) {
            removeProcessEntirely(processName);
        }
        createProcess(processName);
        /*
        createEquipment(equipmentName, processName, DAQMessageHandler.class.getName(),
                daqConfiguration.getConfiguration().getModbusSettings().getAddress(),
                daqConfiguration.getConfiguration().getModbusSettings().getPort(),
                daqConfiguration.getConfiguration().getModbusSettings().getUnitID());
        for (Signal signal : daqConfiguration.getConfiguration().getSignals()) {
            if (signal.getModbus().getType().equals("read")) {
                c2monService.createDataTag(equipmentName, signal.getName(),
                        signal.getType(), signal.getModbus().getStartAddress(),
                        signal.getModbus().getRegister(), signal.getModbus().getCount());
            }
        }*/
    }
}
