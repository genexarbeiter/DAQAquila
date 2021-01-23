package de.tub.sense.daq.modbus;

import com.ghgande.j2mod.modbus.msg.ReadCoilsResponse;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import de.tub.sense.daq.config.xml.HardwareAddress;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author maxmeyer
 * @created 14/12/2020 - 15:15
 * @project DAQConfigLoader
 */

@Slf4j
@NoArgsConstructor
public class ModbusTCPService {

    private TcpModbusSocket tcpModbusSocket;

    /**
     * Establish the connection to the ModbusTCPEndpoint. If it fails, it logs the exception.
     */
    public boolean connect(String host, int port, int unitId) {
        try {
            tcpModbusSocket = new TcpModbusSocket(host, port, unitId);
            tcpModbusSocket.connect();
            log.info("Connection established with modbus host {} port {} and unitId {}",
                    host, port, unitId);
            return true;
        } catch (Throwable e) {
            log.error("Failed to initialize modbus tcp connection.", e);
            return false;
        }
    }

    public boolean isConnected() {
        if (tcpModbusSocket != null) {
            TCPMasterConnection connection = tcpModbusSocket.getConnection();
            return connection.isConnected();
        }
        return false;
    }

    public void putValue(HardwareAddress hardwareAddress, Object value, String dataType) {
        Object[] result = preDataProcessing(value, hardwareAddress, dataType).orElseThrow(RuntimeException::new);
        if (result.length == 1) {
            try {
                tcpModbusSocket.writeRegister(hardwareAddress.getStartAddress(), (int) result[0]);
            } catch (Exception e) {
                log.error("Could not write the value " + result[0] + " to register with startAddress " + hardwareAddress.getStartAddress(), e);
            }
        } else {
            int[] intResult = new int[result.length];
            for (int i = 0; i < intResult.length; i++) {
                intResult[i] = (int) result[i];
            }
            try {
                tcpModbusSocket.writeRegisters(hardwareAddress.getStartAddress(), intResult);
            } catch (Exception e) {
                log.error("Could not write values " + Arrays.stream(intResult).toString() + " write to register with startAddress " + hardwareAddress.getStartAddress(), e);
            }
        }
    }


    public Optional<Object> getValue(HardwareAddress hardwareAddress, String dataType) {
        if (hardwareAddress.getType().equals("holding32") || hardwareAddress.getType().equals("holding64") || hardwareAddress.getType().equals("holding")) {
            try {
                return Optional.of(parseHoldingResponse(tcpModbusSocket.readHoldingRegisters(hardwareAddress.getStartAddress(),
                        hardwareAddress.getValueCount()), dataType, hardwareAddress));
            } catch (Exception e) {
                log.warn("Could not read holding register with startAddress " + hardwareAddress.getStartAddress(), e);
                return Optional.empty();
            }
        } else if (hardwareAddress.getType().equals("coil")) {
            try {
                return Optional.of(parseCoilResponse(tcpModbusSocket.readCoils(hardwareAddress.getStartAddress(), hardwareAddress.getValueCount())));
            } catch (Exception e) {
                log.warn("Could not read coil with startAddress " + hardwareAddress.getStartAddress(), e);
                return Optional.empty();
            }
        } else {
            log.warn("Modbus type {} not valid.", hardwareAddress.getType());
            return Optional.empty();
        }
    }

    public void disconnect() {
        tcpModbusSocket.disconnect();
    }

    /**
     * Parse a holding register response to a object of the given data type
     *
     * @param response to parse
     * @param dataType expected from the response
     * @return object instance of the given data type parsed from the holding register response
     */
    private Object parseHoldingResponse(ReadMultipleRegistersResponse response, String dataType, HardwareAddress hardwareAddress) {
        final Integer[] respValues = new Integer[response.getByteCount()];
        final ByteBuffer bb = ByteBuffer.allocate(2 * response.getByteCount());
        for (int i = 0; i < response.getWordCount(); i++) {
            respValues[i] = response.getRegisterValue(i);
            bb.putShort(respValues[i].shortValue());
        }
        bb.rewind();
        switch (dataType) {
            case "bool":
            case "java.lang.Boolean":
                short s = bb.getShort();
                return readBooleanFromShort(s, hardwareAddress.getBitNumber());
            case "s8":
            case "u8":
            case "java.lang.Byte":
                return bb.get();
            case "s16":
            case "u16":
            case "java.lang.Short":
                return bb.getShort();
            case "u32":
            case "s32":
            case "java.lang.Integer":
                return bb.getInt();
            case "s64":
            case "java.lang.Long": //"u64" (switched to XML Config)
                return bb.getLong();
            case "float32":
            case "java.lang.Float":
                return bb.getFloat();
            case "java.lang.Double":
            case "float64":
                return bb.getDouble();
            default:
                throw new IllegalArgumentException("Datatype " + dataType + " could not be converted");
        }
    }

    /**
     * Parses a coil response to a single boolean or a boolean array
     *
     * @param response to parse
     * @return single boolean or boolean array depending on the response
     */
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

    public Optional<Object[]> preDataProcessing(final Object writeValue, HardwareAddress hardwareAddress, String dataType) {
        if (hardwareAddress.getType().equals("holding32") ||
                hardwareAddress.getType().equals("holding64")) {
            final Integer[] result = new Integer[hardwareAddress.getValueCount()];
            ByteBuffer bb = ByteBuffer.allocate(2 * hardwareAddress.getValueCount());
            byte[] bytes = null;
            switch (hardwareAddress.getValueCount()) {
                case 4:
                    if (dataType.equals(Long.class.getCanonicalName())) {
                        Number num = (Number) writeValue;
                        bytes = bb.putLong(num.longValue()).array();
                        break;
                    }
                    if (dataType.equals(Double.class.getCanonicalName())) {
                        Number num = (Number) writeValue;
                        bytes = bb.putDouble(num.doubleValue()).array();
                    }
                    break;
                case 2:
                    if (dataType.equals(Integer.class.getCanonicalName())) {
                        bytes = bb.putInt((Integer) writeValue).array();
                        break;
                    }
                    if (dataType.equals(Long.class.getCanonicalName())) {
                        Number num = (Number) writeValue;
                        bytes = bb.putInt(num.intValue()).array();
                        break;
                    }
                    if (dataType.equals(Float.class.getCanonicalName())) {
                        Number num = (Number) writeValue;
                        bytes = bb.putFloat(num.floatValue()).array();
                    }
                    break;
            }
            if (bytes != null) {
                for (int i = 0; i < hardwareAddress.getValueCount(); i++) {
                    short aShort = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 2 * i, 2 * i + 2)).getShort();
                    result[i] = (aShort < 0) ? (aShort + 65536) : aShort;
                }
                log.debug(Arrays.toString(result));
                return Optional.of(result);
            }
            throw new RuntimeException("preDataProcessing: Cannot process data of type " + dataType +
                    " for write value count " + hardwareAddress.getValueCount() +
                    " from holding32/64 register");
        }
        if (hardwareAddress.getType().equals("holding") && dataType.equals(Boolean.class.getCanonicalName())) {
            try {
                short registerValue = (short) getValue(hardwareAddress, "java.lang.Short").orElseThrow(RuntimeException::new);
                if(log.isTraceEnabled()) {
                    log.trace("Register value before: {}", registerValue);
                }
                byte[] shortInBytes = new byte[2];
                shortInBytes[0] = (byte) (registerValue & 0xff);
                shortInBytes[1] = (byte) ((registerValue >> 8) & 0xff);
                if(log.isTraceEnabled()) {
                    log.trace("Before Bit number {} : {}", hardwareAddress.getBitNumber(), Integer.toBinaryString(registerValue));
                }
                if (hardwareAddress.getBitNumber() <= 7) {
                    if ((boolean) writeValue) {
                        shortInBytes[0] |= 1L << hardwareAddress.getBitNumber(); //TRUE
                    } else {
                        shortInBytes[0] &= ~(1 << hardwareAddress.getBitNumber());

                    }
                } else {
                    if ((boolean) writeValue) {
                        shortInBytes[1] |= 1L << hardwareAddress.getBitNumber() - 8;
                    } else {
                        shortInBytes[1] &= ~(1 << hardwareAddress.getBitNumber() - 8);
                    }
                }
                if(log.isTraceEnabled()) {
                    log.trace("After Bit number {}: {}", hardwareAddress.getBitNumber(), Integer.toBinaryString(ByteBuffer.wrap(shortInBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get()));
                }
                Object[] result = new Object[1];
                result[0] = (int) ByteBuffer.wrap(shortInBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get();
                if(log.isTraceEnabled()) {
                    log.trace("Holding result {}", result[0]);
                }
                return Optional.of(result);
            } catch (Exception e) {
                log.error("Error while retrieving value for modbus holding register address " + hardwareAddress.getStartAddress(), e);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private boolean readBooleanFromShort(short encoded, int index) {
        log.debug("Reading index {} from {}", index, Integer.toBinaryString((int) encoded));
        return (encoded & (1L << index)) != 0;
    }
}
