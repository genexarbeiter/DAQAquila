package de.tub.sense.daq.old.address.command;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.simpleframework.xml.Element;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ModbusTcpBitCommandTagHardwareAddress extends ModbusTcpCommandTagHardwareAddress {

    @Element
    protected int bitNumber;

    protected ModbusTcpBitCommandTagHardwareAddress() {
    }

    @JsonCreator
    public ModbusTcpBitCommandTagHardwareAddress(@JsonProperty("startAddress") int startAddress, @JsonProperty("writeValueCount") int writeValueCount, @JsonProperty("writingType") ModbusTcpCommandTagHardwareAddress.WRITE_TYPE writingType, @JsonProperty("bitNumber") int bitNumber) {
        super(startAddress, writeValueCount, writingType);
        this.bitNumber = bitNumber;
    }

    public final int getBitNumber() {
        return this.bitNumber;
    }
}
