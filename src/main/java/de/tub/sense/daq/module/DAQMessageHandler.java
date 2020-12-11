package de.tub.sense.daq.module;

import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import de.tub.sense.daq.config.ConfigurationParser;
import de.tub.sense.daq.config.file.DAQConfiguration;
import de.tub.sense.daq.modbus.protocols.TcpModbusSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author maxmeyer
 * @created 11/12/2020 - 15:13
 * @project DAQConfigLoader
 */

@Slf4j
public class DAQMessageHandler extends EquipmentMessageHandler {

    private final DAQConfiguration daqConfiguration;

    public DAQMessageHandler(DAQConfiguration daqConfiguration) {
        this.daqConfiguration = daqConfiguration;
    }

    /**
     * Perform the necessary tasks to connect to the underlying data source. The
     * handler is expected to be ready to publish data as soon as this method
     * returns.
     */
    @Override
    public void connectToDataSource() {
        String host = daqConfiguration.getModbusSettings().getAddress();
        int port = daqConfiguration.getModbusSettings().getPort();
        int unitId = daqConfiguration.getModbusSettings().getUnitID();
        try {
            TcpModbusSocket tcpModbusSocket = new TcpModbusSocket(host, port, unitId);
            tcpModbusSocket.connect();
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
}
