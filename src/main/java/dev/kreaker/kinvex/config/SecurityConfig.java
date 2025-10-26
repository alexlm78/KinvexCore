package dev.kreaker.kinvex.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import dev.kreaker.kinvex.security.JwtAuthenticationEntryPoint;
import dev.kreaker.kinvex.security.JwtAuthenticationFilter;

/**
 * Configuración principal de seguridad para el sistema Kinvex. Define las
 * reglas de autenticación, autorización, CORS y JWT.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CorsProperties corsProperties;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            CorsProperties corsProperties
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.corsProperties = corsProperties;
    }

    /**
     * Configuración principal de la cadena de filtros de seguridad
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // Deshabilitar CSRF ya que usamos JWT (stateless)
                .csrf(AbstractHttpConfigurer::disable)
                // Configurar CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Configurar manejo de sesiones como stateless
                .sessionManagement(session
                        -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Configurar punto de entrada para errores de autenticación
                .exceptionHandling(exceptions
                        -> exceptions.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                // Configurar reglas de autorización
                .authorizeHttpRequests(auth -> auth
                // Endpoints públicos - no requieren autenticación
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-resources/**").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                // Endpoints de inventario - requieren autenticación
                .requestMatchers(HttpMethod.GET, "/api/inventory/**")
                .hasAnyRole("VIEWER", "OPERATOR", "MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/inventory/**")
                .hasAnyRole("OPERATOR", "MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/inventory/**")
                .hasAnyRole("OPERATOR", "MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/inventory/**")
                .hasAnyRole("MANAGER", "ADMIN")
                // Endpoints de órdenes - requieren autenticación
                .requestMatchers(HttpMethod.GET, "/api/orders/**")
                .hasAnyRole("VIEWER", "OPERATOR", "MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/orders/**")
                .hasAnyRole("OPERATOR", "MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/orders/**")
                .hasAnyRole("OPERATOR", "MANAGER", "ADMIN")
                // Endpoints de reportes - solo managers y admins
                .requestMatchers("/api/reports/**")
                .hasAnyRole("MANAGER", "ADMIN")
                // Endpoints de administración - solo admins
                .requestMatchers("/api/admin/**")
                .hasRole("ADMIN")
                // Endpoints de actuator protegidos
                .requestMatchers("/actuator/**")
                .hasRole("ADMIN")
                // Cualquier otra request requiere autenticación
                .anyRequest().authenticated()
                )
                // Agregar el filtro JWT antes del filtro de autenticación estándar
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Configuración de CORS basada en las propiedades de la aplicación
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Configurar orígenes permitidos
        configuration.setAllowedOriginPatterns(corsProperties.allowedOrigins());

        // Configurar métodos HTTP permitidos
        configuration.setAllowedMethods(corsProperties.allowedMethods());

        // Configurar headers permitidos
        configuration.setAllowedHeaders(corsProperties.allowedHeaders());

        // Permitir credenciales (cookies, headers de autorización)
        configuration.setAllowCredentials(corsProperties.allowCredentials());

        // Configurar tiempo de caché para preflight requests
        configuration.setMaxAge(corsProperties.maxAge());

        // Headers que el cliente puede acceder
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Total-Count",
                "X-Total-Pages"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Bean para el encoder de contraseñas usando BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Usar strength 12 para mayor seguridad
    }
}
