package de.tub.sense.daq.module;

import cern.c2mon.daq.DaqStartup;
import cern.c2mon.daq.config.DaqCoreModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * @author maxmeyer
 * @created 07/12/2020 - 17:49
 * @project DAQConfigLoader
 */

@Slf4j
@SpringBootApplication(scanBasePackages = {"de.tub.sense.daq"},
        exclude = {JmsAutoConfiguration.class, ActiveMQAutoConfiguration.class, DataSourceAutoConfiguration.class}
)
@Import(DaqCoreModule.class)
public class DAQMain {

    public static void main(String[] args) throws Exception {
        //Set environmental variables (only for development)
        System.setProperty("c2mon.daq.name", "P_CINERGIA_EL_20_DEV6");
        System.setProperty("c2mon.client.jms.url", "failover:tcp://192.168.111.77:32302");
        System.setProperty("c2mon.daq.jms.url", "failover:tcp://192.168.111.77:32302");
        System.setProperty("c2mon.daq.jms.secondaryUrl", "failover:tcp://192.168.111.77:32302");

        if (!loadEnvironment()) {
            return;
        }

        DaqStartup.main(args);
    }

    /**
     * Validate that the environment variables are present, if not prevent default and log a warning.
     */
    private static boolean loadEnvironment() {
        if (!System.getProperties().containsKey("c2mon.daq.name")) {
            log.error("Missing environment variable 'c2mon.daq.name'");
            return false;
        }
        if (!System.getProperties().containsKey("c2mon.client.jms.url")) {
            log.error("Missing environment variable 'c2mon.client.jms.url'");
            return false;
        }
        if (!System.getProperties().containsKey("c2mon.daq.jms.url")) {
            log.error("Missing environment variable 'c2mon.daq.jms.url'");
            return false;
        }
        return true;
    }

}
