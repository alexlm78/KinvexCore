package dev.kreaker.kinvex.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para solicitudes de refresh token. Contiene el refresh token para obtener un nuevo access
 * token.
 */
public class RefreshTokenRequest {

    @NotBlank(message = "El refresh token es requerido")
    private String refreshToken;

    // Default constructor
    public RefreshTokenRequest() {}

    // Constructor with parameters
    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // Getters and Setters
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public String toString() {
        return "RefreshTokenRequest{" + "refreshToken='[PROTECTED]'" + '}';
    }
}
