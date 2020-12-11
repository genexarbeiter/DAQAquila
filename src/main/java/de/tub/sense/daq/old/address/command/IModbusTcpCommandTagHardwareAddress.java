package de.tub.sense.daq.old.address.command;

import de.tub.sense.daq.old.address.data.IModbusTcpTagHardwareAddress;
import de.tub.sense.daq.modbus.protocols.ModbusFunctionFactory;

import java.util.Objects;

/**
 * @author maxmeyer
 * @created 11/12/2020 - 13:33
 * @project DAQConfigLoader
 */
public interface IModbusTcpCommandTagHardwareAddress extends IModbusTcpTagHardwareAddress {
    int getStartAddress();

    int getWriteValueCount();

    WRITE_TYPE getWritingType();

    void writeData(ModbusFunctionFactory paramModbusFunctionFactory, ModbusData paramModbusData) throws Exception;

    public enum WRITE_TYPE {
        COILS {
            public void write(ModbusFunctionFactory socket, Integer startAddress, IModbusTcpCommandTagHardwareAddress.ModbusData data) throws Exception {
                Objects.requireNonNull(socket);
                Objects.requireNonNull(startAddress);
                Objects.requireNonNull(data);
                if (data.getValue() != null && data.getValue() instanceof Boolean) {
                    socket.writeCoil(startAddress.intValue(), ((Boolean)data.getValue()).booleanValue());
                } else if (data.getValues() != null && data.getValues() instanceof Boolean[]) {
                    boolean[] b = new boolean[(data.getValues()).length];
                    for (int i = 0; i < (data.getValues()).length; i++)
                        b[i] = ((Boolean)data.getValues()[i]).booleanValue();
                    socket.writeCoils(startAddress.intValue(), b);
                } else {
                    throw new IllegalArgumentException("Object or Object[] to be written is not of type Boolean or Boolean[]!");
                }
            }
        },
        HOLDING {
            public void write(ModbusFunctionFactory socket, Integer startAddress, IModbusTcpCommandTagHardwareAddress.ModbusData data) throws Exception {
                Objects.requireNonNull(socket);
                Objects.requireNonNull(startAddress);
                Objects.requireNonNull(data);
                if (data.getValue() != null && data.getValue() instanceof Integer) {
                    socket.writeRegister(startAddress.intValue(), ((Integer)data.getValue()).intValue());
                } else if (data.getValues() != null && data.getValues() instanceof Integer[]) {
                    int[] l = new int[(data.getValues()).length];
                    for (int i = 0; i < (data.getValues()).length; i++)
                        l[i] = ((Integer)data.getValues()[i]).intValue();
                    socket.writeRegisters(startAddress.intValue(), l);
                } else {
                    throw new IllegalArgumentException("Object or Object[] to be written is not of type Integer or Integer[]!");
                }
            }
        },
        HOLDING32 {
            public void write(ModbusFunctionFactory socket, Integer startAddress, IModbusTcpCommandTagHardwareAddress.ModbusData data) throws Exception {
                Objects.requireNonNull(socket);
                Objects.requireNonNull(startAddress);
                Objects.requireNonNull(data);
                if (data.getValue() == null && data.getValues() != null && data.getValues() instanceof Integer[]) {
                    if ((data.getValues()).length != 2)
                        throw new IllegalStateException("Writing 32 Bit value needs always two values!");
                    int[] l = new int[(data.getValues()).length];
                    for (int i = 0; i < (data.getValues()).length; i++)
                        l[i] = ((Integer)data.getValues()[i]).intValue();
                    socket.writeRegisters(startAddress.intValue(), l);
                } else {
                    throw new IllegalArgumentException("Object[] to be written is not of type Integer[]!");
                }
            }
        },
        HOLDING64 {
            public void write(ModbusFunctionFactory socket, Integer startAddress, IModbusTcpCommandTagHardwareAddress.ModbusData data) throws Exception {
                Objects.requireNonNull(socket);
                Objects.requireNonNull(startAddress);
                Objects.requireNonNull(data);
                if (data.getValue() == null && data.getValues() != null && data.getValues() instanceof Integer[]) {
                    if ((data.getValues()).length != 4)
                        throw new IllegalStateException("Writing 64 Bit value needs always four values!");
                    int[] l = new int[(data.getValues()).length];
                    for (int i = 0; i < (data.getValues()).length; i++)
                        l[i] = ((Integer)data.getValues()[i]).intValue();
                    socket.writeRegisters(startAddress.intValue(), l);
                } else {
                    throw new IllegalArgumentException("Object[] to be written is not of type Integer[]!");
                }
            }
        };

        public abstract void write(ModbusFunctionFactory param1ModbusFunctionFactory, Integer param1Integer, IModbusTcpCommandTagHardwareAddress.ModbusData param1ModbusData) throws Exception;
    }

    public static class ModbusDataAdapter implements ModbusData {
        public Object getValue() {
            return null;
        }

        public Object[] getValues() {
            return null;
        }
    }

    public static interface ModbusData {
        Object getValue();

        Object[] getValues();
    }
}
