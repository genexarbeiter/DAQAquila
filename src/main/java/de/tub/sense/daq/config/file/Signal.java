package de.tub.sense.daq.config.file;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author maxmeyer
 * @created 05/12/2020 - 16:29
 * @project DaqConfigurationLoader
 */

@Getter
@Setter
@ToString
public class Signal {

    private String name;
    private String type;
    private String description;
    private Modbus modbus;
    private long min;
    private long max;
    private double offset;
    private double multiplier;
    private double threshold;
}
