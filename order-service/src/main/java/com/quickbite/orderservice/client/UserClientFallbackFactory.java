package com.quickbite.orderservice.client;

import com.quickbite.orderservice.dto.UserDto;
import com.quickbite.orderservice.exception.UserServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

    private static final Logger log = LoggerFactory.getLogger(UserClientFallbackFactory.class);

    @Override
    public UserClient create(Throwable cause) {
        return new UserClient() {
            @Override
            public UserDto getUserById(Long id) {
                // Вместо заглушки пробрасываем ошибку выше
                throw new UserServiceException(
                        "User-service недоступен. Создание заказа невозможно.", cause
                );
            }
            /*          @Override
            public UserDto getUserById(Long id) {
                // Логируем причину срабатывания Fallback
                log.error("Сработал Fallback при вызове user-service для userId={}. Причина: {}",
                        id, cause.getMessage());

                // Возвращаем дефолтные данные (заглушку)
                UserDto stubUser = new UserDto();
                stubUser.setId(id);
                stubUser.setEmail("fallback@quickbite.com");
                stubUser.setName("Временно недоступен");
                stubUser.setRole("anonymous");
                return stubUser;
            }*/
        };
    }
}
