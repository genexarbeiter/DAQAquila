package de.tub.sense.daq.old.exception;

public class ModbusCriticalException extends RuntimeException {
    public ModbusCriticalException() {}

    public ModbusCriticalException(String message) {
        super(message);
    }

    public ModbusCriticalException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModbusCriticalException(Throwable cause) {
        super(cause);
    }

    public ModbusCriticalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}