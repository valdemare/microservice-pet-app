package com.quickbite.gatewayservice.filter;


import com.quickbite.gatewayservice.security.JwtUtils;
import com.quickbite.gatewayservice.security.RouterValidator;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

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

        // 1. Проверяем, нужно ли проверять токен для этого маршрута
        if (routerValidator.isSecured.test(request)) {

            // 2. Проверяем наличие заголовка Authorization
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                // 3. Проверяем токен
                jwtUtils.validateToken(token);
                Claims claims = jwtUtils.getClaims(token);

                // 4. Обогащаем запрос заголовками с данными пользователя
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-Id", claims.getSubject())
                        .header("X-User-Role", claims.get("role", String.class))
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (Exception e) {
                // Токен невалиден или просрочен -> 401 Unauthorized
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }
        }

        // Если маршрут публичный (например, /login), пропускаем дальше
        return chain.filter(exchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1; // Высший приоритет (выполняется до других фильтров)
    }
}