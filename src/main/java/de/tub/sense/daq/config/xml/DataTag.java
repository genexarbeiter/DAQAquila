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
public class DataTag extends Tag {

    private String dataType;
    private HardwareAddress address;
}
