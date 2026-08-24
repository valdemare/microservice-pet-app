package com.quickbite.orderservice.client;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.quickbite.orderservice.dto.UserDto;
import com.quickbite.orderservice.exception.UserServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@WireMockTest(httpPort = 8089)
@TestPropertySource(properties = {
        "spring.cloud.loadbalancer.enabled=false",
        "user-service.url=http://localhost:8089"
})
class UserClientWireMockTest {

    @Autowired
    private UserClient userClient;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Подменяем URL клиента на порт WireMock
        registry.add("user-service.url", () -> "http://localhost:8089");
    }

    @Test
    @DisplayName("Feign должен успешно спарсить UserDto при HTTP 200 OK")
    void getUserById_whenSuccess_shouldReturnUser() {
        stubFor(get(urlEqualTo("/api/v1/users/1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\": 1, \"name\": \"Иван\", \"email\": \"ivan@test.com\"}")
                        .withStatus(200)));

        UserDto user = userClient.getUserById(1L);

        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("При HTTP 500 должен сработать Fallback / выкинуться UserServiceException")
    void getUserById_whenServerError_shouldTriggerFallback() {
        stubFor(get(urlEqualTo("/api/v1/users/99"))
                .willReturn(aResponse()
                        .withStatus(500)));

        assertThrows(UserServiceException.class, () -> userClient.getUserById(99L));
    }
}