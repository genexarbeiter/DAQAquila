package de.tub.sense.daq.old.exception;


public class ModbusTcpCommunicationException extends RuntimeException {
    public ModbusTcpCommunicationException() {}

    public ModbusTcpCommunicationException(String message) {
        super(message);
    }

    public ModbusTcpCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModbusTcpCommunicationException(Throwable cause) {
        super(cause);
    }

    public ModbusTcpCommunicationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
