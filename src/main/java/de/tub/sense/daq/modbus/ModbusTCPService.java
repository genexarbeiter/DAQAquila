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

import java.nio.ByteBuffer;
import java.util.Optional;

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

    public ModbusTCPService(DAQConfiguration daqConfiguration) {
        this.daqConfiguration = daqConfiguration;
    }

    /**
     * Establish the connection to the ModbusTCPEndpoint. If it fails, it logs the exception.
     */
    public void connect() {
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

    public Optional<Object> getValue(long dataTagId) {
        /*if (!dataTags.containsKey(dataTagId)) {
            log.error("No data tag found with dataTagId {}", dataTagId);
            return Optional.empty();
        }*/
        String tagName = ""; //dataTags.get(dataTagId);
        Signal signal = getSignalFromTagName(tagName).orElseThrow(
                () -> new RuntimeException("No signal found with tag name " + tagName)
        );
        Modbus modbus = signal.getModbus();
        switch (modbus.getType()) {
            case "holding":
                try {
                    return Optional.of(parseHoldingResponse(tcpModbusSocket.readHoldingRegisters(modbus.getStartAddress(),
                            modbus.getCount()), signal.getType()));
                } catch (Exception e) {
                    log.warn("Could not read holding register with tagName " + tagName, e);
                    return Optional.empty();
                }
            case "coil":
                try {
                    return Optional.of(parseCoilResponse(tcpModbusSocket.readCoils(modbus.getStartAddress(), modbus.getCount())));
                } catch (Exception e) {
                    log.warn("Could not read coil register with tagName " + tagName, e);
                    return Optional.empty();
                }
            /*
            case "input":
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
            default:
                log.warn("Modbus type {} not valid.", modbus.getType());
                return Optional.empty();
        }
    }

    public void disconnect() {
        tcpModbusSocket.disconnect();
    }

    private Optional<Signal> getSignalFromTagName(String tagName) {
        for (Signal signal : daqConfiguration.getConfiguration().getSignals()) {
            if (signal.getName().equals(tagName)) {
                return Optional.of(signal);
            }
        }
        log.warn("No signal found with tag name {}.", tagName);
        return Optional.empty();
    }

    private Object parseHoldingResponse(ReadMultipleRegistersResponse response, String dataType) {
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

    private Object parseCoilResponse(@NonNull ReadCoilsResponse response) {
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
}
