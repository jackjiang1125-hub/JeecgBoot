package org.jeecg.modules.iot.acc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.time.LocalDateTime;

/**
 * Real-time log reported by an access control device (table=rtlog).
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("iot_acc_device_rtlog")
public class AccDeviceRtLog extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    @TableField("sn")
    private String sn;

    @TableField("log_time")
    private LocalDateTime logTime;

    @TableField("pin")
    private String pin;

    @TableField("card_no")
    private String cardNo;

    @TableField("event_addr")
    private Integer eventAddr;

    @TableField("event_code")
    private Integer eventCode;

    @TableField("inout_status")
    private Integer inoutStatus;

    @TableField("verify_type")
    private Integer verifyType;

    @TableField("record_index")
    private Integer recordIndex;

    @TableField("site_code")
    private Integer siteCode;

    @TableField("link_id")
    private Integer linkId;

    @TableField("mask_flag")
    private Integer maskFlag;

    @TableField("temperature")
    private Integer temperature;

    @TableField("conv_temperature")
    private Integer convTemperature;

    @TableField("raw_payload")
    private String rawPayload;

    @TableField("client_ip")
    private String clientIp;
}
