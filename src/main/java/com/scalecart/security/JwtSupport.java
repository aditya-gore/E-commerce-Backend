package com.scalecart.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtSupport {

    private final SecretKey key;
    private final long expirationMs;

    public JwtSupport(
        @Value("${app.jwt.secret:ScaleCartCapstoneJwtSecretKeyForSigningMustBeAtLeast256BitsLongForHS256}") String secret,
        @Value("${app.jwt.expiration-ms:3600000}") long expirationMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username, List<String> roles) {
        return Jwts.builder()
            .subject(username)
            .claim("roles", roles)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expirationMs))
            .signWith(key)
            .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(Claims claims) {
        List<?> list = claims.get("roles", List.class);
        if (list == null) return List.of();
        return list.stream().map(Object::toString).collect(Collectors.toList());
    }
}
