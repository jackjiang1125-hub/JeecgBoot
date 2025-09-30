package org.jeecg.modules.iot.acc.enums;

/**
 * Lifecycle of a command issued to an access control device.
 */
public enum AccDeviceCommandStatus {
    /**
     * Command has been queued but not yet delivered to the device.
     */
    PENDING,
    /**
     * Command has been handed to the device during a heartbeat window.
     */
    SENT,
    /**
     * Device reported successful execution of the command.
     */
    ACKED,
    /**
     * Device reported an error while executing the command.
     */
    FAILED
}
