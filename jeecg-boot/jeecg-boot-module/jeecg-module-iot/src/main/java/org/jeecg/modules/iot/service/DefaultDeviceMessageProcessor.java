package org.jeecg.modules.iot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jeecg.modules.iot.model.DeviceMessage;
import org.jeecg.modules.iot.model.DeviceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation that simply acknowledges the device payload and echoes the content.
 * This can be replaced with a project specific processor bean.
 */
public class DefaultDeviceMessageProcessor implements DeviceMessageProcessor {

    private static final Logger log = LoggerFactory.getLogger(DefaultDeviceMessageProcessor.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public DeviceResponse process(DeviceMessage message) {
        log.info("Received device message: uri={}, method={}, payload={}", message.getUri(), message.getMethod(), message.getPayload());
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", "OK");
        responseBody.put("receivedUri", message.getUri());
        responseBody.put("receivedMethod", message.getMethod());
        responseBody.put("receivedPayload", message.getPayload());
        try {
            String body = OBJECT_MAPPER.writeValueAsString(responseBody);
            return DeviceResponse.builder()
                    .statusCode(200)
                    .body(body)
                    .build();
        } catch (Exception e) {
            log.error("Failed to serialize response for device message", e);
            return DeviceResponse.builder()
                    .statusCode(500)
                    .body("{\"status\":\"ERROR\"}")
                    .build();
        }
    }
}
