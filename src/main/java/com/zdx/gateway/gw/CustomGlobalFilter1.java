package com.zdx.gateway.gw;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

//@Component
public class CustomGlobalFilter1 implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();

        // 检查是否是支持的可读取报文体的媒体类型，比如JSON等
        if (headers.getContentType() != null &&
                (headers.getContentType().isCompatibleWith(MediaType.APPLICATION_JSON) ||
                        headers.getContentType().isCompatibleWith(MediaType.TEXT_PLAIN))) {

            // 获取请求体内容并处理
            return DataBufferUtils.join(request.getBody())
                    .flatMap(dataBuffer -> {

                        DataBufferUtils.retain(dataBuffer);
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        String requestBody = new String(bytes, StandardCharsets.UTF_8);

                        // 根据报文体内容进行路由判断，这里假设根据某个关键字判断
                        if (requestBody.contains("keyword")) {
                            // 构建新的请求，修改路由目标等信息
                            ServerHttpRequest newRequest = null;
                            try {
                                newRequest = request.mutate()
                                        .uri(new URI("http://localhost:9080/A002"))
                                        .build();
                            } catch (URISyntaxException e) {
                                throw new RuntimeException(e);
                            }
                            ServerWebExchange newExchange = exchange.mutate()
                                    .request(newRequest)
                                    .build();
                            return chain.filter(newExchange);
                        } else {
                            // 按原路由处理
                            return chain.filter(exchange);
                        }
                    });
        } else {
            // 对于不支持读取报文体的类型，按原路由处理
            return chain.filter(exchange);
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
}