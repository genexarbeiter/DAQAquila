package de.tub.sense.daq.old.connection;

import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.shared.common.command.ISourceCommandTag;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.ValueUpdate;
import cern.c2mon.shared.common.datatag.address.SimpleHardwareAddress;
import cern.c2mon.shared.daq.command.SourceCommandTagValue;
import de.tub.sense.daq.old.address.command.ModbusTcpBitCommandTagHardwareAddress;
import de.tub.sense.daq.old.address.command.ModbusTcpCommandTagHardwareAddress;
import de.tub.sense.daq.old.address.data.*;
import de.tub.sense.daq.old.common.AbstractModbusTcpEndpointAddress;
import de.tub.sense.daq.old.common.IModbusTcpEndpoint;
import de.tub.sense.daq.old.exception.ModbusTcpCommunicationException;
import de.tub.sense.daq.modbus.protocols.TcpModbusSocket;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ModbusTcpEndpoint extends TcpModbusSocket implements IModbusTcpEndpoint {

    private final List<ISourceDataTag> oDataTagCache = new ArrayList<>();

    private final List<ISourceCommandTag> oCommandTagCache = new ArrayList<>();

    private final IEquipmentMessageSender oEquipmentMessageSender;

    private final Logger oEquipmentLogger;

    private ModbusTcpEndpoint.STATE oSTATE;

    public ModbusTcpEndpoint(String host, Integer port, Integer unitID, IEquipmentMessageSender equipmentMessageSender, Logger equipmentLogger) throws UnknownHostException {
        super(host, port, unitID);
        this.oEquipmentMessageSender = equipmentMessageSender;
        this.oEquipmentLogger = equipmentLogger;
        this.oSTATE = ModbusTcpEndpoint.STATE.NOT_INITIALIZED;
    }

    public void refreshDataTags(Collection<ISourceDataTag> values) {
        values.forEach(iSourceDataTag -> {
            if (iSourceDataTag.getHardwareAddress() instanceof SimpleHardwareAddress) {
                try {
                    SimpleHardwareAddress hardwareAddress = (SimpleHardwareAddress) iSourceDataTag.getHardwareAddress();
                    IModbusTcpDataTagHardwareAddress address = BaseModbusTcpTagAddress.getModbusTcpTagHardwareAddress(hardwareAddress, ModbusTcpDataTagHardwareAddress.class);
                    IModbusTcpDataTagHardwareAddress.ModbusData data = postDataProcessing(address.readData(this), hardwareAddress, iSourceDataTag.getDataType());
                    assert data != null;
                    if (data.getValue() instanceof ByteBuffer && data.getValues() != null)
                        throw new RuntimeException("Please provide the correct value from ByteBuffer by overriding method postDataProcessing()!");
                    if (data.getValue() != null) {
                        getEquipmentMessageSender().update(iSourceDataTag.getId(), new ValueUpdate(data.getValue()));
                    } else if (data.getValues() != null) {
                        getEquipmentMessageSender().update(iSourceDataTag.getId(), new ValueUpdate(data.getValues()));
                    }
                } catch (Exception e) {
                    getEquipmentLogger().error("Error while retrieving data value for data tag " + iSourceDataTag.getName(), e);
                }
            } else {
                getEquipmentLogger().error("Datatag '" + iSourceDataTag.getName() + "' has no modbus tcp hardware address!");
            }
        });
    }

    protected IEquipmentMessageSender getEquipmentMessageSender() {
        return this.oEquipmentMessageSender;
    }

    protected Logger getEquipmentLogger() {
        return this.oEquipmentLogger;
    }

    public ModbusTcpEndpoint.STATE getState() {
        return this.oSTATE;
    }

    public void addCommandTag(ISourceCommandTag sourceCommandTag) {
        if (!this.oCommandTagCache.contains(sourceCommandTag))
            this.oCommandTagCache.add(sourceCommandTag);
    }

    public void removeCommandTag(ISourceCommandTag sourceCommandTag) {
        this.oCommandTagCache.remove(sourceCommandTag);
    }

    public void addDataTag(ISourceDataTag sourceDataTag) {
        if (!this.oDataTagCache.contains(sourceDataTag))
            this.oDataTagCache.add(sourceDataTag);
    }

    public void removeDataTag(ISourceDataTag sourceDataTag) {
        this.oDataTagCache.remove(sourceDataTag);
    }

    public String executeCommand(SimpleHardwareAddress hardwareAddress, SourceCommandTagValue sourceCommandTagValue) {
        try {
            IModbusTcpCommandTagHardwareAddress address = (IModbusTcpCommandTagHardwareAddress) BaseModbusTcpTagAddress.getModbusTcpTagHardwareAddress(hardwareAddress, ModbusTcpCommandTagHardwareAddress.class);
            address.writeData(this, preDataProcessing(sourceCommandTagValue.getValue(), hardwareAddress, sourceCommandTagValue.getDataType()));
            return "Value '" + sourceCommandTagValue.getValue() + "' written to " + hardwareAddress.getAddress();
        } catch (Exception e) {
            getEquipmentLogger().error("Error while execution of command tag '" + sourceCommandTagValue.getName() + "'", e);
            return null;
        }
    }

    public void setStateOperational() {
        this.oSTATE = ModbusTcpEndpoint.STATE.OPERATIONAL;
    }

    public void addDataTags(Collection<ISourceDataTag> values) {
        for (ISourceDataTag tag : values)
            addDataTag(tag);
    }

    public void addCommandTags(Collection<ISourceCommandTag> values) {
        for (ISourceCommandTag tag : values)
            addCommandTag(tag);
    }

    public void initialize(AbstractModbusTcpEndpointAddress address) {
        try {
            if (connect())
                this.oSTATE = ModbusTcpEndpoint.STATE.INITIALIZED;
        } catch (Exception e) {
            this.oSTATE = ModbusTcpEndpoint.STATE.NOT_INITIALIZED;
            getEquipmentMessageSender().confirmEquipmentStateIncorrect("Connection failed to endpoint " + address);
            throw new ModbusTcpCommunicationException(e);
        }
    }

    public void reset() {
        disconnect();
    }

    public IModbusTcpDataTagHardwareAddress.ModbusData postDataProcessing(IModbusTcpDataTagHardwareAddress.ModbusData readValue, SimpleHardwareAddress hardwareAddress, String dataType) {
        IModbusTcpDataTagHardwareAddress.ModbusDataAdaper modbusDataAdaper = null;
        if (readValue.getValue() != null && readValue.getValue() instanceof ByteBuffer && readValue.getValues() != null) {
            try {
                IModbusTcpDataTagHardwareAddress address =
                        BaseModbusTcpTagAddress.getModbusTcpTagHardwareAddress(hardwareAddress, ModbusTcpDataTagHardwareAddress.class);
                if (address.getReadingType().equals(IModbusTcpDataTagHardwareAddress.READ_TYPE.HOLDING32) ||
                        address.getReadingType().equals(IModbusTcpDataTagHardwareAddress.READ_TYPE.INPUT32) ||
                        address.getReadingType().equals(IModbusTcpDataTagHardwareAddress.READ_TYPE.HOLDING64) ||
                        address.getReadingType().equals(IModbusTcpDataTagHardwareAddress.READ_TYPE.INPUT64)) {
                    ByteBuffer bb = (ByteBuffer) readValue.getValue();
                    Object retValue = null;
                    switch (address.getReadValueCount()) {
                        case 2:
                            if (dataType.equals(Integer.class.getCanonicalName())) {
                                retValue = bb.getInt();
                                break;
                            }
                            if (dataType.equals(Long.class.getCanonicalName())) {
                                retValue = bb.getInt();
                                break;
                            }
                            if (dataType.equals(Float.class.getCanonicalName()))
                                retValue = bb.getFloat();
                            break;
                        case 4:
                            if (dataType.equals(Long.class.getCanonicalName())) {
                                retValue = bb.getLong();
                                break;
                            }
                            if (dataType.equals(Double.class.getCanonicalName()))
                                retValue = bb.getDouble();
                            break;
                    }
                    if (retValue != null) {
                        final Object finalRetValue = retValue;
                        modbusDataAdaper = new IModbusTcpDataTagHardwareAddress.ModbusDataAdaper() {
                            public Object getValue() {
                                return finalRetValue;
                            }
                        };
                    }
                }
            } catch (IOException e) {
                getEquipmentLogger().error("Error while deserialize modbus tcp data tag hardware address.", e);
                return null;
            }
        } else if (modbusDataAdaper.getValue() != null && modbusDataAdaper.getValues() == null && dataType.equals(Boolean.class.getCanonicalName())) {
            try {
                IModbusTcpDataTagHardwareAddress address =
                        BaseModbusTcpTagAddress.getModbusTcpTagHardwareAddress(hardwareAddress, ModbusTcpDataTagHardwareAddress.class);
                if (address.getReadingType().equals(IModbusTcpDataTagHardwareAddress.READ_TYPE.INPUT) ||
                        address.getReadingType().equals(IModbusTcpDataTagHardwareAddress.READ_TYPE.HOLDING)) {
                    ModbusTcpBitDataTagHardwareAddress bitDataTagHardwareAddress = (ModbusTcpBitDataTagHardwareAddress) BaseModbusTcpTagAddress.getModbusTcpTagHardwareAddress(hardwareAddress, ModbusTcpBitDataTagHardwareAddress.class);
                    final int mask = (int) Math.pow(2.0D, bitDataTagHardwareAddress.getBitNumber());
                    final int v = (Integer) modbusDataAdaper.getValue();
                    modbusDataAdaper = new IModbusTcpDataTagHardwareAddress.ModbusDataAdaper() {
                        public Object getValue() {
                            return ((v & mask) == mask) ? Boolean.valueOf(true) : Boolean.valueOf(false);
                        }
                    };
                }
            } catch (IOException e) {
                getEquipmentLogger().error("Error while deserialize modbus tcp data tag hardware address.", e);
                return modbusDataAdaper;
            }
        }
        return modbusDataAdaper;
    }

    public IModbusTcpCommandTagHardwareAddress.ModbusData preDataProcessing(final Object writeValue, SimpleHardwareAddress hardwareAddress, String dataType) {
        try {
            IModbusTcpCommandTagHardwareAddress address =
                    (IModbusTcpCommandTagHardwareAddress) BaseModbusTcpTagAddress.getModbusTcpTagHardwareAddress(hardwareAddress, ModbusTcpCommandTagHardwareAddress.class);
            if (address.getWritingType().equals(IModbusTcpCommandTagHardwareAddress.WRITE_TYPE.HOLDING32) ||
                    address.getWritingType().equals(IModbusTcpCommandTagHardwareAddress.WRITE_TYPE.HOLDING64)) {
                final Integer[] result = new Integer[address.getWriteValueCount()];
                ByteBuffer bb = ByteBuffer.allocate(2 * address.getWriteValueCount());
                byte[] bytes = null;
                switch (address.getWriteValueCount()) {
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
                    getEquipmentLogger().debug(String.valueOf(bytes.length));
                    getEquipmentLogger().debug(Arrays.toString(bytes));
                    for (int i = 0; i < address.getWriteValueCount(); i++) {
                        getEquipmentLogger().debug("lower limit: " + (2 * i) + " upper limit: " + (2 * i + 2));
                        short aShort = ByteBuffer.wrap(Arrays.copyOfRange(bytes, 2 * i, 2 * i + 2)).getShort();
                        result[i] = (aShort < 0) ? (aShort + 65536) : aShort;
                    }
                    getEquipmentLogger().debug(Arrays.toString(result));
                    return new IModbusTcpCommandTagHardwareAddress.ModbusDataAdapter() {
                        public Object[] getValues() {
                            return result;
                        }
                    };
                }
                throw new RuntimeException("preDataProcessing: Cannot process data of type " + dataType +
                        " for write value count " + address.getWriteValueCount() +
                        " from holding32/64 register");
            }
            if (address.getWritingType().equals(IModbusTcpCommandTagHardwareAddress.WRITE_TYPE.HOLDING) && dataType.equals(Boolean.class.getCanonicalName())) {
                ModbusTcpBitCommandTagHardwareAddress bitCommandTagHardwareAddress = BaseModbusTcpTagAddress.getModbusTcpTagHardwareAddress(hardwareAddress, ModbusTcpBitCommandTagHardwareAddress.class);
                int mask = 1 << bitCommandTagHardwareAddress.getBitNumber();
                try {
                    int registerValue = (Integer) IModbusTcpDataTagHardwareAddress.READ_TYPE.HOLDING.read(this, address.getStartAddress(), 1).getValue();
                    final Object result = (Boolean) writeValue ? (registerValue | mask) : (registerValue & (~mask));
                    return new IModbusTcpCommandTagHardwareAddress.ModbusDataAdapter() {
                        public Object getValue() {
                            return result;
                        }
                    };
                } catch (Exception e) {
                    getEquipmentLogger().error("Error while retrieving value for modbus holding register address " + address.getStartAddress(), e);
                    return null;
                }
            }
        } catch (IOException e) {
            getEquipmentLogger().error("Error while deserialize modbus tcp command tag hardware address.", e);
            return null;
        }
        return new IModbusTcpCommandTagHardwareAddress.ModbusDataAdapter() {
            public Object getValue() {
                return writeValue;
            }
        };
    }
}

