package de.tub.sense.daq.config.file;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * @author maxmeyer
 * @created 05/12/2020 - 16:32
 * @project DaqConfigurationLoader
 */

@Getter
@ToString
public class Modbus {

    private ModbusType type;
    private String register;
    private long startAddress;

}
