package de.tub.sense.daq.module;

import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.common.ICommandRunner;
import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.ValueUpdate;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;
import cern.c2mon.shared.daq.command.SourceCommandTagValue;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.tub.sense.daq.config.xml.EquipmentAddress;
import de.tub.sense.daq.config.xml.HardwareAddress;
import de.tub.sense.daq.modbus.ModbusTCPService;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author maxmeyer
 * @created 11/12/2020 - 15:13
 * @project DAQConfigLoader
 */

@Slf4j
@NoArgsConstructor
public class DAQMessageHandler extends EquipmentMessageHandler implements ICommandRunner {

    private final HashMap<Long, Object> valueCache = new HashMap<>();
    private final HashMap<Long, HardwareAddress> addressCache = new HashMap<>();
    private final HashMap<Long, String> dataTypeCache = new HashMap<>();
    private IEquipmentMessageSender equipmentMessageSender;
    private boolean autoRefreshRunning = false;
    private IEquipmentConfiguration equipmentConfiguration;
    private ModbusTCPService modbusTCPService;
    private int skipped = 0;
    private int threshold_skipped = 0;
    private int tagCount = 0;
    private boolean performanceMode = false;

    /**
     * Perform the necessary tasks to connect to the underlying data source. The
     * handler is expected to be ready to publish data as soon as this method
     * returns.
     */
    @Override
    public void connectToDataSource() {
        log.info("Connecting to datasource...");
        checkPerformanceMode();
        equipmentConfiguration = getEquipmentConfiguration();
        equipmentMessageSender = getEquipmentMessageSender();
        getEquipmentCommandHandler().setCommandRunner(this);

        EquipmentAddress equipmentAddress = EquipmentAddress.parseEquipmentAddress(
                equipmentConfiguration.getAddress()).orElseThrow(RuntimeException::new);
        modbusTCPService = new ModbusTCPService();
        int triedConnection = 0;
        int sleepTime = 1;
        while (!modbusTCPService.connect(equipmentAddress.getHost(), equipmentAddress.getPort(), equipmentAddress.getUnitId())) {
            try {
                triedConnection++;
                if (triedConnection > 5) {
                    sleepTime = 5;
                }
                if (triedConnection > 20) {
                    sleepTime = 20;
                }
                if (triedConnection > 100) {
                    sleepTime = 300;
                }
                log.error("Connection to modbus failed. Trying again in {} second(s)...", sleepTime);
                Thread.sleep(sleepTime * 1000);
            } catch (InterruptedException e) {
                log.warn("Interrupted exception occurred while waiting for connection", e);
            }
        }
    }

    /**
     * Disconnect and release any resources to allow a clean shutdown.
     */
    @Override
    public void disconnectFromDataSource() {
        log.info("Disconnecting from datasource...");
        modbusTCPService.disconnect();
        log.info("Disconnected from datasource.");
    }

    /**
     * Publish the latest value of all tags on request.
     * Starts if not already started a thread which schedules this function at a fixed rate
     */
    @Override
    public void refreshAllDataTags() {
        log.info("Refreshing all data tags ...");
        if (!modbusTCPService.isConnected()) {
            log.warn("Modbus TCP connection lost, trying to reconnect...");
            equipmentMessageSender.confirmEquipmentStateIncorrect("Modbus TCP connection lost, trying to reconnect...");
            connectToDataSource();
            return;
        } else {
            equipmentMessageSender.confirmEquipmentStateOK("Everything fine.");
        }
        long millis = System.currentTimeMillis();
        skipped = 0;
        threshold_skipped = 0;
        if (!autoRefreshRunning) {
            equipmentConfiguration.getSourceDataTags().keySet().forEach(this::refreshDataTag);
            tagCount = equipmentConfiguration.getSourceDataTags().size();
        } else {
            dataTypeCache.keySet().forEach(this::refreshDataTag);
        }
        long millis2 = System.currentTimeMillis();
        long time = millis2 - millis;
        log.info("Refreshed all data tags successful took {}ms | skipped {}/{} (equal) | skipped {}/{} (threshold) | updated {}/{}"
                , time, skipped, tagCount, threshold_skipped, tagCount, tagCount - threshold_skipped - skipped, tagCount);
        if (!autoRefreshRunning) {
            startAutoRefresh();
        }
    }

    /**
     * Publish the latest value of a single tag on request.
     *
     * @param tagId the id of the data tag to refresh.
     */
    @Override
    public void refreshDataTag(long tagId) {
        if (log.isTraceEnabled()) {
            log.trace("Refreshing data tag {}...", tagId);
        }
        try {
            HardwareAddress hardwareAddress;
            String datatype;
            if (!autoRefreshRunning) {
                ISourceDataTag dataTag = equipmentConfiguration.getSourceDataTag(tagId);
                hardwareAddress = HardwareAddress.parseHardwareAddressFromXML(dataTag.getHardwareAddress().toConfigXML()).orElseThrow(RuntimeException::new);
                datatype = dataTag.getDataType();
                addressCache.put(tagId, hardwareAddress);
                dataTypeCache.put(tagId, datatype);
            } else {
                hardwareAddress = addressCache.get(tagId);
                datatype = dataTypeCache.get(tagId);
            }

            log.trace("Retrieving tag value from modbus tcp service...");
            Optional<Object> value = modbusTCPService.getValue(hardwareAddress, datatype);
            if (!value.isPresent()) {
                log.warn("Failed to read value from tagId {}, skipping update", tagId);
                skipped++;
                return;
            }
            Object valueObject = value.get();
            if (!autoRefreshRunning) {
                valueCache.put(tagId, valueObject);
                equipmentMessageSender.update(tagId, new ValueUpdate(valueObject));
                return;
            }
            Object prevValue = valueCache.get(tagId);
            if (prevValue.equals(valueObject)) {
                skipped++;
                return;
            }
            if (!performanceMode) {
                double multiplier = hardwareAddress.getMultiplier();
                double offset = hardwareAddress.getOffset();
                double threshold = hardwareAddress.getThreshold();
                if (multiplier == 0.0) multiplier = 1;
                if (valueObject instanceof Float) {
                    float valueFloat = (float) valueObject;
                    float prevFloat = (float) prevValue;
                    if (Math.abs(valueFloat - prevFloat) > threshold) {
                        valueFloat *= multiplier;
                        valueFloat += offset;
                        equipmentMessageSender.update(tagId, new ValueUpdate(valueFloat));
                    } else {
                        threshold_skipped++;
                        return;
                    }
                } else if (valueObject instanceof Boolean) {
                    log.debug("Boolean update: {}", (boolean) valueObject);
                    equipmentMessageSender.update(tagId, new ValueUpdate(valueObject));
                } else if (valueObject instanceof Integer) {
                    int valueInt = (int) valueObject;
                    valueInt *= multiplier;
                    valueInt += offset;
                    equipmentMessageSender.update(tagId, new ValueUpdate(valueInt));
                } else if (valueObject instanceof Byte) {
                    byte valueByte = (byte) valueObject;
                    valueByte *= multiplier;
                    valueByte += offset;
                    equipmentMessageSender.update(tagId, new ValueUpdate(valueByte));
                } else if (valueObject instanceof Long) {
                    long valueLong = (long) valueObject;
                    valueLong *= multiplier;
                    valueLong += offset;
                    equipmentMessageSender.update(tagId, new ValueUpdate(valueLong));
                } else if (valueObject instanceof Short) {
                    short valueShort = (short) valueObject;
                    valueShort *= multiplier;
                    valueShort += offset;
                    equipmentMessageSender.update(tagId, new ValueUpdate(valueShort));
                } else if (valueObject instanceof Double) {
                    double valueDouble = (double) valueObject;
                    double prevDouble = (double) prevValue;
                    if (Math.abs(valueDouble - prevDouble) > threshold) {
                        valueDouble *= multiplier;
                        valueDouble += offset;
                        equipmentMessageSender.update(tagId, new ValueUpdate(valueDouble));
                    } else {
                        threshold_skipped++;
                        return;
                    }
                } else {
                    equipmentMessageSender.update(tagId, new ValueUpdate(valueObject));
                }
            } else {
                equipmentMessageSender.update(tagId, new ValueUpdate(valueObject));
            }
            valueCache.put(tagId, valueObject);
            if (log.isTraceEnabled()) {
                log.trace("Refreshing data tag {} success", tagId);
            }
        } catch (Throwable e) {
            log.error("Could not refresh data tag {}", tagId);
            log.error("Exception occurred", e);
        }
    }


    /**
     * Starts a new thread called refresh-thread, which automatically calls the method refreshAllDataTags() in a specific
     * interval. The method scheduleAtFixedRate() is the way to go, as its not scheduling the next task until the previous
     * is done.
     */
    private void startAutoRefresh() {
        log.debug("Enabling auto refresh...");
        int delay = EquipmentAddress.parseEquipmentAddress(
                equipmentConfiguration.getAddress()).orElseThrow(RuntimeException::new).getRefreshInterval();//Long.parseLong(System.getProperty("c2mon.daq.refreshDelay"));
        ThreadFactory refreshThreadFactory =
                new ThreadFactoryBuilder().setNameFormat(String.valueOf(equipmentConfiguration.getId())).build();
        Executors.newSingleThreadScheduledExecutor(refreshThreadFactory).scheduleAtFixedRate(this::refreshAllDataTags, delay, delay, TimeUnit.MILLISECONDS);
        autoRefreshRunning = true;
    }

    /**
     * Checks if performance mode environment variable,
     * if its true, the performance mode is enabled, if not it stays disabled
     */
    private void checkPerformanceMode() {
        if (Boolean.parseBoolean(System.getProperty("c2mon.daq.performanceMode"))) {
            performanceMode = true;
            log.info("Performance mode enabled (No offset, multiplier and accuracy check)");
        } else {
            log.info("Performance mode disabled");
        }
    }

    /**
     * Is run by C2mon if a command is executed
     *
     * @param sourceCommandTagValue of the command
     * @return String of the execution state
     */
    @Override
    public String runCommand(SourceCommandTagValue sourceCommandTagValue) {
        if (log.isDebugEnabled()) {
            log.debug("Running command for tag {} in equipment {} with data type {} and value {}",
                    sourceCommandTagValue.getName(), sourceCommandTagValue.getEquipmentId(),
                    sourceCommandTagValue.getDataType(), sourceCommandTagValue.getValue());
        }
        Optional<HardwareAddress> optionalHardwareAddress = HardwareAddress.parseHardwareAddressFromXML(
                equipmentConfiguration.getSourceCommandTag(sourceCommandTagValue.getId()).getHardwareAddress().toConfigXML());
        if (optionalHardwareAddress.isPresent()) {
            modbusTCPService.putValue(optionalHardwareAddress.get(), sourceCommandTagValue.getValue(), sourceCommandTagValue.getDataType());
            return "Success";
        } else {
            return "Could not parse hardware address. Failed.";
        }

    }
}
