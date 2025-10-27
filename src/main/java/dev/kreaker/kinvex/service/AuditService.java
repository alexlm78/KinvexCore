package dev.kreaker.kinvex.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kreaker.kinvex.entity.AuditLog;
import dev.kreaker.kinvex.entity.User;
import dev.kreaker.kinvex.repository.AuditLogRepository;
import dev.kreaker.kinvex.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Servicio de auditoría que registra todas las operaciones del sistema.
 *
 * <p>Implementa el requerimiento 6.2 del sistema Kinvex para registrar todas las operaciones en
 * logs de auditoría con usuario, timestamp y acción.
 */
@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AuditService(
            AuditLogRepository auditLogRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Registra una operación de auditoría con información básica.
     *
     * @param action Acción realizada
     * @param entityType Tipo de entidad afectada
     * @param entityId ID de la entidad afectada
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logOperation(String action, String entityType, Long entityId) {
        logOperation(action, entityType, entityId, null, null);
    }

    /**
     * Registra una operación de auditoría con valores anteriores y nuevos.
     *
     * @param action Acción realizada
     * @param entityType Tipo de entidad afectada
     * @param entityId ID de la entidad afectada
     * @param oldValues Valores anteriores de la entidad
     * @param newValues Valores nuevos de la entidad
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logOperation(
            String action, String entityType, Long entityId, Object oldValues, Object newValues) {
        try {
            User currentUser = getCurrentUser();
            HttpServletRequest request = getCurrentRequest();

            AuditLog auditLog = new AuditLog();
            auditLog.setUser(currentUser);
            auditLog.setAction(action);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);

            if (oldValues != null) {
                auditLog.setOldValues(serializeToJson(oldValues));
            }

            if (newValues != null) {
                auditLog.setNewValues(serializeToJson(newValues));
            }

            if (request != null) {
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            }

            auditLogRepository.save(auditLog);

            logger.debug(
                    "Audit log created: action={}, entityType={}, entityId={}, user={}",
                    action,
                    entityType,
                    entityId,
                    currentUser != null ? currentUser.getUsername() : "system");

        } catch (Exception e) {
            logger.error(
                    "Error creating audit log: action={}, entityType={}, entityId={}",
                    action,
                    entityType,
                    entityId,
                    e);
            // No relanzamos la excepción para no afectar la operación principal
        }
    }

    /**
     * Registra una operación de autenticación (login/logout).
     *
     * @param action Acción de autenticación
     * @param username Nombre de usuario
     * @param success Si la operación fue exitosa
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAuthenticationOperation(String action, String username, boolean success) {
        try {
            HttpServletRequest request = getCurrentRequest();

            AuditLog auditLog = new AuditLog();
            auditLog.setAction(action + (success ? "_SUCCESS" : "_FAILURE"));
            auditLog.setEntityType(AuditLog.ENTITY_USER);

            // Para operaciones de autenticación, intentamos obtener el usuario por username
            if (username != null) {
                Optional<User> user = userRepository.findByUsername(username);
                if (user.isPresent()) {
                    auditLog.setUser(user.get());
                    auditLog.setEntityId(user.get().getId());
                } else {
                    auditLog.setNewValues("{\"username\":\"" + username + "\"}");
                }
            }

            if (request != null) {
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            }

            auditLogRepository.save(auditLog);

            logger.debug(
                    "Authentication audit log created: action={}, username={}, success={}",
                    action,
                    username,
                    success);

        } catch (Exception e) {
            logger.error(
                    "Error creating authentication audit log: action={}, username={}",
                    action,
                    username,
                    e);
        }
    }

    /**
     * Registra una operación de movimiento de inventario.
     *
     * @param action Acción realizada
     * @param productId ID del producto
     * @param quantity Cantidad del movimiento
     * @param movementType Tipo de movimiento
     * @param sourceSystem Sistema origen del movimiento
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logInventoryMovement(
            String action,
            Long productId,
            Integer quantity,
            String movementType,
            String sourceSystem) {
        try {
            User currentUser = getCurrentUser();
            HttpServletRequest request = getCurrentRequest();

            AuditLog auditLog = new AuditLog();
            auditLog.setUser(currentUser);
            auditLog.setAction(action);
            auditLog.setEntityType(AuditLog.ENTITY_INVENTORY_MOVEMENT);
            auditLog.setEntityId(productId);

            // Create movement data as JSON string directly
            String movementData =
                    String.format(
                            "{\"quantity\":%d,\"movementType\":\"%s\",\"sourceSystem\":\"%s\"}",
                            quantity, movementType, sourceSystem != null ? sourceSystem : "SYSTEM");
            auditLog.setNewValues(movementData);

            if (request != null) {
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            }

            auditLogRepository.save(auditLog);

            logger.debug(
                    "Inventory movement audit log created: action={}, productId={}, quantity={}",
                    action,
                    productId,
                    quantity);

        } catch (Exception e) {
            logger.error(
                    "Error creating inventory movement audit log: productId={}, quantity={}",
                    productId,
                    quantity,
                    e);
        }
    }

    /**
     * Obtiene el historial de auditoría para una entidad específica.
     *
     * @param entityType Tipo de entidad
     * @param entityId ID de la entidad
     * @return Lista de logs de auditoría
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getEntityHistory(String entityType, Long entityId) {
        return auditLogRepository.findEntityHistory(entityType, entityId);
    }

    /**
     * Obtiene logs de auditoría por rango de fechas.
     *
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @param pageable Información de paginación
     * @return Página de logs de auditoría
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByDateRange(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findByCreatedAtBetween(startDate, endDate, pageable);
    }

    /**
     * Obtiene estadísticas de actividad por acción.
     *
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Lista de estadísticas [acción, cantidad]
     */
    @Transactional(readOnly = true)
    public List<Object[]> getActionStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findActionStatisticsBetween(startDate, endDate);
    }

    /**
     * Obtiene estadísticas de actividad por tipo de entidad.
     *
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Lista de estadísticas [tipo_entidad, cantidad]
     */
    @Transactional(readOnly = true)
    public List<Object[]> getEntityTypeStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findEntityTypeStatisticsBetween(startDate, endDate);
    }

    /**
     * Obtiene estadísticas de actividad por usuario.
     *
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Lista de estadísticas [usuario, cantidad]
     */
    @Transactional(readOnly = true)
    public List<Object[]> getUserActivityStatistics(
            LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findUserActivityStatisticsBetween(startDate, endDate);
    }

    /**
     * Obtiene logs de actividad de autenticación.
     *
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Lista de logs de autenticación
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getLoginActivity(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findLoginActivityBetween(startDate, endDate);
    }

    /**
     * Obtiene el usuario actual del contexto de seguridad.
     *
     * @return Usuario actual o null si no está autenticado
     */
    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null
                    && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {

                String username = authentication.getName();
                return userRepository.findByUsername(username).orElse(null);
            }
        } catch (Exception e) {
            logger.debug("Could not get current user from security context", e);
        }
        return null;
    }

    /**
     * Obtiene la request HTTP actual.
     *
     * @return HttpServletRequest actual o null si no está disponible
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            logger.debug("Could not get current request", e);
            return null;
        }
    }

    /**
     * Obtiene la dirección IP del cliente.
     *
     * @param request Request HTTP
     * @return Dirección IP del cliente
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Serializa un objeto a JSON.
     *
     * @param object Objeto a serializar
     * @return JSON string o null si hay error
     */
    private String serializeToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.warn(
                    "Could not serialize object to JSON: {}", object.getClass().getSimpleName(), e);
            return object.toString();
        }
    }
}
