package de.tub.sense.daq.config.file;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author maxmeyer
 * @created 05/12/2020 - 16:38
 * @project DaqConfigurationLoader
 */

@Getter
@Setter
@ToString
public class ModbusSettings {

    private String address;
    private int port;
    private int unitID;

}
