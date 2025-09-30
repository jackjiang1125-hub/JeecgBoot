package org.jeecg.modules.iot.acc.enums;

/**
 * Status values representing the lifecycle of an access control device.
 */
public enum AccDeviceStatus {
    /**
     * Device has been discovered but awaits manual authorization.
     */
    PENDING,
    /**
     * Device has been authorized by an operator and can receive commands.
     */
    AUTHORIZED,
    /**
     * Device authorization has been revoked.
     */
    REVOKED
}
