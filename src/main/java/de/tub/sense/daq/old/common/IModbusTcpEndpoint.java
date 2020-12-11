package de.tub.sense.daq.old.common;

import cern.c2mon.shared.common.command.ISourceCommandTag;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.address.SimpleHardwareAddress;
import cern.c2mon.shared.daq.command.SourceCommandTagValue;
import de.tub.sense.daq.old.address.data.IModbusTcpCommandTagHardwareAddress;
import de.tub.sense.daq.old.address.data.IModbusTcpDataTagHardwareAddress;

import java.util.Collection;

public interface IModbusTcpEndpoint {
    void refreshDataTags(Collection<ISourceDataTag> paramCollection);

    STATE getState();

    void addCommandTag(ISourceCommandTag paramISourceCommandTag);

    void removeCommandTag(ISourceCommandTag paramISourceCommandTag);

    void addDataTag(ISourceDataTag paramISourceDataTag);

    void removeDataTag(ISourceDataTag paramISourceDataTag);

    String executeCommand(SimpleHardwareAddress paramSimpleHardwareAddress, SourceCommandTagValue paramSourceCommandTagValue);

    void setStateOperational();

    void addDataTags(Collection<ISourceDataTag> paramCollection);

    void addCommandTags(Collection<ISourceCommandTag> paramCollection);

    void initialize(AbstractModbusTcpEndpointAddress paramAbstractModbusTcpEndpointAddress);

    void reset();

    IModbusTcpDataTagHardwareAddress.ModbusData postDataProcessing(IModbusTcpDataTagHardwareAddress.ModbusData paramModbusData, SimpleHardwareAddress paramSimpleHardwareAddress, String paramString);

    IModbusTcpCommandTagHardwareAddress.ModbusData preDataProcessing(Object paramObject, SimpleHardwareAddress paramSimpleHardwareAddress, String paramString);

    enum STATE {
        INITIALIZED, OPERATIONAL, NOT_INITIALIZED;
    }
}