package org.jeecg.modules.iot.acc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.time.LocalDateTime;

/**
 * Door status snapshot reported by an access control device (table=rsstate).
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("iot_acc_device_state")
public class AccDeviceState extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    @TableField("sn")
    private String sn;

    @TableField("log_time")
    private LocalDateTime logTime;

    @TableField("sensor")
    private String sensor;

    @TableField("relay")
    private String relay;

    @TableField("alarm")
    private String alarm;

    @TableField("door")
    private String door;

    @TableField("raw_payload")
    private String rawPayload;

    @TableField("client_ip")
    private String clientIp;
}
