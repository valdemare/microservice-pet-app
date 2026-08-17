package com.quickbite.orderservice.config;

import com.quickbite.common.security.SecurityConstants;
import com.quickbite.common.security.UserContext;
import com.quickbite.common.security.UserContextFilter;
import com.quickbite.orderservice.exception.UserServiceException;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;

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
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserContext userContext) {
                requestTemplate.header(SecurityConstants.X_USER_ID, String.valueOf(userContext.getUserId()));
                if (userContext.getRoles() != null && !userContext.getRoles().isEmpty()) {
                    requestTemplate.header(SecurityConstants.X_USER_ROLE, String.join(",", userContext.getRoles()));
                }
            }
        };
    }
}