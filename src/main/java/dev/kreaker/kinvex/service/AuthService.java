package dev.kreaker.kinvex.service;

import dev.kreaker.kinvex.config.JwtProperties;
import dev.kreaker.kinvex.dto.auth.AuthResponse;
import dev.kreaker.kinvex.dto.auth.LoginRequest;
import dev.kreaker.kinvex.dto.auth.LogoutRequest;
import dev.kreaker.kinvex.dto.auth.RefreshTokenRequest;
import dev.kreaker.kinvex.entity.User;
import dev.kreaker.kinvex.exception.AuthenticationException;
import dev.kreaker.kinvex.exception.InvalidTokenException;
import dev.kreaker.kinvex.repository.UserRepository;
import dev.kreaker.kinvex.security.JwtTokenProvider;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de autenticación que maneja login, logout y refresh de tokens JWT. Implementa la lógica
 * de negocio para autenticación y autorización de usuarios.
 */
@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    // Set para mantener tokens invalidados (blacklist)
    // En producción, esto debería ser un cache distribuido como Redis
    private final Set<String> invalidatedTokens = ConcurrentHashMap.newKeySet();

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            JwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
    }

    /**
     * Autentica un usuario con username y password.
     *
     * @param loginRequest Credenciales del usuario
     * @return AuthResponse con tokens y información del usuario
     * @throws AuthenticationException si las credenciales son inválidas
     */
    public AuthResponse login(LoginRequest loginRequest) {
        logger.info("Intento de login para usuario: {}", loginRequest.getUsername());

        // Buscar usuario por username
        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());

        if (userOptional.isEmpty()) {
            logger.warn("Usuario no encontrado: {}", loginRequest.getUsername());
            throw new AuthenticationException("Credenciales inválidas");
        }

        User user = userOptional.get();

        // Verificar que el usuario esté activo
        if (!user.getActive()) {
            logger.warn("Usuario inactivo intentó hacer login: {}", loginRequest.getUsername());
            throw new AuthenticationException("Usuario inactivo");
        }

        // Verificar contraseña
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            logger.warn("Contraseña incorrecta para usuario: {}", loginRequest.getUsername());
            throw new AuthenticationException("Credenciales inválidas");
        }

        // Generar tokens
        List<String> roles = List.of("ROLE_" + user.getRole().name());
        String accessToken = jwtTokenProvider.generateToken(user.getUsername(), roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

        // Crear información del usuario para la respuesta
        AuthResponse.UserInfo userInfo =
                new AuthResponse.UserInfo(
                        user.getId(), user.getUsername(), user.getEmail(), user.getRole());

        logger.info("Login exitoso para usuario: {}", user.getUsername());

        return new AuthResponse(accessToken, refreshToken, jwtProperties.expiration(), userInfo);
    }

    /**
     * Refresca un access token usando un refresh token válido.
     *
     * @param refreshRequest Solicitud con refresh token
     * @return AuthResponse con nuevo access token
     * @throws InvalidTokenException si el refresh token es inválido
     */
    public AuthResponse refreshToken(RefreshTokenRequest refreshRequest) {
        String refreshToken = refreshRequest.getRefreshToken();

        logger.info("Intento de refresh token");

        // Verificar que el token no esté en la blacklist
        if (invalidatedTokens.contains(refreshToken)) {
            logger.warn("Intento de usar refresh token invalidado");
            throw new InvalidTokenException("Refresh token inválido");
        }

        // Validar refresh token
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            logger.warn("Refresh token inválido o expirado");
            throw new InvalidTokenException("Refresh token inválido o expirado");
        }

        // Extraer username del refresh token
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

        // Buscar usuario
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            logger.warn("Usuario no encontrado para refresh token: {}", username);
            throw new InvalidTokenException("Usuario no encontrado");
        }

        User user = userOptional.get();

        // Verificar que el usuario esté activo
        if (!user.getActive()) {
            logger.warn("Usuario inactivo intentó refresh token: {}", username);
            throw new InvalidTokenException("Usuario inactivo");
        }

        // Generar nuevo access token
        List<String> roles = List.of("ROLE_" + user.getRole().name());
        String newAccessToken = jwtTokenProvider.generateToken(user.getUsername(), roles);

        // Crear información del usuario para la respuesta
        AuthResponse.UserInfo userInfo =
                new AuthResponse.UserInfo(
                        user.getId(), user.getUsername(), user.getEmail(), user.getRole());

        logger.info("Refresh token exitoso para usuario: {}", user.getUsername());

        return new AuthResponse(
                newAccessToken,
                refreshToken, // Mantener el mismo refresh token
                jwtProperties.expiration(),
                userInfo);
    }

    /**
     * Invalida un refresh token (logout).
     *
     * @param logoutRequest Solicitud con refresh token a invalidar
     */
    public void logout(LogoutRequest logoutRequest) {
        String refreshToken = logoutRequest.getRefreshToken();

        logger.info("Intento de logout");

        // Validar que el refresh token sea válido antes de invalidarlo
        if (jwtTokenProvider.validateRefreshToken(refreshToken)) {
            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

            // Agregar token a la blacklist
            invalidatedTokens.add(refreshToken);

            logger.info("Logout exitoso para usuario: {}", username);
        } else {
            logger.warn("Intento de logout con refresh token inválido");
            throw new InvalidTokenException("Refresh token inválido");
        }
    }

    /**
     * Verifica si un refresh token está invalidado.
     *
     * @param refreshToken Token a verificar
     * @return true si el token está invalidado
     */
    public boolean isTokenInvalidated(String refreshToken) {
        return invalidatedTokens.contains(refreshToken);
    }

    /**
     * Limpia tokens expirados de la blacklist. Este método debería ser llamado periódicamente por
     * un scheduler.
     */
    public void cleanupExpiredTokens() {
        logger.info("Iniciando limpieza de tokens expirados");

        invalidatedTokens.removeIf(
                token -> {
                    try {
                        return jwtTokenProvider.isTokenExpired(token);
                    } catch (Exception e) {
                        // Si hay error al parsear el token, lo removemos
                        return true;
                    }
                });

        logger.info(
                "Limpieza de tokens completada. Tokens activos en blacklist: {}",
                invalidatedTokens.size());
    }
}
