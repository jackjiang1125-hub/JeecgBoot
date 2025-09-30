package org.jeecg.modules.iot.acc.util;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility helpers to parse proprietary device payloads.
 */
public final class DevicePayloadParser {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DevicePayloadParser() {
    }

    public static Map<String, String> parseKeyValuePayload(String payload) {
        Map<String, String> map = new LinkedHashMap<>();
        if (StringUtils.isBlank(payload)) {
            return map;
        }
        String sanitized = payload.replace('\r', '\n');
        sanitized = sanitized.replace(',', '\n');
        String[] segments = sanitized.split("[\\n\\t]+");
        for (String segment : segments) {
            if (StringUtils.isBlank(segment)) {
                continue;
            }
            int idx = segment.indexOf('=');
            if (idx <= 0) {
                continue;
            }
            String key = segment.substring(0, idx).trim();
            String value = segment.substring(idx + 1).trim();
            if (StringUtils.isNotEmpty(key)) {
                map.put(key, value);
            }
        }
        return map;
    }

    public static List<Map<String, String>> parseKeyValueRecords(String payload) {
        List<Map<String, String>> records = new ArrayList<>();
        if (StringUtils.isBlank(payload)) {
            return records;
        }
        String normalized = payload.replace('\r', '\n');
        String[] lines = normalized.split("\\n+");
        for (String line : lines) {
            if (StringUtils.isBlank(line)) {
                continue;
            }
            Map<String, String> record = parseKeyValuePayload(line);
            if (!record.isEmpty()) {
                records.add(record);
            }
        }
        return records;
    }

    public static LocalDateTime parseDateTime(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.trim(), DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
