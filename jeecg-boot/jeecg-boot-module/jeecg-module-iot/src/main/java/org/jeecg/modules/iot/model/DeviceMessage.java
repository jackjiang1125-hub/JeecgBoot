package org.jeecg.modules.iot.model;

import java.util.Collections;
import java.util.Map;

/**
 * Represents a message sent by a connected device through the HTTP based private protocol.
 */
public class DeviceMessage {

    private final String uri;
    private final String method;
    private final Map<String, String> headers;
    private final String payload;

    private DeviceMessage(Builder builder) {
        this.uri = builder.uri;
        this.method = builder.method;
        this.headers = builder.headers == null ? Collections.emptyMap() : Collections.unmodifiableMap(builder.headers);
        this.payload = builder.payload;
    }

    public String getUri() {
        return uri;
    }

    public String getMethod() {
        return method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getPayload() {
        return payload;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String uri;
        private String method;
        private Map<String, String> headers;
        private String payload;

        private Builder() {
        }

        public Builder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public DeviceMessage build() {
            return new DeviceMessage(this);
        }
    }
}
