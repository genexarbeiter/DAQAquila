package de.tub.sense.daq.module;

import cern.c2mon.client.common.tag.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author maxmeyer
 * @created 14/12/2020 - 14:40
 * @project DAQConfigLoader
 */

@Component
@Slf4j
public class TestRunner implements CommandLineRunner {

    private final C2monService c2monService;

    public TestRunner(C2monService c2monService) {
        this.c2monService = c2monService;
    }

    @Override
    public void run(String... args) throws Exception {
        String process = "P_TestProcess_7";
        String equipment = "E_Testequipment_9";


        /*c2monService.createProcess(process);*/
        c2monService.createEquipment(equipment, process, "de.tub.sense.daq.module.DAQMessageHandler", 20000);
        /*
        long tagId = c2monService.createDataTag(equipment, "testtag", "s32");
        Tag tag = c2monService.getDataTag(tagId);
        System.out.println("Tag " + tag.getName() + " has the value " + tag.getValue().toString());
        log.info("Finished");*/
    }
}
