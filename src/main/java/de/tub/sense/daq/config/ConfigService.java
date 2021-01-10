package de.tub.sense.daq.config;

import de.tub.sense.daq.config.xml.EquipmentAddress;
import de.tub.sense.daq.config.xml.EquipmentUnit;
import de.tub.sense.daq.config.xml.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

/**
 * @author maxmeyer
 * @created 09/01/2021 - 11:32
 * @project DAQConfigLoader
 */

@Service
@Slf4j
public class ConfigService {

    private final DAQConfiguration daqConfiguration;
    private final ProcessConfiguration processConfiguration;

    public ConfigService(DAQConfiguration daqConfiguration, ProcessConfiguration processConfiguration) {
        this.daqConfiguration = daqConfiguration;
        this.processConfiguration = processConfiguration;
    }

    public ArrayList<EquipmentUnit> getEquipmentUnits() {
        return processConfiguration.getConfig().getEquipmentUnits();
    }

    //TODO Possible performance increase, when the Tags are cached
    public Optional<Tag> getTagFromTagId(long tagId) {
        for (EquipmentUnit equipmentUnit : processConfiguration.getConfig().getEquipmentUnits()) {
            for (Tag tag : equipmentUnit.getDataTags()) {
                if (tag.getId() == tagId) {
                    return Optional.of(tag);
                }
            }
            for (Tag tag : equipmentUnit.getCommandTags()) {
                if (tag.getId() == tagId) {
                    return Optional.of(tag);
                }
            }
        }
        log.warn("Tag with tagId {} not found", tagId);
        return Optional.empty();
    }

    public Optional<EquipmentAddress> getEquipmentAddress(long equipmentId) {
        for(EquipmentUnit equipmentUnit : processConfiguration.getConfig().getEquipmentUnits()) {
            if(equipmentUnit.getId() == equipmentId) {
                return Optional.of(equipmentUnit.getEquipmentAddress());
            }
        }
        log.warn("Equipment with equipmentId {} not found", equipmentId);
        return Optional.empty();
    }
}
