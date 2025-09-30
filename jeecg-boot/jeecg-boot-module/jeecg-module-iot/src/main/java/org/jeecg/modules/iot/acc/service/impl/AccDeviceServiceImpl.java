package org.jeecg.modules.iot.acc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.iot.acc.entity.AccDevice;
import org.jeecg.modules.iot.acc.enums.AccDeviceStatus;
import org.jeecg.modules.iot.acc.mapper.AccDeviceMapper;
import org.jeecg.modules.iot.acc.service.AccDeviceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Default implementation for {@link AccDeviceService}.
 */
@Service
@RequiredArgsConstructor
public class AccDeviceServiceImpl extends JeecgServiceImpl<AccDeviceMapper, AccDevice> implements AccDeviceService {

    private final ObjectMapper objectMapper;

    @Override
    public Optional<AccDevice> findBySn(String sn) {
        if (StringUtils.isBlank(sn)) {
            return Optional.empty();
        }
        return Optional.ofNullable(getOne(new LambdaQueryWrapper<AccDevice>()
                .eq(AccDevice::getSn, sn)
                .last("limit 1"), false));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccDevice recordInitialization(String sn, String deviceType, Map<String, String> queryParams,
                                          String clientIp, String rawPayload, LocalDateTime now) {
        AccDevice device = findBySn(sn).orElseGet(() -> {
            AccDevice created = new AccDevice();
            created.setSn(sn);
            created.setStatus(AccDeviceStatus.PENDING);
            created.setAuthorized(Boolean.FALSE);
            created.setDeviceType(deviceType);
            return created;
        });
        if (StringUtils.isNotBlank(deviceType)) {
            device.setDeviceType(deviceType);
        }
        device.setInitPayload(rawPayload);
        device.setLastInitTime(now);
        device.setLastKnownIp(StringUtils.defaultIfBlank(clientIp, device.getLastKnownIp()));
        if (queryParams != null && queryParams.containsKey("DeviceType")) {
            device.setDeviceType(queryParams.get("DeviceType"));
        }
        saveOrUpdate(device);
        return device;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccDevice recordRegistry(String sn, Map<String, String> registryParams, String clientIp,
                                    String rawPayload, LocalDateTime now) {
        AccDevice device = findBySn(sn).orElseGet(() -> {
            AccDevice created = new AccDevice();
            created.setSn(sn);
            created.setStatus(AccDeviceStatus.PENDING);
            created.setAuthorized(Boolean.FALSE);
            return created;
        });
        device.setLastRegistryTime(now);
        device.setLastKnownIp(StringUtils.defaultIfBlank(clientIp, device.getLastKnownIp()));
        if (registryParams != null) {
            device.setDeviceType(StringUtils.defaultIfBlank(registryParams.get("DeviceType"), device.getDeviceType()));
            device.setDeviceName(firstNonBlank(registryParams.get("DeviceName"), registryParams.get("~DeviceName"), device.getDeviceName()));
            device.setFirmwareVersion(StringUtils.defaultIfBlank(registryParams.get("FirmVer"), device.getFirmwareVersion()));
            device.setPushVersion(StringUtils.defaultIfBlank(registryParams.get("PushVersion"), device.getPushVersion()));
            device.setLockCount(parseInteger(registryParams.get("LockCount"), device.getLockCount()));
            device.setReaderCount(parseInteger(registryParams.get("ReaderCount"), device.getReaderCount()));
            device.setMachineType(parseInteger(registryParams.get("MachineType"), device.getMachineType()));
            device.setIpAddress(StringUtils.defaultIfBlank(registryParams.get("IPAddress"), device.getIpAddress()));
            device.setGatewayIp(StringUtils.defaultIfBlank(registryParams.get("GATEIPAddress"), device.getGatewayIp()));
            device.setNetMask(StringUtils.defaultIfBlank(registryParams.get("NetMask"), device.getNetMask()));
            try {
                device.setRegistryPayload(objectMapper.writeValueAsString(registryParams));
            } catch (JsonProcessingException e) {
                device.setRegistryPayload(rawPayload);
            }
        }
        if (StringUtils.isBlank(device.getDeviceType()) && registryParams != null) {
            device.setDeviceType(registryParams.get("DeviceType"));
        }
        saveOrUpdate(device);
        return device;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markHeartbeat(String sn, String clientIp, LocalDateTime heartbeatTime) {
        findBySn(sn).ifPresent(device -> {
            device.setLastHeartbeatTime(heartbeatTime);
            if (StringUtils.isNotBlank(clientIp)) {
                device.setLastKnownIp(clientIp);
            }
            updateById(device);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(String sn, AccDeviceStatus status, boolean authorized) {
        findBySn(sn).ifPresent(device -> {
            device.setStatus(status);
            device.setAuthorized(authorized);
            updateById(device);
        });
    }

    private Integer parseInteger(String value, Integer defaultValue) {
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }
}
