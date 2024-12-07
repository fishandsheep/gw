package com.zdx.gateway.gw;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();

        // 非 application/json 类型
        if (headers.getContentType() == null || !(headers.getContentType().isCompatibleWith(MediaType.APPLICATION_JSON))) {
            return chain.filter(exchange);
        }

        // 获取请求体内容并处理
        return DataBufferUtils.join(request.getBody()).flatMap(dataBuffer -> {
            byte[] bytes = new byte[dataBuffer.readableByteCount()];
            dataBuffer.read(bytes);
            String requestBody = new String(bytes, StandardCharsets.UTF_8);
            //释放资源，防止下次读取失败
            DataBufferUtils.release(dataBuffer);

            //默认url  http://localhost:9080/A001
            URI uri = request.getURI();
            // 根据报文体内容进行路由判断，这里假设根据某个关键字判断
            ObjectMapper objectMapper = new ObjectMapper();
            // 解析 JSON 字符串为 JsonNode
            JsonNode rootNode = null;
            try {
                rootNode = objectMapper.readTree(requestBody);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            // 获取嵌套字段
            JsonNode keyword = rootNode.path("body")
                    .path("keyword");

            if (keyword.asText().contains("B")) {
                try {
                    uri = new URI("http://localhost:7080/A002");
                    Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
                    Route newRoute = Route.async()
                            .asyncPredicate(route.getPredicate())
                            .filters(route.getFilters())
                            .id(route.getId())
                            .order(route.getOrder())
                            .uri(uri)
                            .build();
                    //覆盖原有路由
                    exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, newRoute);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
            //创建新的装饰器
            ServerHttpRequestDecorator requestDecorator = createRequestDecorator(exchange, bytes, uri);
            return chain.filter(exchange.mutate().request(requestDecorator).build());
        });

    }

    private ServerHttpRequestDecorator createRequestDecorator(ServerWebExchange exchange, byte[] bytes, URI newUri) {
        return new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public Flux<DataBuffer> getBody() {
                return Flux.just(exchange.getResponse().bufferFactory().wrap(bytes));
            }

            @Override
            public URI getURI() {
                return newUri;
            }
        };
    }

    @Override
    public int getOrder() {
        return -1;
    }
}