package de.tub.sense.daq.config.xml;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author maxmeyer
 * @created 18/12/2020 - 12:39
 * @project DAQConfigLoader
 */

@Getter
@Setter
@ToString
@NoArgsConstructor
public class HardwareAddress {

    private int startAddress;
    private int valueCount;
    private String type;
    private double minValue;
    private double maxValue;
    private double offset;
    private double multiplier;
    private double threshold;

    public HardwareAddress(int startAddress, int valueCount, String type) {
        this.startAddress = startAddress;
        this.valueCount = valueCount;
        this.type = type;
    }

    public HardwareAddress(int startAddress, int valueCount, String type, double minValue, double maxValue) {
        this.startAddress = startAddress;
        this.valueCount = valueCount;
        this.type = type;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public static Optional<HardwareAddress> fromXMLString(String address) {
        HardwareAddress hardwareAddress = new HardwareAddress();
        ObjectMapper mapper = new ObjectMapper();
        try {
            HashMap<String, Object> map = mapper.readValue(address, HashMap.class);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                switch (entry.getKey()) {
                    case "startAddress":
                        hardwareAddress.setStartAddress((int) entry.getValue());
                        break;
                    case "writeValueCount":
                    case "readValueCount":
                        hardwareAddress.setValueCount((int) entry.getValue());
                        break;
                    case "readingType":
                    case "writingType":
                        hardwareAddress.setType(String.valueOf(entry.getValue()));
                        break;
                    case "minimalValue":
                        hardwareAddress.setMinValue(Double.parseDouble(entry.getValue().toString()));
                        break;
                    case "maximalValue":
                        hardwareAddress.setMaxValue(Double.parseDouble(entry.getValue().toString()));
                        break;
                    default:
                        break;
                }
            }
            return Optional.of(hardwareAddress);
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
