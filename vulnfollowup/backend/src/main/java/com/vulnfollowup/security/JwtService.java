package com.vulnfollowup.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

// Emite y verifica los tokens JWT.
// ¿Por qué JWT y no HTTP Basic como en la práctica de clase? Porque aquí hay DOS clientes
// (web y Android): con un token la contraseña solo viaja una vez (en el login) y el
// servidor sigue siendo STATELESS (no guarda sesión). El token caduca y va firmado con
// HMAC-SHA256: si alguien cambia un rol dentro del token, la firma deja de cuadrar -> 401.
@Service
public class JwtService {

    public static final String ROLES_CLAIM = "roles";
    private static final String ISSUER = "vulnfollowup-api";
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey secretKey;
    private final JwtEncoder encoder;
    private final long expirationMinutes;

    public JwtService(@Value("${vulnfollowup.security.jwt-secret:}") String secret,
                      @Value("${vulnfollowup.security.jwt-expiration-minutes:120}") long expirationMinutes) {
        this.secretKey = buildKey(secret);
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
        this.expirationMinutes = expirationMinutes;
    }

    public IssuedToken issue(String username, List<String> roles) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expirationMinutes, ChronoUnit.MINUTES);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(username)
                .claim(ROLES_CLAIM, roles)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedToken(token, expiresAt);
    }

    // El mismo secreto sirve para verificar: SecurityConfig registra este decoder
    public JwtDecoder decoder() {
        return NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();
    }

    private static SecretKey buildKey(String secret) {
        byte[] bytes;
        if (secret == null || secret.isBlank()) {
            // Sin JWT_SECRET: clave aleatoria. Seguro (nunca hay una clave fija en el repo),
            // pero los tokens dejan de valer al reiniciar. Avisamos en el log.
            bytes = new byte[32];
            new SecureRandom().nextBytes(bytes);
            log.warn("JWT_SECRET no definido: se usa una clave aleatoria (los tokens caducan al reiniciar)");
        } else {
            bytes = secret.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < 32) {
                throw new IllegalStateException("JWT_SECRET debe tener al menos 32 caracteres (HS256)");
            }
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    public record IssuedToken(String value, Instant expiresAt) {
    }
}
