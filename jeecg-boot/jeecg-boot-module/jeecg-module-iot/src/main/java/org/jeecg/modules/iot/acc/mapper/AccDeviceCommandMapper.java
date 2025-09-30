package org.jeecg.modules.iot.acc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.iot.acc.entity.AccDeviceCommand;

/**
 * Mapper for queued access-control device commands.
 */
@Mapper
public interface AccDeviceCommandMapper extends BaseMapper<AccDeviceCommand> {
}
