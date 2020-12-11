package de.tub.sense.daq.old.address.data;

import cern.c2mon.shared.common.datatag.address.impl.HardwareAddressImpl;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import de.tub.sense.daq.old.json.JacksonFactory;
import de.tub.sense.daq.modbus.protocols.ModbusFunctionFactory;
import org.simpleframework.xml.Element;

/**
 * @author maxmeyer
 * @created 11/12/2020 - 13:26
 * @project DAQConfigLoader
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class ModbusTcpDataTagHardwareAddress extends HardwareAddressImpl implements IModbusTcpDataTagHardwareAddress {
    @Element
    protected int startAddress;

    @Element
    protected int readValueCount;

    @Element
    protected IModbusTcpDataTagHardwareAddress.READ_TYPE readingType;

    protected ModbusTcpDataTagHardwareAddress() {}

    @JsonCreator
    public ModbusTcpDataTagHardwareAddress(@JsonProperty("startAddress") int startAddress, @JsonProperty("readValueCount") int readValueCount, @JsonProperty("readingType") IModbusTcpDataTagHardwareAddress.READ_TYPE readingType) {
        this.startAddress = startAddress;
        this.readValueCount = readValueCount;
        this.readingType = readingType;
    }

    public final int getStartAddress() {
        return this.startAddress;
    }

    public final int getReadValueCount() {
        return this.readValueCount;
    }

    public final IModbusTcpDataTagHardwareAddress.READ_TYPE getReadingType() {
        return this.readingType;
    }

    public IModbusTcpDataTagHardwareAddress.ModbusData readData(ModbusFunctionFactory socket) throws Exception {
        assert socket != null;
        return getReadingType().read(socket, getStartAddress(), getReadValueCount());
    }

    public String toJson() {
        try {
            return JacksonFactory.serialize(this);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
