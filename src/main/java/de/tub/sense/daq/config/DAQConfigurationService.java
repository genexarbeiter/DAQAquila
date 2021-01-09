package de.tub.sense.daq.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tub.sense.daq.config.xml.EquipmentAddress;
import de.tub.sense.daq.config.xml.EquipmentUnit;
import de.tub.sense.daq.config.xml.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author maxmeyer
 * @created 09/01/2021 - 11:32
 * @project DAQConfigLoader
 */

@Service
@Slf4j
public class DAQConfigurationService {

    private final DAQConfiguration daqConfiguration;
    private final ProcessConfiguration processConfiguration;

    public DAQConfigurationService(DAQConfiguration daqConfiguration, ProcessConfiguration processConfiguration) {
        this.daqConfiguration = daqConfiguration;
        this.processConfiguration = processConfiguration;
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
