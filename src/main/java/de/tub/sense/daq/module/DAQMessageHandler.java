package de.tub.sense.daq.module;

import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
import de.tub.sense.daq.config.DAQConfiguration;
import de.tub.sense.daq.modbus.TcpModbusSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

/**
 * @author maxmeyer
 * @created 11/12/2020 - 15:13
 * @project DAQConfigLoader
 */

@Slf4j
@Service
public class DAQMessageHandler extends EquipmentMessageHandler implements CommandLineRunner {

    private final DAQConfiguration configuration;
    private TcpModbusSocket tcpModbusSocket;

    public DAQMessageHandler(DAQConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Perform the necessary tasks to connect to the underlying data source. The
     * handler is expected to be ready to publish data as soon as this method
     * returns.
     */
    @Override
    public void connectToDataSource() {
        String host = configuration.getConfiguration().getModbusSettings().getAddress();
        int port = configuration.getConfiguration().getModbusSettings().getPort();
        int unitId = configuration.getConfiguration().getModbusSettings().getUnitID();


        try {
            tcpModbusSocket = new TcpModbusSocket(host, port, unitId);
            tcpModbusSocket.connect();
            log.info("Connection established with modbus host {} port {} and unitId {}", host, port, unitId);

            ReadMultipleRegistersResponse response = tcpModbusSocket.readHoldingRegisters(23094, 2);
            System.out.println(response.getRegisterValue(0) + " " + response.getRegisterValue(1));
        } catch (Exception e) {
            log.error("Connection to modbus datasource with address {}:{} and unit id {} failed with exception message {}.", host, port, unitId, e.getMessage());
        }
    }

    /**
     * Disconnect and release any resources to allow a clean shutdown.
     *
     * @throws EqIOException if an error occurs while disconnecting.
     */
    @Override
    public void disconnectFromDataSource() throws EqIOException {
        log.info("Disconnecting from datasource...");
        tcpModbusSocket.disconnect();
        log.info("Disconnected from datasource.");
    }

    /**
     * Publish the latest value of all tags on request.
     */
    @Override
    public void refreshAllDataTags() {

    }

    /**
     * Publish the latest value of a single tag on request.
     *
     * @param tagId the id of the data tag to refresh.
     */
    @Override
    public void refreshDataTag(long tagId) {

    }


    @Override
    public void run(String... args) throws Exception {
        log.info("Connecting to datasource...");
        this.connectToDataSource();
    }
}
