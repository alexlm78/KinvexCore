package dev.kreaker.kinvex.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para marcar métodos que deben ser auditados automáticamente.
 *
 * <p>Los métodos marcados con esta anotación serán interceptados por el AuditAspect y se registrará
 * automáticamente un log de auditoría.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /** Acción específica a registrar. Si está vacío, se deriva del nombre del método. */
    String action() default "";

    /** Tipo de entidad específico. Si está vacío, se deriva de la clase del servicio. */
    String entityType() default "";

    /** Nombre del campo que contiene el ID de la entidad en el objeto resultado. */
    String entityIdField() default "id";

    /** Descripción opcional de la operación. */
    String description() default "";
}
