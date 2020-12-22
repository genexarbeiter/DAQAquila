package de.tub.sense.daq.module;

import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import de.tub.sense.daq.modbus.ModbusTCPService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author maxmeyer
 * @created 11/12/2020 - 15:13
 * @project DAQConfigLoader
 */

@Slf4j
public class DAQMessageHandler extends EquipmentMessageHandler {

    private final ModbusTCPService modbusTCPService;

    public DAQMessageHandler(ModbusTCPService modbusTCPService) {
        this.modbusTCPService = modbusTCPService;
    }

    /**
     * Perform the necessary tasks to connect to the underlying data source. The
     * handler is expected to be ready to publish data as soon as this method
     * returns.
     */
    @Override
    public void connectToDataSource() {
        log.info("Connecting to datasource...");
        modbusTCPService.connect();
    }

    /**
     * Disconnect and release any resources to allow a clean shutdown.
     *
     * @throws EqIOException if an error occurs while disconnecting.
     */
    @Override
    public void disconnectFromDataSource() throws EqIOException {
        log.info("Disconnecting from datasource...");
        modbusTCPService.disconnect();
        log.info("Disconnected from datasource.");
    }

    /**
     * Publish the latest value of all tags on request.
     */
    @Override
    public void refreshAllDataTags() {
        log.info("Refreshing all datatags...");
        //FOR all datatags: refreshDataTag(tagId)
        log.info("Done");
    }

    /**
     * Publish the latest value of a single tag on request.
     *
     * @param tagId the id of the data tag to refresh.
     */
    @Override
    public void refreshDataTag(long tagId) {
        IEquipmentMessageSender sender = getEquipmentMessageSender();
        sender.confirmEquipmentStateOK();
    }


}
