package org.jeecg.modules.iot.acc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.time.LocalDateTime;

/**
 * Command execution report pushed by devices (/iclock/devicecmd).
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("iot_acc_device_command_report")
public class AccDeviceCommandReport extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    @TableField("sn")
    private String sn;

    @TableField("command_id")
    private String commandId;

    @TableField("command_content")
    private String commandContent;

    @TableField("result_code")
    private String resultCode;

    @TableField("result_message")
    private String resultMessage;

    @TableField("report_time")
    private LocalDateTime reportTime;

    @TableField("raw_payload")
    private String rawPayload;

    @TableField("client_ip")
    private String clientIp;
}
