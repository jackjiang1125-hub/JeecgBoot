package org.jeecg.modules.iot.service;

import org.jeecg.modules.iot.model.DeviceMessage;
import org.jeecg.modules.iot.model.DeviceResponse;

/**
 * Strategy interface responsible for converting inbound device messages into responses.
 */
public interface DeviceMessageProcessor {

    /**
     * Process the inbound device message and return a response that will be sent to the device.
     *
     * @param message inbound message
     * @return response to send back to the device
     */
    DeviceResponse process(DeviceMessage message);
}
