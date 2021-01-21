package com.rconcept.gateway.infrastructure.config.filter;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * @author LZx
 * @since 2021/1/15
 */
@Component
public class RequestIdFilter implements WebFilter, Ordered {

    private static final String REQUEST_ID_KEY_NAME = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange)
                .transformDeferredContextual((original, contextView) -> {
                    contextView.getOrEmpty(REQUEST_ID_KEY_NAME).ifPresent(requestId -> {
                        MDC.put(REQUEST_ID_KEY_NAME, (String) requestId);
                    });
                    return original;
                })
                .contextWrite(Context.of(REQUEST_ID_KEY_NAME, exchange.getRequest().getId()))
                .doFinally(signalType -> MDC.remove(REQUEST_ID_KEY_NAME));
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
