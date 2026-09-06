package com.quickbite.orderservice.config;

import com.quickbite.common.security.SecurityConstants;
import com.quickbite.common.security.UserContext;
import com.quickbite.common.security.UserContextFilter;
import com.quickbite.orderservice.exception.UserServiceException;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
//import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class UserFeignConfig {
    @Bean
    public ErrorDecoder userErrorDecoder() {
        return (methodKey, response) ->
                new UserServiceException(
                    String.format("Ошибка при вызове user-service [%s]: HTTP %d", methodKey, response.status())
                );
    }
    @Bean
    public RequestInterceptor userContextRequestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                String userId = request.getHeader(SecurityConstants.X_USER_ID);
                String userRole = request.getHeader(SecurityConstants.X_USER_ROLE);

                if (userId != null && !userId.isBlank()) {
                    requestTemplate.header(SecurityConstants.X_USER_ID, userId);
                }
                if (userRole != null && !userRole.isBlank()) {
                    requestTemplate.header(SecurityConstants.X_USER_ROLE, userRole);
                }
            }else {
                // Если попадаем сюда — контекст потока пуст!
                System.err.println("Feign Interceptor: Authentication is NULL or invalid principal!");
            }
        };
    }
}