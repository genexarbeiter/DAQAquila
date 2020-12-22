package de.tub.sense.daq.module;

import de.tub.sense.daq.config.DAQConfiguration;
import de.tub.sense.daq.config.file.Signal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author maxmeyer
 * @created 14/12/2020 - 14:40
 * @project DAQConfigLoader
 */

@Component
@Slf4j
public class TestRunner implements CommandLineRunner {

    private final C2monService c2monService;
    private final DAQConfiguration daqConfiguration;

    public TestRunner(C2monService c2monService, DAQConfiguration daqConfiguration) {
        this.c2monService = c2monService;
        this.daqConfiguration = daqConfiguration;
    }

    @Override
    public void run(String... args) throws Exception {
        //Print all running processes
        StringBuilder sb = new StringBuilder("Processes running on C2mon: ");
        c2monService.getAllProcesses().forEach(processName -> sb.append(processName).append(" "));
        log.info(sb.toString());

        String processName = daqConfiguration.getConfiguration().getModelSettings().getProcessName();
        String equipmentName = daqConfiguration.getConfiguration().getModelSettings().getEquipmentName();
        if (c2monService.processExists(processName)) {
            c2monService.removeProcessEntirely(processName);
        }
        c2monService.createProcess(processName);
        c2monService.createEquipment(equipmentName, processName, "de.tub.sense.daq.DAQMessageHandler",
                daqConfiguration.getConfiguration().getModbusSettings().getAddress(),
                daqConfiguration.getConfiguration().getModbusSettings().getPort(),
                daqConfiguration.getConfiguration().getModbusSettings().getUnitID());
        for (Signal signal : daqConfiguration.getConfiguration().getSignals()) {
            if (signal.getModbus().getType().equals("read")) {
                c2monService.createDataTag(equipmentName, signal.getName(),
                        signal.getType(), signal.getModbus().getStartAddress(),
                        signal.getModbus().getRegister(), signal.getModbus().getCount());
            }
        }
        c2monService.reloadProcessConfiguration(processName);
    }
}
