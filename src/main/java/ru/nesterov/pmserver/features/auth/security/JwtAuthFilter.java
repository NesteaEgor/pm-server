package ru.nesterov.pmserver.features.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.nesterov.pmserver.features.users.entity.UserEntity;
import ru.nesterov.pmserver.features.users.repository.UserRepository;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = auth.substring(7);
        try {
            var userId = jwtService.parseUserId(token);
            UserEntity user = userRepository.findById(userId).orElse(null);

            if (user != null) {
                var authentication = new UsernamePasswordAuthenticationToken(user.getId(), null, java.util.List.of());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ignored) {
            // плохой токен -> просто без аутентификации
        }

        chain.doFilter(request, response);
    }
}
