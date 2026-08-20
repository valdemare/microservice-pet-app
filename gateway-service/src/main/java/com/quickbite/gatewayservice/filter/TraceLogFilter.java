package com.quickbite.gatewayservice.filter;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class TraceLogFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(TraceLogFilter.class);
    private final ObservationRegistry observationRegistry;

    public TraceLogFilter(ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return Mono.defer(() -> {

            Observation observation = Observation.start("gateway-request", observationRegistry);
            log.info("[DIAG-1] Observation создан: {}", observation.getContext().getName());
            log.info("[DIAG-2] Входящий traceparent заголовок на Gateway: {}",
                    exchange.getRequest().getHeaders().getFirst("traceparent"));
            return chain.filter(exchange)
                    .doOnSuccess(v -> log.info("Входящий запрос на Gateway: {}", exchange.getRequest().getPath()))
                    .doOnError(e -> log.error("Ошибка при обработке запроса: {}", e.getMessage()))
                    .doFinally(signal -> observation.stop())
                    .contextWrite(context -> context.put(ObservationThreadLocalAccessor.KEY, observation));
        });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}