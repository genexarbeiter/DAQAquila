package de.tub.sense.daq.module;

import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.ValueUpdate;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.tub.sense.daq.config.xml.EquipmentAddress;
import de.tub.sense.daq.config.xml.HardwareAddress;
import de.tub.sense.daq.modbus.ModbusTCPService;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author maxmeyer
 * @created 11/12/2020 - 15:13
 * @project DAQConfigLoader
 */

@Slf4j
public class DAQMessageHandler extends EquipmentMessageHandler {

    private final HashMap<Long, Object> valueCache = new HashMap<>();
    private final HashMap<Long, HardwareAddress> addressCache = new HashMap<>();
    private final HashMap<Long, String> dataTypeCache = new HashMap<>();
    IEquipmentMessageSender equipmentMessageSender;
    private boolean autoRefreshRunning = false;
    private IEquipmentConfiguration equipmentConfiguration;
    private ModbusTCPService modbusTCPService;
    private int skipped = 0;
    private int threshold_skipped = 0;
    private int tagCount = 0;
    private boolean performanceMode = false;

    public DAQMessageHandler() {
    }

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
        EquipmentAddress equipmentAddress = parseEquipmentAddress(
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
     * Starts if not already started a thread which schedules this function at a fixed rate
     */
    @Override
    public void refreshAllDataTags() {
        log.info("Refreshing all data tags...");
        if(!modbusTCPService.isConnected()) {
            log.warn("Modbus TCP connection lost, trying to reconnect...");
            connectToDataSource();
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
                hardwareAddress = parseHardwareAddress(
                        dataTag.getHardwareAddress().toConfigXML()).orElseThrow(RuntimeException::new);
                datatype = dataTag.getDataType();
                addressCache.put(tagId, hardwareAddress);
                dataTypeCache.put(tagId, datatype);
            } else {
                hardwareAddress = addressCache.get(tagId);
                datatype = dataTypeCache.get(tagId);
            }

            log.trace("Retrieving tag value from modbus tcp service...");
            Optional<Object> value = modbusTCPService.getValue(tagId, hardwareAddress, datatype);
            if (!value.isPresent()) {
                log.warn("Failed to read value from tagId {}, skipping update", tagId);
                return;
            }
            Object valueObject = value.get();
            if (!autoRefreshRunning) {
                valueCache.put(tagId, valueObject);
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
                    if(Math.abs(valueFloat - prevFloat) > threshold) {
                        valueFloat *= multiplier;
                        valueFloat += offset;
                        equipmentMessageSender.update(tagId, new ValueUpdate(valueFloat));
                    } else {
                        threshold_skipped++;
                        return;
                    }
                } else if (valueObject instanceof Boolean) {
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
                    if(Math.abs(valueDouble - prevDouble) > threshold) {
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
        long delay = Long.parseLong(System.getProperty("c2mon.daq.refreshDelay"));
        ThreadFactory refreshThreadFactory =
                new ThreadFactoryBuilder().setNameFormat(equipmentConfiguration.getId() + " REFRESH").build();
        ScheduledFuture<?> future = Executors.newSingleThreadScheduledExecutor(refreshThreadFactory).scheduleAtFixedRate(() -> {
            autoRefreshRunning = true;
            refreshAllDataTags();
        }, delay, delay, TimeUnit.MILLISECONDS);
    }

    private void checkPerformanceMode() {
        if (Boolean.parseBoolean(System.getProperty("c2mon.daq.performanceMode"))) {
            performanceMode = true;
            log.info("Performance mode enabled (No offset, multiplier and accuracy check)");
        } else {
            log.info("Performance mode disabled");
        }
    }

    //TODO Put parsing somewhere else
    private Optional<EquipmentAddress> parseEquipmentAddress(String address) {
        EquipmentAddress equipmentAddress = new EquipmentAddress();
        ObjectMapper mapper = new ObjectMapper();
        try {
            HashMap<String, Object> map = mapper.readValue(address, HashMap.class);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                switch (entry.getKey()) {
                    case "host":
                        equipmentAddress.setHost(String.valueOf(entry.getValue()));
                        break;
                    case "port":
                        equipmentAddress.setPort((int) entry.getValue());
                        break;
                    case "unitID":
                        equipmentAddress.setUnitId((int) entry.getValue());
                        break;
                    case "delay":
                        equipmentAddress.setDelay((int) entry.getValue());
                        break;
                    case "timeUnit":
                        equipmentAddress.setTimeUnit(String.valueOf(entry.getValue()));
                        break;
                    default:
                        log.warn("Unrecognized equipment address key: {}", entry.getKey());
                        break;
                }
            }
            return Optional.of(equipmentAddress);
        } catch (IOException e) {
            log.warn("Could not parse equipment address from string", e);
            return Optional.empty();
        }
    }

    private Optional<HardwareAddress> parseHardwareAddress(String xmlAddress) {
        String address = parseXMLHardwareAddress(xmlAddress).orElseThrow(RuntimeException::new);
        HardwareAddress hardwareAddress = new HardwareAddress();
        ObjectMapper mapper = new ObjectMapper();
        try {
            HashMap<String, Object> map = mapper.readValue(address, HashMap.class);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                switch (entry.getKey()) {
                    case "startAddress":
                        hardwareAddress.setStartAddress((int) entry.getValue());
                        break;
                    case "writeValueCount":
                    case "readValueCount":
                        hardwareAddress.setValueCount((int) entry.getValue());
                        break;
                    case "readingType":
                    case "writingType":
                        hardwareAddress.setType(String.valueOf(entry.getValue()));
                        break;
                    case "minimalValue":
                        hardwareAddress.setMinValue(Double.parseDouble(entry.getValue().toString()));
                        break;
                    case "maximalValue":
                        hardwareAddress.setMaxValue(Double.parseDouble(entry.getValue().toString()));
                        break;
                    case "value_offset":
                        hardwareAddress.setOffset(Double.parseDouble(entry.getValue().toString()));
                        break;
                    case "value_multiplier":
                        hardwareAddress.setMultiplier(Double.parseDouble(entry.getValue().toString()));
                        break;
                    case "value_threshold":
                        hardwareAddress.setThreshold(Double.parseDouble(entry.getValue().toString()));
                        break;
                    default:
                        break;
                }
            }
            return Optional.of(hardwareAddress);
        } catch (IOException e) {
            log.warn("Could not parse hardware address from string", e);
            return Optional.empty();
        }
    }

    private Optional<String> parseXMLHardwareAddress(String xml) {
        Document document = convertStringToXMLDocument(xml).orElseThrow(() -> new RuntimeException("Failed parsing xml string."));
        return Optional.of(document.getElementsByTagName("address").item(0).getTextContent());
    }

    private Optional<Document> convertStringToXMLDocument(String xmlString) {
        //Parser that produces DOM object trees from XML content
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        //API to obtain DOM Document instance
        DocumentBuilder builder = null;
        try {
            //Create DocumentBuilder with default configuration
            builder = factory.newDocumentBuilder();

            //Parse the content to Document object
            Document doc = builder.parse(new InputSource(new StringReader(xmlString.replaceAll("\n", ""))));
            return Optional.of(doc);
        } catch (Exception e) {
            log.warn("Could not parse XML-String to XML-Document", e);
            return Optional.empty();
        }
    }

}
