package org.jeecg.modules.iot.acc.service.impl;

import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.iot.acc.entity.AccDeviceRtLog;
import org.jeecg.modules.iot.acc.mapper.AccDeviceRtLogMapper;
import org.jeecg.modules.iot.acc.service.AccDeviceRtLogService;
import org.springframework.stereotype.Service;

@Service
public class AccDeviceRtLogServiceImpl extends JeecgServiceImpl<AccDeviceRtLogMapper, AccDeviceRtLog>
        implements AccDeviceRtLogService {
}
