package com.quickbite.orderservice.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
public class CustomFeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Feign error occurred during call '{}'. Status code: {}", methodKey, response.status());

        return switch (response.status()) {
            case 404 -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Пользователь не найден в user-service"
            );
            case 400 -> new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Некорректный запрос к user-service"
            );
            case 500, 502, 503, 504 -> new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "user-service временно недоступен или вернул ошибку сервера"
            );
            default -> defaultErrorDecoder.decode(methodKey, response);
        };
    }
}