package dev.kreaker.kinvex.controller;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.kreaker.kinvex.dto.order.CreateOrderRequest;
import dev.kreaker.kinvex.dto.order.OrderReceiptResponse;
import dev.kreaker.kinvex.dto.order.ReceiveOrderRequest;
import dev.kreaker.kinvex.dto.order.UpdateOrderStatusRequest;
import dev.kreaker.kinvex.entity.PurchaseOrder;
import dev.kreaker.kinvex.entity.PurchaseOrder.OrderStatus;
import dev.kreaker.kinvex.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controlador REST para la gestión de órdenes de compra.
 *
 * Implementa los requerimientos: - 3.1: Crear órdenes de compra especificando
 * proveedor, productos, cantidades y fechas esperadas - 3.2: Registrar la
 * recepción parcial o total de productos de una orden de compra - 3.4:
 * Actualizar el estado de las órdenes de compra
 */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "API para gestión de órdenes de compra")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ========== CRUD Operations ==========
    /**
     * Crea una nueva orden de compra. Requerimiento 3.1: Crear órdenes de
     * compra especificando proveedor, productos, cantidades y fechas esperadas.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Crear nueva orden de compra",
            description = "Crea una nueva orden de compra con los productos especificados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Orden creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Proveedor o producto no encontrado"),
        @ApiResponse(responseCode = "409", description = "Número de orden duplicado")
    })
    public ResponseEntity<PurchaseOrder> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        logger.info("Creando nueva orden de compra: {}", request.getOrderNumber());

        PurchaseOrder createdOrder = orderService.createOrder(request);

        logger.info("Orden de compra creada exitosamente: {} (ID: {})",
                createdOrder.getOrderNumber(), createdOrder.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    /**
     * Obtiene todas las órdenes de compra con paginación.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('VIEWER', 'OPERATOR', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Obtener todas las órdenes",
            description = "Obtiene una lista paginada de todas las órdenes de compra")
    @ApiResponse(responseCode = "200", description = "Lista de órdenes obtenida exitosamente")
    public ResponseEntity<Page<PurchaseOrder>> getAllOrders(
            @PageableDefault(size = 20) Pageable pageable) {
        logger.debug("Obteniendo órdenes de compra - Página: {}, Tamaño: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<PurchaseOrder> orders = orderService.getAllOrders(pageable);

        return ResponseEntity.ok(orders);
    }

    /**
     * Obtiene una orden de compra por su ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VIEWER', 'OPERATOR', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Obtener orden por ID",
            description = "Obtiene los detalles de una orden de compra específica")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orden encontrada"),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    public ResponseEntity<PurchaseOrder> getOrderById(
            @Parameter(description = "ID de la orden de compra")
            @PathVariable Long id) {
        logger.debug("Obteniendo orden de compra por ID: {}", id);

        PurchaseOrder order = orderService.getOrderById(id);

        return ResponseEntity.ok(order);
    }

    /**
     * Obtiene una orden de compra por su número.
     */
    @GetMapping("/number/{orderNumber}")
    @PreAuthorize("hasAnyRole('VIEWER', 'OPERATOR', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Obtener orden por número",
            description = "Obtiene los detalles de una orden de compra por su número")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orden encontrada"),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    public ResponseEntity<PurchaseOrder> getOrderByNumber(
            @Parameter(description = "Número de la orden de compra")
            @PathVariable String orderNumber) {
        logger.debug("Obteniendo orden de compra por número: {}", orderNumber);

        PurchaseOrder order = orderService.getOrderByNumber(orderNumber);

        return ResponseEntity.ok(order);
    }

    // ========== Status Management ==========
    /**
     * Actualiza el estado de una orden de compra. Requerimiento 3.4: Actualizar
     * el estado de las órdenes de compra.
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('OPERATOR', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Actualizar estado de orden",
            description = "Actualiza el estado de una orden de compra")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Transición de estado inválida"),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada")
    })
    public ResponseEntity<PurchaseOrder> updateOrderStatus(
            @Parameter(description = "ID de la orden de compra")
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        logger.info("Actualizando estado de orden ID: {} a {}", id, request.getStatus());

        PurchaseOrder updatedOrder = orderService.updateOrderStatus(id, request);

        logger.info("Estado de orden actualizado exitosamente: {} -> {}",
                updatedOrder.getOrderNumber(), updatedOrder.getStatus());

        return ResponseEntity.ok(updatedOrder);
    }

    /**
     * Obtiene órdenes por estado.
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('VIEWER', 'OPERATOR', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Obtener órdenes por estado",
            description = "Obtiene una lista paginada de órdenes filtradas por estado")
    @ApiResponse(responseCode = "200", description = "Lista de órdenes obtenida exitosamente")
    public ResponseEntity<Page<PurchaseOrder>> getOrdersByStatus(
            @Parameter(description = "Estado de las órdenes")
            @PathVariable OrderStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        logger.debug("Obteniendo órdenes por estado: {}", status);

        Page<PurchaseOrder> orders = orderService.getOrdersByStatus(status, pageable);

        return ResponseEntity.ok(orders);
    }

    // ========== Order Reception ==========
    /**
     * Registra la recepción de productos de una orden de compra. Requerimiento
     * 3.2: Registrar la recepción parcial o total de productos de una orden de
     * compra.
     */
    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAnyRole('OPERATOR', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Recibir productos de orden",
            description = "Registra la recepción parcial o total de productos de una orden de compra")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recepción registrada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de recepción inválidos"),
        @ApiResponse(responseCode = "404", description = "Orden no encontrada"),
        @ApiResponse(responseCode = "409", description = "Operación de recepción inválida")
    })
    public ResponseEntity<OrderReceiptResponse> receiveOrder(
            @Parameter(description = "ID de la orden de compra")
            @PathVariable Long id,
            @Valid @RequestBody ReceiveOrderRequest request) {
        logger.info("Procesando recepción de orden ID: {}", id);

        OrderReceiptResponse response = orderService.receiveOrder(id, request);

        logger.info("Recepción procesada exitosamente para orden ID: {} - Estado: {}",
                id, response.getStatus());

        return ResponseEntity.ok(response);
    }

    // ========== Query Operations ==========
    /**
     * Obtiene órdenes por proveedor.
     */
    @GetMapping("/supplier/{supplierId}")
    @PreAuthorize("hasAnyRole('VIEWER', 'OPERATOR', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Obtener órdenes por proveedor",
            description = "Obtiene todas las órdenes de un proveedor específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de órdenes obtenida exitosamente"),
        @ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
    })
    public ResponseEntity<List<PurchaseOrder>> getOrdersBySupplier(
            @Parameter(description = "ID del proveedor")
            @PathVariable Long supplierId) {
        logger.debug("Obteniendo órdenes por proveedor ID: {}", supplierId);

        List<PurchaseOrder> orders = orderService.getOrdersBySupplier(supplierId);

        return ResponseEntity.ok(orders);
    }

    /**
     * Obtiene órdenes vencidas.
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Obtener órdenes vencidas",
            description = "Obtiene todas las órdenes que han excedido su fecha esperada de entrega")
    @ApiResponse(responseCode = "200", description = "Lista de órdenes vencidas obtenida exitosamente")
    public ResponseEntity<List<PurchaseOrder>> getOverdueOrders() {
        logger.debug("Obteniendo órdenes vencidas");

        List<PurchaseOrder> overdueOrders = orderService.getOverdueOrders();

        logger.info("Se encontraron {} órdenes vencidas", overdueOrders.size());

        return ResponseEntity.ok(overdueOrders);
    }

    /**
     * Obtiene órdenes que vencen pronto.
     */
    @GetMapping("/due-soon")
    @PreAuthorize("hasAnyRole('OPERATOR', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Obtener órdenes que vencen pronto",
            description = "Obtiene órdenes que vencen en los próximos días especificados")
    @ApiResponse(responseCode = "200", description = "Lista de órdenes obtenida exitosamente")
    public ResponseEntity<List<PurchaseOrder>> getOrdersDueSoon(
            @Parameter(description = "Días hacia adelante para considerar (por defecto 7)")
            @RequestParam(defaultValue = "7") int daysAhead) {
        logger.debug("Obteniendo órdenes que vencen en {} días", daysAhead);

        List<PurchaseOrder> ordersDueSoon = orderService.getOrdersDueSoon(daysAhead);

        logger.info("Se encontraron {} órdenes que vencen pronto", ordersDueSoon.size());

        return ResponseEntity.ok(ordersDueSoon);
    }

    /**
     * Obtiene órdenes por rango de fechas.
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('VIEWER', 'OPERATOR', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Obtener órdenes por rango de fechas",
            description = "Obtiene órdenes creadas en un rango de fechas específico")
    @ApiResponse(responseCode = "200", description = "Lista de órdenes obtenida exitosamente")
    public ResponseEntity<List<PurchaseOrder>> getOrdersByDateRange(
            @Parameter(description = "Fecha de inicio (formato: yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Fecha de fin (formato: yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        logger.debug("Obteniendo órdenes por rango de fechas: {} - {}", startDate, endDate);

        List<PurchaseOrder> orders = orderService.getOrdersByDateRange(startDate, endDate);

        logger.info("Se encontraron {} órdenes en el rango de fechas", orders.size());

        return ResponseEntity.ok(orders);
    }
}
