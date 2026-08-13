package com.quickbite.gatewayservice.filter;

import com.quickbite.gatewayservice.security.JwtUtils;
import com.quickbite.gatewayservice.security.RouterValidator;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String SECRET_KEY = Encoders.BASE64.encode(
            "super-secret-key-for-testing-jwt-tokens-must-be-long-enough".getBytes()
    );

    private JwtAuthenticationFilter filter;
    private JwtUtils jwtUtils;
    private RouterValidator routerValidator;

    @Mock
    private GatewayFilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        // Внедряем тестовый секретный ключ в JwtUtils
        ReflectionTestUtils.setField(jwtUtils, "secret", SECRET_KEY);

        routerValidator = new RouterValidator();
        filter = new JwtAuthenticationFilter(jwtUtils, routerValidator);

        lenient().when(filterChain.filter(any())).thenReturn(Mono.empty());
    }

    // Хелпер для генерации валидного токена
    private String generateValidToken(String userId, String role) {
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(SECRET_KEY);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
                .subject(userId)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000)) // +1 минута
                .signWith(key)
                .compact();
    }

    // --- ТЕСТЫ ---

    @Test
    @DisplayName("Публичный маршрут (/api/v1/auth/login) - должен пропускать без токена")
    void filter_whenPublicRoute_shouldPassWithoutToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/auth/login").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, filterChain).block();

        verify(filterChain, times(1)).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isNull(); // Статус ошибки не выставлялся
    }

    @Test
    @DisplayName("Защищенный маршрут без заголовка Authorization - должен возвращать 401 Unauthorized")
    void filter_whenPrivateRouteNoAuthHeader_shouldReturn401() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/orders").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, filterChain).block();

        verify(filterChain, never()).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Защищенный маршрут с невалидным токеном - должен возвращать 401 Unauthorized")
    void filter_whenPrivateRouteInvalidToken_shouldReturn401() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid_token_123")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, filterChain).block();

        verify(filterChain, never()).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Валидный токен - должен пробрасывать X-User-Id и X-User-Role дальше в chain")
    void filter_whenValidToken_shouldEnrichHeadersAndPass() {
        String token = generateValidToken("100", "ROLE_USER");

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, filterChain).block();

        // Захватываем exchange, который передали дальше в цепочку
        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(filterChain).filter(captor.capture());

        ServerHttpRequest mutatedRequest = captor.getValue().getRequest();
        assertThat(mutatedRequest.getHeaders().getFirst("X-User-Id")).isEqualTo("100");
        assertThat(mutatedRequest.getHeaders().getFirst("X-User-Role")).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("🚨 Защита от подмены заголовков - внешние X-User-Id должны удаляться из запроса")
    void filter_whenHeaderSpoofingAttempt_shouldRemoveExternalHeaders() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/auth/login")
                .header("X-User-Id", "666-HACKER") // Злоумышленник пытался передать свой ID
                .header("X-User-Role", "ROLE_ADMIN")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, filterChain).block();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(filterChain).filter(captor.capture());

        ServerHttpRequest mutatedRequest = captor.getValue().getRequest();

        // ❌ На текущем (старом) фильтре этот тест УПАДЕТ, так как фильтр не очищает эти заголовки!
        // ✅ После того, как мы перепишем JwtAuthenticationFilter, тест станет ЗЕЛЕНЫМ.
        assertThat(mutatedRequest.getHeaders().get("X-User-Id")).isNullOrEmpty();
        assertThat(mutatedRequest.getHeaders().get("X-User-Role")).isNullOrEmpty();
    }
}