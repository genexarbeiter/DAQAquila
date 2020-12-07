package de.tub.sense.daq.config.file;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author maxmeyer
 * @created 05/12/2020 - 16:35
 * @project DaqConfigurationLoader
 */

@Getter
@Setter
@Builder
public class C2monSettings {

    private String hostname;
    private String port;
    private String daqAddress;
    private String daqPort;

}
