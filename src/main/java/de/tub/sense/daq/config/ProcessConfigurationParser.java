package de.tub.sense.daq.config;

import de.tub.sense.daq.config.xml.EquipmentUnit;
import de.tub.sense.daq.config.xml.ProcessConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.Optional;

/**
 * @author maxmeyer
 * @created 18/12/2020 - 12:50
 * @project DAQConfigLoader
 */

@Slf4j
@Component
public class ProcessConfigurationParser {


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

    public ProcessConfiguration parseXML(String xml) {
        Document document = convertStringToXMLDocument(xml).orElseThrow(() -> new RuntimeException("Failed parsing xml string."));
        ProcessConfiguration processConfiguration = new ProcessConfiguration();

        //Process information
        processConfiguration.setProcessId(document.getDocumentElement().getAttribute("process-id"));
        processConfiguration.setName(document.getDocumentElement().getAttribute("name"));
        processConfiguration.setType(document.getDocumentElement().getAttribute("type"));
        processConfiguration.setAliveTagId(document.getDocumentElement().getElementsByTagName("alive-tag-id").item(0).getTextContent());
        processConfiguration.setAliveTagInterval(document.getDocumentElement().getElementsByTagName("alive-interval").item(0).getTextContent());
        processConfiguration.setAliveTagInterval(document.getDocumentElement().getElementsByTagName("max-message-size").item(0).getTextContent());
        processConfiguration.setAliveTagInterval(document.getDocumentElement().getElementsByTagName("max-message-delay").item(0).getTextContent());

        //Equipment information
        EquipmentUnit equipmentUnit = new EquipmentUnit();
        Element equipmentElement = (Element) document.getDocumentElement().getElementsByTagName("EquipmentUnit").item(0);
        equipmentUnit.setId(equipmentElement.getAttributes().getNamedItem("id").getTextContent());
        equipmentUnit.setName(equipmentElement.getAttributes().getNamedItem("name").getTextContent());
        equipmentUnit.setHandlerClassName(equipmentElement.getElementsByTagName("handler-class-name").item(0).getTextContent());


        NodeList nodeListDataTags = document.getDocumentElement().getElementsByTagName("DataTag");
        NodeList nodeListCommandTags = document.getDocumentElement().getElementsByTagName("DataTag");
        return null;
    }
}