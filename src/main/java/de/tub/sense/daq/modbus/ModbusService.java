package de.tub.sense.daq.modbus;

import cern.c2mon.daq.common.IEquipmentMessageSender;
import de.tub.sense.daq.old.common.AbstractModbusTcpEndpointAddress;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.UnknownHostException;

/**
 * @author maxmeyer
 * @created 11/12/2020 - 14:24
 * @project DAQConfigLoader
 */

@Service
@Slf4j
public class ModbusService {

    public void createModbusTCPEndpoint(AbstractModbusTcpEndpointAddress modbusAddress,
                                        IEquipmentMessageSender messageSender,
                                        Logger equipmentLogger) throws UnknownHostException {
        ModbusEndpoint modbusEndpoint = new ModbusEndpoint(modbusAddress, messageSender, equipmentLogger);
    }

    @PostConstruct
    private void modbusTest() {

    }
}
