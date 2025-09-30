package org.jeecg.modules.iot.config;

import org.jeecg.modules.iot.acc.cache.AccDeviceRedisCache;
import org.jeecg.modules.iot.acc.protocol.AccDeviceMessageProcessor;
import org.jeecg.modules.iot.acc.service.AccDeviceCommandReportService;
import org.jeecg.modules.iot.acc.service.AccDevicePhotoService;
import org.jeecg.modules.iot.acc.service.AccDeviceRtLogService;
import org.jeecg.modules.iot.acc.service.AccDeviceService;
import org.jeecg.modules.iot.acc.service.AccDeviceStateService;
import org.jeecg.modules.iot.server.IotNettyServer;
import org.jeecg.modules.iot.server.IotNettyServerProperties;
import org.jeecg.modules.iot.service.DeviceMessageProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto configuration that wires and starts the Netty server for IoT device communication.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(IotNettyServer.class)
@EnableConfigurationProperties(IotNettyServerProperties.class)
@ConditionalOnProperty(prefix = "jeecg.iot.netty", name = "enabled", havingValue = "true", matchIfMissing = true)
public class IotNettyAutoConfiguration {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public IotNettyServer iotNettyServer(IotNettyServerProperties properties, DeviceMessageProcessor deviceMessageProcessor) {
        return new IotNettyServer(properties, deviceMessageProcessor);
    }

    @Bean
    @ConditionalOnMissingBean
    public DeviceMessageProcessor deviceMessageProcessor(AccDeviceService accDeviceService,
                                                         AccDeviceRtLogService accDeviceRtLogService,
                                                         AccDeviceStateService accDeviceStateService,
                                                         AccDevicePhotoService accDevicePhotoService,
                                                         AccDeviceCommandReportService accDeviceCommandReportService,
                                                         AccDeviceRedisCache accDeviceRedisCache) {
        return new AccDeviceMessageProcessor(accDeviceService, accDeviceRtLogService, accDeviceStateService,
                accDevicePhotoService, accDeviceCommandReportService, accDeviceRedisCache);
    }
}
