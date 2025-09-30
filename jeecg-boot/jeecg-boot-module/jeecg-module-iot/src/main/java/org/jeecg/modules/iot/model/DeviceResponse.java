package org.jeecg.modules.iot.model;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

/**
 * Simple response model returned to devices after processing a message.
 */
public class DeviceResponse {

    private final int statusCode;
    private final Map<String, String> headers;
    private final String body;
    private final Charset charset;

    private DeviceResponse(Builder builder) {
        this.statusCode = builder.statusCode;
        this.headers = builder.headers == null ? Collections.emptyMap() : Collections.unmodifiableMap(builder.headers);
        this.body = builder.body == null ? "" : builder.body;
        this.charset = builder.charset == null ? StandardCharsets.UTF_8 : builder.charset;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public Charset getCharset() {
        return charset;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int statusCode = 200;
        private Map<String, String> headers;
        private String body;
        private Charset charset;

        private Builder() {
        }

        public Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder charset(Charset charset) {
            this.charset = charset;
            return this;
        }

        public DeviceResponse build() {
            return new DeviceResponse(this);
        }
    }
}
