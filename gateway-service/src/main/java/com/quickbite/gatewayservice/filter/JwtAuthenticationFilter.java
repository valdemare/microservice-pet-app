package com.quickbite.gatewayservice.filter;


import com.quickbite.gatewayservice.security.JwtUtils;
import com.quickbite.gatewayservice.security.RouterValidator;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtils jwtUtils;
    private final RouterValidator routerValidator;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, RouterValidator routerValidator) {
        this.jwtUtils = jwtUtils;
        this.routerValidator = routerValidator;
    }

@Override
public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();

    // 1. ПРИНУДИТЕЛЬНО очищаем входящий запрос от внешних заголовков X-User-* (защита от Spoofing)
    ServerHttpRequest.Builder requestBuilder = request.mutate()
            .headers(headers -> {
                headers.remove("X-User-Id");
                headers.remove("X-User-Role");
            });

    // 2. Если маршрут публичный (например, /api/v1/auth/login) — пробрасываем ОЧИЩЕННЫЙ запрос дальше
    if (!routerValidator.isSecured.test(request)) {
        return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
    }

    // 3. Для защищенных маршрутов проверяем заголовок Authorization
    String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        return onError(exchange, "Отсутствует или неверный заголовок Authorization", HttpStatus.UNAUTHORIZED);
    }

    String token = authHeader.substring(7);

    try {
        // 4. Извлекаем Claims (проверка подписи происходит внутри getClaims)
        Claims claims = jwtUtils.getClaims(token);

        // 5. Обогащаем очищенный запрос валидными заголовками
        ServerHttpRequest mutatedRequest = requestBuilder
                .header("X-User-Id", claims.getSubject())
                .header("X-User-Role", claims.get("role", String.class))
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());

    } catch (Exception e) {
        return onError(exchange, "Невалидный или просроченный JWT токен", HttpStatus.UNAUTHORIZED);
    }
}

    private Mono<Void> onError(ServerWebExchange exchange, String errorMessage, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String jsonBody = String.format("{\"status\": %d, \"error\": \"%s\"}", httpStatus.value(), errorMessage);
        DataBuffer buffer = response.bufferFactory().wrap(jsonBody.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }
    @Override
    public int getOrder() {
        return -1; // Высший приоритет (выполняется до других фильтров)
    }
}