package dev.kreaker.kinvex.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import dev.kreaker.kinvex.audit.AuditHelper;
import dev.kreaker.kinvex.config.JwtProperties;
import dev.kreaker.kinvex.dto.auth.AuthResponse;
import dev.kreaker.kinvex.dto.auth.LoginRequest;
import dev.kreaker.kinvex.dto.auth.LogoutRequest;
import dev.kreaker.kinvex.dto.auth.RefreshTokenRequest;
import dev.kreaker.kinvex.entity.User;
import dev.kreaker.kinvex.entity.User.UserRole;
import dev.kreaker.kinvex.exception.AuthenticationException;
import dev.kreaker.kinvex.exception.InvalidTokenException;
import dev.kreaker.kinvex.repository.UserRepository;
import dev.kreaker.kinvex.security.JwtTokenProvider;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;

    @Mock private PasswordEncoder passwordEncoder;

    @Mock private JwtTokenProvider jwtTokenProvider;

    @Mock private JwtProperties jwtProperties;

    @Mock private AuditHelper auditHelper;

    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        authService =
                new AuthService(
                        userRepository,
                        passwordEncoder,
                        jwtTokenProvider,
                        jwtProperties,
                        auditHelper);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedpassword");
        testUser.setRole(UserRole.OPERATOR);
        testUser.setActive(true);
    }

    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponse() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("testuser", "password");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "hashedpassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(anyString(), any(List.class)))
                .thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken("testuser")).thenReturn("refresh-token");
        when(jwtProperties.expiration()).thenReturn(3600L);

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresIn());

        assertNotNull(response.getUser());
        assertEquals(1L, response.getUser().getId());
        assertEquals("testuser", response.getUser().getUsername());
        assertEquals("test@example.com", response.getUser().getEmail());
        assertEquals(UserRole.OPERATOR, response.getUser().getRole());
    }

    @Test
    void login_WithInvalidUsername_ShouldThrowAuthenticationException() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("invaliduser", "password");
        when(userRepository.findByUsername("invaliduser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_WithInvalidPassword_ShouldThrowAuthenticationException() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "hashedpassword")).thenReturn(false);

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_WithInactiveUser_ShouldThrowAuthenticationException() {
        // Arrange
        testUser.setActive(false);
        LoginRequest loginRequest = new LoginRequest("testuser", "password");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> authService.login(loginRequest));
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewAuthResponse() {
        // Arrange
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest("valid-refresh-token");

        when(jwtTokenProvider.validateRefreshToken("valid-refresh-token")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("valid-refresh-token")).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateToken(anyString(), any(List.class)))
                .thenReturn("new-access-token");
        when(jwtProperties.expiration()).thenReturn(3600L);

        // Act
        AuthResponse response = authService.refreshToken(refreshRequest);

        // Assert
        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("valid-refresh-token", response.getRefreshToken());
        assertEquals("testuser", response.getUser().getUsername());
    }

    @Test
    void refreshToken_WithInvalidToken_ShouldThrowInvalidTokenException() {
        // Arrange
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest("invalid-refresh-token");
        when(jwtTokenProvider.validateRefreshToken("invalid-refresh-token")).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> authService.refreshToken(refreshRequest));
    }

    @Test
    void logout_WithValidToken_ShouldInvalidateToken() {
        // Arrange
        LogoutRequest logoutRequest = new LogoutRequest("valid-refresh-token");

        when(jwtTokenProvider.validateRefreshToken("valid-refresh-token")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("valid-refresh-token")).thenReturn("testuser");

        // Act
        assertDoesNotThrow(() -> authService.logout(logoutRequest));

        // Assert
        assertTrue(authService.isTokenInvalidated("valid-refresh-token"));
    }

    @Test
    void logout_WithInvalidToken_ShouldThrowInvalidTokenException() {
        // Arrange
        LogoutRequest logoutRequest = new LogoutRequest("invalid-refresh-token");
        when(jwtTokenProvider.validateRefreshToken("invalid-refresh-token")).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> authService.logout(logoutRequest));
    }
}
