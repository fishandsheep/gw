package com.zdx.gateway.gw;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

//@Component
public class PbsGlobalFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Flux<DataBuffer> body = exchange.getRequest().getBody();

        body.flatMap(dataBuffer -> {
            byte[] bytes = new byte[dataBuffer.readableByteCount()];
            dataBuffer.read(bytes);
            String content = new String(bytes);
            System.out.println(content);
            return Mono.just(dataBuffer);
        });
//
//        return chain.filter(exchange);

        // 获取请求体
        return exchange.getRequest().getBody()
                .map(dataBuffer -> {
                    // 将请求体解析为字符串（可以根据实际需求调整解析方式）
                    return dataBuffer.toString(java.nio.charset.StandardCharsets.UTF_8);
                })
                .next()
                .flatMap(requestBody -> {
                    // 在这里处理请求体（例如，解析JSON、验证字段等）
                    System.out.println("请求体内容: " + requestBody);
                    // 例如，你可以根据请求体的内容做一些验证或者修改请求体等操作
                    // 在这里可以将解析后的内容加入到请求的属性中
                    exchange.getAttributes().put("parsedRequestBody", requestBody);

                    // 继续执行原有的过滤器链
                    return chain.filter(exchange);
                });

    }

    @Override
    public int getOrder() {
        return 0;
    }
}
