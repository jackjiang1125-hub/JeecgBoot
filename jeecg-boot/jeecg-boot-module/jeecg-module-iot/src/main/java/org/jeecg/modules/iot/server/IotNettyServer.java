package org.jeecg.modules.iot.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.jeecg.modules.iot.handler.DeviceMessageHandler;
import org.jeecg.modules.iot.service.DeviceMessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Netty server that receives HTTP requests from the IoT devices and hands them over to the configured processor.
 */
public class IotNettyServer {

    private static final Logger log = LoggerFactory.getLogger(IotNettyServer.class);

    private final IotNettyServerProperties properties;
    private final DeviceMessageProcessor messageProcessor;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    public IotNettyServer(IotNettyServerProperties properties, DeviceMessageProcessor messageProcessor) {
        this.properties = properties;
        this.messageProcessor = messageProcessor;
    }

    public synchronized void start() throws InterruptedException {
        if (!properties.isEnabled()) {
            log.info("IoT Netty server is disabled via configuration");
            return;
        }
        if (serverChannel != null && serverChannel.isActive()) {
            log.debug("IoT Netty server already started");
            return;
        }
        bossGroup = properties.getBossThreads() > 0 ? new NioEventLoopGroup(properties.getBossThreads()) : new NioEventLoopGroup();
        workerGroup = properties.getWorkerThreads() > 0 ? new NioEventLoopGroup(properties.getWorkerThreads()) : new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                                .addLast(new HttpServerCodec())
                                .addLast(new HttpObjectAggregator(1024 * 1024))
                                .addLast(new ReadTimeoutHandler(60, TimeUnit.SECONDS))
                                .addLast(new DeviceMessageHandler(messageProcessor));
                    }
                });

        ChannelFuture channelFuture = bootstrap.bind(properties.getPort()).sync();
        serverChannel = channelFuture.channel();
        log.info("IoT Netty server started on port {}", properties.getPort());
    }

    public synchronized void stop() {
        if (serverChannel != null) {
            try {
                serverChannel.close().syncUninterruptibly();
            } finally {
                serverChannel = null;
            }
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
            workerGroup = null;
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
            bossGroup = null;
        }
        log.info("IoT Netty server stopped");
    }
}
