package ru.nesterov.pmserver.features.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final byte[] secretBytes;
    private final long ttlMinutes;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.ttlMinutes}") long ttlMinutes
    ) {
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.ttlMinutes = ttlMinutes;
    }

    public String generateToken(UUID userId, String email) {
        Instant now = Instant.now();
        Instant exp = now.plus(ttlMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("email", email)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(Keys.hmacShaKeyFor(secretBytes), SignatureAlgorithm.HS256)
                .compact();
    }

    public UUID parseUserId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretBytes))
                .build()
                .parseClaimsJws(token)
                .getBody();

        return UUID.fromString(claims.getSubject());
    }
}
