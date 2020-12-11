package de.tub.sense.daq.config.file;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author maxmeyer
 * @created 05/12/2020 - 16:35
 * @project DaqConfigurationLoader
 */

@Getter
@Setter
@ToString
public class C2monSettings {

    private String clientAddress;
    private String clientPort;
    private String daqAddress;
    private String daqPort;

}
