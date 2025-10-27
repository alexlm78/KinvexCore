package dev.kreaker.kinvex.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import dev.kreaker.kinvex.entity.User;
import dev.kreaker.kinvex.entity.User.UserRole;
import dev.kreaker.kinvex.repository.UserRepository;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Cargando datos iniciales...");

        // Crear usuarios de prueba si no existen
        createUserIfNotExists("admin", "admin@kinvex.com", "admin123", UserRole.ADMIN);
        createUserIfNotExists("manager", "manager@kinvex.com", "manager123", UserRole.MANAGER);
        createUserIfNotExists("operator", "operator@kinvex.com", "operator123", UserRole.OPERATOR);

        logger.info("Datos iniciales cargados correctamente");
    }

    private void createUserIfNotExists(String username, String email, String password, UserRole role) {
        if (!userRepository.findByUsername(username).isPresent()) {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setRole(role);
            user.setActive(true);

            userRepository.save(user);
            logger.info("Usuario creado: {} con rol {}", username, role);
        } else {
            logger.info("Usuario ya existe: {}", username);
        }
    }
}
