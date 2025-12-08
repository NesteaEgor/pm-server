package ru.nesterov.pmserver.features.chat.ws;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.nesterov.pmserver.features.auth.security.JwtService;
import ru.nesterov.pmserver.features.users.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        // Авторизуемся на CONNECT
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticate(accessor);
            // ВАЖНО: вернуть НОВОЕ сообщение с обновлёнными headers
            return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
        }

        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders == null || authHeaders.isEmpty()) {
            throw new MessagingException("Missing Authorization header");
        }

        String auth = authHeaders.get(0);
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new MessagingException("Invalid Authorization header");
        }

        String token = auth.substring(7);
        UUID userId = jwtService.parseUserId(token);

        userRepository.findById(userId).orElseThrow(() -> new MessagingException("User not found"));

        var authentication = new UsernamePasswordAuthenticationToken(userId, null, List.of());
        accessor.setUser(authentication);
    }
}
