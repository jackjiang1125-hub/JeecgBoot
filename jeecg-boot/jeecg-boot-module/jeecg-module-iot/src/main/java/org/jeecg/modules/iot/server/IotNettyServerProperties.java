package org.jeecg.modules.iot.server;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the IoT Netty server.
 */
@ConfigurationProperties(prefix = "jeecg.iot.netty")
public class IotNettyServerProperties {

    /**
     * Whether the server should start automatically.
     */
    private boolean enabled = true;

    /**
     * Port used by the Netty HTTP server.
     */
    private int port = 9090;

    /**
     * Number of boss threads.
     */
    private int bossThreads = 1;

    /**
     * Number of worker threads.
     */
    private int workerThreads = 0;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getBossThreads() {
        return bossThreads;
    }

    public void setBossThreads(int bossThreads) {
        this.bossThreads = bossThreads;
    }

    public int getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }
}
