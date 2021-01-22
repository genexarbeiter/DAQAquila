package de.tub.sense.daq.config.xml;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
@Slf4j
public class EquipmentAddress {

    private String host;
    private int port;
    private int unitId;
    private int delay;
    private int refreshInterval;
    private String timeUnit;

    public EquipmentAddress(String host, int port, int unitId, int refreshInterval) {
        this.host = host;
        this.port = port;
        this.unitId = unitId;
        this.refreshInterval = refreshInterval;
    }

    public static Optional<EquipmentAddress> parseEquipmentAddress(String address) {
        EquipmentAddress equipmentAddress = new EquipmentAddress();
        ObjectMapper mapper = new ObjectMapper();
        try {
            HashMap<String, Object> map = mapper.readValue(address, HashMap.class);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                switch (entry.getKey()) {
                    case "host":
                        equipmentAddress.setHost(String.valueOf(entry.getValue()));
                        break;
                    case "port":
                        equipmentAddress.setPort((int) entry.getValue());
                        break;
                    case "unitID":
                        equipmentAddress.setUnitId((int) entry.getValue());
                        break;
                    case "delay":
                        equipmentAddress.setDelay((int) entry.getValue());
                        break;
                    case "timeUnit":
                        equipmentAddress.setTimeUnit(String.valueOf(entry.getValue()));
                        break;
                    case "refreshInterval":
                        equipmentAddress.setRefreshInterval((int) entry.getValue());
                        break;
                    default:
                        log.warn("Unrecognized equipment address key: {}", entry.getKey());
                        break;
                }
            }
            return Optional.of(equipmentAddress);
        } catch (IOException e) {
            log.warn("Could not parse equipment address from string", e);
            return Optional.empty();
        }
    }
}
