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

    private final String path;
    private final Map<String, String> queryParameters;
    private final String clientIp;
    private final String contentType;


    private DeviceMessage(Builder builder) {
        this.uri = builder.uri;
        this.method = builder.method;
        this.headers = builder.headers == null ? Collections.emptyMap() : Collections.unmodifiableMap(builder.headers);
        this.payload = builder.payload;

        this.path = builder.path;
        this.queryParameters = builder.queryParameters == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(builder.queryParameters);
        this.clientIp = builder.clientIp;
        this.contentType = builder.contentType;

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


    public String getPath() {
        return path;
    }

    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    public String getClientIp() {
        return clientIp;
    }

    public String getContentType() {
        return contentType;
    }


    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String uri;
        private String method;
        private Map<String, String> headers;
        private String payload;

        private String path;
        private Map<String, String> queryParameters;
        private String clientIp;
        private String contentType;


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


        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder queryParameters(Map<String, String> queryParameters) {
            this.queryParameters = queryParameters;
            return this;
        }

        public Builder clientIp(String clientIp) {
            this.clientIp = clientIp;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }


        public DeviceMessage build() {
            return new DeviceMessage(this);
        }
    }
}
