package com.quickbite.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OrderCreateDto(
        @NotBlank(message = "Описание не должно быть пустым")
        String description,

        @NotNull(message = "Укажите цену")
        @Positive(message = "Цена должна быть больше 0")
        BigDecimal price
) {}
