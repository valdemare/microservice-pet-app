package com.quickbite.orderservice.client;

import com.quickbite.orderservice.config.UserFeignConfig;
import com.quickbite.orderservice.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Вызывает user-service по указанному URL
@FeignClient(name = "user-service"
        ,url = "${user-service.url:}"
        ,fallbackFactory = UserClientFallbackFactory.class
        ,configuration = UserFeignConfig.class
)
public interface UserClient {

    @GetMapping("/api/v1/users/{id}")
    UserDto getUserById(@PathVariable("id") Long id);
}