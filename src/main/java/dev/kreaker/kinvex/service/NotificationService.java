package dev.kreaker.kinvex.service;

import dev.kreaker.kinvex.entity.PurchaseOrder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Servicio de notificaciones para el sistema de alertas.
 *
 * <p>Maneja el envío de notificaciones para órdenes vencidas y que vencen pronto. En una
 * implementación completa, este servicio podría integrar con sistemas de email, SMS, o
 * notificaciones push.
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Envía una alerta para una orden vencida.
     *
     * @param order Orden vencida
     * @param daysOverdue Días que lleva vencida la orden
     */
    public void sendOverdueOrderAlert(PurchaseOrder order, long daysOverdue) {
        String message = buildOverdueOrderMessage(order, daysOverdue);

        // En una implementación real, aquí se enviaría el email/SMS/notificación
        logger.warn("ALERTA ORDEN VENCIDA: {}", message);

        // Simular envío de notificación
        simulateNotificationSending("OVERDUE_ORDER", order.getOrderNumber(), message);
    }

    /**
     * Envía una alerta para una orden que vence pronto.
     *
     * @param order Orden que vence pronto
     * @param daysUntilDue Días restantes hasta el vencimiento
     */
    public void sendOrderDueSoonAlert(PurchaseOrder order, long daysUntilDue) {
        String message = buildOrderDueSoonMessage(order, daysUntilDue);

        // En una implementación real, aquí se enviaría el email/SMS/notificación
        logger.info("ALERTA ORDEN VENCE PRONTO: {}", message);

        // Simular envío de notificación
        simulateNotificationSending("ORDER_DUE_SOON", order.getOrderNumber(), message);
    }

    /**
     * Envía un resumen de todas las órdenes vencidas.
     *
     * @param overdueOrders Lista de órdenes vencidas
     */
    public void sendOverdueOrdersSummary(List<PurchaseOrder> overdueOrders) {
        if (overdueOrders.isEmpty()) {
            return;
        }

        StringBuilder summary = new StringBuilder();
        summary.append("RESUMEN DIARIO - ÓRDENES VENCIDAS\n");
        summary.append("================================\n");
        summary.append("Fecha: ").append(LocalDate.now().format(DATE_FORMATTER)).append("\n");
        summary.append("Total de órdenes vencidas: ").append(overdueOrders.size()).append("\n\n");

        for (PurchaseOrder order : overdueOrders) {
            long daysOverdue = LocalDate.now().toEpochDay() - order.getExpectedDate().toEpochDay();
            summary.append("• Orden: ")
                    .append(order.getOrderNumber())
                    .append(" | Proveedor: ")
                    .append(order.getSupplier().getName())
                    .append(" | Vencida hace: ")
                    .append(daysOverdue)
                    .append(" días")
                    .append(" | Estado: ")
                    .append(order.getStatus())
                    .append("\n");
        }

        logger.warn("RESUMEN ÓRDENES VENCIDAS:\n{}", summary.toString());

        // Simular envío de resumen
        simulateNotificationSending("OVERDUE_SUMMARY", "DAILY_REPORT", summary.toString());
    }

    /**
     * Envía un resumen de órdenes que vencen pronto.
     *
     * @param ordersDueSoon Lista de órdenes que vencen pronto
     */
    public void sendOrdersDueSoonSummary(List<PurchaseOrder> ordersDueSoon) {
        if (ordersDueSoon.isEmpty()) {
            return;
        }

        StringBuilder summary = new StringBuilder();
        summary.append("RESUMEN DIARIO - ÓRDENES QUE VENCEN PRONTO\n");
        summary.append("=========================================\n");
        summary.append("Fecha: ").append(LocalDate.now().format(DATE_FORMATTER)).append("\n");
        summary.append("Total de órdenes que vencen pronto: ")
                .append(ordersDueSoon.size())
                .append("\n\n");

        for (PurchaseOrder order : ordersDueSoon) {
            long daysUntilDue = order.getExpectedDate().toEpochDay() - LocalDate.now().toEpochDay();
            summary.append("• Orden: ")
                    .append(order.getOrderNumber())
                    .append(" | Proveedor: ")
                    .append(order.getSupplier().getName())
                    .append(" | Vence en: ")
                    .append(daysUntilDue)
                    .append(" días")
                    .append(" | Fecha esperada: ")
                    .append(order.getExpectedDate().format(DATE_FORMATTER))
                    .append(" | Estado: ")
                    .append(order.getStatus())
                    .append("\n");
        }

        logger.info("RESUMEN ÓRDENES QUE VENCEN PRONTO:\n{}", summary.toString());

        // Simular envío de resumen
        simulateNotificationSending("DUE_SOON_SUMMARY", "DAILY_REPORT", summary.toString());
    }

    /** Construye el mensaje para una orden vencida. */
    private String buildOverdueOrderMessage(PurchaseOrder order, long daysOverdue) {
        return String.format(
                "La orden de compra %s del proveedor %s está vencida hace %d días. "
                        + "Fecha esperada: %s, Estado actual: %s. "
                        + "Se requiere seguimiento inmediato.",
                order.getOrderNumber(),
                order.getSupplier().getName(),
                daysOverdue,
                order.getExpectedDate().format(DATE_FORMATTER),
                order.getStatus());
    }

    /** Construye el mensaje para una orden que vence pronto. */
    private String buildOrderDueSoonMessage(PurchaseOrder order, long daysUntilDue) {
        return String.format(
                "La orden de compra %s del proveedor %s vence en %d días. "
                        + "Fecha esperada: %s, Estado actual: %s. "
                        + "Se recomienda hacer seguimiento.",
                order.getOrderNumber(),
                order.getSupplier().getName(),
                daysUntilDue,
                order.getExpectedDate().format(DATE_FORMATTER),
                order.getStatus());
    }

    /**
     * Simula el envío de una notificación. En una implementación real, aquí se integraría con
     * servicios de email, SMS, push notifications, etc.
     */
    private void simulateNotificationSending(String type, String reference, String message) {
        // Simular latencia de envío
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        logger.debug(
                "Notificación enviada - Tipo: {}, Referencia: {}, Mensaje: {}",
                type,
                reference,
                message.substring(0, Math.min(50, message.length())) + "...");
    }
}
