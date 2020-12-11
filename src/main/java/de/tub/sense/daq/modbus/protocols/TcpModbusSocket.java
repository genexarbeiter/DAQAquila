package de.tub.sense.daq.modbus.protocols;

import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.io.ModbusTransaction;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class TcpModbusSocket extends ModbusFunctionFactory {

    private final TCPMasterConnection oConnection;

    private final Integer oUnitID;

    public TcpModbusSocket(String host) throws UnknownHostException {
        this(host, null);
    }

    public TcpModbusSocket(String host, Integer unitID) throws UnknownHostException {
        this(host, 502, unitID);
    }

    public TcpModbusSocket(String host, int port, Integer unitID) throws UnknownHostException {
        this.oConnection = new TCPMasterConnection(InetAddress.getByName(host));
        getConnection().setPort(port);
        this.oUnitID = unitID;
    }

    public boolean connect() throws Exception {
        if (getConnection().isConnected())
            return true;
        getConnection().connect();
        return getConnection().isConnected();
    }

    public void disconnect() {
        getConnection().close();
    }

    protected synchronized ModbusResponse execute(ModbusRequest request) throws Exception {
        if (!connect())
            throw new ModbusIOException(
                    "Connection failed [" + getConnection().getAddress().toString() + ":" + getConnection().getPort() +
                            "]");
        ModbusTransaction trans = getConnection().getModbusTransport().createTransaction();
        if (this.oUnitID != null && this.oUnitID > 0)
            request.setUnitID(this.oUnitID);
        trans.setRequest(request);
        trans.execute();
        return trans.getResponse();
    }

    protected TCPMasterConnection getConnection() {
        return this.oConnection;
    }
}
