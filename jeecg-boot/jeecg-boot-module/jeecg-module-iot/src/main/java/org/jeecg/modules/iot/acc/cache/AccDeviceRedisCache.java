package org.jeecg.modules.iot.acc.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis cache helper for storing device state, heartbeats and command queues.
 */
@Component
@RequiredArgsConstructor
public class AccDeviceRedisCache {

    private static final Logger log = LoggerFactory.getLogger(AccDeviceRedisCache.class);

    private static final Duration DEFAULT_TTL = Duration.ofHours(24);
    private static final int MAX_COMMANDS_PER_HEARTBEAT = 50;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void cacheInitializationSnapshot(String sn, Map<String, String> queryParams, String clientIp) {
        if (StringUtils.isBlank(sn)) {
            return;
        }
        Map<String, Object> snapshot = new HashMap<>();
        if (queryParams != null) {
            snapshot.putAll(queryParams);
        }
        snapshot.put("clientIp", clientIp);
        snapshot.put("timestamp", Instant.now().toString());
        writeJson(key("init", sn), snapshot, DEFAULT_TTL);
    }

    public void cacheRegistrySnapshot(String sn, Map<String, String> registryParams, String clientIp) {
        if (StringUtils.isBlank(sn)) {
            return;
        }
        Map<String, Object> snapshot = new HashMap<>();
        if (registryParams != null) {
            snapshot.putAll(registryParams);
        }
        snapshot.put("clientIp", clientIp);
        snapshot.put("timestamp", Instant.now().toString());
        writeJson(key("registry", sn), snapshot, DEFAULT_TTL);
    }

    public void recordHeartbeat(String sn, String clientIp) {
        if (StringUtils.isBlank(sn)) {
            return;
        }
        Map<String, Object> heartbeat = new HashMap<>();
        heartbeat.put("clientIp", clientIp);
        heartbeat.put("timestamp", Instant.now().toString());
        writeJson(key("heartbeat", sn), heartbeat, DEFAULT_TTL);
    }


    public List<QueuedCommand> drainCommands(String sn) {
        List<QueuedCommand> commands = new ArrayList<>();

    public List<String> drainCommands(String sn) {
        List<String> commands = new ArrayList<>();

        if (StringUtils.isBlank(sn)) {
            return commands;
        }
        String redisKey = key("commands", sn);
        for (int i = 0; i < MAX_COMMANDS_PER_HEARTBEAT; i++) {

            String payload = redisTemplate.opsForList().leftPop(redisKey);
            if (payload == null) {
                break;
            }
            QueuedCommand command = QueuedCommand.fromPayload(payload);
            if (command != null) {
                commands.add(command);
            }

        }
        if (commands.isEmpty()) {
            redisTemplate.expire(redisKey, DEFAULT_TTL);
        }
        return commands;
    }


    public void enqueueCommand(String sn, String commandId, String commandContent) {
        if (StringUtils.isAnyBlank(sn, commandId, commandContent)) {
            return;
        }
        String redisKey = key("commands", sn);
        redisTemplate.opsForList().rightPush(redisKey, commandId + "|" + commandContent);
        redisTemplate.expire(redisKey, DEFAULT_TTL);
    }

    private void writeJson(String key, Map<String, Object> snapshot, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(snapshot);
            redisTemplate.opsForValue().set(key, json, ttl);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize redis snapshot for key {}", key, e);
        }
    }

    private String key(String category, String sn) {
        return "iot:acc:" + category + ":" + sn;
    }


    /**
     * Value object used for queued commands in Redis.
     */
    public record QueuedCommand(String id, String content) {

        private static QueuedCommand fromPayload(String payload) {
            if (StringUtils.isBlank(payload)) {
                return null;
            }
            int idx = payload.indexOf('|');
            if (idx <= 0 || idx >= payload.length() - 1) {
                return null;
            }
            String id = payload.substring(0, idx);
            String content = payload.substring(idx + 1);
            if (StringUtils.isAnyBlank(id, content)) {
                return null;
            }
            return new QueuedCommand(id, content);
        }
    }

}
