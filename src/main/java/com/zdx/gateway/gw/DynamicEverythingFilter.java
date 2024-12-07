//package com.zdx.gateway.gw;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.cloud.gateway.route.Route;
//import org.springframework.core.Ordered;
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import org.springframework.web.util.UriComponentsBuilder;
//import reactor.core.publisher.Mono;
//
//import java.net.URI;
//
//import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;
//
//@Component
//public class DynamicEverythingFilter implements GlobalFilter, Ordered {
//    private static Logger log = LoggerFactory.getLogger(DynamicEverythingFilter.class);
//
//    @Autowired
//    private APIRepository apiRepository;
//
//    public DynamicEverythingFilter(APIRepository apiRepository) {
//        this.apiRepository = apiRepository;
//    }
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        // 定义API路径最后一位，为服务ID，只判断最后一位，实际上也可以自由添加任何逻辑
//        String serviceID = requestPathSets[requestPathSets.length - 1];
//
//        APIInfo apiInfo = this.apiRepository.get(serviceID);
//        String newPath = apiInfo.getRoutePath();
//
//        exchange.getAttributes().put(ContextConstants.TARGET_URL, newPath);
//
//        log.info("服务ID {}, 路由到后端[{}]", serviceID, newPath);
//
//
//        // 动态修改路由开始
//        ServerHttpRequest request = exchange.getRequest();
//        URI uri = UriComponentsBuilder.fromHttpUrl(newPath).build().toUri();
//
//        ServerHttpRequest newRequest = request.mutate().uri(uri).build();
//        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
//        if (route == null) {
//            //log.error(ErrorCodeEnum.NO_PATH_ROUTE.getDesc());
//            //return ExceptionHandler.genErrResponse(exchange, ErrorCodeEnum.NO_PATH_ROUTE);
//        }
//        Route newRoute = Route.async()
//                .asyncPredicate(route.getPredicate())
//                .filters(route.getFilters())
//                .id(route.getId())
//                .order(route.getOrder())
//                .uri(uri)
//                .build();
//        exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, newRoute);
//        return chain.filter(exchange.mutate().request(newRequest).build());
//    }
//
//    @Override
//    public int getOrder() {
//        return -80;
//    }
//}