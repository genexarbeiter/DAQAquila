package de.tub.sense.daq.config.xml;

import lombok.*;

/**
 * @author maxmeyer
 * @created 18/12/2020 - 12:44
 * @project DAQConfigLoader
 */

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class EquipmentAddress {

    private String host;
    private int port;
    private int unitId;
    private int delay;
    private String timeUnit;

    public EquipmentAddress(String host, int port, int unitId) {
        this.host = host;
        this.port = port;
        this.unitId = unitId;
    }
}
