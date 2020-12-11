package de.tub.sense.daq.old;

import de.tub.sense.daq.old.common.AbstractModbusTcpEndpointAddress;
import de.tub.sense.daq.old.connection.ModbusTcpEndpoint;

public interface ModbusTcpEndpointFactory {
    ModbusTcpEndpoint createEndpoint(AbstractModbusTcpEndpointAddress paramAbstractModbusTcpEndpointAddress);
}
