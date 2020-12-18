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
    private String port;
    private String unitId;
    private String delay;
    private String timeUnit;
}
