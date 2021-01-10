package de.tub.sense.daq.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tub.sense.daq.config.xml.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author maxmeyer
 * @created 18/12/2020 - 12:50
 * @project DAQConfigLoader
 */

@Slf4j
@Component
public class ProcessConfigurationParser {

    private ProcessConfigurationFile processConfigurationFile;

    private static Optional<Document> convertStringToXMLDocument(String xmlString) {
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

    public void parseConfiguration(String xmlConfig) {
        processConfigurationFile = parseXML(xmlConfig);
    }

    private ProcessConfigurationFile parseXML(String xml) {
        Document document = convertStringToXMLDocument(xml).orElseThrow(() -> new RuntimeException("Failed parsing xml string."));
        ProcessConfigurationFile processConfigurationFile = new ProcessConfigurationFile();

        //Process information
        processConfigurationFile.setProcessId(Long.parseLong(document.getDocumentElement().getAttribute("process-id")));
        processConfigurationFile.setName(document.getDocumentElement().getAttribute("name"));
        processConfigurationFile.setType(document.getDocumentElement().getAttribute("type"));
        processConfigurationFile.setAliveTagId(document.getDocumentElement().getElementsByTagName("alive-tag-id").item(0).getTextContent());
        processConfigurationFile.setAliveTagInterval(document.getDocumentElement().getElementsByTagName("alive-interval").item(0).getTextContent());
        processConfigurationFile.setAliveTagInterval(document.getDocumentElement().getElementsByTagName("max-message-size").item(0).getTextContent());
        processConfigurationFile.setAliveTagInterval(document.getDocumentElement().getElementsByTagName("max-message-delay").item(0).getTextContent());

        //Equipment information
        //TODO handle multiple equipments
        ArrayList<EquipmentUnit> equipmentUnits = new ArrayList<>();
        EquipmentUnit equipmentUnit = new EquipmentUnit();

        NodeList nodeListDataTags = document.getDocumentElement().getElementsByTagName("DataTag");
        ArrayList<DataTag> dataTags = new ArrayList<>();

        NodeList nodeListCommandTags = document.getDocumentElement().getElementsByTagName("CommandTag");
        ArrayList<CommandTag> commandTags = new ArrayList<>();

        NodeList nodeListControlTags = document.getDocumentElement().getElementsByTagName("ControlTag");
        ArrayList<CommandTag> controlTags = new ArrayList<>();

        if(document.getDocumentElement().getElementsByTagName("EquipmentUnit").item(0) != null) {
            Element equipmentElement = (Element) document.getDocumentElement().getElementsByTagName("EquipmentUnit").item(0);
            equipmentUnit.setId(Long.parseLong(equipmentElement.getAttributes().getNamedItem("id").getTextContent()));
            equipmentUnit.setName(equipmentElement.getAttributes().getNamedItem("name").getTextContent());
            equipmentUnit.setHandlerClassName(equipmentElement.getElementsByTagName("handler-class-name").item(0).getTextContent());
            equipmentUnit.setCommfaultTagId(Integer.parseInt(equipmentElement.getElementsByTagName("commfault-tag-id").item(0).getTextContent()));
            equipmentUnit.setEquipmentAddress(parseEquipmentAddress(
                    equipmentElement.getElementsByTagName("address").item(0).getTextContent()).orElse(new EquipmentAddress()));

            //DataTags
            for (int i = 0; i < nodeListDataTags.getLength(); i++) {
                DataTag dataTag = new DataTag();
                Element el = (Element) nodeListDataTags.item(i);
                dataTag.setId(Long.parseLong(el.getAttributes().getNamedItem("id").getTextContent()));
                dataTag.setName(el.getAttributes().getNamedItem("name").getTextContent());
                dataTag.setDataType(el.getElementsByTagName("data-type").item(0).getTextContent());
                dataTag.setAddress(parseHardwareAddress(
                        el.getElementsByTagName("address").item(0).getTextContent()).orElse(new HardwareAddress()));
                dataTags.add(dataTag);
            }

            //CommandTags
            for (int i = 0; i < nodeListCommandTags.getLength(); i++) {
                CommandTag commandTag = new CommandTag();
                Element el = (Element) nodeListCommandTags.item(i);
                commandTag.setId(Long.parseLong(el.getAttributes().getNamedItem("id").getTextContent()));
                commandTag.setName(el.getAttributes().getNamedItem("name").getTextContent());
                commandTag.setAddress(parseHardwareAddress(
                        el.getElementsByTagName("address").item(0).getTextContent()).orElse(new HardwareAddress()));
                commandTags.add(commandTag);
            }
        }
        equipmentUnit.setDataTags(dataTags);
        equipmentUnit.setCommandTags(commandTags);
        equipmentUnits.add(equipmentUnit);
        processConfigurationFile.setEquipmentUnits(equipmentUnits);
        return processConfigurationFile;
    }

    private Optional<HardwareAddress> parseHardwareAddress(String address) {
        if (log.isDebugEnabled()) {
            log.debug("Parsing hardware address {}", address);
        }
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
                        log.warn("Unrecognized hardware address key: {}", entry.getKey());
                        break;
                }
            }
            return Optional.of(hardwareAddress);
        } catch (IOException e) {
            log.warn("Could not parse hardware address from string", e);
            return Optional.empty();
        }
    }

    private Optional<EquipmentAddress> parseEquipmentAddress(String address) {
        if (log.isDebugEnabled()) {
            log.debug("Parsing equipment address {}", address);
        }
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

    public ProcessConfigurationFile getProcessConfigurationFile() {
        return processConfigurationFile;
    }
}