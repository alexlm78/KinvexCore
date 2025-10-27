package dev.kreaker.kinvex.controller;

import dev.kreaker.kinvex.dto.alert.AlertSummaryResponse;
import dev.kreaker.kinvex.dto.alert.OrderAlertResponse;
import dev.kreaker.kinvex.entity.PurchaseOrder;
import dev.kreaker.kinvex.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para el sistema de alertas de órdenes.
 *
 * <p>Expone endpoints para consultar y gestionar alertas de órdenes vencidas y que vencen pronto.
 */
@RestController
@RequestMapping("/api/alerts")
@Tag(name = "Alertas", description = "API para gestión de alertas de órdenes")
public class AlertController {

    private static final Logger logger = LoggerFactory.getLogger(AlertController.class);

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    /** Obtiene un resumen de todas las alertas activas. */
    @GetMapping("/summary")
    @Operation(
            summary = "Obtener resumen de alertas",
            description =
                    "Obtiene un resumen de todas las alertas activas (órdenes vencidas y que vencen pronto)")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Resumen obtenido exitosamente"),
                @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<AlertSummaryResponse> getAlertsSummary(
            @Parameter(
                            description = "Días hacia adelante para órdenes que vencen pronto",
                            example = "3")
                    @RequestParam(defaultValue = "3")
                    int daysAhead) {

        logger.info("Obteniendo resumen de alertas (días adelante: {})", daysAhead);

        try {
            // Obtener órdenes vencidas
            List<PurchaseOrder> overdueOrders = alertService.getOverdueOrders();
            List<OrderAlertResponse> overdueAlerts =
                    overdueOrders.stream()
                            .map(
                                    order -> {
                                        long daysOverdue =
                                                LocalDate.now().toEpochDay()
                                                        - order.getExpectedDate().toEpochDay();
                                        return OrderAlertResponse.forOverdueOrder(
                                                order, daysOverdue);
                                    })
                            .collect(Collectors.toList());

            // Obtener órdenes que vencen pronto
            List<PurchaseOrder> ordersDueSoon = alertService.getOrdersDueSoon(daysAhead);
            List<OrderAlertResponse> dueSoonAlerts =
                    ordersDueSoon.stream()
                            .map(
                                    order -> {
                                        long daysUntilDue =
                                                order.getExpectedDate().toEpochDay()
                                                        - LocalDate.now().toEpochDay();
                                        return OrderAlertResponse.forOrderDueSoon(
                                                order, daysUntilDue);
                                    })
                            .collect(Collectors.toList());

            AlertSummaryResponse response =
                    AlertSummaryResponse.create(overdueAlerts, dueSoonAlerts);

            logger.info(
                    "Resumen de alertas obtenido: {} vencidas, {} que vencen pronto",
                    overdueAlerts.size(),
                    dueSoonAlerts.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error obteniendo resumen de alertas", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /** Obtiene todas las órdenes vencidas. */
    @GetMapping("/overdue")
    @Operation(
            summary = "Obtener órdenes vencidas",
            description = "Obtiene todas las órdenes que han excedido su fecha esperada de entrega")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Órdenes vencidas obtenidas exitosamente"),
                @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PreAuthorize("hasAnyRole('OPERATOR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<OrderAlertResponse>> getOverdueOrders() {

        logger.info("Obteniendo órdenes vencidas");

        try {
            List<PurchaseOrder> overdueOrders = alertService.getOverdueOrders();

            List<OrderAlertResponse> response =
                    overdueOrders.stream()
                            .map(
                                    order -> {
                                        long daysOverdue =
                                                LocalDate.now().toEpochDay()
                                                        - order.getExpectedDate().toEpochDay();
                                        return OrderAlertResponse.forOverdueOrder(
                                                order, daysOverdue);
                                    })
                            .collect(Collectors.toList());

            logger.info("Obtenidas {} órdenes vencidas", response.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error obteniendo órdenes vencidas", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /** Obtiene órdenes que vencen pronto. */
    @GetMapping("/due-soon")
    @Operation(
            summary = "Obtener órdenes que vencen pronto",
            description = "Obtiene órdenes que vencen dentro del número de días especificado")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Órdenes que vencen pronto obtenidas exitosamente"),
                @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
                @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PreAuthorize("hasAnyRole('OPERATOR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<OrderAlertResponse>> getOrdersDueSoon(
            @Parameter(description = "Días hacia adelante para considerar", example = "7")
                    @RequestParam(defaultValue = "7")
                    int daysAhead) {

        logger.info("Obteniendo órdenes que vencen en {} días", daysAhead);

        if (daysAhead < 1 || daysAhead > 365) {
            logger.warn("Parámetro daysAhead inválido: {}", daysAhead);
            return ResponseEntity.badRequest().build();
        }

        try {
            List<PurchaseOrder> ordersDueSoon = alertService.getOrdersDueSoon(daysAhead);

            List<OrderAlertResponse> response =
                    ordersDueSoon.stream()
                            .map(
                                    order -> {
                                        long daysUntilDue =
                                                order.getExpectedDate().toEpochDay()
                                                        - LocalDate.now().toEpochDay();
                                        return OrderAlertResponse.forOrderDueSoon(
                                                order, daysUntilDue);
                                    })
                            .collect(Collectors.toList());

            logger.info("Obtenidas {} órdenes que vencen pronto", response.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error obteniendo órdenes que vencen pronto", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /** Ejecuta manualmente la verificación de órdenes vencidas. */
    @PostMapping("/check-overdue")
    @Operation(
            summary = "Verificar órdenes vencidas manualmente",
            description =
                    "Ejecuta manualmente la verificación y procesamiento de alertas para órdenes vencidas")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Verificación ejecutada exitosamente"),
                @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<String> checkOverdueOrdersManually() {

        logger.info("Iniciando verificación manual de órdenes vencidas");

        try {
            int processedOrders = alertService.checkOverdueOrdersManually();

            String message =
                    String.format(
                            "Verificación completada. %d órdenes vencidas procesadas.",
                            processedOrders);

            logger.info("Verificación manual completada: {} órdenes procesadas", processedOrders);
            return ResponseEntity.ok(message);

        } catch (Exception e) {
            logger.error("Error en verificación manual de órdenes vencidas", e);
            return ResponseEntity.internalServerError()
                    .body("Error ejecutando verificación: " + e.getMessage());
        }
    }

    /** Ejecuta manualmente la verificación de órdenes que vencen pronto. */
    @PostMapping("/check-due-soon")
    @Operation(
            summary = "Verificar órdenes que vencen pronto manualmente",
            description =
                    "Ejecuta manualmente la verificación y procesamiento de alertas para órdenes que vencen pronto")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Verificación ejecutada exitosamente"),
                @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
                @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<String> checkOrdersDueSoonManually(
            @Parameter(description = "Días hacia adelante para considerar", example = "3")
                    @RequestParam(defaultValue = "3")
                    int daysAhead) {

        logger.info(
                "Iniciando verificación manual de órdenes que vencen pronto (días: {})", daysAhead);

        if (daysAhead < 1 || daysAhead > 365) {
            logger.warn("Parámetro daysAhead inválido: {}", daysAhead);
            return ResponseEntity.badRequest()
                    .body("El parámetro daysAhead debe estar entre 1 y 365");
        }

        try {
            int processedOrders = alertService.checkOrdersDueSoonManually(daysAhead);

            String message =
                    String.format(
                            "Verificación completada. %d órdenes que vencen pronto procesadas.",
                            processedOrders);

            logger.info("Verificación manual completada: {} órdenes procesadas", processedOrders);
            return ResponseEntity.ok(message);

        } catch (Exception e) {
            logger.error("Error en verificación manual de órdenes que vencen pronto", e);
            return ResponseEntity.internalServerError()
                    .body("Error ejecutando verificación: " + e.getMessage());
        }
    }
}
