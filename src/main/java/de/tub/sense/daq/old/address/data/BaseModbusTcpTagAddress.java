package de.tub.sense.daq.old.address.data;

import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.datatag.address.SimpleHardwareAddress;
import cern.c2mon.shared.common.datatag.address.impl.SimpleHardwareAddressImpl;
import de.tub.sense.daq.old.json.JacksonFactory;

import java.io.IOException;

/**
 * @author maxmeyer
 * @created 11/12/2020 - 13:18
 * @project DAQConfigLoader
 */


public abstract class BaseModbusTcpTagAddress extends DataTagAddress {
    public BaseModbusTcpTagAddress(IModbusTcpTagHardwareAddress hardwareAddress) {
        super((HardwareAddress)new SimpleHardwareAddressImpl(hardwareAddress.toJson()));
    }

    public static <T extends IModbusTcpTagHardwareAddress> T getModbusTcpTagHardwareAddress(SimpleHardwareAddress hardwareAddress, Class<T> deserializerClazz) throws IOException {
        return (T) JacksonFactory.deserialize(hardwareAddress.getAddress(), deserializerClazz);
    }
}
