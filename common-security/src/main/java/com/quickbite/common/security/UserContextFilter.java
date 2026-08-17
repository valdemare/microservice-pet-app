package com.quickbite.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UserContextFilter extends OncePerRequestFilter {

    public static final String X_USER_ID = "X-User-Id";
    public static final String X_USER_ROLE = "X-User-Role";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String userIdHeader = request.getHeader(X_USER_ID);
        String userRolesHeader = request.getHeader(X_USER_ROLE);

        if (userIdHeader != null && !userIdHeader.isBlank()) {
            try{
                Long userId = Long.parseLong(userIdHeader);

                List<String> roles = (userRolesHeader != null && !userRolesHeader.isBlank())
                        ? Arrays.asList(userRolesHeader.split(","))
                        : Collections.emptyList();

                // Преобразуем строки ролей в GrantedAuthority для Spring Security
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                UserContext userContext = new UserContext(userId, roles);

                // Аутентифицируем пользователя внутри Spring Security
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userContext, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }catch(NumberFormatException e)
            {
                logger.warn("Получен некорректный заголовок X-User-Id: "+ userIdHeader);
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Очищаем контекст после выполнения запроса для предотвращения утечек в ThreadPool
            SecurityContextHolder.clearContext();
        }
    }
}