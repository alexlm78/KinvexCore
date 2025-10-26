package dev.kreaker.kinvex.config;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/** Tests de integración para la configuración de seguridad */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired private WebApplicationContext context;

    @Test
    void shouldAllowAccessToPublicEndpoints() throws Exception {
        MockMvc mockMvc =
                MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

        // Verificar que endpoints públicos son accesibles sin autenticación
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    void shouldRequireAuthenticationForProtectedEndpoints() throws Exception {
        MockMvc mockMvc =
                MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

        // Verificar que endpoints protegidos requieren autenticación
        mockMvc.perform(get("/api/inventory/products")).andExpect(status().isUnauthorized());
    }
}
