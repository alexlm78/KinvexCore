package dev.kreaker.kinvex.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Punto de entrada personalizado para manejar errores de autenticación. Se ejecuta cuando un
 * usuario no autenticado intenta acceder a un recurso protegido.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException {

        // Configurar la respuesta HTTP
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Crear el cuerpo de la respuesta de error
        ErrorResponse errorResponse =
                new ErrorResponse(
                        "UNAUTHORIZED",
                        "Acceso no autorizado. Token JWT requerido.",
                        request.getRequestURI());

        // Escribir la respuesta JSON
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    /** Clase interna para representar la respuesta de error */
    private record ErrorResponse(String code, String message, String path) {}
}
