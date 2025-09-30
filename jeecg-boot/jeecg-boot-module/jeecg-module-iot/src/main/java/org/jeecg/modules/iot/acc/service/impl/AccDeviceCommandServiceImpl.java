package org.jeecg.modules.iot.acc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.iot.acc.cache.AccDeviceRedisCache;
import org.jeecg.modules.iot.acc.entity.AccDeviceCommand;
import org.jeecg.modules.iot.acc.enums.AccDeviceCommandStatus;
import org.jeecg.modules.iot.acc.mapper.AccDeviceCommandMapper;
import org.jeecg.modules.iot.acc.service.AccDeviceCommandService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default implementation of {@link AccDeviceCommandService}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccDeviceCommandServiceImpl extends JeecgServiceImpl<AccDeviceCommandMapper, AccDeviceCommand>
        implements AccDeviceCommandService {

    private static final Pattern COMMAND_CODE_PATTERN = Pattern.compile("^C:([^:]+):.*", Pattern.CASE_INSENSITIVE);

    private final AccDeviceRedisCache redisCache;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<AccDeviceCommand> enqueueCommands(String sn, List<String> commands, String operator) {
        if (StringUtils.isBlank(sn) || commands == null || commands.isEmpty()) {
            return List.of();
        }
        LocalDateTime now = LocalDateTime.now();
        Date nowDate = new Date();
        List<AccDeviceCommand> entities = new ArrayList<>();
        for (String command : commands) {
            if (StringUtils.isBlank(command)) {
                continue;
            }
            AccDeviceCommand entity = new AccDeviceCommand();
            entity.setSn(sn);
            entity.setCommandContent(command.trim());
            entity.setCommandCode(extractCommandCode(command));
            entity.setStatus(AccDeviceCommandStatus.PENDING);
            entity.setEnqueueTime(now);
            entity.setCreateBy(operator);
            entity.setCreateTime(nowDate);
            entities.add(entity);
        }
        if (entities.isEmpty()) {
            return List.of();
        }
        saveBatch(entities);
        for (AccDeviceCommand command : entities) {
            redisCache.enqueueCommand(sn, command.getId(), command.getCommandContent());
        }
        return entities;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markCommandsSent(List<String> commandIds, LocalDateTime sentTime) {
        if (commandIds == null || commandIds.isEmpty() || sentTime == null) {
            return;
        }
        Date now = new Date();
        List<AccDeviceCommand> records = listByIds(commandIds);
        if (records.isEmpty()) {
            return;
        }
        records.forEach(command -> {
            if (command.getStatus() == AccDeviceCommandStatus.PENDING) {
                command.setStatus(AccDeviceCommandStatus.SENT);
            }
            command.setSentTime(sentTime);
            command.setUpdateTime(now);
        });
        updateBatchById(records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Optional<AccDeviceCommand> handleCommandReport(String sn, String commandCode, String resultCode,
                                                          String resultMessage, String rawPayload, String clientIp) {
        if (StringUtils.isAnyBlank(sn, commandCode)) {
            return Optional.empty();
        }
        AccDeviceCommand record = getOne(new LambdaQueryWrapper<AccDeviceCommand>()
                .eq(AccDeviceCommand::getSn, sn)
                .eq(AccDeviceCommand::getCommandCode, commandCode)
                .orderByDesc(AccDeviceCommand::getCreateTime)
                .last("limit 1"), false);
        if (record == null) {
            log.warn("Received command report for unknown command. sn={}, cmdId={}", sn, commandCode);
            return Optional.empty();
        }
        LocalDateTime now = LocalDateTime.now();
        Date nowDate = new Date();
        record.setAckTime(now);
        record.setResultCode(resultCode);
        record.setResultMessage(resultMessage);
        record.setLastReportPayload(StringUtils.defaultIfBlank(rawPayload, record.getLastReportPayload()));
        record.setLastReportIp(StringUtils.defaultIfBlank(clientIp, record.getLastReportIp()));
        record.setUpdateTime(nowDate);
        if (StringUtils.isBlank(resultCode) || StringUtils.equalsAnyIgnoreCase(resultCode.trim(), "0", "ok", "success")) {
            record.setStatus(AccDeviceCommandStatus.ACKED);
        } else {
            record.setStatus(AccDeviceCommandStatus.FAILED);
        }
        updateById(record);
        return Optional.of(record);
    }

    private String extractCommandCode(String command) {
        if (StringUtils.isBlank(command)) {
            return null;
        }
        Matcher matcher = COMMAND_CODE_PATTERN.matcher(command.trim());
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }
}
