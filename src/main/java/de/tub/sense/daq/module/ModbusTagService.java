package de.tub.sense.daq.module;

import cern.c2mon.client.core.service.TagService;
import org.springframework.stereotype.Service;

/**
 * @author maxmeyer
 * @created 07/12/2020 - 18:15
 * @project DAQConfigLoader
 */

@Service
public class ModbusTagService {

    private final TagService tagService;

    public ModbusTagService(TagService tagService) {
        this.tagService = tagService;
    }

    public static void createTag(String equipment, String tag, Class c, String address) {

    }
}
