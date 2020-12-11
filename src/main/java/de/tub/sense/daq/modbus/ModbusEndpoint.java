package de.tub.sense.daq.modbus;

import cern.c2mon.daq.common.IEquipmentMessageSender;
import de.tub.sense.daq.old.common.AbstractModbusTcpEndpointAddress;
import de.tub.sense.daq.old.connection.ModbusTcpEndpoint;
import org.slf4j.Logger;

import java.net.UnknownHostException;

/**
 * @author maxmeyer
 * @created 11/12/2020 - 13:45
 * @project DAQConfigLoader
 */
public class ModbusEndpoint extends ModbusTcpEndpoint {

    public ModbusEndpoint(AbstractModbusTcpEndpointAddress address, IEquipmentMessageSender equipmentMessageSender, Logger equipmentLogger) throws UnknownHostException {
        super(address.getHost(), address.getPort(), address.getUnitID(), equipmentMessageSender, equipmentLogger);
        if(getEquipmentLogger().isDebugEnabled()) {
            getEquipmentLogger().debug("TCPModbusMasterConnection initial timeout value {}", getConnection().getTimeout());
        }
    }
}
