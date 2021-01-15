package com.rconcept.gateway.infrastructure.config.filter.params;

import io.netty.buffer.EmptyByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBuffer;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

/**
 * @author LZx
 * @since 2020/12/31
 */
@Slf4j
@Component
public class ParamsValidateFilter implements GlobalFilter, Ordered {

    private final List<ParamsValidator> validators;

    public ParamsValidateFilter(List<ParamsValidator> validators) {
        Objects.requireNonNull(validators, "请求体验证器不能为空");
        this.validators = validators;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // body只能被获取一次，此处创建一个ServerHttpRequest的新的实现做包装以躲避该规则
        // 实现方式参考ServerWebExchangeUtils#cacheRequestBody
        ServerHttpRequest request = exchange.getRequest();
        MultiValueMap<String, String> queryParams = request.getQueryParams();
        MediaType contentType = request.getHeaders().getContentType();
        NettyDataBufferFactory factory = (NettyDataBufferFactory) exchange.getResponse().bufferFactory();
        return DataBufferUtils.join(request.getBody())
                .defaultIfEmpty(factory.wrap(new EmptyByteBuf(factory.getByteBufAllocator())))
                .map(dataBuffer -> {
                    try {
                        validators.forEach(validator -> {
                            validator.params(queryParams);
                            validator.body(contentType, dataBuffer);
                        });
                    } catch (ValidateException e) {
                        log.warn("验证请求数据异常", e);
                        throw e;
                    }
                    return new ServerHttpRequestDecorator(exchange.getRequest()) {

                        @Override
                        public Flux<DataBuffer> getBody() {
                            return Mono.fromSupplier(() -> (DataBuffer) factory
                                    .wrap(((NettyDataBuffer) dataBuffer).getNativeBuffer().retainedSlice())
                            ).flux();
                        }
                    };
                }).flatMap(decoratedRequest -> {
                    ServerWebExchange webExchange = exchange.mutate().request(decoratedRequest).build();
                    return chain.filter(webExchange);
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

}
