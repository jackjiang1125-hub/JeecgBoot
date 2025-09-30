package org.jeecg.modules.iot.acc.service;

import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.iot.acc.entity.AccDevice;
import org.jeecg.modules.iot.acc.enums.AccDeviceStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Service for persisting and updating access control device metadata.
 */
public interface AccDeviceService extends JeecgService<AccDevice> {

    Optional<AccDevice> findBySn(String sn);

    AccDevice recordInitialization(String sn, String deviceType, Map<String, String> queryParams,
                                   String clientIp, String rawPayload, LocalDateTime now);

    AccDevice recordRegistry(String sn, Map<String, String> registryParams, String clientIp,
                             String rawPayload, LocalDateTime now);

    void markHeartbeat(String sn, String clientIp, LocalDateTime heartbeatTime);

    void updateStatus(String sn, AccDeviceStatus status, boolean authorized);

    Optional<AccDevice> authorizeDevice(String sn, String registryCode, String remark, String operator);
}
