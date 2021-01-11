package de.tub.sense.daq.module;

import cern.c2mon.daq.common.DriverKernel;
import cern.c2mon.daq.config.DaqCoreModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

import java.io.IOException;

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

    private static SpringApplication application;

    public static void main(String[] args) throws Exception {
        //Set environmental variables (only for development)
        /*
        System.setProperty("c2mon.daq.name", "P_CINERGIA_EL_20_DEV11");
        System.setProperty("c2mon.client.jms.url", "failover:tcp://192.168.111.77:32302");
        System.setProperty("c2mon.daq.jms.url", "failover:tcp://192.168.111.77:32302");
        System.setProperty("c2mon.daq.jms.secondaryUrl", "failover:tcp://192.168.111.77:32302");
        System.setProperty("c2mon.daq.forceConfiguration", "false");
        System.setProperty("c2mon.daq.refreshDelay", "100");*/
        if (!loadEnvironment()) {
            return;
        }

        start(args);
    }

    /**
     * Validate that the environment variables are present, if not prevent default and log a warning.
     */
    private static boolean loadEnvironment() {
        if (!System.getenv().containsKey("c2mon.daq.name")) {
            log.error("Missing environment variable 'c2mon.daq.name'");
            return false;
        } else {
            System.setProperty("c2mon.daq.name", System.getenv("c2mon.daq.name"));
        }
        if (!System.getenv().containsKey("c2mon.client.jms.url")) {
            log.error("Missing environment variable 'c2mon.client.jms.url'");
            return false;
        } else {
            System.setProperty("c2mon.client.jms.url", System.getenv("c2mon.client.jms.url"));
        }
        if (!System.getenv().containsKey("c2mon.daq.jms.url")) {
            log.error("Missing environment variable 'c2mon.daq.jms.url'");
            return false;
        } else {
            System.setProperty("c2mon.daq.jms.url", System.getenv("c2mon.daq.jms.url"));
        }
        if (!System.getenv().containsKey("c2mon.daq.forceConfiguration")) {
            log.info("Missing environment variable 'forceConfiguration'. Setting it to default value: 'false'");
            System.setProperty("c2mon.daq.forceConfiguration", "false");
        } else {
            System.setProperty("c2mon.daq.forceConfiguration", System.getenv("c2mon.daq.forceConfiguration"));
        }
        if(!System.getenv().containsKey("c2mon.daq.refreshDelay")) {
            log.info("Missing environment variable 'c2mon.daq.refreshDelay'. Setting it to default value: '1000ms'");
            System.setProperty("c2mon.daq.refreshDelay", "1000");
        } else {
            System.setProperty("c2mon.daq.refreshDelay", System.getenv("c2mon.daq.refreshDelay"));
        }
        if(!System.getenv().containsKey("c2mon.daq.performanceMode")) {
            log.info("Missing environment variable 'c2mon.daq.performanceMode'. Setting it to default value: 'true'");
            System.setProperty("c2mon.daq.performanceMode", "true");
        } else {
            System.setProperty("c2mon.daq.performanceMode", System.getenv("c2mon.daq.performanceMode"));
        }
        return true;
    }

    /**
     * Start a new DAQ application
     *
     * @param args from main method
     * @throws IOException on exception
     */
    public static synchronized void start(String[] args) throws IOException {
        if (application == null) {
            application = (new SpringApplicationBuilder(DAQMain.class)).bannerMode(Banner.Mode.OFF).build();
        }

        ConfigurableApplicationContext context = application.run(args);
        DriverKernel driverKernel = context.getBean(DriverKernel.class);
        driverKernel.init();
    }


}
