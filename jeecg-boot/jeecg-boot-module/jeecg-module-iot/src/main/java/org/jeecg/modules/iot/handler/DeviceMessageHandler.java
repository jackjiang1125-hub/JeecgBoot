package org.jeecg.modules.iot.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import io.netty.handler.codec.http.QueryStringDecoder;




import io.netty.util.CharsetUtil;
import org.jeecg.modules.iot.model.DeviceMessage;
import org.jeecg.modules.iot.model.DeviceResponse;
import org.jeecg.modules.iot.service.DeviceMessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Netty channel handler that converts HTTP requests into {@link DeviceMessage} instances.
 */
public class DeviceMessageHandler extends io.netty.channel.SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger log = LoggerFactory.getLogger(DeviceMessageHandler.class);

    private final DeviceMessageProcessor messageProcessor;

    public DeviceMessageHandler(DeviceMessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {

        QueryStringDecoder decoder = new QueryStringDecoder(msg.uri());

        DeviceMessage message = DeviceMessage.builder()
                .uri(msg.uri())
                .method(msg.method().name())
                .headers(extractHeaders(msg))
                .payload(msg.content().toString(CharsetUtil.UTF_8))

                .path(decoder.path())
                .queryParameters(decoder.parameters().entrySet().stream()
                        .filter(entry -> !entry.getValue().isEmpty())
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get(0))))
                .clientIp(resolveClientIp(ctx))
                .contentType(msg.headers().get(HttpHeaderNames.CONTENT_TYPE))
                .build();
        DeviceResponse response = messageProcessor.process(message);
        FullHttpResponse httpResponse = toHttpResponse(response);
        boolean keepAlive = io.netty.handler.codec.http.HttpUtil.isKeepAlive(msg);
        if (keepAlive) {
            httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            ctx.writeAndFlush(httpResponse);
        } else {
            ctx.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Unexpected error while handling device message", cause);
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.INTERNAL_SERVER_ERROR,
                Unpooled.copiedBuffer("Internal Server Error", CharsetUtil.UTF_8)
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private Map<String, String> extractHeaders(FullHttpRequest request) {
        Map<String, String> headers = new HashMap<>();
        request.headers().forEach(entry -> headers.put(entry.getKey(), entry.getValue()));
        return headers;
    }

    private FullHttpResponse toHttpResponse(DeviceResponse response) {
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.valueOf(response.getStatusCode()),
                Unpooled.copiedBuffer(response.getBody(), response.getCharset())
        );
        response.getHeaders().forEach(httpResponse.headers()::set);
        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, response.getContentType());
        httpResponse.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
        return httpResponse;
    }

    private String resolveClientIp(ChannelHandlerContext ctx) {
        if (ctx.channel().remoteAddress() instanceof InetSocketAddress socketAddress) {
            return socketAddress.getAddress().getHostAddress();
        }
        return "";
    }

}
