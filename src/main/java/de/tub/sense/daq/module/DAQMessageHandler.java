package de.tub.sense.daq.module;

import cern.c2mon.daq.common.EquipmentMessageHandler;
import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.c2mon.shared.common.command.ISourceCommandTag;
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
import java.util.HashMap;
import java.util.Map;
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
public class DAQMessageHandler extends EquipmentMessageHandler {

    IEquipmentMessageSender equipmentMessageSender;
    private boolean autoRefreshRunning = false;
    private IEquipmentConfiguration equipmentConfiguration;
    private ModbusTCPService modbusTCPService;


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
        equipmentConfiguration = getEquipmentConfiguration();
        equipmentMessageSender = getEquipmentMessageSender();
        EquipmentAddress equipmentAddress = parseEquipmentAddress(
                equipmentConfiguration.getAddress()).orElseThrow(RuntimeException::new);
        modbusTCPService = new ModbusTCPService();
        modbusTCPService.connect(equipmentAddress.getHost(), equipmentAddress.getPort(), equipmentAddress.getUnitId());
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
        equipmentConfiguration.getSourceDataTags().keySet().forEach(this::refreshDataTag);
        log.info("Refreshed all data tags successful");
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
        if (log.isDebugEnabled()) {
            log.debug("Refreshing data tag {}...", tagId);
        }
        equipmentMessageSender.confirmEquipmentStateOK();
        ISourceDataTag dataTag = equipmentConfiguration.getSourceDataTag(tagId);
        ISourceCommandTag commandTag = equipmentConfiguration.getSourceCommandTag(tagId);
        HardwareAddress hardwareAddress = parseHardwareAddress(
                dataTag.getHardwareAddress().toConfigXML()).orElseThrow(RuntimeException::new);

        log.debug("Retrieving tag value from modbus tcp service...");
        Optional<Object> value = modbusTCPService.getValue(tagId, hardwareAddress, dataTag.getDataType());

        if (!value.isPresent()) {
            log.warn("Failed to read value from tagId {}, skipping update", tagId);
            return;
        }

        try {
            equipmentMessageSender.update(tagId, new ValueUpdate(value.get()));
        } catch (Exception e) {
            log.error("Could not send value update", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Refreshing data tag {} success", tagId);
        }
    }

    /**
     * Starts a new thread called refresh-thread, which automatically calls the method refreshAllDataTags() in a specific
     * interval. The method scheduleAtFixedRate() is the way to go, as its not scheduling the next task until the previous
     * is done.
     */
    private void startAutoRefresh() {
        log.debug("Enabling auto refresh...");
        ThreadFactory namedThreadFactory =
                new ThreadFactoryBuilder().setNameFormat("refresh-thread").build();
        Executors.newScheduledThreadPool(1, namedThreadFactory).scheduleAtFixedRate(() -> {
            autoRefreshRunning = true;
            refreshAllDataTags();
        }, 10, 10, TimeUnit.SECONDS);
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
