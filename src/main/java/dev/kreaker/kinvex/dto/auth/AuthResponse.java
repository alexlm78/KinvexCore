package dev.kreaker.kinvex.dto.auth;

import dev.kreaker.kinvex.entity.User.UserRole;

/**
 * DTO para respuestas de autenticación. Contiene el token JWT, información del usuario y metadatos
 * de la sesión.
 */
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserInfo user;

    // Default constructor
    public AuthResponse() {}

    // Constructor with parameters
    public AuthResponse(String accessToken, String refreshToken, Long expiresIn, UserInfo user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    // Getters and Setters
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    /** Información básica del usuario para incluir en la respuesta de autenticación. */
    public static class UserInfo {

        private Long id;
        private String username;
        private String email;
        private UserRole role;

        // Default constructor
        public UserInfo() {}

        // Constructor with parameters
        public UserInfo(Long id, String username, String email, UserRole role) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.role = role;
        }

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public UserRole getRole() {
            return role;
        }

        public void setRole(UserRole role) {
            this.role = role;
        }
    }
}
