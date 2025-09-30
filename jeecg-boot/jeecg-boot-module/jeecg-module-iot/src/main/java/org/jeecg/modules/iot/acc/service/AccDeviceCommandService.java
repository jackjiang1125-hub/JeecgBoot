package org.jeecg.modules.iot.acc.service;

import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.iot.acc.entity.AccDeviceCommand;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for queuing and tracking commands destined for access control devices.
 */
public interface AccDeviceCommandService extends JeecgService<AccDeviceCommand> {

    /**
     * Enqueue raw command payloads for a device.
     *
     * @param sn        device serial number
     * @param commands  raw command lines
     * @param operator  username of the operator creating the commands
     * @return persisted commands
     */
    List<AccDeviceCommand> enqueueCommands(String sn, List<String> commands, String operator);

    /**
     * Mark queued commands as delivered to the device.
     *
     * @param commandIds ids of commands delivered
     * @param sentTime   delivery timestamp
     */
    void markCommandsSent(List<String> commandIds, LocalDateTime sentTime);

    /**
     * Update command status based on a device acknowledgement payload.
     *
     * @param sn            device serial number
     * @param commandCode   command identifier reported by the device
     * @param resultCode    acknowledgement result code
     * @param resultMessage acknowledgement message
     * @param rawPayload    raw acknowledgement payload
     * @param clientIp      ip address reporting the acknowledgement
     * @return updated command if found
     */
    Optional<AccDeviceCommand> handleCommandReport(String sn, String commandCode, String resultCode,
                                                   String resultMessage, String rawPayload, String clientIp);
}
