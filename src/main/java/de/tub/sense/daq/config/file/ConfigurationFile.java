package de.tub.sense.daq.config.file;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;

/**
 * @author maxmeyer
 * @created 05/12/2020 - 16:40
 * @project DaqConfigurationLoader
 */

@Getter
@Setter
@ToString
public class ConfigurationFile {

    private ModbusSettings modbusSettings;
    private C2monSettings c2monSettings;
    private ModelSettings modelSettings;
    private ArrayList<Signal> signals;
}
