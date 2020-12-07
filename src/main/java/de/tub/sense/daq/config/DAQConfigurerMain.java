package de.tub.sense.daq.config;

import cern.c2mon.daq.DaqStartup;
import org.springframework.context.annotation.PropertySource;

import java.io.IOException;

/**
 * @author maxmeyer
 * @created 07/12/2020 - 14:28
 * @project DAQConfigLoader
 */

@PropertySource("classpath:c2mon_client.properties")
public class DAQConfigurerMain extends DaqStartup {

    public static void main(String[] args) throws IOException {
        /*ConfigurationParser parser = new ConfigurationParser();
        DAQConfiguration daqConfiguration = parser.getDaqConfiguration();*/
        System.setProperty("c2mon.client.jms.url", "failover:tcp://192.168.111.77:30203");
        System.setProperty("c2mon.daq.jms.url", "failover:tcp://192.168.111.77:30203");

        System.setProperty("c2mon.daq.name", "P_CINERGIA_EL_20_81_AC");
        System.out.println("Running...");
        start(args);
    }

}
