package dev.kreaker.kinvex.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTO para solicitudes de login. Contiene las credenciales del usuario para autenticación. */
public class LoginRequest {

    @NotBlank(message = "El username es requerido")
    @Size(max = 50, message = "El username no puede exceder 50 caracteres")
    private String username;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    private String password;

    // Default constructor
    public LoginRequest() {}

    // Constructor with parameters
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginRequest{" + "username='" + username + '\'' + ", password='[PROTECTED]'" + '}';
    }
}
