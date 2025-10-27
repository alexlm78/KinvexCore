package dev.kreaker.kinvex.audit;

import dev.kreaker.kinvex.entity.AuditLog;
import dev.kreaker.kinvex.service.AuditService;
import java.lang.reflect.Method;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Aspecto AOP para interceptar automáticamente operaciones y registrar auditoría.
 *
 * <p>Implementa el requerimiento 6.2 del sistema Kinvex para capturar cambios automáticamente
 * mediante interceptores.
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditService auditService;

    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    /** Pointcut para métodos de servicio que deben ser auditados. */
    @Pointcut("@annotation(dev.kreaker.kinvex.audit.Auditable)")
    public void auditableMethod() {}

    /** Pointcut para métodos de creación en servicios. */
    @Pointcut("execution(* dev.kreaker.kinvex.service.*.create*(..))")
    public void createMethods() {}

    /** Pointcut para métodos de actualización en servicios. */
    @Pointcut("execution(* dev.kreaker.kinvex.service.*.update*(..))")
    public void updateMethods() {}

    /** Pointcut para métodos de eliminación en servicios. */
    @Pointcut("execution(* dev.kreaker.kinvex.service.*.delete*(..))")
    public void deleteMethods() {}

    /** Pointcut para métodos de stock en InventoryService. */
    @Pointcut("execution(* dev.kreaker.kinvex.service.InventoryService.*Stock(..))")
    public void stockMethods() {}

    /** Pointcut para métodos de recepción de órdenes. */
    @Pointcut("execution(* dev.kreaker.kinvex.service.OrderService.receiveOrder(..))")
    public void receiveOrderMethods() {}

    /** Intercepta métodos marcados con @Auditable. */
    @AfterReturning(pointcut = "auditableMethod()", returning = "result")
    public void auditAnnotatedMethod(JoinPoint joinPoint, Object result) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Auditable auditable = method.getAnnotation(Auditable.class);

            if (auditable != null) {
                String action =
                        auditable.action().isEmpty()
                                ? deriveActionFromMethodName(method.getName())
                                : auditable.action();
                String entityType =
                        auditable.entityType().isEmpty()
                                ? deriveEntityTypeFromClass(joinPoint.getTarget().getClass())
                                : auditable.entityType();

                Long entityId = extractEntityId(result, auditable.entityIdField());

                auditService.logOperation(action, entityType, entityId, null, result);
            }
        } catch (Exception e) {
            logger.error("Error in audit aspect for annotated method", e);
        }
    }

    /** Intercepta métodos de creación automáticamente. */
    @AfterReturning(pointcut = "createMethods()", returning = "result")
    public void auditCreateMethod(JoinPoint joinPoint, Object result) {
        try {
            String entityType = deriveEntityTypeFromClass(joinPoint.getTarget().getClass());
            Long entityId = extractEntityId(result, "id");

            auditService.logOperation(AuditLog.ACTION_CREATE, entityType, entityId, null, result);

        } catch (Exception e) {
            logger.error("Error in audit aspect for create method", e);
        }
    }

    /** Intercepta métodos de actualización automáticamente. */
    @AfterReturning(pointcut = "updateMethods()", returning = "result")
    public void auditUpdateMethod(JoinPoint joinPoint, Object result) {
        try {
            String entityType = deriveEntityTypeFromClass(joinPoint.getTarget().getClass());
            Long entityId = extractEntityId(result, "id");

            // Para updates, idealmente necesitaríamos el valor anterior
            // Por simplicidad, solo registramos el nuevo valor
            auditService.logOperation(AuditLog.ACTION_UPDATE, entityType, entityId, null, result);

        } catch (Exception e) {
            logger.error("Error in audit aspect for update method", e);
        }
    }

    /** Intercepta métodos de eliminación automáticamente. */
    @AfterReturning(pointcut = "deleteMethods()")
    public void auditDeleteMethod(JoinPoint joinPoint) {
        try {
            String entityType = deriveEntityTypeFromClass(joinPoint.getTarget().getClass());
            Object[] args = joinPoint.getArgs();

            // Asumimos que el primer argumento es el ID de la entidad
            Long entityId = null;
            if (args.length > 0 && args[0] instanceof Long) {
                entityId = (Long) args[0];
            }

            auditService.logOperation(AuditLog.ACTION_DELETE, entityType, entityId);

        } catch (Exception e) {
            logger.error("Error in audit aspect for delete method", e);
        }
    }

    /** Intercepta métodos de stock específicamente. */
    @AfterReturning(pointcut = "stockMethods()", returning = "result")
    public void auditStockMethod(JoinPoint joinPoint, Object result) {
        try {
            String methodName = joinPoint.getSignature().getName();
            String action =
                    methodName.contains("increase") || methodName.contains("Increase")
                            ? AuditLog.ACTION_STOCK_INCREASE
                            : AuditLog.ACTION_STOCK_DECREASE;

            Object[] args = joinPoint.getArgs();
            Long productId = null;
            Integer quantity = null;

            // Extraer productId y quantity de los argumentos
            for (Object arg : args) {
                if (arg instanceof Long && productId == null) {
                    productId = (Long) arg;
                } else if (arg != null && arg.getClass().getSimpleName().contains("Request")) {
                    // Extraer información del request object
                    try {
                        Method getQuantityMethod = arg.getClass().getMethod("getQuantity");
                        quantity = (Integer) getQuantityMethod.invoke(arg);
                    } catch (Exception e) {
                        logger.debug("Could not extract quantity from request object", e);
                    }
                }
            }

            if (productId != null && quantity != null) {
                auditService.logInventoryMovement(
                        action,
                        productId,
                        quantity,
                        action.equals(AuditLog.ACTION_STOCK_INCREASE) ? "IN" : "OUT",
                        "SYSTEM");
            }

        } catch (Exception e) {
            logger.error("Error in audit aspect for stock method", e);
        }
    }

    /** Intercepta métodos de recepción de órdenes. */
    @AfterReturning(pointcut = "receiveOrderMethods()", returning = "result")
    public void auditReceiveOrderMethod(JoinPoint joinPoint, Object result) {
        try {
            Object[] args = joinPoint.getArgs();
            Long orderId = null;

            if (args.length > 0 && args[0] instanceof Long) {
                orderId = (Long) args[0];
            }

            auditService.logOperation(
                    AuditLog.ACTION_ORDER_RECEIVE,
                    AuditLog.ENTITY_PURCHASE_ORDER,
                    orderId,
                    null,
                    result);

        } catch (Exception e) {
            logger.error("Error in audit aspect for receive order method", e);
        }
    }

    /** Deriva el tipo de acción del nombre del método. */
    private String deriveActionFromMethodName(String methodName) {
        if (methodName.startsWith("create")) {
            return AuditLog.ACTION_CREATE;
        } else if (methodName.startsWith("update")) {
            return AuditLog.ACTION_UPDATE;
        } else if (methodName.startsWith("delete")) {
            return AuditLog.ACTION_DELETE;
        } else {
            return methodName.toUpperCase();
        }
    }

    /** Deriva el tipo de entidad de la clase del servicio. */
    private String deriveEntityTypeFromClass(Class<?> serviceClass) {
        String className = serviceClass.getSimpleName();
        if (className.contains("Inventory")) {
            return AuditLog.ENTITY_PRODUCT;
        } else if (className.contains("Order")) {
            return AuditLog.ENTITY_PURCHASE_ORDER;
        } else if (className.contains("Auth")) {
            return AuditLog.ENTITY_USER;
        } else {
            return className.replace("Service", "");
        }
    }

    /** Extrae el ID de la entidad del objeto resultado. */
    private Long extractEntityId(Object result, String fieldName) {
        if (result == null) {
            return null;
        }

        try {
            Method getIdMethod =
                    result.getClass()
                            .getMethod(
                                    "get"
                                            + fieldName.substring(0, 1).toUpperCase()
                                            + fieldName.substring(1));
            Object id = getIdMethod.invoke(result);
            return id instanceof Long ? (Long) id : null;
        } catch (Exception e) {
            logger.debug("Could not extract entity ID from result object", e);
            return null;
        }
    }
}
