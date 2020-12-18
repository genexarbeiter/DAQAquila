package de.tub.sense.daq.config.xml;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;

/**
 * @author maxmeyer
 * @created 18/12/2020 - 12:38
 * @project DAQConfigLoader
 */

@Getter
@Setter
@ToString
public class ProcessConfiguration {

    private String name;
    private String processId;
    private String type;
    private String aliveTagId;
    private String aliveTagInterval;
    private String maxMessageSize;
    private String maxMessageDelay;
    private ArrayList<EquipmentUnit> equipmentUnits;

}
