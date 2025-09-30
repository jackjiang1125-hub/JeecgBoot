package org.jeecg.modules.iot.acc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.apache.ibatis.type.EnumTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecg.modules.iot.acc.enums.AccDeviceStatus;

import java.time.LocalDateTime;

/**
 * Represents an access control device that connects to the Netty gateway.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("iot_acc_device")
public class AccDevice extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    /**
     * Device serial number reported by the terminal.
     */
    @TableField("sn")
    private String sn;

    /**
     * Device type (acc, att, pos ...).
     */
    @TableField("device_type")
    private String deviceType;

    /**
     * Human readable device name.
     */
    @TableField("device_name")
    private String deviceName;

    /**
     * Firmware version string.
     */
    @TableField("firmware_version")
    private String firmwareVersion;

    /**
     * Push protocol version reported by device.
     */
    @TableField("push_version")
    private String pushVersion;

    /**
     * Number of locks supported by the controller.
     */
    @TableField("lock_count")
    private Integer lockCount;

    /**
     * Number of reader heads supported.
     */
    @TableField("reader_count")
    private Integer readerCount;

    /**
     * Machine type identifier (e.g. 101 for all-in-one devices).
     */
    @TableField("machine_type")
    private Integer machineType;

    /**
     * IPv4 address configured on the device.
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * Gateway IPv4 address configured on the device.
     */
    @TableField("gateway_ip")
    private String gatewayIp;

    /**
     * Netmask configured on the device.
     */
    @TableField("net_mask")
    private String netMask;

    /**
     * Raw initialization request payload for auditing.
     */
    @TableField("init_payload")
    private String initPayload;

    /**
     * Raw registry payload for auditing.
     */
    @TableField("registry_payload")
    private String registryPayload;

    /**
     * Last known IPv4 address detected from connection.
     */
    @TableField("last_known_ip")
    private String lastKnownIp;

    /**
     * Last time an initialization handshake was received.
     */
    @TableField("last_init_time")
    private LocalDateTime lastInitTime;

    /**
     * Last time the device registered itself.
     */
    @TableField("last_registry_time")
    private LocalDateTime lastRegistryTime;

    /**
     * Last heartbeat timestamp.
     */
    @TableField("last_heartbeat_time")
    private LocalDateTime lastHeartbeatTime;

    /**
     * Status in the authorization workflow.
     */
    @TableField(value = "status", typeHandler = EnumTypeHandler.class)
    private AccDeviceStatus status;

    /**
     * Whether the device is currently authorized for communication.
     */
    @TableField("authorized")
    private Boolean authorized;

    /**
     * Registry code returned to authorized devices.
     */
    @TableField("registry_code")
    private String registryCode;

    /**
     * Optional remark.
     */
    @TableField("remark")
    private String remark;
}
