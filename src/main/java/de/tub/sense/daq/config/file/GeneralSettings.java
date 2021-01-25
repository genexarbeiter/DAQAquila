package de.tub.sense.daq.config.file;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author maxmeyer
 * @created 25/01/2021 - 09:33
 * @project DAQConfigLoader
 */

@Getter
@Setter
@ToString
public class GeneralSettings {

    private String c2monHost;
    private int c2monPort;
    private String processName;
    private boolean forceConfiguration;
    private boolean performanceMode;
}
