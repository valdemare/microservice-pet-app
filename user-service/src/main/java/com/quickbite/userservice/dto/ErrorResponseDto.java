package com.quickbite.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorResponseDto {
    private int status;          // HTTP-код (например, 404)
    private String error;        // Название ошибки ("Not Found")
    private String message;      // Понятное описание ("Пользователь с ID 999 не найден")
    private LocalDateTime timestamp; // Время возникновения
}
