package dev.kreaker.kinvex.security;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.kreaker.kinvex.config.JwtProperties;

/**
 * Tests unitarios para JwtTokenProvider
 */
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        // Configurar propiedades JWT para testing
        jwtProperties = new JwtProperties(
                "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdGVzdGluZy1tdXN0LWJlLWF0LWxlYXN0LTI1Ni1iaXRz", // base64 encoded test secret
                3600L,
                86400L,
                "kinvex-test",
                "kinvex-test-users"
        );
        jwtTokenProvider = new JwtTokenProvider(jwtProperties);
    }

    @Test
    void shouldGenerateValidToken() {
        // Given
        String username = "testuser";
        List<String> roles = List.of("ADMIN", "USER");

        // When
        String token = jwtTokenProvider.generateToken(username, roles);

        // Then
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(username, jwtTokenProvider.getUsernameFromToken(token));
        assertEquals(roles, jwtTokenProvider.getRolesFromToken(token));
    }

    @Test
    void shouldGenerateValidRefreshToken() {
        // Given
        String username = "testuser";

        // When
        String refreshToken = jwtTokenProvider.generateRefreshToken(username);

        // Then
        assertNotNull(refreshToken);
        assertTrue(jwtTokenProvider.validateRefreshToken(refreshToken));
        assertEquals(username, jwtTokenProvider.getUsernameFromToken(refreshToken));
    }

    @Test
    void shouldRejectInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    void shouldRejectExpiredToken() {
        // Given - crear propiedades con expiración muy corta
        JwtProperties shortExpiryProps = new JwtProperties(
                jwtProperties.secret(),
                -1L, // Token ya expirado
                86400L,
                jwtProperties.issuer(),
                jwtProperties.audience()
        );
        JwtTokenProvider shortExpiryProvider = new JwtTokenProvider(shortExpiryProps);

        String username = "testuser";
        List<String> roles = List.of("USER");

        // When
        String expiredToken = shortExpiryProvider.generateToken(username, roles);

        // Then
        assertFalse(jwtTokenProvider.validateToken(expiredToken));
    }
}
