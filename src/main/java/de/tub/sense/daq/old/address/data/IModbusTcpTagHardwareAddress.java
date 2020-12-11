package de.tub.sense.daq.old.address.data;

import cern.c2mon.shared.common.datatag.address.HardwareAddress;

public interface IModbusTcpTagHardwareAddress extends HardwareAddress {
    String toJson();
}