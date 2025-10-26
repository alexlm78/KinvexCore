package dev.kreaker.kinvex.dto.auth;

import jakarta.validation.constraints.NotBlank;

/** DTO para solicitudes de logout. Contiene el refresh token que debe ser invalidado. */
public class LogoutRequest {

    @NotBlank(message = "El refresh token es requerido")
    private String refreshToken;

    // Default constructor
    public LogoutRequest() {}

    // Constructor with parameters
    public LogoutRequest(String refreshToken) {
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
        return "LogoutRequest{" + "refreshToken='[PROTECTED]'" + '}';
    }
}
