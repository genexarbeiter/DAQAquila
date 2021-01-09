package de.tub.sense.daq.module;

import cern.c2mon.daq.common.DriverKernel;
import cern.c2mon.daq.config.DaqCoreModule;
import de.tub.sense.daq.modbus.ModbusTCPService;
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

    public static void main(String[] args) throws Exception {
        //Set environmental variables
        System.setProperty("c2mon.daq.name", "P_CINERGIA_EL_20_DEV6");
        System.setProperty("c2mon.client.jms.url", "failover:tcp://192.168.111.77:32302");
        System.setProperty("c2mon.daq.jms.url", "failover:tcp://192.168.111.77:32302");
        System.setProperty("c2mon.daq.jms.secondaryUrl", "failover:tcp://192.168.111.77:32302");
        //Debug
        System.setProperty("local.server.port", "32302");
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
     *
     * @param args from main method
     * @throws IOException on exception
     */
    public static synchronized void start(String[] args) throws IOException {
        if (application == null) {
            application = (new SpringApplicationBuilder(DAQMain.class)).bannerMode(Banner.Mode.OFF).build();
        }

        ConfigurableApplicationContext context = application.run(args);
        DriverKernel driverKernel = (DriverKernel) context.getBean(DriverKernel.class);

        //Dirty because the MessageHandler has to be instantiated by DriverKernel,
        // and there is no possibility to use Spring beans directly in the MessageHandler
        DAQMessageHandler.setModbusTCPService(context.getBean(ModbusTCPService.class));
        driverKernel.init();
        log.info("DAQ core is now initialized");
    }


}
