package org.jeecg.modules.iot.acc.protocol;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.iot.acc.cache.AccDeviceRedisCache;
import org.jeecg.modules.iot.acc.entity.AccDevice;
import org.jeecg.modules.iot.acc.entity.AccDeviceCommandReport;
import org.jeecg.modules.iot.acc.entity.AccDevicePhoto;
import org.jeecg.modules.iot.acc.entity.AccDeviceRtLog;
import org.jeecg.modules.iot.acc.entity.AccDeviceState;
import org.jeecg.modules.iot.acc.enums.AccDeviceStatus;
import org.jeecg.modules.iot.acc.service.AccDeviceCommandReportService;
import org.jeecg.modules.iot.acc.service.AccDevicePhotoService;
import org.jeecg.modules.iot.acc.service.AccDeviceRtLogService;
import org.jeecg.modules.iot.acc.service.AccDeviceService;
import org.jeecg.modules.iot.acc.service.AccDeviceStateService;
import org.jeecg.modules.iot.acc.util.DevicePayloadParser;
import org.jeecg.modules.iot.model.DeviceMessage;
import org.jeecg.modules.iot.model.DeviceResponse;
import org.jeecg.modules.iot.service.DeviceMessageProcessor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Message processor implementing the proprietary HTTP workflow for access control devices.
 */
@Slf4j
@RequiredArgsConstructor
public class AccDeviceMessageProcessor implements DeviceMessageProcessor {

    private static final String OK = "OK";

    private final AccDeviceService accDeviceService;
    private final AccDeviceRtLogService accDeviceRtLogService;
    private final AccDeviceStateService accDeviceStateService;
    private final AccDevicePhotoService accDevicePhotoService;
    private final AccDeviceCommandReportService accDeviceCommandReportService;
    private final AccDeviceRedisCache redisCache;

    @Override
    public DeviceResponse process(DeviceMessage message) {
        try {
            String path = message.getPath();
            if (StringUtils.isBlank(path)) {
                return DeviceResponse.text(404, "NOT FOUND");
            }
            return switch (path) {
                case "/iclock/cdata" -> handleCdata(message);
                case "/iclock/registry" -> handleRegistry(message);
                case "/iclock/push" -> handlePush(message);
                case "/iclock/getrequest" -> handleHeartbeat(message);
                case "/iclock/devicecmd" -> handleDeviceCommandReport(message);
                default -> DeviceResponse.text(404, "NOT FOUND");
            };
        } catch (Exception e) {
            log.error("Failed to process device message: uri={}, method={}, payload={}",
                    message.getUri(), message.getMethod(), message.getPayload(), e);
            return DeviceResponse.text(500, "ERROR");
        }
    }

    private DeviceResponse handleCdata(DeviceMessage message) {
        Map<String, String> query = message.getQueryParameters();
        String sn = firstValue(query, "sn");
        String table = firstValue(query, "table");
        if (StringUtils.equalsIgnoreCase(firstValue(query, "options"), "all")
                && StringUtils.equalsIgnoreCase(firstValue(query, "DeviceType"), "acc")) {
            return handleInitialization(message, query);
        }
        if (StringUtils.isBlank(table)) {
            return DeviceResponse.text(400, "MISSING TABLE");
        }
        switch (table.toLowerCase()) {
            case "rtlog" -> handleRtLog(sn, message);
            case "rsstate" -> handleState(sn, message);
            case "attphoto" -> handlePhoto(sn, message);
            default -> log.warn("Unsupported table {} from device {}", table, sn);
        }
        return DeviceResponse.text(OK);
    }

    private DeviceResponse handleInitialization(DeviceMessage message, Map<String, String> query) {
        String sn = firstValue(query, "sn");
        if (StringUtils.isBlank(sn)) {
            return DeviceResponse.text("406");
        }
        String deviceType = firstValue(query, "DeviceType");
        LocalDateTime now = LocalDateTime.now();
        accDeviceService.recordInitialization(sn, deviceType, query, message.getClientIp(), message.getUri(), now);
        redisCache.cacheInitializationSnapshot(sn, query, message.getClientIp());
        return DeviceResponse.builder()
                .body("OK\nSupportPing=1")
                .contentType("text/plain; charset=UTF-8")
                .build();
    }

    private DeviceResponse handleRegistry(DeviceMessage message) {
        Map<String, String> query = message.getQueryParameters();
        String sn = firstValue(query, "sn");
        if (StringUtils.isBlank(sn)) {
            return DeviceResponse.text("406");
        }
        Map<String, String> body = DevicePayloadParser.parseKeyValuePayload(message.getPayload());
        LocalDateTime now = LocalDateTime.now();
        accDeviceService.recordRegistry(sn, body, message.getClientIp(), message.getPayload(), now);
        redisCache.cacheRegistrySnapshot(sn, body, message.getClientIp());
        Optional<AccDevice> device = accDeviceService.findBySn(sn);
        if (device.isPresent() && Boolean.TRUE.equals(device.get().getAuthorized())
                && StringUtils.isNotBlank(device.get().getRegistryCode())) {
            return DeviceResponse.text("RegistryCode=" + device.get().getRegistryCode());
        }
        // Ensure device is in pending status for manual authorization.
        device.filter(value -> value.getStatus() == null)
                .ifPresent(value -> accDeviceService.updateStatus(sn, AccDeviceStatus.PENDING, false));
        return DeviceResponse.text("UNAUTHORIZED");
    }

    private DeviceResponse handlePush(DeviceMessage message) {
        Map<String, String> query = message.getQueryParameters();
        String sn = firstValue(query, "sn");
        if (StringUtils.isNotBlank(sn)) {
            accDeviceService.findBySn(sn).ifPresent(device -> {
                if (Boolean.FALSE.equals(device.getAuthorized())) {
                    log.warn("Unauthorized device {} attempted to push configuration", sn);
                }
            });
        }
        String response = String.join("\n",
                "ServerVersion=3.0.1",
                "ServerName=ADMS",
                "PushVersion=3.0.1",
                "ErrorDelay=30",
                "RequestDelay=2",
                "TransTimes=00:0014:00",
                "TransInterval=1",
                "TransTables=User Transaction",
                "Realtime=1",
                "SessionID=30BFB04B2C8AECC72C01C03BFD549D15",
                "TimeoutSec=10",
                "");
        return DeviceResponse.builder()
                .body(response)
                .contentType("text/plain; charset=UTF-8")
                .build();
    }

    private DeviceResponse handleHeartbeat(DeviceMessage message) {
        Map<String, String> query = message.getQueryParameters();
        String sn = firstValue(query, "sn");
        if (StringUtils.isBlank(sn)) {
            return DeviceResponse.text(OK);
        }
        LocalDateTime heartbeatTime = LocalDateTime.now();
        redisCache.recordHeartbeat(sn, message.getClientIp());
        accDeviceService.markHeartbeat(sn, message.getClientIp(), heartbeatTime);
        List<String> commands = redisCache.drainCommands(sn);
        if (commands.isEmpty()) {
            return DeviceResponse.text(OK);
        }
        String body = commands.stream().collect(Collectors.joining("\n"));
        return DeviceResponse.text(body);
    }

    private DeviceResponse handleDeviceCommandReport(DeviceMessage message) {
        Map<String, String> query = message.getQueryParameters();
        String sn = firstValue(query, "sn");
        Map<String, String> body = DevicePayloadParser.parseKeyValuePayload(message.getPayload());
        if (StringUtils.isBlank(sn)) {
            sn = firstValue(body, "sn");
        }
        AccDeviceCommandReport report = new AccDeviceCommandReport();
        report.setSn(sn);
        report.setCommandId(firstValue(body, "CmdId", "ID"));
        report.setCommandContent(StringUtils.defaultIfBlank(firstValue(body, "Cmd"), message.getPayload()));
        report.setResultCode(firstValue(body, "Result"));
        report.setResultMessage(firstValue(body, "Info"));
        report.setReportTime(LocalDateTime.now());
        report.setRawPayload(StringUtils.defaultIfBlank(message.getPayload(), message.getUri()));
        report.setClientIp(message.getClientIp());
        accDeviceCommandReportService.save(report);
        return DeviceResponse.text(OK);
    }

    private void handleRtLog(String sn, DeviceMessage message) {
        List<Map<String, String>> records = DevicePayloadParser.parseKeyValueRecords(message.getPayload());
        if (records.isEmpty()) {
            return;
        }
        List<AccDeviceRtLog> logs = records.stream().map(map -> {
            AccDeviceRtLog logEntity = new AccDeviceRtLog();
            logEntity.setSn(sn);
            logEntity.setLogTime(DevicePayloadParser.parseDateTime(firstValue(map, "time")));
            logEntity.setPin(firstValue(map, "pin"));
            logEntity.setCardNo(firstValue(map, "cardno", "CardNo"));
            logEntity.setEventAddr(parseInteger(firstValue(map, "eventaddr", "EventAddr")));
            logEntity.setEventCode(parseInteger(firstValue(map, "event", "Event")));
            logEntity.setInoutStatus(parseInteger(firstValue(map, "inoutstatus", "InOutStatus")));
            logEntity.setVerifyType(parseInteger(firstValue(map, "verifytype", "VerifyType")));
            logEntity.setRecordIndex(parseInteger(firstValue(map, "index", "Index")));
            logEntity.setSiteCode(parseInteger(firstValue(map, "sitecode", "SiteCode")));
            logEntity.setLinkId(parseInteger(firstValue(map, "linkid", "LinkID")));
            logEntity.setMaskFlag(parseInteger(firstValue(map, "maskflag", "MaskFlag")));
            logEntity.setTemperature(parseInteger(firstValue(map, "temperature", "Temperature")));
            logEntity.setConvTemperature(parseInteger(firstValue(map, "convtemperature", "ConvTemperature")));
            logEntity.setRawPayload(String.join("\t", map.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.toList())));
            logEntity.setClientIp(message.getClientIp());
            return logEntity;
        }).collect(Collectors.toList());
        accDeviceRtLogService.saveBatch(logs);
    }

    private void handleState(String sn, DeviceMessage message) {
        List<Map<String, String>> records = DevicePayloadParser.parseKeyValueRecords(message.getPayload());
        if (records.isEmpty()) {
            return;
        }
        List<AccDeviceState> states = records.stream().map(map -> {
            AccDeviceState state = new AccDeviceState();
            state.setSn(sn);
            state.setLogTime(DevicePayloadParser.parseDateTime(firstValue(map, "time")));
            state.setSensor(firstValue(map, "sensor", "Sensor"));
            state.setRelay(firstValue(map, "relay", "Relay"));
            state.setAlarm(firstValue(map, "alarm", "Alarm"));
            state.setDoor(firstValue(map, "door", "Door"));
            state.setRawPayload(String.join("\t", map.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.toList())));
            state.setClientIp(message.getClientIp());
            return state;
        }).collect(Collectors.toList());
        accDeviceStateService.saveBatch(states);
    }

    private void handlePhoto(String sn, DeviceMessage message) {
        Map<String, String> payload = DevicePayloadParser.parseKeyValuePayload(message.getPayload());
        if (payload.isEmpty()) {
            return;
        }
        AccDevicePhoto photo = new AccDevicePhoto();
        photo.setSn(StringUtils.defaultIfBlank(sn, firstValue(payload, "sn")));
        photo.setPin(firstValue(payload, "pin"));
        photo.setPhotoName(firstValue(payload, "pin", "photoName"));
        photo.setFileSize(parseInteger(firstValue(payload, "size")));
        photo.setPhotoBase64(firstValue(payload, "photo"));
        photo.setUploadedTime(LocalDateTime.now());
        photo.setRawPayload(message.getPayload());
        photo.setClientIp(message.getClientIp());
        accDevicePhotoService.save(photo);
    }

    private Integer parseInteger(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String firstValue(Map<String, String> map, String... keys) {
        if (map == null || map.isEmpty() || keys == null) {
            return null;
        }
        for (String key : keys) {
            if (key == null) {
                continue;
            }
            if (map.containsKey(key)) {
                return map.get(key);
            }
            String value = map.entrySet().stream()
                    .filter(entry -> entry.getKey() != null && entry.getKey().equalsIgnoreCase(key))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
