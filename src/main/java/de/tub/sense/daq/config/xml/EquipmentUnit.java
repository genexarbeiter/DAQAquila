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
public class EquipmentUnit {

    private long id;
    private String name;
    private EquipmentAddress equipmentAddress;
    private String handlerClassName;
    private ArrayList<DataTag> dataTags;
    private ArrayList<CommandTag> commandTags;

}
