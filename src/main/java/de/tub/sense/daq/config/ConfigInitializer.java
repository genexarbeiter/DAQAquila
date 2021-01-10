package de.tub.sense.daq.config;

import de.tub.sense.daq.config.file.*;
import de.tub.sense.daq.module.DAQMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author maxmeyer
 * @created 10/01/2021 - 11:01
 * @project DAQConfigLoader
 */

@Slf4j
@Component
public class ConfigInitializer implements CommandLineRunner {

    private final ConfigService configService;
    private static boolean FORCE_CONFIGURATION;
    private static final String HANDLER_CLASS_NAME = DAQMessageHandler.class.getName();

    public ConfigInitializer(ConfigService configService) {
        this.configService = configService;
        FORCE_CONFIGURATION = System.getProperty("c2mon.daq.forceConfiguration") != null
                && Boolean.parseBoolean(System.getProperty("c2mon.daq.forceConfiguration"));
    }

    @Override
    public void run(String... args) throws Exception {
        if(loadConfigFromServer()) {
            log.debug("Configuration loaded from C2mon. Checking if force configuration is set to true...");
            if(FORCE_CONFIGURATION) {
                log.debug("Forcing reconfiguration...");
                removeC2monConfiguration();
                configureC2mon();
                log.debug("Finished");
            } else {
                //TODO Add new data tags / equipments
            }
        } else {
            log.debug("Configuration is not yet on C2mon server. Configuring...");
            configureC2mon();
        }
    }

    private boolean loadConfigFromServer() {
        log.debug("Loading config from C2mon...");
        log.debug("Checking if process already exists...");
        if(configService.processExists()) {
            if(!configService.isC2monConfigurationLoaded()) {
                configService.reloadC2monConfiguration();
            }
            return true;
        } else {
            return false;
        }
    }

    private void configureC2mon()  {
        ConfigurationFile configurationFile = configService.getConfigurationFile();
        configService.createProcess();
        for(Equipment equipment : configurationFile.getEquipments()) {
            ConnectionSettings connectionSettings = equipment.getConnectionSettings();
            configService.createEquipment(equipment.getName(), HANDLER_CLASS_NAME, equipment.getAliveTagInterval(),
                    connectionSettings.getAddress(), connectionSettings.getPort(), connectionSettings.getUnitID());
            for(Signal signal : equipment.getSignals()) {
                Modbus modbus = signal.getModbus();
                configService.createDataTag(equipment.getName(), signal.getName(), signal.getType(), modbus.getStartAddress(), modbus.getRegister(), modbus.getCount());
            }
        }
    }

    private void removeC2monConfiguration() {
        configService.removeProcessFromC2monEntirely();
    }
}
