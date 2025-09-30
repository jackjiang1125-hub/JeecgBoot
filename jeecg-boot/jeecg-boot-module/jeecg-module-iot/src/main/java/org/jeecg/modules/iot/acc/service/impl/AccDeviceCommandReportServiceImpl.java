package org.jeecg.modules.iot.acc.service.impl;

import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.iot.acc.entity.AccDeviceCommandReport;
import org.jeecg.modules.iot.acc.mapper.AccDeviceCommandReportMapper;
import org.jeecg.modules.iot.acc.service.AccDeviceCommandReportService;
import org.springframework.stereotype.Service;

@Service
public class AccDeviceCommandReportServiceImpl extends JeecgServiceImpl<AccDeviceCommandReportMapper, AccDeviceCommandReport>
        implements AccDeviceCommandReportService {
}
