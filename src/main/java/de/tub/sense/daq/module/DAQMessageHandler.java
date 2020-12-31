package de.tub.sense.daq.module;

import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.common.datatag.ValueUpdate;
import de.tub.sense.daq.modbus.ModbusTCPService;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author maxmeyer
 * @created 11/12/2020 - 15:13
 * @project DAQConfigLoader
 */

@Slf4j
public class DAQMessageHandler extends EquipmentMessageHandler {

    private static ModbusTCPService modbusTCPService;

    public DAQMessageHandler() {
    }

    public static void setModbusTCPService(ModbusTCPService modbusTCPService1) {
        modbusTCPService = modbusTCPService1;
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

        //TODO Get it working
        new Thread(() -> {
            ExecutorService threadpool = Executors.newCachedThreadPool();
            Future futureTask = null;
            while (true) {
                try {
                    Thread.sleep(10000);
                    if (futureTask != null) {
                        if (!futureTask.isDone()) {
                            if (log.isDebugEnabled()) {
                                log.debug("Skipping refreshing all data tags, operation already running!");
                            }
                            continue;
                        }
                    }
                    futureTask = threadpool.submit(this::refreshAllDataTags);
                } catch (InterruptedException e) {
                    log.warn("Interrupted exception occurred", e);
                }
            }
        }).start();
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
        log.info("Refreshing all data tags...");
        getEquipmentConfiguration().getSourceDataTags().keySet().forEach(this::refreshDataTag);
        log.info("Refreshed all data tags successful");
    }

    /**
     * Publish the latest value of a single tag on request.
     *
     * @param tagId the id of the data tag to refresh.
     */
    @Override
    public void refreshDataTag(long tagId) {
        if (log.isDebugEnabled()) {
            log.debug("Refreshing data tag {}...", tagId);
        }
        IEquipmentMessageSender sender = getEquipmentMessageSender();
        sender.confirmEquipmentStateOK();
        Optional<Object> value = modbusTCPService.getValue(tagId);
        if (!value.isPresent()) {
            log.warn("Failed to read value from tagId {}.", tagId);
            return;
        }
        sender.update(tagId, new ValueUpdate(value.get()));
        if (log.isDebugEnabled()) {
            log.debug("Refreshing data tag {} success", tagId);
        }
    }
}
