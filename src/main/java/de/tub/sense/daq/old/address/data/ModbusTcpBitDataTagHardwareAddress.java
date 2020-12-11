package de.tub.sense.daq.old.address.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.simpleframework.xml.Element;

/**
 * @author maxmeyer
 * @created 11/12/2020 - 13:30
 * @project DAQConfigLoader
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModbusTcpBitDataTagHardwareAddress extends ModbusTcpDataTagHardwareAddress {
    @Element
    protected int bitNumber;

    protected ModbusTcpBitDataTagHardwareAddress() {
    }

    @JsonCreator
    public ModbusTcpBitDataTagHardwareAddress(@JsonProperty("startAddress") int startAddress, @JsonProperty("readValueCount") int readValueCount, @JsonProperty("readingType") ModbusTcpDataTagHardwareAddress.READ_TYPE readingType, @JsonProperty("bitNumber") int bitNumber) {
        super(startAddress, readValueCount, readingType);
        this.bitNumber = bitNumber;
    }

    public final int getBitNumber() {
        return this.bitNumber;
    }
}
