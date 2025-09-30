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

    private final String contentType;


    private DeviceResponse(Builder builder) {
        this.statusCode = builder.statusCode;
        this.headers = builder.headers == null ? Collections.emptyMap() : Collections.unmodifiableMap(builder.headers);
        this.body = builder.body == null ? "" : builder.body;
        this.charset = builder.charset == null ? StandardCharsets.UTF_8 : builder.charset;

        this.contentType = builder.contentType == null
                ? "text/plain; charset=" + this.charset.name()
                : builder.contentType;

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


    public String getContentType() {
        return contentType;
    }


    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int statusCode = 200;
        private Map<String, String> headers;
        private String body;
        private Charset charset;

        private String contentType;


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


        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public DeviceResponse build() {
            return new DeviceResponse(this);
        }
    }


    public static DeviceResponse text(String body) {
        return DeviceResponse.builder().body(body).build();
    }

    public static DeviceResponse text(int statusCode, String body) {
        return DeviceResponse.builder().statusCode(statusCode).body(body).build();
    }

}
