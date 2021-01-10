package de.tub.sense.daq.config.file;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author maxmeyer
 * @created 05/12/2020 - 16:27
 * @project DaqConfigurationLoader
 */

@Getter
@Setter
@ToString
public class ModelSettings {

    private String modelName;
    private String modelVersion;
    private String processName;
}
