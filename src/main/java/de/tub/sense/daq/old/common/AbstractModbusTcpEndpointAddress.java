package de.tub.sense.daq.old.common;

/**
 * @author maxmeyer
 * @created 11/12/2020 - 13:11
 * @project DAQConfigLoader
 */

public abstract class AbstractModbusTcpEndpointAddress {
    protected String host;

    protected Integer port;

    protected Integer unitID;

    public String getHost() {
        return this.host;
    }

    public Integer getPort() {
        return this.port;
    }

    public Integer getUnitID() {
        return this.unitID;
    }

    public String toString() {
        return "modbus//" + getHost() + ":" + getPort() + ":" + getUnitID();
    }
}
