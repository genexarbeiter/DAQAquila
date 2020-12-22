package de.tub.sense.daq.config.xml;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author maxmeyer
 * @created 18/12/2020 - 12:44
 * @project DAQConfigLoader
 */

@Getter
@Setter
@ToString
public class EquipmentAddress {

    private String host;
    private int port;
    private int unitId;
    private int delay;
    private String timeUnit;
}
