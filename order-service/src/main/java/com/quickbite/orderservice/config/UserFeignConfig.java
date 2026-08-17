package com.quickbite.orderservice.config;

import com.quickbite.orderservice.exception.UserServiceException;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class UserFeignConfig {
    @Bean
    public ErrorDecoder userErrorDecoder() {
        return (methodKey, response) ->
                new UserServiceException(
                    String.format("Ошибка при вызове user-service [%s]: HTTP %d", methodKey, response.status())
                );
    }
}