package de.tub.sense.daq.config.xml;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
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

/**
 * @author maxmeyer
 * @created 18/12/2020 - 12:39
 * @project DAQConfigLoader
 */

@Getter
@Setter
@ToString
@NoArgsConstructor
@Slf4j
public class HardwareAddress {

    private int startAddress;
    private int valueCount;
    private String type;
    private double minValue;
    private double maxValue;
    private double offset;
    private double multiplier;
    private double threshold;
    private int bitNumber;

    public HardwareAddress(int startAddress, int valueCount, String type, double minValue, double maxValue, int bitNumber) {
        this.startAddress = startAddress;
        this.valueCount = valueCount;
        this.type = type;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.bitNumber = bitNumber;
    }

    public HardwareAddress(int startAddress, int valueCount, String type, double offset, double multiplier, double threshold) {
        this.startAddress = startAddress;
        this.valueCount = valueCount;
        this.type = type;
        this.offset = offset;
        this.multiplier = multiplier;
        this.threshold = threshold;
    }

    public static Optional<HardwareAddress> parseHardwareAddress(String xmlAddress) {
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
                    case "minimum":
                        hardwareAddress.setMinValue(Double.parseDouble(entry.getValue().toString()));
                        break;
                    case "maximum":
                        hardwareAddress.setMaxValue(Double.parseDouble(entry.getValue().toString()));
                        break;
                    case "bitnumber":
                        hardwareAddress.setBitNumber(Integer.parseInt(entry.getValue().toString()));
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

    private static Optional<String> parseXMLHardwareAddress(String xml) {
        Document document = convertStringToXMLDocument(xml).orElseThrow(() -> new RuntimeException("Failed parsing xml string."));
        return Optional.of(document.getElementsByTagName("address").item(0).getTextContent());
    }

    private static Optional<Document> convertStringToXMLDocument(String xmlString) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlString.replaceAll("\n", ""))));
            return Optional.of(doc);
        } catch (Exception e) {
            log.warn("Could not parse XML-String to XML-Document", e);
            return Optional.empty();
        }
    }
}
