package dev.kreaker.kinvex.controller;

import dev.kreaker.kinvex.dto.auth.AuthResponse;
import dev.kreaker.kinvex.dto.auth.LoginRequest;
import dev.kreaker.kinvex.dto.auth.LogoutRequest;
import dev.kreaker.kinvex.dto.auth.RefreshTokenRequest;
import dev.kreaker.kinvex.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para operaciones de autenticación. Maneja login, logout y refresh de tokens JWT.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Operaciones de autenticación y autorización")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint para autenticar usuarios.
     *
     * @param loginRequest Credenciales del usuario
     * @return AuthResponse con tokens JWT y información del usuario
     */
    @PostMapping("/login")
    @Operation(
            summary = "Autenticar usuario",
            description = "Autentica un usuario con username y password, retorna tokens JWT")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Autenticación exitosa"),
                @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
                @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
                @ApiResponse(responseCode = "403", description = "Usuario inactivo")
            })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para refrescar access token.
     *
     * @param refreshRequest Solicitud con refresh token
     * @return AuthResponse con nuevo access token
     */
    @PostMapping("/refresh")
    @Operation(
            summary = "Refrescar access token",
            description = "Genera un nuevo access token usando un refresh token válido")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Token refrescado exitosamente"),
                @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
                @ApiResponse(
                        responseCode = "401",
                        description = "Refresh token inválido o expirado")
            })
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshRequest) {
        AuthResponse response = authService.refreshToken(refreshRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para cerrar sesión (logout).
     *
     * @param logoutRequest Solicitud con refresh token a invalidar
     * @return Respuesta vacía indicando logout exitoso
     */
    @PostMapping("/logout")
    @Operation(
            summary = "Cerrar sesión",
            description = "Invalida el refresh token del usuario para cerrar la sesión")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Logout exitoso"),
                @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
                @ApiResponse(responseCode = "401", description = "Refresh token inválido")
            })
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest logoutRequest) {
        authService.logout(logoutRequest);
        return ResponseEntity.ok().build();
    }
}
