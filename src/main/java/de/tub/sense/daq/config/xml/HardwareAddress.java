package de.tub.sense.daq.config.xml;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author maxmeyer
 * @created 18/12/2020 - 12:39
 * @project DAQConfigLoader
 */

@Getter
@Setter
@ToString
public class HardwareAddress {

    private String startAddress;
    private String valueCount;
    private String type;
    private String minValue;
    private String maxValue;
}
