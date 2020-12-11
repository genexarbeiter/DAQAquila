package de.tub.sense.daq.old.address.data;

import com.ghgande.j2mod.modbus.msg.ReadCoilsResponse;
import com.ghgande.j2mod.modbus.msg.ReadInputDiscretesResponse;
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersResponse;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
import de.tub.sense.daq.modbus.protocols.ModbusFunctionFactory;

import java.nio.ByteBuffer;
import java.util.Objects;

public interface IModbusTcpDataTagHardwareAddress extends IModbusTcpTagHardwareAddress {
    int getStartAddress();

    int getReadValueCount();

    READ_TYPE getReadingType();

    ModbusData readData(ModbusFunctionFactory paramModbusFunctionFactory) throws Exception;

    public enum READ_TYPE {
        DISCRETES {
            public IModbusTcpDataTagHardwareAddress.ModbusData read(ModbusFunctionFactory socket, Integer startAddress, Integer readValueCount) throws Exception {
                Objects.requireNonNull(socket);
                Objects.requireNonNull(startAddress);
                readValueCount = Integer.valueOf((readValueCount == null) ? 1 : readValueCount.intValue());
                if (readValueCount.intValue() == 1) {
                    final ReadInputDiscretesResponse response = socket.readDiscreteInputs(startAddress.intValue());
                    if (response != null)
                        return new IModbusTcpDataTagHardwareAddress.ModbusDataAdaper() {
                            public Object getValue() {
                                return Boolean.valueOf(response.getDiscreteStatus(0));
                            }
                        };
                } else {
                    final ReadInputDiscretesResponse response = socket.readDiscreteInputs(startAddress.intValue(), readValueCount);
                    if (response != null) {
                        final Boolean[] respValues = new Boolean[readValueCount.intValue()];
                        for (int i = 0; i < readValueCount.intValue(); i++)
                            respValues[i] = Boolean.valueOf(response.getDiscreteStatus(i));
                        return new IModbusTcpDataTagHardwareAddress.ModbusDataAdaper() {
                            public Object[] getValues() {
                                return (Object[]) respValues;
                            }
                        };
                    }
                }
                return null;
            }
        },
        COILS {
            public IModbusTcpDataTagHardwareAddress.ModbusData read(ModbusFunctionFactory socket, Integer startAddress, Integer readValueCount) throws Exception {
                Objects.requireNonNull(socket);
                Objects.requireNonNull(startAddress);
                readValueCount = Integer.valueOf((readValueCount == null) ? 1 : readValueCount.intValue());
                if (readValueCount.intValue() == 1) {
                    final ReadCoilsResponse response = socket.readCoils(startAddress.intValue());
                    if (response != null)
                        return new IModbusTcpDataTagHardwareAddress.ModbusDataAdaper() {
                            public Object getValue() {
                                return Boolean.valueOf(response.getCoilStatus(0));
                            }
                        };
                } else {
                    final ReadCoilsResponse response = socket.readCoils(startAddress.intValue(), readValueCount);
                    if (response != null) {
                        final Boolean[] respValues = new Boolean[readValueCount.intValue()];
                        for (int i = 0; i < readValueCount.intValue(); i++)
                            respValues[i] = Boolean.valueOf(response.getCoilStatus(i));
                        return new IModbusTcpDataTagHardwareAddress.ModbusDataAdaper() {
                            public Object[] getValues() {
                                return (Object[]) respValues;
                            }
                        };
                    }
                }
                return null;
            }
        },
        INPUT {
            public IModbusTcpDataTagHardwareAddress.ModbusData read(ModbusFunctionFactory socket, Integer startAddress, Integer readValueCount) throws Exception {
                Objects.requireNonNull(socket);
                Objects.requireNonNull(startAddress);
                readValueCount = Integer.valueOf((readValueCount == null) ? 1 : readValueCount.intValue());
                if (readValueCount.intValue() == 1) {
                    final ReadInputRegistersResponse response = socket.readInputRegisters(startAddress.intValue());
                    if (response != null)
                        return new IModbusTcpDataTagHardwareAddress.ModbusDataAdaper() {
                            public Object getValue() {
                                return Integer.valueOf(response.getRegisterValue(0));
                            }
                        };
                } else {
                    final ReadInputRegistersResponse response = socket.readInputRegisters(startAddress.intValue(), readValueCount);
                    if (response != null) {
                        final Integer[] respValues = new Integer[readValueCount.intValue()];
                        for (int i = 0; i < readValueCount.intValue(); i++)
                            respValues[i] = Integer.valueOf(response.getRegisterValue(i));
                        return new IModbusTcpDataTagHardwareAddress.ModbusDataAdaper() {
                            public Object[] getValues() {
                                return (Object[]) respValues;
                            }
                        };
                    }
                }
                return null;
            }
        },
        INPUT32 {
            public IModbusTcpDataTagHardwareAddress.ModbusData read(ModbusFunctionFactory socket, Integer startAddress, Integer readValueCount) throws Exception {
                Objects.requireNonNull(socket);
                Objects.requireNonNull(startAddress);
                readValueCount = Integer.valueOf((readValueCount == null) ? 2 : readValueCount.intValue());
                if (readValueCount.intValue() != 2)
                    throw new IllegalStateException("Reading 32 Bit value needs 'readValueCount=null' or 'readValueCount=2'!");
                ReadInputRegistersResponse response = socket.readInputRegisters(startAddress.intValue(), readValueCount);
                if (response != null) {
                    final Integer[] respValues = new Integer[readValueCount.intValue()];
                    final ByteBuffer bb = ByteBuffer.allocate(2 * readValueCount.intValue());
                    for (int i = 0; i < readValueCount.intValue(); i++) {
                        respValues[i] = Integer.valueOf(response.getRegisterValue(i));
                        bb.putShort(respValues[i].shortValue());
                    }
                    bb.rewind();
                    return new IModbusTcpDataTagHardwareAddress.ModbusData() {
                        public Object getValue() {
                            return bb;
                        }

                        public Object[] getValues() {
                            return (Object[]) respValues;
                        }
                    };
                }
                return null;
            }
        },
        INPUT64 {
            public IModbusTcpDataTagHardwareAddress.ModbusData read(ModbusFunctionFactory socket, Integer startAddress, Integer readValueCount) throws Exception {
                Objects.requireNonNull(socket);
                Objects.requireNonNull(startAddress);
                readValueCount = Integer.valueOf((readValueCount == null) ? 4 : readValueCount.intValue());
                if (readValueCount.intValue() != 4)
                    throw new IllegalStateException("Reading 64 Bit value needs 'readValueCount=null' or 'readValueCount=4'!");
                ReadInputRegistersResponse response = socket.readInputRegisters(startAddress.intValue(), readValueCount);
                if (response != null) {
                    final Integer[] respValues = new Integer[readValueCount.intValue()];
                    final ByteBuffer bb = ByteBuffer.allocate(2 * readValueCount.intValue());
                    for (int i = 0; i < readValueCount.intValue(); i++) {
                        respValues[i] = Integer.valueOf(response.getRegisterValue(i));
                        bb.putShort(respValues[i].shortValue());
                    }
                    bb.rewind();
                    return new IModbusTcpDataTagHardwareAddress.ModbusData() {
                        public Object getValue() {
                            return bb;
                        }

                        public Object[] getValues() {
                            return (Object[]) respValues;
                        }
                    };
                }
                return null;
            }
        },
        HOLDING {
            public IModbusTcpDataTagHardwareAddress.ModbusData read(ModbusFunctionFactory socket, Integer startAddress, Integer readValueCount) throws Exception {
                Objects.requireNonNull(socket);
                Objects.requireNonNull(startAddress);
                readValueCount = Integer.valueOf((readValueCount == null) ? 1 : readValueCount.intValue());
                if (readValueCount.intValue() == 1) {
                    final ReadMultipleRegistersResponse response = socket.readHoldingRegisters(startAddress.intValue());
                    if (response != null)
                        return new IModbusTcpDataTagHardwareAddress.ModbusDataAdaper() {
                            public Object getValue() {
                                return Integer.valueOf(response.getRegisterValue(0));
                            }
                        };
                } else {
                    final ReadMultipleRegistersResponse response = socket.readHoldingRegisters(startAddress.intValue(), readValueCount);
                    if (response != null) {
                        final Integer[] respValues = new Integer[readValueCount.intValue()];
                        for (int i = 0; i < readValueCount.intValue(); i++)
                            respValues[i] = Integer.valueOf(response.getRegisterValue(i));
                        return new IModbusTcpDataTagHardwareAddress.ModbusDataAdaper() {
                            public Object[] getValues() {
                                return (Object[]) respValues;
                            }
                        };
                    }
                }
                return null;
            }
        },
        HOLDING32 {
            public IModbusTcpDataTagHardwareAddress.ModbusData read(ModbusFunctionFactory socket, Integer startAddress, Integer readValueCount) throws Exception {
                Objects.requireNonNull(socket);
                Objects.requireNonNull(startAddress);
                readValueCount = Integer.valueOf((readValueCount == null) ? 2 : readValueCount.intValue());
                if (readValueCount.intValue() != 2)
                    throw new IllegalStateException("Reading 32 Bit value needs 'readValueCount=null' or 'readValueCount=2'!");
                ReadMultipleRegistersResponse response = socket.readHoldingRegisters(startAddress.intValue(), readValueCount);
                if (response != null) {
                    final Integer[] respValues = new Integer[readValueCount.intValue()];
                    final ByteBuffer bb = ByteBuffer.allocate(2 * readValueCount.intValue());
                    for (int i = 0; i < readValueCount.intValue(); i++) {
                        respValues[i] = Integer.valueOf(response.getRegisterValue(i));
                        bb.putShort(respValues[i].shortValue());
                    }
                    bb.rewind();
                    return new IModbusTcpDataTagHardwareAddress.ModbusData() {
                        public Object getValue() {
                            return bb;
                        }

                        public Object[] getValues() {
                            return (Object[]) respValues;
                        }
                    };
                }
                return null;
            }
        },
        HOLDING64 {
            public IModbusTcpDataTagHardwareAddress.ModbusData read(ModbusFunctionFactory socket, Integer startAddress, Integer readValueCount) throws Exception {
                Objects.requireNonNull(socket);
                Objects.requireNonNull(startAddress);
                readValueCount = Integer.valueOf((readValueCount == null) ? 4 : readValueCount.intValue());
                if (readValueCount.intValue() != 4)
                    throw new IllegalStateException("Reading 64 Bit value needs 'readValueCount=null' or 'readValueCount=4'!");
                ReadMultipleRegistersResponse response = socket.readHoldingRegisters(startAddress.intValue(), readValueCount);
                if (response != null) {
                    final Integer[] respValues = new Integer[readValueCount.intValue()];
                    final ByteBuffer bb = ByteBuffer.allocate(2 * readValueCount.intValue());
                    for (int i = 0; i < readValueCount.intValue(); i++) {
                        respValues[i] = Integer.valueOf(response.getRegisterValue(i));
                        bb.putShort(respValues[i].shortValue());
                    }
                    bb.rewind();
                    return new IModbusTcpDataTagHardwareAddress.ModbusData() {
                        public Object getValue() {
                            return bb;
                        }

                        public Object[] getValues() {
                            return (Object[]) respValues;
                        }
                    };
                }
                return null;
            }
        };

        public abstract IModbusTcpDataTagHardwareAddress.ModbusData read(ModbusFunctionFactory param1ModbusFunctionFactory, Integer param1Integer1, Integer param1Integer2) throws Exception;
    }

    public static interface ModbusData {
        Object getValue();

        Object[] getValues();
    }

    public static class ModbusDataAdaper implements ModbusData {
        public Object getValue() {
            return null;
        }

        public Object[] getValues() {
            return null;
        }
    }
}
