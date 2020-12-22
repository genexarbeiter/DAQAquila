package de.tub.sense.daq.config.xml;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @author maxmeyer
 * @created 18/12/2020 - 12:38
 * @project DAQConfigLoader
 */

@Getter
@Setter
@ToString
public class Tag {
    private long id;
    private String name;
    private HardwareAddress address;
}
