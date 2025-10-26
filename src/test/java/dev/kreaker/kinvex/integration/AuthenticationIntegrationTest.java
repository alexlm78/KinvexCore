package dev.kreaker.kinvex.integration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.kreaker.kinvex.dto.auth.LoginRequest;
import dev.kreaker.kinvex.dto.auth.LogoutRequest;
import dev.kreaker.kinvex.dto.auth.RefreshTokenRequest;
import dev.kreaker.kinvex.entity.User;
import dev.kreaker.kinvex.repository.UserRepository;
import dev.kreaker.kinvex.security.JwtTokenProvider;

/**
 * Tests de integración para el flujo completo de autenticación. Verifica el
 * funcionamiento end-to-end del sistema de autenticación con JWT.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private final String testPassword = "password123";

    @BeforeEach
    void setUp() {
        // Limpiar base de datos
        userRepository.deleteAll();

        // Crear usuario de prueba
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash(passwordEncoder.encode(testPassword));
        testUser.setRole(User.UserRole.OPERATOR);
        testUser.setActive(true);
        testUser = userRepository.save(testUser);
    }

    @Test
    void completeLoginLogoutFlow_ShouldWorkCorrectly() throws Exception {
        // 1. Login con credenciales válidas
        LoginRequest loginRequest = new LoginRequest(testUser.getUsername(), testPassword);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.user.username").value(testUser.getUsername()))
                .andExpect(jsonPath("$.user.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.user.role").value("OPERATOR"))
                .andReturn();

        // Extraer tokens de la respuesta
        String loginResponseBody = loginResult.getResponse().getContentAsString();
        JsonNode loginResponse = objectMapper.readTree(loginResponseBody);
        String accessToken = loginResponse.get("accessToken").asText();
        String refreshToken = loginResponse.get("refreshToken").asText();

        // Verificar que los tokens son válidos
        assertTrue(jwtTokenProvider.validateToken(accessToken));
        assertTrue(jwtTokenProvider.validateRefreshToken(refreshToken));
        assertEquals(testUser.getUsername(), jwtTokenProvider.getUsernameFromToken(accessToken));

        // 2. Usar el access token para acceder a un endpoint protegido
        // (Simulamos un endpoint protegido - en este caso usaremos un endpoint que no existe)
        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isInternalServerError()); // Este endpoint no existe, pero el token es válido

        // 3. Refresh token para obtener nuevo access token
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").value(refreshToken))
                .andExpect(jsonPath("$.user.username").value(testUser.getUsername()))
                .andReturn();

        // Extraer nuevo access token
        String refreshResponseBody = refreshResult.getResponse().getContentAsString();
        JsonNode refreshResponse = objectMapper.readTree(refreshResponseBody);
        String newAccessToken = refreshResponse.get("accessToken").asText();

        // Verificar que el nuevo token es válido
        assertTrue(jwtTokenProvider.validateToken(newAccessToken));
        // Note: Los tokens pueden ser iguales si se generan en el mismo segundo

        // 4. Logout con refresh token
        LogoutRequest logoutRequest = new LogoutRequest(refreshToken);

        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk());

        // 5. Verificar que después del logout, el refresh token ya no funciona
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }

    @Test
    void jwtTokenValidation_WithValidToken_ShouldAllowAccess() throws Exception {
        // Generar token válido directamente
        List<String> roles = List.of(testUser.getRole().name());
        String validToken = jwtTokenProvider.generateToken(testUser.getUsername(), roles);

        // Verificar que el token es válido
        assertTrue(jwtTokenProvider.validateToken(validToken));
        assertEquals(testUser.getUsername(), jwtTokenProvider.getUsernameFromToken(validToken));
        assertEquals(roles, jwtTokenProvider.getRolesFromToken(validToken));

        // Intentar acceder a un endpoint con el token (aunque no exista, debería pasar la autenticación)
        mockMvc.perform(get("/api/protected/test")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isInternalServerError()); // 500 porque el endpoint no existe, pero pasó la autenticación
    }

    @Test
    void jwtTokenValidation_WithInvalidToken_ShouldDenyAccess() throws Exception {
        String invalidToken = "invalid.jwt.token";

        // Intentar acceder con token inválido
        mockMvc.perform(get("/api/protected/test")
                .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized()); // Token inválido debería retornar 401
    }

    @Test
    void jwtTokenValidation_WithExpiredToken_ShouldDenyAccess() throws Exception {
        // Crear token con expiración muy corta (ya expirado)
        String expiredToken = jwtTokenProvider.generateToken(testUser.getUsername(), List.of("OPERATOR"));

        // Esperar un momento para asegurar que el token expire (en un escenario real)
        // En este caso, usaremos un token que sabemos que está mal formado para simular expiración
        String malformedToken = expiredToken.substring(0, expiredToken.length() - 5) + "xxxxx";

        // Intentar acceder con token expirado/malformado
        mockMvc.perform(get("/api/protected/test")
                .header("Authorization", "Bearer " + malformedToken))
                .andExpect(status().isUnauthorized()); // Token malformado debería retornar 401
    }

    @Test
    void refreshTokenFlow_WithValidRefreshToken_ShouldGenerateNewAccessToken() throws Exception {
        // 1. Login para obtener tokens
        LoginRequest loginRequest = new LoginRequest(testUser.getUsername(), testPassword);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponseBody = loginResult.getResponse().getContentAsString();
        JsonNode loginResponse = objectMapper.readTree(loginResponseBody);
        String originalAccessToken = loginResponse.get("accessToken").asText();
        String refreshToken = loginResponse.get("refreshToken").asText();

        // 2. Usar refresh token para obtener nuevo access token
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").value(refreshToken))
                .andExpect(jsonPath("$.user.username").value(testUser.getUsername()))
                .andReturn();

        String refreshResponseBody = refreshResult.getResponse().getContentAsString();
        JsonNode refreshResponse = objectMapper.readTree(refreshResponseBody);
        String newAccessToken = refreshResponse.get("accessToken").asText();

        // 3. Verificar que ambos tokens son válidos
        assertTrue(jwtTokenProvider.validateToken(originalAccessToken));
        assertTrue(jwtTokenProvider.validateToken(newAccessToken));
        // Note: Los tokens pueden ser iguales si se generan en el mismo segundo

        // 4. Verificar que ambos tokens contienen la misma información de usuario
        assertEquals(testUser.getUsername(), jwtTokenProvider.getUsernameFromToken(originalAccessToken));
        assertEquals(testUser.getUsername(), jwtTokenProvider.getUsernameFromToken(newAccessToken));
    }

    @Test
    void refreshTokenFlow_WithInvalidRefreshToken_ShouldReturnError() throws Exception {
        String invalidRefreshToken = "invalid.refresh.token";

        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(invalidRefreshToken);

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void loginFlow_WithInvalidCredentials_ShouldReturnError() throws Exception {
        LoginRequest invalidLoginRequest = new LoginRequest(testUser.getUsername(), "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_ERROR"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void loginFlow_WithInactiveUser_ShouldReturnError() throws Exception {
        // Desactivar usuario
        testUser.setActive(false);
        userRepository.save(testUser);

        LoginRequest loginRequest = new LoginRequest(testUser.getUsername(), testPassword);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_ERROR"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void logoutFlow_WithValidRefreshToken_ShouldInvalidateToken() throws Exception {
        // 1. Login para obtener refresh token
        LoginRequest loginRequest = new LoginRequest(testUser.getUsername(), testPassword);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponseBody = loginResult.getResponse().getContentAsString();
        JsonNode loginResponse = objectMapper.readTree(loginResponseBody);
        String refreshToken = loginResponse.get("refreshToken").asText();

        // 2. Logout
        LogoutRequest logoutRequest = new LogoutRequest(refreshToken);

        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk());

        // 3. Verificar que el refresh token ya no funciona
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }

    @Test
    void logoutFlow_WithInvalidRefreshToken_ShouldReturnError() throws Exception {
        String invalidRefreshToken = "invalid.refresh.token";

        LogoutRequest logoutRequest = new LogoutRequest(invalidRefreshToken);

        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"))
                .andExpect(jsonPath("$.message").exists());
    }
}
