package dev.kreaker.kinvex.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.kreaker.kinvex.dto.order.CreateOrderRequest;
import dev.kreaker.kinvex.dto.order.OrderDetailReceiptRequest;
import dev.kreaker.kinvex.dto.order.OrderDetailReceiptResponse;
import dev.kreaker.kinvex.dto.order.OrderReceiptResponse;
import dev.kreaker.kinvex.dto.order.ReceiveOrderRequest;
import dev.kreaker.kinvex.dto.order.UpdateOrderStatusRequest;
import dev.kreaker.kinvex.entity.InventoryMovement;
import dev.kreaker.kinvex.entity.OrderDetail;
import dev.kreaker.kinvex.entity.Product;
import dev.kreaker.kinvex.entity.PurchaseOrder;
import dev.kreaker.kinvex.entity.PurchaseOrder.OrderStatus;
import dev.kreaker.kinvex.entity.Supplier;
import dev.kreaker.kinvex.entity.User;
import dev.kreaker.kinvex.exception.DuplicateOrderNumberException;
import dev.kreaker.kinvex.exception.InvalidOrderOperationException;
import dev.kreaker.kinvex.exception.OrderNotFoundException;
import dev.kreaker.kinvex.exception.ProductNotFoundException;
import dev.kreaker.kinvex.exception.SupplierNotFoundException;
import dev.kreaker.kinvex.repository.InventoryMovementRepository;
import dev.kreaker.kinvex.repository.OrderDetailRepository;
import dev.kreaker.kinvex.repository.ProductRepository;
import dev.kreaker.kinvex.repository.PurchaseOrderRepository;
import dev.kreaker.kinvex.repository.SupplierRepository;
import dev.kreaker.kinvex.repository.UserRepository;

/**
 * Servicio de órdenes de compra que maneja la creación, gestión y recepción de
 * órdenes.
 *
 * Implementa los requerimientos: - 3.1: Crear órdenes de compra especificando
 * proveedor, productos, cantidades y fechas esperadas - 3.2: Registrar la
 * recepción parcial o total de productos de una orden de compra - 3.3:
 * Incrementar el stock de los productos recibidos cuando se registre una
 * recepción - 3.4: Actualizar el estado de las órdenes de compra (pendiente,
 * parcial, completada, cancelada)
 */
@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final UserRepository userRepository;

    public OrderService(
            PurchaseOrderRepository purchaseOrderRepository,
            OrderDetailRepository orderDetailRepository,
            SupplierRepository supplierRepository,
            ProductRepository productRepository,
            InventoryMovementRepository inventoryMovementRepository,
            UserRepository userRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.userRepository = userRepository;
    }

    // ========== CRUD Operations ==========
    /**
     * Crea una nueva orden de compra. Requerimiento 3.1: Crear órdenes de
     * compra especificando proveedor, productos, cantidades y fechas esperadas.
     *
     * @param request Datos de la orden a crear
     * @return Orden de compra creada
     * @throws SupplierNotFoundException si el proveedor no existe
     * @throws ProductNotFoundException si algún producto no existe
     * @throws DuplicateOrderNumberException si el número de orden ya existe
     */
    public PurchaseOrder createOrder(CreateOrderRequest request) {
        logger.info("Creando orden de compra con número: {}", request.getOrderNumber());

        // Validar que el número de orden no exista
        if (purchaseOrderRepository.existsByOrderNumber(request.getOrderNumber())) {
            throw new DuplicateOrderNumberException(request.getOrderNumber());
        }

        // Buscar proveedor
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new SupplierNotFoundException(request.getSupplierId()));

        // Validar que el proveedor esté activo
        if (!supplier.getActive()) {
            throw new SupplierNotFoundException("Proveedor inactivo con ID: " + request.getSupplierId());
        }

        // Obtener usuario actual
        User currentUser = getCurrentUser();

        // Crear orden de compra
        PurchaseOrder order = new PurchaseOrder(
                request.getOrderNumber(),
                supplier,
                request.getOrderDate(),
                request.getExpectedDate(),
                currentUser
        );
        order.setNotes(request.getNotes());

        // Guardar orden para obtener ID
        order = purchaseOrderRepository.save(order);

        // Crear detalles de orden
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (var detailRequest : request.getOrderDetails()) {
            Product product = productRepository.findById(detailRequest.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(detailRequest.getProductId()));

            // Validar que el producto esté activo
            if (!product.getActive()) {
                throw new ProductNotFoundException("Producto inactivo con ID: " + detailRequest.getProductId());
            }

            OrderDetail detail = new OrderDetail(
                    order,
                    product,
                    detailRequest.getQuantityOrdered(),
                    detailRequest.getUnitPrice()
            );
            orderDetails.add(detail);
        }

        // Guardar detalles
        orderDetails = orderDetailRepository.saveAll(orderDetails);
        order.setOrderDetails(orderDetails);

        // Calcular total
        order.calculateTotalAmount();
        order = purchaseOrderRepository.save(order);

        logger.info("Orden de compra creada exitosamente: {} (ID: {})",
                order.getOrderNumber(), order.getId());
        return order;
    }

    /**
     * Obtiene una orden de compra por su ID.
     *
     * @param orderId ID de la orden
     * @return Orden de compra encontrada
     * @throws OrderNotFoundException si la orden no existe
     */
    @Transactional(readOnly = true)
    public PurchaseOrder getOrderById(Long orderId) {
        return purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    /**
     * Obtiene una orden de compra por su número.
     *
     * @param orderNumber Número de la orden
     * @return Orden de compra encontrada
     * @throws OrderNotFoundException si la orden no existe
     */
    @Transactional(readOnly = true)
    public PurchaseOrder getOrderByNumber(String orderNumber) {
        return purchaseOrderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));
    }

    /**
     * Obtiene todas las órdenes de compra con paginación.
     *
     * @param pageable Configuración de paginación
     * @return Página de órdenes de compra
     */
    @Transactional(readOnly = true)
    public Page<PurchaseOrder> getAllOrders(Pageable pageable) {
        return purchaseOrderRepository.findAll(pageable);
    }

    /**
     * Obtiene órdenes de compra por estado.
     *
     * @param status Estado de las órdenes
     * @param pageable Configuración de paginación
     * @return Página de órdenes con el estado especificado
     */
    @Transactional(readOnly = true)
    public Page<PurchaseOrder> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return purchaseOrderRepository.findByStatus(status, pageable);
    }

    /**
     * Obtiene órdenes de compra por proveedor.
     *
     * @param supplierId ID del proveedor
     * @return Lista de órdenes del proveedor
     * @throws SupplierNotFoundException si el proveedor no existe
     */
    @Transactional(readOnly = true)
    public List<PurchaseOrder> getOrdersBySupplier(Long supplierId) {
        // Validar que el proveedor existe
        if (!supplierRepository.existsById(supplierId)) {
            throw new SupplierNotFoundException(supplierId);
        }
        return purchaseOrderRepository.findBySupplierId(supplierId);
    }

    // ========== Order Status Management ==========
    /**
     * Actualiza el estado de una orden de compra. Requerimiento 3.4: Actualizar
     * el estado de las órdenes de compra.
     *
     * @param orderId ID de la orden
     * @param request Datos de actualización del estado
     * @return Orden actualizada
     * @throws OrderNotFoundException si la orden no existe
     * @throws InvalidOrderOperationException si la transición de estado no es
     * válida
     */
    public PurchaseOrder updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        logger.info("Actualizando estado de orden ID: {} a {}", orderId, request.getStatus());

        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Validar transición de estado
        validateStatusTransition(order.getStatus(), request.getStatus());

        // Actualizar estado
        order.setStatus(request.getStatus());

        // Agregar notas si se proporcionan
        if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
            String existingNotes = order.getNotes() != null ? order.getNotes() : "";
            String newNotes = existingNotes.isEmpty() ? request.getNotes()
                    : existingNotes + "\n" + request.getNotes();
            order.setNotes(newNotes);
        }

        // Si se marca como completada, establecer fecha de recepción si no existe
        if (request.getStatus() == OrderStatus.COMPLETED && order.getReceivedDate() == null) {
            order.setReceivedDate(LocalDate.now());
        }

        order = purchaseOrderRepository.save(order);
        logger.info("Estado de orden actualizado exitosamente: {} -> {}",
                order.getOrderNumber(), order.getStatus());
        return order;
    }

    // ========== Order Reception ==========
    /**
     * Registra la recepción de productos de una orden de compra.
     * Requerimientos: - 3.2: Registrar la recepción parcial o total de
     * productos de una orden de compra - 3.3: Incrementar el stock de los
     * productos recibidos cuando se registre una recepción
     *
     * @param orderId ID de la orden
     * @param request Datos de la recepción
     * @return Respuesta con detalles de la recepción
     * @throws OrderNotFoundException si la orden no existe
     * @throws InvalidOrderOperationException si la orden no puede recibir
     * productos
     */
    public OrderReceiptResponse receiveOrder(Long orderId, ReceiveOrderRequest request) {
        logger.info("Procesando recepción de orden ID: {}", orderId);

        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Validar que la orden puede recibir productos
        validateOrderCanReceiveProducts(order);

        // Establecer fecha de recepción si no se proporciona
        LocalDate receivedDate = request.getReceivedDate() != null
                ? request.getReceivedDate() : LocalDate.now();

        List<OrderDetailReceiptResponse> receiptDetails = new ArrayList<>();

        // Procesar cada detalle de recepción
        for (OrderDetailReceiptRequest receiptRequest : request.getReceivedDetails()) {
            OrderDetail orderDetail = orderDetailRepository.findById(receiptRequest.getOrderDetailId())
                    .orElseThrow(() -> new InvalidOrderOperationException(
                    "Detalle de orden no encontrado: " + receiptRequest.getOrderDetailId()));

            // Validar que el detalle pertenece a la orden
            if (!orderDetail.getOrder().getId().equals(orderId)) {
                throw new InvalidOrderOperationException(
                        "El detalle de orden no pertenece a la orden especificada");
            }

            // Validar cantidad a recibir
            Integer quantityToReceive = receiptRequest.getQuantityReceived();
            Integer quantityPending = orderDetail.getQuantityPending();

            if (quantityToReceive > quantityPending) {
                throw new InvalidOrderOperationException(
                        String.format("Cantidad a recibir (%d) excede la cantidad pendiente (%d) para el producto %s",
                                quantityToReceive, quantityPending, orderDetail.getProduct().getCode()));
            }

            if (quantityToReceive > 0) {
                // Guardar cantidad previamente recibida
                Integer previouslyReceived = orderDetail.getQuantityReceived();

                // Actualizar cantidad recibida
                orderDetail.setQuantityReceived(previouslyReceived + quantityToReceive);
                orderDetailRepository.save(orderDetail);

                // Incrementar stock del producto (Requerimiento 3.3)
                Product product = orderDetail.getProduct();
                product.increaseStock(quantityToReceive);
                productRepository.save(product);

                // Crear movimiento de inventario
                createInventoryMovementForReceipt(product, quantityToReceive, order.getId(),
                        request.getNotes());

                // Crear respuesta del detalle
                OrderDetailReceiptResponse detailResponse = new OrderDetailReceiptResponse(
                        orderDetail.getId(),
                        product.getId(),
                        product.getCode(),
                        product.getName(),
                        orderDetail.getQuantityOrdered(),
                        previouslyReceived,
                        quantityToReceive,
                        orderDetail.getQuantityReceived(),
                        orderDetail.getQuantityPending(),
                        orderDetail.isFullyReceived()
                );
                receiptDetails.add(detailResponse);

                logger.info("Recibido producto {} - Cantidad: {}, Stock actualizado: {}",
                        product.getCode(), quantityToReceive, product.getCurrentStock());
            }
        }

        // Actualizar estado de la orden basado en la recepción
        updateOrderStatusAfterReceipt(order, receivedDate);

        // Crear respuesta
        OrderReceiptResponse response = OrderReceiptResponse.success(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                receivedDate,
                request.getNotes(),
                receiptDetails,
                order.isFullyReceived()
        );

        logger.info("Recepción de orden procesada exitosamente: {} - Estado: {}",
                order.getOrderNumber(), order.getStatus());
        return response;
    }

    // ========== Query Methods ==========
    /**
     * Obtiene órdenes vencidas (que han pasado su fecha esperada).
     * Requerimiento 3.5: Generar alertas cuando una orden de compra exceda la
     * fecha esperada de entrega.
     *
     * @return Lista de órdenes vencidas
     */
    @Transactional(readOnly = true)
    public List<PurchaseOrder> getOverdueOrders() {
        return purchaseOrderRepository.findOverdueOrders();
    }

    /**
     * Obtiene órdenes que vencen pronto.
     *
     * @param daysAhead Días hacia adelante para considerar
     * @return Lista de órdenes que vencen pronto
     */
    @Transactional(readOnly = true)
    public List<PurchaseOrder> getOrdersDueSoon(int daysAhead) {
        LocalDate futureDate = LocalDate.now().plusDays(daysAhead);
        return purchaseOrderRepository.findOrdersDueSoon(futureDate);
    }

    /**
     * Obtiene órdenes por rango de fechas.
     *
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Lista de órdenes en el rango de fechas
     */
    @Transactional(readOnly = true)
    public List<PurchaseOrder> getOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        return purchaseOrderRepository.findByOrderDateBetween(startDate, endDate);
    }

    // ========== Helper Methods ==========
    /**
     * Obtiene el usuario actual del contexto de seguridad.
     */
    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getName() != null) {
                Optional<User> user = userRepository.findByUsername(authentication.getName());
                return user.orElse(null);
            }
        } catch (Exception e) {
            logger.debug("No se pudo obtener el usuario actual", e);
        }
        return null;
    }

    /**
     * Valida que la transición de estado sea válida.
     */
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Reglas de transición de estado
        switch (currentStatus) {
            case PENDING:
                if (newStatus != OrderStatus.CONFIRMED && newStatus != OrderStatus.CANCELLED) {
                    throw new InvalidOrderOperationException(
                            "Transición de estado inválida: " + currentStatus + " -> " + newStatus);
                }
                break;
            case CONFIRMED:
                if (newStatus != OrderStatus.PARTIAL && newStatus != OrderStatus.COMPLETED
                        && newStatus != OrderStatus.CANCELLED) {
                    throw new InvalidOrderOperationException(
                            "Transición de estado inválida: " + currentStatus + " -> " + newStatus);
                }
                break;
            case PARTIAL:
                if (newStatus != OrderStatus.COMPLETED && newStatus != OrderStatus.CANCELLED) {
                    throw new InvalidOrderOperationException(
                            "Transición de estado inválida: " + currentStatus + " -> " + newStatus);
                }
                break;
            case COMPLETED:
            case CANCELLED:
                throw new InvalidOrderOperationException(
                        "No se puede cambiar el estado de una orden " + currentStatus.name().toLowerCase());
        }
    }

    /**
     * Valida que una orden puede recibir productos.
     */
    private void validateOrderCanReceiveProducts(PurchaseOrder order) {
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderOperationException("No se pueden recibir productos de una orden cancelada");
        }
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new InvalidOrderOperationException("La orden ya está completada");
        }
        if (order.getStatus() == OrderStatus.PENDING) {
            throw new InvalidOrderOperationException("La orden debe estar confirmada antes de recibir productos");
        }
    }

    /**
     * Actualiza el estado de la orden después de una recepción.
     */
    private void updateOrderStatusAfterReceipt(PurchaseOrder order, LocalDate receivedDate) {
        // Actualizar fecha de recepción
        if (order.getReceivedDate() == null) {
            order.setReceivedDate(receivedDate);
        }

        // Determinar nuevo estado basado en la recepción
        if (order.isFullyReceived()) {
            order.setStatus(OrderStatus.COMPLETED);
        } else if (order.isPartiallyReceived()) {
            order.setStatus(OrderStatus.PARTIAL);
        }

        purchaseOrderRepository.save(order);
    }

    /**
     * Crea un movimiento de inventario para una recepción.
     */
    private void createInventoryMovementForReceipt(Product product, Integer quantity,
            Long orderId, String notes) {
        InventoryMovement movement = new InventoryMovement();
        movement.setProduct(product);
        movement.setMovementType(InventoryMovement.MovementType.IN);
        movement.setQuantity(quantity);
        movement.setReferenceType(InventoryMovement.ReferenceType.PURCHASE_ORDER);
        movement.setReferenceId(orderId);
        movement.setSourceSystem("ORDER_RECEIPT");
        movement.setNotes(notes != null ? notes : "Recepción de orden de compra");
        movement.setCreatedBy(getCurrentUser());

        inventoryMovementRepository.save(movement);
    }
}
