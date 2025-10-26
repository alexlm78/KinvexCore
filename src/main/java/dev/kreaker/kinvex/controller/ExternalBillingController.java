package dev.kreaker.kinvex.controller;

import dev.kreaker.kinvex.dto.inventory.ExternalStockDeductionRequest;
import dev.kreaker.kinvex.dto.inventory.ExternalStockDeductionResponse;
import dev.kreaker.kinvex.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST específico para sistemas externos de facturación.
 *
 * <p>Implementa el requerimiento 2: API para sistemas externos de facturación que permite descontar
 * inventario mediante endpoints REST con autenticación JWT.
 *
 * <p>Requerimientos implementados: - 2.1: Endpoint REST para descuento de inventario que reciba
 * código de producto y cantidad - 2.2: Reducir el stock del producto especificado cuando se reciba
 * una solicitud válida - 2.3: Retornar error HTTP 400 si el stock disponible es insuficiente - 2.4:
 * Registrar cada movimiento de salida con timestamp, producto, cantidad y sistema origen - 2.5:
 * Requerir autenticación mediante token JWT para todas las operaciones
 */
@RestController
@RequestMapping("/api/external/billing")
@Tag(name = "External Billing", description = "API para sistemas externos de facturación")
public class ExternalBillingController {

    private static final Logger logger = LoggerFactory.getLogger(ExternalBillingController.class);

    private final InventoryService inventoryService;

    public ExternalBillingController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Descuenta stock de un producto para sistemas externos de facturación.
     *
     * <p>Implementa todos los requerimientos 2.1 a 2.5: - Recibe código de producto y cantidad -
     * Reduce el stock si la solicitud es válida - Retorna HTTP 400 si hay stock insuficiente -
     * Registra el movimiento con timestamp y detalles - Requiere autenticación JWT
     *
     * @param request Solicitud de descuento de stock
     * @return Respuesta con detalles del descuento realizado
     */
    @PostMapping("/stock/deduct")
    @Operation(
            summary = "Descontar stock para sistema de facturación",
            description =
                    "Descuenta stock de un producto específico para sistemas externos de facturación. "
                            + "Requiere autenticación JWT y valida disponibilidad de stock antes de procesar.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Stock deducido exitosamente"),
                @ApiResponse(
                        responseCode = "400",
                        description = "Stock insuficiente o datos de entrada inválidos"),
                @ApiResponse(responseCode = "401", description = "Token JWT inválido o ausente"),
                @ApiResponse(
                        responseCode = "403",
                        description = "Acceso denegado - permisos insuficientes"),
                @ApiResponse(responseCode = "404", description = "Producto no encontrado")
            })
    @PreAuthorize("hasAnyRole('OPERATOR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ExternalStockDeductionResponse> deductStock(
            @Valid @RequestBody ExternalStockDeductionRequest request) {

        logger.info(
                "Recibida solicitud de descuento de stock externo: producto={}, cantidad={}, sistema={}",
                request.getProductCode(),
                request.getQuantity(),
                request.getSourceSystem());

        ExternalStockDeductionResponse response =
                inventoryService.deductStockForExternalSystem(request);

        logger.info(
                "Descuento de stock procesado exitosamente: producto={}, cantidad={}, stock_restante={}",
                response.getProductCode(),
                response.getQuantityDeducted(),
                response.getCurrentStock());

        return ResponseEntity.ok(response);
    }
}
