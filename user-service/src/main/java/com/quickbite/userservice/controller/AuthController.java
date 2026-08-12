package com.quickbite.userservice.controller;

import com.quickbite.userservice.dto.AuthRequest;
import com.quickbite.userservice.dto.AuthResponse;
import com.quickbite.userservice.entity.UserEntity;
import com.quickbite.userservice.repository.UserRepository;
import com.quickbite.userservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
// 1. Находим пользователя в БД по email
        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Неверный email или пароль"
                ));

        // 2. В реальности здесь проверяется пароль через PasswordEncoder:
        // if (!passwordEncoder.matches(request.password(), user.getPassword())) ...

        // 3. Генерируем токен, подтягивая роль пользователя из БД
        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name() // ◄ "ROLE_USER" или "ROLE_ADMIN" из поля user.getRole()
        );
        return ResponseEntity.ok(new AuthResponse(token));
    }
}