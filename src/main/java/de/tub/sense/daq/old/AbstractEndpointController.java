package de.tub.sense.daq.old;

import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.common.conf.equipment.ICommandTagChanger;
import cern.c2mon.daq.common.conf.equipment.IDataTagChanger;
import cern.c2mon.shared.common.command.ISourceCommandTag;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.address.SimpleHardwareAddress;
import cern.c2mon.shared.common.process.IEquipmentConfiguration;
import cern.c2mon.shared.daq.command.SourceCommandTagValue;
import cern.c2mon.shared.daq.config.ChangeReport;
import de.tub.sense.daq.old.common.AbstractModbusTcpEndpointAddress;
import de.tub.sense.daq.old.connection.ModbusTcpEndpoint;
import de.tub.sense.daq.old.exception.EndpointTypesUnknownException;
import de.tub.sense.daq.old.exception.ModbusCriticalException;
import de.tub.sense.daq.old.exception.ModbusTcpCommunicationException;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class AbstractEndpointController implements ICommandTagChanger, IDataTagChanger {

    protected Logger logger;

    protected List<? extends AbstractModbusTcpEndpointAddress> modbusAddresses = null;

    protected AbstractModbusTcpEndpointAddress currentAddress;

    protected IEquipmentMessageSender sender;

    protected ModbusTcpEndpointFactory modbusEndpointFactory = null;

    protected ModbusTcpEndpoint endpoint;

    protected IEquipmentConfiguration equipmentConfiguration;

    private String noConnectionReason;

    public synchronized boolean startEndpoint() {
        try {
            startProcedure();
        } catch (ModbusTcpCommunicationException e) {
            this.logger.error("Endpoint creation failed. Controller will try again. ", (Throwable) e);
            return false;
        }
        return true;
    }

    public synchronized boolean restartEndpoint() {
        try {
            startProcedure();
        } catch (ModbusTcpCommunicationException e) {
            this.logger.error("Endpoint creation failed. Controller will try again. ", (Throwable) e);
            this.noConnectionReason = "Problems connecting to " + this.currentAddress.getHost() + ": " + e.getMessage();
            return false;
        }
        return true;
    }

    protected synchronized void startProcedure() throws ModbusTcpCommunicationException {
        createEndpoint();
        addTagsToEndpoint();
        this.sender.confirmEquipmentStateOK("Connected to " + this.currentAddress.toString());
        this.endpoint.setStateOperational();
    }

    protected void addTagsToEndpoint() {
        this.endpoint.addDataTags(this.equipmentConfiguration.getSourceDataTags().values());
        this.endpoint.addCommandTags(this.equipmentConfiguration.getSourceCommandTags().values());
    }

    protected void createEndpoint() {
        if (this.endpoint == null || this.endpoint.getState().equals(ModbusTcpEndpoint.STATE.NOT_INITIALIZED)) {
            AbstractModbusTcpEndpointAddress address = getNextModbusTcpAddress();
            this.logger.info("createEndpoint - Trying to create endpoint '" + address + "'");
            this.endpoint = this.modbusEndpointFactory.createEndpoint(address);
            if (this.endpoint == null && this.modbusAddresses.size() > 1) {
                this.logger.warn("createEndpoint - Endpoint creation for '" + address +
                        "' failed. Trying alternative address.");
                address = getNextModbusTcpAddress();
                this.endpoint = this.modbusEndpointFactory.createEndpoint(address);
            }
            if (this.endpoint != null) {
                this.endpoint.initialize(address);
                this.logger.info("createEndpoint - Endpoint '" + address + "' created and initialized");
            } else {
                this.logger.error("createEndpoint - Endpoint creation for '" + address +
                        "' failed. Stop Startup.");
                throw new EndpointTypesUnknownException();
            }
        }
    }

    protected synchronized AbstractModbusTcpEndpointAddress getNextModbusTcpAddress() {
        if (this.currentAddress == null) {
            this.currentAddress = this.modbusAddresses.get(0);
        } else if (this.modbusAddresses.size() > 1) {
            if (((AbstractModbusTcpEndpointAddress) this.modbusAddresses.get(0)).equals(this.currentAddress)) {
                this.currentAddress = this.modbusAddresses.get(1);
            } else {
                this.currentAddress = this.modbusAddresses.get(0);
            }
        }
        return this.currentAddress;
    }

    public synchronized void stop() {
        if (this.endpoint != null)
            this.endpoint.reset();
    }

    protected synchronized AbstractModbusTcpEndpointAddress getCurrentModbusTcpAddress() {
        return this.currentAddress;
    }

    public synchronized void refresh() {
        this.logger.info("refresh - Refreshing values of all data tags.");
        requiresEndpoint();
        this.endpoint.refreshDataTags(this.equipmentConfiguration.getSourceDataTags().values());
    }

    public synchronized void refresh(ISourceDataTag sourceDataTag) {
        requiresEndpoint();
        Collection<ISourceDataTag> tags = Collections.singletonList(sourceDataTag);
        this.logger.info("Refreshing value of data tag with id '" + sourceDataTag.getId() + "'.");
        this.endpoint.refreshDataTags(tags);
    }

    public void onAddCommandTag(ISourceCommandTag sourceCommandTag, ChangeReport changeReport) {
        this.logger.info("Adding command tag " + sourceCommandTag.getId());
        requiresEndpoint();
        this.endpoint.addCommandTag(sourceCommandTag);
        changeReport.appendInfo("CommandTag added.");
        changeReport.setState(ChangeReport.CHANGE_STATE.SUCCESS);
        this.logger.info("Added command tag " + sourceCommandTag.getId());
    }

    public void onRemoveCommandTag(ISourceCommandTag sourceCommandTag, ChangeReport changeReport) {
        this.logger.info("Removing command tag " + sourceCommandTag.getId());
        requiresEndpoint();
        this.endpoint.removeCommandTag(sourceCommandTag);
        changeReport.appendInfo("CommandTag removed.");
        changeReport.setState(ChangeReport.CHANGE_STATE.SUCCESS);
        this.logger.info("Removed command tag " + sourceCommandTag.getId());
    }

    public void onUpdateCommandTag(ISourceCommandTag sourceCommandTag, ISourceCommandTag oldSourceCommandTag, ChangeReport changeReport) {
        this.logger.info("Updating command tag " + sourceCommandTag.getId());
        requiresEndpoint();
        if (!sourceCommandTag.getHardwareAddress().equals(oldSourceCommandTag.getHardwareAddress())) {
            this.endpoint.removeCommandTag(oldSourceCommandTag);
            this.endpoint.addCommandTag(sourceCommandTag);
            changeReport.appendInfo("CommandTag updated.");
        } else {
            changeReport.appendInfo("No changes for Modbus necessary.");
        }
        changeReport.setState(ChangeReport.CHANGE_STATE.SUCCESS);
        this.logger.info("Updated command tag " + sourceCommandTag.getId());
    }

    public void onAddDataTag(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
        this.logger.info("Adding data tag " + sourceDataTag.getId());
        requiresEndpoint();
        this.endpoint.addDataTag(sourceDataTag);
        refresh(sourceDataTag);
        changeReport.appendInfo("DataTag added.");
        changeReport.setState(ChangeReport.CHANGE_STATE.SUCCESS);
        this.logger.info("Added data tag " + sourceDataTag.getId());
    }

    public void onRemoveDataTag(ISourceDataTag sourceDataTag, ChangeReport changeReport) {
        this.logger.info("Removing data tag " + sourceDataTag.getId());
        requiresEndpoint();
        this.endpoint.removeDataTag(sourceDataTag);
        changeReport.appendInfo("DataTag removed.");
        changeReport.setState(ChangeReport.CHANGE_STATE.SUCCESS);
        this.logger.info("Removed data tag " + sourceDataTag.getId());
    }

    public void onUpdateDataTag(ISourceDataTag sourceDataTag, ISourceDataTag oldSourceDataTag, ChangeReport changeReport) {
        this.logger.info("Updating data tag " + sourceDataTag.getId());
        requiresEndpoint();
        if (!sourceDataTag.getHardwareAddress().equals(oldSourceDataTag.getHardwareAddress())) {
            this.endpoint.removeDataTag(oldSourceDataTag);
            this.endpoint.addDataTag(sourceDataTag);
            changeReport.appendInfo("Data tag updated.");
        } else {
            changeReport.appendInfo("No changes for Modbus necessary.");
        }
        changeReport.setState(ChangeReport.CHANGE_STATE.SUCCESS);
        this.logger.info("Updated data tag " + sourceDataTag.getId());
    }

    public String runCommand(ISourceCommandTag commandTag, SourceCommandTagValue sourceCommandTagValue) throws EndpointTypesUnknownException {
        requiresEndpoint();
        return this.endpoint.executeCommand((SimpleHardwareAddress) commandTag.getHardwareAddress(), sourceCommandTagValue);
    }

    private void requiresEndpoint() {
        try {
            if (this.endpoint == null || this.endpoint.getState().equals(ModbusTcpEndpoint.STATE.NOT_INITIALIZED))
                throw new Exception("No Endpoint was created or Endpoint was not initialized/started.");
        } catch (Exception e) {
            throw new ModbusCriticalException(e.getMessage());
        }
    }
}
