package de.tub.sense.daq.config;

import com.google.gson.internal.$Gson$Preconditions;
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

    private void configureC2mon() {
        ConfigurationFile configurationFile = configService.getConfigurationFile();
        configService.createProcess();
        for (Equipment equipment : configurationFile.getEquipments()) {
            ConnectionSettings connectionSettings = equipment.getConnectionSettings();
            configService.createEquipment(equipment.getName(), HANDLER_CLASS_NAME, equipment.getAliveTagInterval(),
                    connectionSettings.getAddress(), connectionSettings.getPort(), connectionSettings.getUnitID());
            for (Signal signal : equipment.getSignals()) {
                Modbus modbus = signal.getModbus();
                configService.createDataTag(equipment.getName(), signal.getName(), signal.getType(), modbus.getStartAddress(), modbus.getRegister(), modbus.getCount());
            }
        }
    }

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
            if(!equipmentExists) {
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

    private void updateEquipment(Equipment updatedEquipment, EquipmentUnit currentEquipmentUnit) {
        if(log.isDebugEnabled()) {
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
            if(!signalExists) {
                if (log.isDebugEnabled()) {
                    log.debug("Data tag {} not found. Creating a new Data tag...", signal.getName());
                }
                configService.createDataTag(currentEquipmentUnit.getName(), signal.getName(), signal.getType(),
                        signal.getModbus().getStartAddress(), signal.getModbus().getRegister(), signal.getModbus().getCount());
            }
        }

    }

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

    private void removeC2monConfiguration() {
        configService.removeProcessFromC2monEntirely();
    }
}
