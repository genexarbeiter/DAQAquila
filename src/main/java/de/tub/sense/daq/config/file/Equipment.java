package de.tub.sense.daq.config.file;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;

/**
 * @author maxmeyer
 * @created 10/01/2021 - 10:21
 * @project DAQConfigLoader
 */
@Getter
@Setter
@ToString
public class Equipment {

    private String name;
    private String type;
    private int interval;
    private int aliveTagInterval;
    private ConnectionSettings connectionSettings;
    private ArrayList<Signal> signals;
}
