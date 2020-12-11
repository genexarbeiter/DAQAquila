package de.tub.sense.daq.module;

import cern.c2mon.client.core.C2monServiceGateway;
import de.tub.sense.daq.modbus.ModbusService;
import de.tub.sense.daq.modbus.protocols.TcpModbusSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

/**
 * @author maxmeyer
 * @created 07/12/2020 - 17:49
 * @project DAQConfigLoader
 */

@Slf4j
@SpringBootApplication
public class DAQMain {

    public static void main(String[] args) throws Exception {
        System.setProperty("c2mon.daq.name", "TESTDAQ");
        System.setProperty("c2mon.client.jms.url", "failover:tcp://192.168.111.77:30203");
        System.setProperty("c2mon.daq.jms.url", "failover:tcp://192.168.111.77:30203");
        if (!loadEnvironment()) {
            return;
        }
        C2monServiceGateway.startC2monClientSynchronous();
        Runtime.getRuntime().addShutdownHook(new Thread(C2monServiceGateway::stopC2monClient));

        TcpModbusSocket tcpModbusSocket = new TcpModbusSocket("172.16.0.82", 502, 1);
        tcpModbusSocket.connect();
        System.out.println(tcpModbusSocket.readHoldingRegisters(13000).toString());
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