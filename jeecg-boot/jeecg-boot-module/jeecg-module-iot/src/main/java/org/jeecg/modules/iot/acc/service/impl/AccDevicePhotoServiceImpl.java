package org.jeecg.modules.iot.acc.service.impl;

import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.iot.acc.entity.AccDevicePhoto;
import org.jeecg.modules.iot.acc.mapper.AccDevicePhotoMapper;
import org.jeecg.modules.iot.acc.service.AccDevicePhotoService;
import org.springframework.stereotype.Service;

@Service
public class AccDevicePhotoServiceImpl extends JeecgServiceImpl<AccDevicePhotoMapper, AccDevicePhoto>
        implements AccDevicePhotoService {
}
