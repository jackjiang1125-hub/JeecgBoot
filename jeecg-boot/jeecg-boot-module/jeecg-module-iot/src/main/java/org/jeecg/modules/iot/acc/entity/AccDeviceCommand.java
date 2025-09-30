package org.jeecg.modules.iot.acc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.EnumTypeHandler;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecg.modules.iot.acc.enums.AccDeviceCommandStatus;

import java.time.LocalDateTime;

/**
 * Command queued for delivery to an access control device.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("iot_acc_device_command")
public class AccDeviceCommand extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    /**
     * Device serial number that should receive the command.
     */
    @TableField("sn")
    private String sn;

    /**
     * Parsed command identifier (e.g. 2001 extracted from C:2001:...).
     */
    @TableField("command_code")
    private String commandCode;

    /**
     * Raw command payload that will be sent to the device.
     */
    @TableField("command_content")
    private String commandContent;

    /**
     * Current lifecycle status.
     */
    @TableField(value = "status", typeHandler = EnumTypeHandler.class)
    private AccDeviceCommandStatus status;

    /**
     * Time when the command was enqueued for delivery.
     */
    @TableField("enqueue_time")
    private LocalDateTime enqueueTime;

    /**
     * Time when the command was handed to the device.
     */
    @TableField("sent_time")
    private LocalDateTime sentTime;

    /**
     * Time when an acknowledgement was received from the device.
     */
    @TableField("ack_time")
    private LocalDateTime ackTime;

    /**
     * Result code reported by the device, if available.
     */
    @TableField("result_code")
    private String resultCode;

    /**
     * Result message reported by the device, if available.
     */
    @TableField("result_message")
    private String resultMessage;

    /**
     * Raw payload from the latest device acknowledgement.
     */
    @TableField("last_report_payload")
    private String lastReportPayload;

    /**
     * Last IP address observed when the device acknowledged the command.
     */
    @TableField("last_report_ip")
    private String lastReportIp;
}
