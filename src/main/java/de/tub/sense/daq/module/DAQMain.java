package de.tub.sense.daq.module;

import cern.c2mon.client.core.C2monServiceGateway;
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

    private static SpringApplication application = null;
    private static ConfigurableApplicationContext context = null;
    private static DriverKernel driverKernel;

    public static void main(String[] args) throws Exception {
        //Set environmental variables
        System.setProperty("c2mon.daq.name", "TESTDAQ");
        System.setProperty("c2mon.client.jms.url", "failover:tcp://192.168.111.77:30203");
        System.setProperty("c2mon.daq.jms.url", "failover:tcp://192.168.111.77:30203");

        //Debug
        System.setProperty("local.server.port", "30203");
        System.setProperty("server.address", "192.168.111.77");
        if (!loadEnvironment()) {
            return;
        }
        start(args);
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

    /**
     * Start a new DAQ application
     * @param args from main method
     * @throws IOException on exception
     */
    public static synchronized void start(String[] args) throws IOException {
        String daqName = System.getProperty("c2mon.daq.name");
        /*if (daqName == null) {
            throw new RuntimeException("Please specify the DAQ process name using 'c2mon.daq.name'");
        } else {

            if (application == null) {
                application = (new SpringApplicationBuilder(DAQMain.class)).bannerMode(Banner.Mode.OFF).build();
            }

            context = application.run(args);
            driverKernel = context.getBean(DriverKernel.class);
            driverKernel.init();
            context.registerShutdownHook();
            log.info("DAQ core is now initialized");
        }*/
        C2monServiceGateway.startC2monClient();
    }

    /**
     * Stop the DAQ process gently
     */
    public static synchronized void stop() {
        try {
            log.info("Stopping DAQ process...");
            if (driverKernel != null) {
                driverKernel.shutdown();
            }

            if (context.isRunning()) {
                context.close();
            }
        } catch (Exception e) {
            log.error("Error occurred whilst gradually stopping DAQ process", e);
        }

    }


}
