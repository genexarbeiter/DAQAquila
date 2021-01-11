package de.tub.sense.daq.config;

import de.tub.sense.daq.config.file.*;
import de.tub.sense.daq.config.xml.*;
import de.tub.sense.daq.module.DAQMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * @author maxmeyer
 * @created 10/01/2021 - 11:01
 * @project DAQConfigLoader
 */

@Slf4j
@Component
public class ConfigInitializer implements CommandLineRunner {

    private static final String HANDLER_CLASS_NAME = DAQMessageHandler.class.getName();
    private static boolean FORCE_CONFIGURATION;
    private final ConfigService configService;

    public ConfigInitializer(ConfigService configService) {
        this.configService = configService;
        FORCE_CONFIGURATION = System.getProperty("c2mon.daq.forceConfiguration") != null
                && Boolean.parseBoolean(System.getProperty("c2mon.daq.forceConfiguration"));
    }

    /**
     * Run by Spring on start up, checks if the C2mon server is configured properly for the DAQ, if not it configures the
     * C2mon server to work with the DAQ. If c2mon.daq.forceConfiguration is true, the DAQ overwrites any settings for
     * the process of the DAQ and configures it with the config file, if false the configuration on the C2mon server is
     * only updated, and nothing is deleted.
     *
     * @param args from start
     * @throws Exception on exception
     */
    @Override
    public void run(String... args) throws Exception {
        if (loadConfigFromServer()) {
            log.debug("Configuration loaded from C2mon. Checking if force configuration is set to true...");
            if (FORCE_CONFIGURATION) {
                log.debug("TRUE! Forcing reconfiguration...");
                removeC2monConfiguration();
                configureC2mon();
                log.debug("Finished reconfiguration");
            } else {
                log.debug("FALSE! Only updating configuration...");
                updateC2monConfiguration();
                log.debug("Finished updating configuration.");
            }
        } else {
            log.debug("Configuration is not yet on C2mon server. Configuring...");
            configureC2mon();
        }
    }

    /**
     * Checks if there is a configuration for the DAQ on the C2mon server. If there is one it loads it if the
     * configuration is not already loaded
     *
     * @return true if the configuration is available, false if not
     */
    private boolean loadConfigFromServer() {
        log.debug("Loading config from C2mon...");
        log.debug("Checking if process already exists...");
        if (configService.processExists()) {
            if (!configService.isC2monConfigurationLoaded()) {
                configService.reloadC2monConfiguration();
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Configures the C2mon from config file. If this method is started, no configuration may yet be performed on the C2mon server!
     */
    private void configureC2mon() {
        ConfigurationFile configurationFile = configService.getConfigurationFile();
        configService.createProcess();
        for (Equipment equipment : configurationFile.getEquipments()) {
            ConnectionSettings connectionSettings = equipment.getConnectionSettings();
            configService.createEquipment(equipment.getName(), HANDLER_CLASS_NAME, equipment.getAliveTagInterval(),
                    connectionSettings.getAddress(), connectionSettings.getPort(), connectionSettings.getUnitID());
            for (Signal signal : equipment.getSignals()) {
                Modbus modbus = signal.getModbus();
                configService.createDataTag(equipment.getName(), signal.getName(), signal.getType(),
                        modbus.getStartAddress(), modbus.getRegister(), modbus.getCount(),
                        signal.getOffset(), signal.getMultiplier(), signal.getThreshold());
            }
        }
    }

    /**
     * Updates the C2mon configuration from config file. The method can only be run,
     * if there is already a process for this DAQ configured on the C2mon server.
     */
    private void updateC2monConfiguration() {
        log.debug("Updating C2mon configuration...");
        ConfigurationFile configurationFile = configService.getConfigurationFile();
        ArrayList<EquipmentUnit> equipmentUnits = configService.getEquipmentUnits();
        for (Equipment equipment : configurationFile.getEquipments()) {
            boolean equipmentExists = false;
            for (EquipmentUnit equipmentUnit : equipmentUnits) {
                if (equipmentUnit.getName().equals(equipment.getName())) {
                    equipmentExists = true;
                    updateEquipment(equipment, equipmentUnit);
                }
            }
            if (!equipmentExists) {
                if (log.isDebugEnabled()) {
                    log.debug("Equipment {} not found. Creating a new equipment...", equipment.getName());
                }
                int aliveTagInterval = equipment.getAliveTagInterval() == 0 ? 100000 : equipment.getAliveTagInterval();
                configService.createEquipment(
                        equipment.getName(),
                        HANDLER_CLASS_NAME,
                        aliveTagInterval,
                        equipment.getConnectionSettings().getAddress(),
                        equipment.getConnectionSettings().getPort(),
                        equipment.getConnectionSettings().getUnitID());
            }
        }
    }

    /**
     * Updates an equipment. Needs the updated equipment from config file, and the current corresponding
     * equipment unit from the C2mon server
     *
     * @param updatedEquipment     from the config file
     * @param currentEquipmentUnit from the C2mon server
     */
    private void updateEquipment(Equipment updatedEquipment, EquipmentUnit currentEquipmentUnit) {
        if (log.isDebugEnabled()) {
            log.debug("Updating equipment {} with id {}...", updatedEquipment.getName(), currentEquipmentUnit.getId());
        }
        currentEquipmentUnit.setEquipmentAddress(new EquipmentAddress(
                updatedEquipment.getConnectionSettings().getAddress(),
                updatedEquipment.getConnectionSettings().getPort(),
                updatedEquipment.getConnectionSettings().getUnitID()));
        currentEquipmentUnit.setAliveTagInterval(updatedEquipment.getAliveTagInterval() != 0 ?
                updatedEquipment.getAliveTagInterval() : currentEquipmentUnit.getAliveTagInterval());
        configService.updateEquipment(currentEquipmentUnit);

        for (Signal signal : updatedEquipment.getSignals()) {
            boolean signalExists = false;
            for (DataTag dataTag : currentEquipmentUnit.getDataTags()) {
                if (dataTag.getName().equals(updatedEquipment.getName() + "/" + signal.getName())) {
                    signalExists = true;
                    updateSignal(signal, dataTag);
                }
            }
            //TODO Do it also for command tags
            if (!signalExists) {
                if (log.isDebugEnabled()) {
                    log.debug("Data tag {} not found. Creating a new Data tag...", signal.getName());
                }
                configService.createDataTag(currentEquipmentUnit.getName(), signal.getName(), signal.getType(),
                        signal.getModbus().getStartAddress(), signal.getModbus().getRegister(), signal.getModbus().getCount(),
                        signal.getOffset(), signal.getMultiplier(), signal.getThreshold());
            }
        }

    }

    /**
     * Updates a signal. Needs the updated signal from config file, and the current corresponding
     * data tag from the C2mon server
     *
     * @param signal  from the config file
     * @param dataTag from the C2mon server
     */
    private void updateSignal(Signal signal, DataTag dataTag) {
        if (log.isDebugEnabled()) {
            log.debug("Updating data tag {} with id {}...", signal.getName(), dataTag.getId());
        }
        dataTag.setDataType(signal.getType());
        dataTag.setAddress(new HardwareAddress(signal.getModbus().getStartAddress(), signal.getModbus().getCount(), signal.getModbus().getRegister()));
        configService.updateDataTag(dataTag);
    }

    //TODO code it
    private void updateSignal(Signal signal, CommandTag commandTag) {

    }

    /**
     * Removes the configuration for the DAQ entirely from the C2mon server
     */
    private void removeC2monConfiguration() {
        configService.removeProcessFromC2monEntirely();
    }
}
