/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zdx.gateway.gw;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.handler.AsyncPredicate;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 *
 */
//@Component
public class PbsRoutePredicateFactory extends AbstractRoutePredicateFactory<PbsRoutePredicateFactory.Config> {

    protected static final Log log = LogFactory.getLog(PbsRoutePredicateFactory.class);

    private static final String TEST_ATTRIBUTE = "read_body_predicate_test_attribute";

    private static final String CACHE_REQUEST_BODY_OBJECT_KEY = "cachedRequestBodyObject";

    private final List<HttpMessageReader<?>> messageReaders;

    public PbsRoutePredicateFactory() {
        super(Config.class);
        this.messageReaders = HandlerStrategies.withDefaults().messageReaders();
    }

    public PbsRoutePredicateFactory(List<HttpMessageReader<?>> messageReaders) {
        super(Config.class);
        this.messageReaders = messageReaders;
    }

    @Override
    public Predicate<ServerWebExchange> apply(Consumer<Config> consumer) {
        System.out.println("ceshi");
        return super.apply(consumer);
    }

    @Override
    @SuppressWarnings("unchecked")
    public AsyncPredicate<ServerWebExchange> applyAsync(Config config) {
        return new AsyncPredicate<ServerWebExchange>() {
            @Override
            public Publisher<Boolean> apply(ServerWebExchange exchange) {

                ServerHttpRequest request = exchange.getRequest();
                String path = request.getURI().getPath();
                Flux<DataBuffer> originalBody = request.getBody();

                originalBody.flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    String content = new String(bytes);
                    System.out.println(content);
                    return Mono.just(dataBuffer);
                }).publish();

                return Mono.just(false);


//                Class inClass = config.getInClass();
//
//                Object cachedBody = exchange.getAttribute(CACHE_REQUEST_BODY_OBJECT_KEY);
//                Mono<?> modifiedBody;
//                // We can only read the body from the request once, once that happens if
//                // we try to read the body again an exception will be thrown. The below
//                // if/else caches the body object as a request attribute in the
//                // ServerWebExchange so if this filter is run more than once (due to more
//                // than one route using it) we do not try to read the request body
//                // multiple times
//                if (cachedBody != null) {
//                    try {
//                        boolean test = config.predicate.test(cachedBody);
//                        exchange.getAttributes().put(TEST_ATTRIBUTE, test);
//                        return Mono.just(test);
//                    } catch (ClassCastException e) {
//                        if (log.isDebugEnabled()) {
//                            log.debug("Predicate test failed because class in predicate " + "does not match the cached body object", e);
//                        }
//                    }
//                    return Mono.just(false);
//                } else {
//                    return ServerWebExchangeUtils.cacheRequestBodyAndRequest(exchange,
//                            (serverHttpRequest) -> ServerRequest
//                                    .create(exchange.mutate().request(serverHttpRequest).build(), messageReaders)
//                                    .bodyToMono(inClass).doOnNext(objectValue -> exchange.getAttributes()
//                                            .put(CACHE_REQUEST_BODY_OBJECT_KEY, objectValue))
//                                    .map(objectValue -> config.getPredicate().test(objectValue)));
//                }
                // return Mono.just(true);
            }

            @Override
            public Object getConfig() {
                return config;
            }

        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public Predicate<ServerWebExchange> apply(Config config) {
        throw new UnsupportedOperationException("PbsRoutePredicateFactory is only async.");
    }

    public static class Config {

        private String routeKey;

        private String includeValue;

        private String functionIds;


        public String getRouteKey() {
            return routeKey;
        }

        public void setRouteKey(String routeKey) {
            this.routeKey = routeKey;
        }

        public String getIncludeValue() {
            return includeValue;
        }

        public void setIncludeValue(String includeValue) {
            this.includeValue = includeValue;
        }

        public String getFunctionIds() {
            return functionIds;
        }

        public void setFunctionIds(String functionIds) {
            this.functionIds = functionIds;
        }
    }

}
