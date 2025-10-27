package dev.kreaker.kinvex.service;

import dev.kreaker.kinvex.entity.PurchaseOrder;
import dev.kreaker.kinvex.repository.PurchaseOrderRepository;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de alertas para órdenes de compra.
 *
 * <p>Implementa el requerimiento 3.5: Generar alertas cuando una orden de compra exceda la fecha
 * esperada de entrega.
 */
@Service
@Transactional(readOnly = true)
public class AlertService {

    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final NotificationService notificationService;

    public AlertService(
            PurchaseOrderRepository purchaseOrderRepository,
            NotificationService notificationService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.notificationService = notificationService;
    }

    /**
     * Obtiene todas las órdenes vencidas.
     *
     * @return Lista de órdenes vencidas
     */
    public List<PurchaseOrder> getOverdueOrders() {
        logger.debug("Obteniendo órdenes vencidas");
        List<PurchaseOrder> overdueOrders = purchaseOrderRepository.findOverdueOrders();
        logger.info("Encontradas {} órdenes vencidas", overdueOrders.size());
        return overdueOrders;
    }

    /**
     * Obtiene órdenes que vencen pronto.
     *
     * @param daysAhead Días hacia adelante para considerar
     * @return Lista de órdenes que vencen pronto
     */
    public List<PurchaseOrder> getOrdersDueSoon(int daysAhead) {
        logger.debug("Obteniendo órdenes que vencen en {} días", daysAhead);
        LocalDate futureDate = LocalDate.now().plusDays(daysAhead);
        List<PurchaseOrder> ordersDueSoon = purchaseOrderRepository.findOrdersDueSoon(futureDate);
        logger.info("Encontradas {} órdenes que vencen pronto", ordersDueSoon.size());
        return ordersDueSoon;
    }

    /** Procesa alertas automáticas para órdenes vencidas. Se ejecuta diariamente a las 9:00 AM. */
    @Scheduled(cron = "0 0 9 * * ?")
    public void processOverdueOrderAlerts() {
        logger.info("Iniciando procesamiento de alertas de órdenes vencidas");

        try {
            List<PurchaseOrder> overdueOrders = getOverdueOrders();

            if (!overdueOrders.isEmpty()) {
                logger.info("Procesando {} órdenes vencidas", overdueOrders.size());

                for (PurchaseOrder order : overdueOrders) {
                    processOverdueOrderAlert(order);
                }

                // Enviar resumen de órdenes vencidas
                notificationService.sendOverdueOrdersSummary(overdueOrders);
            } else {
                logger.info("No hay órdenes vencidas para procesar");
            }
        } catch (Exception e) {
            logger.error("Error procesando alertas de órdenes vencidas", e);
        }
    }

    /** Procesa alertas para órdenes que vencen pronto. Se ejecuta diariamente a las 8:00 AM. */
    @Scheduled(cron = "0 0 8 * * ?")
    public void processOrdersDueSoonAlerts() {
        logger.info("Iniciando procesamiento de alertas de órdenes que vencen pronto");

        try {
            // Alertar sobre órdenes que vencen en 3 días
            List<PurchaseOrder> ordersDueSoon = getOrdersDueSoon(3);

            if (!ordersDueSoon.isEmpty()) {
                logger.info("Procesando {} órdenes que vencen pronto", ordersDueSoon.size());

                for (PurchaseOrder order : ordersDueSoon) {
                    processOrderDueSoonAlert(order);
                }

                // Enviar resumen de órdenes que vencen pronto
                notificationService.sendOrdersDueSoonSummary(ordersDueSoon);
            } else {
                logger.info("No hay órdenes que venzan pronto para procesar");
            }
        } catch (Exception e) {
            logger.error("Error procesando alertas de órdenes que vencen pronto", e);
        }
    }

    /**
     * Procesa una alerta individual para una orden vencida.
     *
     * @param order Orden vencida
     */
    private void processOverdueOrderAlert(PurchaseOrder order) {
        try {
            logger.debug("Procesando alerta para orden vencida: {}", order.getOrderNumber());

            long daysOverdue = LocalDate.now().toEpochDay() - order.getExpectedDate().toEpochDay();

            notificationService.sendOverdueOrderAlert(order, daysOverdue);

            logger.debug(
                    "Alerta procesada para orden vencida: {} ({} días vencida)",
                    order.getOrderNumber(),
                    daysOverdue);
        } catch (Exception e) {
            logger.error(
                    "Error procesando alerta para orden vencida: {}", order.getOrderNumber(), e);
        }
    }

    /**
     * Procesa una alerta individual para una orden que vence pronto.
     *
     * @param order Orden que vence pronto
     */
    private void processOrderDueSoonAlert(PurchaseOrder order) {
        try {
            logger.debug(
                    "Procesando alerta para orden que vence pronto: {}", order.getOrderNumber());

            long daysUntilDue = order.getExpectedDate().toEpochDay() - LocalDate.now().toEpochDay();

            notificationService.sendOrderDueSoonAlert(order, daysUntilDue);

            logger.debug(
                    "Alerta procesada para orden que vence pronto: {} ({} días restantes)",
                    order.getOrderNumber(),
                    daysUntilDue);
        } catch (Exception e) {
            logger.error(
                    "Error procesando alerta para orden que vence pronto: {}",
                    order.getOrderNumber(),
                    e);
        }
    }

    /**
     * Verifica manualmente las alertas de órdenes vencidas. Útil para testing o ejecución manual.
     *
     * @return Número de órdenes vencidas procesadas
     */
    public int checkOverdueOrdersManually() {
        logger.info("Verificación manual de órdenes vencidas iniciada");

        List<PurchaseOrder> overdueOrders = getOverdueOrders();

        if (!overdueOrders.isEmpty()) {
            for (PurchaseOrder order : overdueOrders) {
                processOverdueOrderAlert(order);
            }
            notificationService.sendOverdueOrdersSummary(overdueOrders);
        }

        logger.info(
                "Verificación manual completada: {} órdenes vencidas procesadas",
                overdueOrders.size());
        return overdueOrders.size();
    }

    /**
     * Verifica manualmente las alertas de órdenes que vencen pronto.
     *
     * @param daysAhead Días hacia adelante para considerar
     * @return Número de órdenes que vencen pronto procesadas
     */
    public int checkOrdersDueSoonManually(int daysAhead) {
        logger.info(
                "Verificación manual de órdenes que vencen pronto iniciada (días: {})", daysAhead);

        List<PurchaseOrder> ordersDueSoon = getOrdersDueSoon(daysAhead);

        if (!ordersDueSoon.isEmpty()) {
            for (PurchaseOrder order : ordersDueSoon) {
                processOrderDueSoonAlert(order);
            }
            notificationService.sendOrdersDueSoonSummary(ordersDueSoon);
        }

        logger.info(
                "Verificación manual completada: {} órdenes que vencen pronto procesadas",
                ordersDueSoon.size());
        return ordersDueSoon.size();
    }
}
