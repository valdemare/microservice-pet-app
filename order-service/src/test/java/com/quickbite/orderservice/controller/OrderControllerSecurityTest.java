package com.quickbite.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickbite.common.security.CommonSecurityConfig;
import com.quickbite.orderservice.entity.OrderEntity;
import com.quickbite.orderservice.repository.OrderRepository;
import com.quickbite.orderservice.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class,
        properties = {"eureka.client.enabled=false", "spring.cloud.discovery.enabled=false"})
@ActiveProfiles("test")
@Import(CommonSecurityConfig.class)
@EnableMethodSecurity
class OrderControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService; // Заменяем сервис заглушкой, проверяем ТОЛЬКО Security/Controller
    @MockitoBean
    private OrderRepository orderRepository;
    @Test
    @DisplayName("POST /api/v1/orders - Должен успешно создать заказ для ROLE_USER")
    void createOrder_whenUserRole_shouldReturn200() throws Exception {
        OrderEntity orderRequest = new OrderEntity();
        orderRequest.setUserId(1L);
        orderRequest.setDescription("Пицца");
        orderRequest.setPrice(new BigDecimal("500.00"));

        when(orderService.createOrder(any(OrderEntity.class), eq(1L)))
                .thenReturn(orderRequest);

        mockMvc.perform(post("/api/v1/orders")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ROLE_USER") // ◄ Симулируем заголовок от Gateway
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/v1/orders - Должен вернуть 403 Forbidden для роли ROLE_USER")
    void getAllOrders_whenUserRole_shouldReturn403() throws Exception {

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/v1/orders - Должен вернуть 200 OK для роли ROLE_ADMIN")
    void getAllOrders_whenAdminRole_shouldReturn200() throws Exception {
        OrderEntity order = new OrderEntity();
        order.setId(1L);
        order.setDescription("Суши");

        when(orderRepository.findAll()).thenReturn(List.of(order));

        mockMvc.perform(get("/api/v1/orders")
                .header("X-User-Id", "1")            // ◄ Передаем ID
                .header("X-User-Role", "ROLE_ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Суши"));
    }

    @Test
    @DisplayName("GET /api/v1/orders - 401 Unauthorized/403 Forbidden для неаутентифицированного пользователя")
    void getAllOrders_whenAnonymous_shouldBeDenied() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().is4xxClientError()); // Проверяет 401 или 403
    }

    @Test
    @DisplayName("POST /api/v1/orders - 400 Bad Request при невалидном теле запроса")
    @WithMockUser(roles = "USER")
    void createOrder_whenInvalidData_shouldReturn400() throws Exception {
        OrderEntity invalidOrder = new OrderEntity();
        // Передаем пустой объект (если поля размечены аннотациями @NotNull, @NotBlank и т.д.)

        mockMvc.perform(post("/api/v1/orders")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidOrder)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/orders - Без заголовка X-User-Id должен обработать как неавторизованный запрос")
    void createOrder_withoutHeaders_shouldBeHandledCorrectly() throws Exception {
        OrderEntity orderRequest = new OrderEntity();
        orderRequest.setDescription("Суши");
        orderRequest.setPrice(new BigDecimal("1000.00"));

        // Если заголовка нет, UserContextFilter пропускает запрос дальше без аутентификации,
        // а @PreAuthorize("hasAnyRole(...)") отклоняет его (403)
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isForbidden());
    }
}