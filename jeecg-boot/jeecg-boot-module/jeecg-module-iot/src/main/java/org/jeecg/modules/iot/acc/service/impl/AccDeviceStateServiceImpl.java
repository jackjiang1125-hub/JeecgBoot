package org.jeecg.modules.iot.acc.service.impl;

import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.iot.acc.entity.AccDeviceState;
import org.jeecg.modules.iot.acc.mapper.AccDeviceStateMapper;
import org.jeecg.modules.iot.acc.service.AccDeviceStateService;
import org.springframework.stereotype.Service;

@Service
public class AccDeviceStateServiceImpl extends JeecgServiceImpl<AccDeviceStateMapper, AccDeviceState>
        implements AccDeviceStateService {
}
