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
        //Set environmental variables (only for development in idea)
        /*System.setProperty("c2mon.daq.name", "DAQTEST_PROCESS_CINERGIA_4");
        System.setProperty("c2mon.client.jms.url", "failover:tcp://192.168.111.77:32302");
        System.setProperty("c2mon.daq.jms.url", "failover:tcp://192.168.111.77:32302");
        System.setProperty("c2mon.daq.jms.secondaryUrl", "failover:tcp://192.168.111.77:32302");
        System.setProperty("c2mon.daq.forceConfiguration", "false");


        if (!loadEnvironment()) {
            return;
        }
        */

        checkDemoConfig();
        start(args);
        //TODO Implement input(non write) and coil(bool) register
        //TODO Test all data types with all register types
    }

    /**
     * Validate that the environment variables are present, if not prevent default and log a warning.
     */
    private static void checkDemoConfig() {
        if(!System.getenv().containsKey("c2mon.daq.demoConfig")) {
            System.setProperty("c2mon.daq.demo-config", "false");
        } else {
            System.setProperty("c2mon.daq.demo-config", System.getenv("c2mon.daq.demoConfig"));
        }
        //System.setProperty("c2mon.daq.demo-config","true");
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
