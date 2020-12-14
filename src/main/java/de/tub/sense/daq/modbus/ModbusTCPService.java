package de.tub.sense.daq.modbus;

import com.ghgande.j2mod.modbus.msg.ReadCoilsResponse;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
import de.tub.sense.daq.config.DAQConfiguration;
import de.tub.sense.daq.config.file.Modbus;
import de.tub.sense.daq.config.file.ModbusSettings;
import de.tub.sense.daq.config.file.Signal;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * @author maxmeyer
 * @created 14/12/2020 - 15:15
 * @project DAQConfigLoader
 */

@Slf4j
@Service
public class ModbusTCPService {

    private final DAQConfiguration daqConfiguration;
    private TcpModbusSocket tcpModbusSocket;
    private HashMap<Long, String> dataTags;

    public ModbusTCPService(DAQConfiguration daqConfiguration) {
        this.daqConfiguration = daqConfiguration;
        dataTags = new HashMap<>();
    }

    @PostConstruct
    private void init() {
        try {
            ModbusSettings modbusSettings = daqConfiguration.getConfiguration().getModbusSettings();
            tcpModbusSocket = new TcpModbusSocket(modbusSettings.getAddress(), modbusSettings.getPort(), modbusSettings.getUnitID());
            tcpModbusSocket.connect();
            log.info("Connection established with modbus host {} port {} and unitId {}",
                    modbusSettings.getAddress(), modbusSettings.getPort(), modbusSettings.getUnitID());
        } catch (Exception e) {
            log.warn("Failed to initialize modbus tcp connection.", e);
        }
    }

    public Object parseHoldingResponse(@NonNull ReadMultipleRegistersResponse response, String dataType) {
        final Integer[] respValues = new Integer[response.getByteCount()];
        final ByteBuffer bb = ByteBuffer.allocate(2 * response.getByteCount());
        for (int i = 0; i < response.getByteCount(); i++) {
            respValues[i] = response.getRegisterValue(i);
            bb.putShort(respValues[i].shortValue());
        }
        bb.rewind();
        switch (dataType) {
            case "s8":
            case "u8":
                return bb.get();
            case "s16":
            case "u16":
                return bb.getShort();
            case "u32":
            case "s32":
                return bb.getInt();
            case "s64":
            case "u64":
                return bb.getLong();
            case "float32":
                return bb.getFloat();
            case "float64":
                return bb.getDouble();
            default:
                throw new IllegalArgumentException("Datatype " + dataType + " could not be converted");
        }
    }

    public Object parseCoilResponse(@NonNull ReadCoilsResponse response) {
        if (response.getBitCount() == 1) {
            return response.getCoilStatus(0);
        } else {
            final Boolean[] respValues = new Boolean[response.getBitCount()];
            for (int i = 0; i < response.getBitCount(); i++) {
                respValues[i] = response.getCoilStatus(i);
            }
            return respValues;
        }
    }

    public Object getValue(long dataTagId) {
        if (dataTags.containsKey(dataTagId)) {
            String tagName = dataTags.get(dataTagId);
            Signal signal = getSignalFromTagName(tagName);
            if (signal == null) {
                log.error("No signal found with tag name " + tagName);
                return null;
            }
            Modbus modbus = signal.getModbus();
            switch (modbus.getType()) {
                case "holding":
                    try {
                        return parseHoldingResponse(tcpModbusSocket.readHoldingRegisters(modbus.getStartAddress(),
                                modbus.getCount()), signal.getType());
                    } catch (Exception e) {
                        log.warn("Could not read holding register with tagName " + tagName, e);
                    }
                    break;
                case "coil":
                    try {
                        return parseCoilResponse(tcpModbusSocket.readCoils(modbus.getStartAddress(), modbus.getCount()));
                    } catch (Exception e) {
                        log.warn("Could not read coil register with tagName " + tagName, e);
                    }
                    break;
                /*case "input":
                    try {
                        ReadInputRegistersResponse response = tcpModbusSocket.readInputRegisters(modbus.getStartAddress(), modbus.getCount());
                    } catch (Exception e) {
                        log.warn("Could not read input register with tagName " + tagName, e);
                    }
                case "discrete":
                    try {
                        ReadInputDiscretesResponse response = tcpModbusSocket.readDiscreteInputs(modbus.getStartAddress(), modbus.getCount());
                    } catch (Exception e) {
                        log.warn("Could not read discrete input register with tagName " + tagName, e);
                    }

                 */
            }
        }
        return null;
    }

    public void disconnect() {
        tcpModbusSocket.disconnect();
    }

    private Signal getSignalFromTagName(String tagName) {
        for (Signal signal : daqConfiguration.getConfiguration().getSignals()) {
            if (signal.getName().equals(tagName)) {
                return signal;
            }
        }
        log.warn("No signal found with tag name {}.", tagName);
        return null;
    }
}
