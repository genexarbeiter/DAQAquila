package de.tub.sense.daq.config;

import de.tub.sense.daq.config.xml.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
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
        NodeList equipmentsUnits = document.getDocumentElement().getElementsByTagName("EquipmentUnit");


        for (int i = 0; i < equipmentsUnits.getLength(); i++) {
            EquipmentUnit equipmentUnit = new EquipmentUnit();
            Element equipmentElement = (Element) document.getDocumentElement().getElementsByTagName("EquipmentUnit").item(i);


            NodeList nodeListDataTags = equipmentElement.getElementsByTagName("DataTag");
            ArrayList<DataTag> dataTags = new ArrayList<>();

            NodeList nodeListCommandTags = equipmentElement.getElementsByTagName("CommandTag");
            ArrayList<CommandTag> commandTags = new ArrayList<>();


            equipmentUnit.setId(Long.parseLong(equipmentElement.getAttributes().getNamedItem("id").getTextContent()));
            equipmentUnit.setName(equipmentElement.getAttributes().getNamedItem("name").getTextContent());
            equipmentUnit.setHandlerClassName(equipmentElement.getElementsByTagName("handler-class-name").item(0).getTextContent());
            equipmentUnit.setCommfaultTagId(Integer.parseInt(equipmentElement.getElementsByTagName("commfault-tag-id").item(0).getTextContent()));
            equipmentUnit.setAliveTagInterval(Integer.parseInt(document.getDocumentElement().getElementsByTagName("alive-interval").item(0).getTextContent()));
            equipmentUnit.setEquipmentAddress(EquipmentAddress.parseEquipmentAddress(
                    equipmentElement.getElementsByTagName("address").item(0).getTextContent()).orElse(new EquipmentAddress()));

            //DataTags
            for (int j = 0; j < nodeListDataTags.getLength(); j++) {
                DataTag dataTag = new DataTag();
                Element el = (Element) nodeListDataTags.item(j);
                dataTag.setId(Long.parseLong(el.getAttributes().getNamedItem("id").getTextContent()));
                dataTag.setName(el.getAttributes().getNamedItem("name").getTextContent());
                dataTag.setDataType(el.getElementsByTagName("data-type").item(0).getTextContent());
                dataTag.setAddress(HardwareAddress.parseHardwareAddress(
                        el.getElementsByTagName("address").item(0).getTextContent()).orElse(new HardwareAddress()));
                dataTags.add(dataTag);
            }

            //CommandTags
            for (int j = 0; j < nodeListCommandTags.getLength(); j++) {
                CommandTag commandTag = new CommandTag();
                Element el = (Element) nodeListCommandTags.item(j);
                commandTag.setId(Long.parseLong(el.getAttributes().getNamedItem("id").getTextContent()));
                commandTag.setName(el.getAttributes().getNamedItem("name").getTextContent());
                commandTag.setAddress(HardwareAddress.parseHardwareAddress(
                        el.getElementsByTagName("address").item(0).getTextContent()).orElse(new HardwareAddress()));
                commandTags.add(commandTag);
            }
            equipmentUnit.setDataTags(dataTags);
            equipmentUnit.setCommandTags(commandTags);
            equipmentUnits.add(equipmentUnit);
        }


        processConfigurationFile.setEquipmentUnits(equipmentUnits);
        return processConfigurationFile;
    }


    public ProcessConfigurationFile getProcessConfigurationFile() {
        return processConfigurationFile;
    }
}