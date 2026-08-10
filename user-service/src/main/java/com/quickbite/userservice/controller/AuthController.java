package com.quickbite.userservice.controller;

import com.quickbite.userservice.dto.AuthRequest;
import com.quickbite.userservice.dto.AuthResponse;
import com.quickbite.userservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        // Заглушка проверки пользователя (в реальности проверяем пароль через PasswordEncoder)
        // Допустим, пользователь найден в БД с ID=1, ролью ROLE_USER:

        String token = jwtService.generateToken(1L, request.email(), "ROLE_USER");
        return ResponseEntity.ok(new AuthResponse(token));
    }
}