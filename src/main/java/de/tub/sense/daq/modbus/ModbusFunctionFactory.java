package de.tub.sense.daq.modbus;

import com.ghgande.j2mod.modbus.msg.*;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleInputRegister;

public abstract class ModbusFunctionFactory {

    protected abstract ModbusResponse execute(ModbusRequest paramModbusRequest) throws Exception;

    public ReadCoilsResponse readCoils(int startAddress) throws Exception {
        return readCoils(startAddress, null);
    }

    public ReadCoilsResponse readCoils(int startAddress, Integer count) throws Exception {
        int c = 1;
        if (count != null)
            c = count;
        ReadCoilsRequest request = new ReadCoilsRequest(startAddress, c);
        return (ReadCoilsResponse) execute(request);
    }

    public ReadInputDiscretesResponse readDiscreteInputs(int startAddress) throws Exception {
        return readDiscreteInputs(startAddress, null);
    }

    public ReadInputDiscretesResponse readDiscreteInputs(int startAddress, Integer count) throws Exception {
        int c = 1;
        if (count != null)
            c = count;
        ReadInputDiscretesRequest request = new ReadInputDiscretesRequest(startAddress, c);
        return (ReadInputDiscretesResponse) execute(request);
    }

    public ReadMultipleRegistersResponse readHoldingRegisters(int startAddress) throws Exception {
        return readHoldingRegisters(startAddress, null);
    }

    public ReadMultipleRegistersResponse readHoldingRegisters(int startAddress, Integer count) throws Exception {
        int c = 1;
        if (count != null)
            c = count;
        ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(startAddress, c);
        return (ReadMultipleRegistersResponse) execute(request);
    }

    public ReadInputRegistersResponse readInputRegisters(int startAddress) throws Exception {
        return readInputRegisters(startAddress, null);
    }

    public ReadInputRegistersResponse readInputRegisters(int startAddress, Integer count) throws Exception {
        int c = 1;
        if (count != null)
            c = count;
        ReadInputRegistersRequest request = new ReadInputRegistersRequest(startAddress, c);
        return (ReadInputRegistersResponse) execute(request);
    }

    public WriteCoilResponse writeCoil(int startAddress, boolean value) throws Exception {
        WriteCoilRequest request = new WriteCoilRequest(startAddress, value);
        return (WriteCoilResponse) execute(request);
    }

    public WriteMultipleCoilsResponse writeCoils(int startAddress, boolean[] values) throws Exception {
        WriteMultipleCoilsRequest request = new WriteMultipleCoilsRequest(startAddress, values.length);
        for (int i = 0; i < values.length; i++)
            request.getCoils().setBit(i, values[i]);
        return (WriteMultipleCoilsResponse) execute(request);
    }

    public WriteSingleRegisterResponse writeRegister(int startAddress, int value) throws Exception {
        WriteSingleRegisterRequest request = new WriteSingleRegisterRequest(startAddress, new SimpleInputRegister(value));
        return (WriteSingleRegisterResponse) execute(request);
    }

    public WriteMultipleRegistersResponse writeRegisters(int startAddress, int[] values) throws Exception {
        Register[] regs = new Register[values.length];
        for (int i = 0; i < values.length; i++)
            regs[i] = new SimpleInputRegister(values[i]);
        WriteMultipleRegistersRequest request = new WriteMultipleRegistersRequest(startAddress, regs);
        return (WriteMultipleRegistersResponse) execute(request);
    }
}