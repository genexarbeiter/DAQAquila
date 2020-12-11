package de.tub.sense.daq.old.address.command;

import cern.c2mon.shared.common.datatag.address.impl.HardwareAddressImpl;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import de.tub.sense.daq.old.json.JacksonFactory;
import de.tub.sense.daq.modbus.protocols.ModbusFunctionFactory;
import org.simpleframework.xml.Element;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ModbusTcpCommandTagHardwareAddress extends HardwareAddressImpl implements IModbusTcpCommandTagHardwareAddress {

    @Element
    protected int startAddress;

    @Element
    protected int writeValueCount;

    @Element
    protected IModbusTcpCommandTagHardwareAddress.WRITE_TYPE writingType;

    protected ModbusTcpCommandTagHardwareAddress() {
    }

    @JsonCreator
    public ModbusTcpCommandTagHardwareAddress(@JsonProperty("startAddress") int startAddress, @JsonProperty("writeValueCount") int writeValueCount, @JsonProperty("writingType") IModbusTcpCommandTagHardwareAddress.WRITE_TYPE writingType) {
        this.startAddress = startAddress;
        this.writeValueCount = writeValueCount;
        this.writingType = writingType;
    }

    public final int getStartAddress() {
        return this.startAddress;
    }

    public final int getWriteValueCount() {
        return this.writeValueCount;
    }

    public final IModbusTcpCommandTagHardwareAddress.WRITE_TYPE getWritingType() {
        return this.writingType;
    }

    public void writeData(ModbusFunctionFactory socket, IModbusTcpCommandTagHardwareAddress.ModbusData data) throws Exception {
        assert socket != null;
        assert data != null;
        if ((data.getValue() != null && data.getValues() == null && getWriteValueCount() == 1) || (
                data.getValue() == null && data.getValues() != null && getWriteValueCount() == (data.getValues()).length)) {
            getWritingType().write(socket, Integer.valueOf(getStartAddress()), data);
        } else {
            throw new IllegalStateException("Number of values to be written does not match hardware address writing value count!");
        }
    }

    public String toJson() {
        try {
            return JacksonFactory.serialize(this);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
