package dev.kreaker.kinvex.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.kreaker.kinvex.dto.order.CreateOrderRequest;
import dev.kreaker.kinvex.dto.order.OrderDetailReceiptRequest;
import dev.kreaker.kinvex.dto.order.OrderDetailRequest;
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
import dev.kreaker.kinvex.exception.OrderStateConflictException;
import dev.kreaker.kinvex.exception.ProductNotFoundException;
import dev.kreaker.kinvex.exception.SupplierNotFoundException;
import dev.kreaker.kinvex.repository.InventoryMovementRepository;
import dev.kreaker.kinvex.repository.OrderDetailRepository;
import dev.kreaker.kinvex.repository.ProductRepository;
import dev.kreaker.kinvex.repository.PurchaseOrderRepository;
import dev.kreaker.kinvex.repository.SupplierRepository;
import dev.kreaker.kinvex.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Tests unitarios para OrderService.
 *
 * <p>Verifica la lógica de negocio de órdenes de compra según los requerimientos 3.3 y 3.4.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private PurchaseOrderRepository purchaseOrderRepository;
    @Mock private OrderDetailRepository orderDetailRepository;
    @Mock private SupplierRepository supplierRepository;
    @Mock private ProductRepository productRepository;
    @Mock private InventoryMovementRepository inventoryMovementRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private OrderService orderService;

    private Supplier testSupplier;
    private Product testProduct;
    private User testUser;
    private PurchaseOrder testOrder;
    private OrderDetail testOrderDetail;

    @BeforeEach
    void setUp() {
        // Setup test entities
        testSupplier =
                new Supplier("Test Supplier", "John Doe", "supplier@example.com", "123-456-7890");
        testSupplier.setId(1L);
        testSupplier.setActive(true);

        testProduct = new Product("PROD001", "Test Product", new BigDecimal("10.00"));
        testProduct.setId(1L);
        testProduct.setCurrentStock(100);
        testProduct.setActive(true);

        testUser =
                new User("testuser", "test@example.com", "hashedpassword", User.UserRole.OPERATOR);
        testUser.setId(1L);

        testOrder =
                new PurchaseOrder(
                        "PO001",
                        testSupplier,
                        LocalDate.now(),
                        LocalDate.now().plusDays(7),
                        testUser);
        testOrder.setId(1L);
        testOrder.setStatus(OrderStatus.PENDING);

        testOrderDetail = new OrderDetail(testOrder, testProduct, 10, new BigDecimal("10.00"));
        testOrderDetail.setId(1L);
        testOrder.getOrderDetails().add(testOrderDetail);
    }

    // ========== Order Creation Tests ==========
    @Test
    void createOrder_WithDuplicateOrderNumber_ShouldThrowException() {
        // Arrange
        OrderDetailRequest orderDetailRequest = new OrderDetailRequest();
        orderDetailRequest.setProductId(1L);
        orderDetailRequest.setQuantityOrdered(10);
        orderDetailRequest.setUnitPrice(new BigDecimal("10.00"));

        CreateOrderRequest createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setOrderNumber("PO001");
        createOrderRequest.setSupplierId(1L);
        createOrderRequest.setOrderDate(LocalDate.now());
        createOrderRequest.setExpectedDate(LocalDate.now().plusDays(7));
        createOrderRequest.setOrderDetails(Arrays.asList(orderDetailRequest));

        when(purchaseOrderRepository.existsByOrderNumber("PO001")).thenReturn(true);

        // Act & Assert
        assertThrows(
                DuplicateOrderNumberException.class,
                () -> {
                    orderService.createOrder(createOrderRequest);
                });

        verify(purchaseOrderRepository).existsByOrderNumber("PO001");
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    @Test
    void createOrder_WithNonExistentSupplier_ShouldThrowException() {
        // Arrange
        OrderDetailRequest orderDetailRequest = new OrderDetailRequest();
        orderDetailRequest.setProductId(1L);
        orderDetailRequest.setQuantityOrdered(10);
        orderDetailRequest.setUnitPrice(new BigDecimal("10.00"));

        CreateOrderRequest createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setOrderNumber("PO001");
        createOrderRequest.setSupplierId(1L);
        createOrderRequest.setOrderDate(LocalDate.now());
        createOrderRequest.setExpectedDate(LocalDate.now().plusDays(7));
        createOrderRequest.setOrderDetails(Arrays.asList(orderDetailRequest));

        when(purchaseOrderRepository.existsByOrderNumber("PO001")).thenReturn(false);
        when(supplierRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                SupplierNotFoundException.class,
                () -> {
                    orderService.createOrder(createOrderRequest);
                });

        verify(supplierRepository).findById(1L);
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    @Test
    void createOrder_WithInactiveSupplier_ShouldThrowException() {
        // Arrange
        testSupplier.setActive(false);

        OrderDetailRequest orderDetailRequest = new OrderDetailRequest();
        orderDetailRequest.setProductId(1L);
        orderDetailRequest.setQuantityOrdered(10);
        orderDetailRequest.setUnitPrice(new BigDecimal("10.00"));

        CreateOrderRequest createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setOrderNumber("PO001");
        createOrderRequest.setSupplierId(1L);
        createOrderRequest.setOrderDate(LocalDate.now());
        createOrderRequest.setExpectedDate(LocalDate.now().plusDays(7));
        createOrderRequest.setOrderDetails(Arrays.asList(orderDetailRequest));

        when(purchaseOrderRepository.existsByOrderNumber("PO001")).thenReturn(false);
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));

        // Act & Assert
        assertThrows(
                SupplierNotFoundException.class,
                () -> {
                    orderService.createOrder(createOrderRequest);
                });

        verify(supplierRepository).findById(1L);
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    @Test
    void createOrder_WithNonExistentProduct_ShouldThrowException() {
        // Arrange
        OrderDetailRequest orderDetailRequest = new OrderDetailRequest();
        orderDetailRequest.setProductId(1L);
        orderDetailRequest.setQuantityOrdered(10);
        orderDetailRequest.setUnitPrice(new BigDecimal("10.00"));

        CreateOrderRequest createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setOrderNumber("PO001");
        createOrderRequest.setSupplierId(1L);
        createOrderRequest.setOrderDate(LocalDate.now());
        createOrderRequest.setExpectedDate(LocalDate.now().plusDays(7));
        createOrderRequest.setOrderDetails(Arrays.asList(orderDetailRequest));

        when(purchaseOrderRepository.existsByOrderNumber("PO001")).thenReturn(false);
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ProductNotFoundException.class,
                () -> {
                    orderService.createOrder(createOrderRequest);
                });

        verify(productRepository).findById(1L);
        verify(orderDetailRepository, never()).saveAll(any());
    }

    // ========== Order Retrieval Tests ==========
    @Test
    void getOrderById_WithExistingId_ShouldReturnOrder() {
        // Arrange
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act
        PurchaseOrder result = orderService.getOrderById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testOrder, result);
        verify(purchaseOrderRepository).findById(1L);
    }

    @Test
    void getOrderById_WithNonExistentId_ShouldThrowException() {
        // Arrange
        when(purchaseOrderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                OrderNotFoundException.class,
                () -> {
                    orderService.getOrderById(999L);
                });

        verify(purchaseOrderRepository).findById(999L);
    }

    @Test
    void getAllOrders_ShouldReturnPagedOrders() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<PurchaseOrder> expectedPage = new PageImpl<>(Arrays.asList(testOrder));
        when(purchaseOrderRepository.findAll(pageable)).thenReturn(expectedPage);

        // Act
        Page<PurchaseOrder> result = orderService.getAllOrders(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testOrder, result.getContent().get(0));
        verify(purchaseOrderRepository).findAll(pageable);
    }

    // ========== Order Status Management Tests ==========
    @Test
    void updateOrderStatus_WithValidTransition_ShouldUpdateStatus() {
        // Arrange
        UpdateOrderStatusRequest updateStatusRequest = new UpdateOrderStatusRequest();
        updateStatusRequest.setStatus(OrderStatus.CONFIRMED);
        updateStatusRequest.setNotes("Status updated");

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testOrder);

        // Act
        PurchaseOrder result = orderService.updateOrderStatus(1L, updateStatusRequest);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        assertTrue(result.getNotes().contains("Status updated"));
        verify(purchaseOrderRepository).findById(1L);
        verify(purchaseOrderRepository).save(testOrder);
    }

    @Test
    void updateOrderStatus_WithInvalidTransition_ShouldThrowException() {
        // Arrange
        testOrder.setStatus(OrderStatus.COMPLETED);
        UpdateOrderStatusRequest updateStatusRequest = new UpdateOrderStatusRequest();
        updateStatusRequest.setStatus(OrderStatus.PENDING);

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThrows(
                InvalidOrderOperationException.class,
                () -> {
                    orderService.updateOrderStatus(1L, updateStatusRequest);
                });

        verify(purchaseOrderRepository).findById(1L);
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    // ========== Order Reception Tests ==========
    @Test
    void receiveOrder_WithCancelledOrder_ShouldThrowException() {
        // Arrange
        testOrder.setStatus(OrderStatus.CANCELLED);

        OrderDetailReceiptRequest receiptDetailRequest = new OrderDetailReceiptRequest();
        receiptDetailRequest.setOrderDetailId(1L);
        receiptDetailRequest.setQuantityReceived(5);

        ReceiveOrderRequest receiveOrderRequest = new ReceiveOrderRequest();
        receiveOrderRequest.setReceivedDate(LocalDate.now());
        receiveOrderRequest.setReceivedDetails(Arrays.asList(receiptDetailRequest));

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThrows(
                OrderStateConflictException.class,
                () -> {
                    orderService.receiveOrder(1L, receiveOrderRequest);
                });

        verify(purchaseOrderRepository).findById(1L);
        verify(orderDetailRepository, never()).findById(anyLong());
    }

    @Test
    void receiveOrder_WithPendingOrder_ShouldThrowException() {
        // Arrange
        testOrder.setStatus(OrderStatus.PENDING);

        OrderDetailReceiptRequest receiptDetailRequest = new OrderDetailReceiptRequest();
        receiptDetailRequest.setOrderDetailId(1L);
        receiptDetailRequest.setQuantityReceived(5);

        ReceiveOrderRequest receiveOrderRequest = new ReceiveOrderRequest();
        receiveOrderRequest.setReceivedDate(LocalDate.now());
        receiveOrderRequest.setReceivedDetails(Arrays.asList(receiptDetailRequest));

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThrows(
                InvalidOrderOperationException.class,
                () -> {
                    orderService.receiveOrder(1L, receiveOrderRequest);
                });

        verify(purchaseOrderRepository).findById(1L);
        verify(orderDetailRepository, never()).findById(anyLong());
    }

    @Test
    void receiveOrder_WithExcessiveQuantity_ShouldThrowException() {
        // Arrange
        testOrder.setStatus(OrderStatus.CONFIRMED);

        OrderDetailReceiptRequest receiptDetailRequest = new OrderDetailReceiptRequest();
        receiptDetailRequest.setOrderDetailId(1L);
        receiptDetailRequest.setQuantityReceived(15); // More than ordered (10)

        ReceiveOrderRequest receiveOrderRequest = new ReceiveOrderRequest();
        receiveOrderRequest.setReceivedDate(LocalDate.now());
        receiveOrderRequest.setReceivedDetails(Arrays.asList(receiptDetailRequest));

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderDetailRepository.findById(1L)).thenReturn(Optional.of(testOrderDetail));

        // Act & Assert
        assertThrows(
                InvalidOrderOperationException.class,
                () -> {
                    orderService.receiveOrder(1L, receiveOrderRequest);
                });

        verify(orderDetailRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void receiveOrder_WithValidData_ShouldProcessReceipt() {
        // Arrange
        testOrder.setStatus(OrderStatus.CONFIRMED);

        OrderDetailReceiptRequest receiptDetailRequest = new OrderDetailReceiptRequest();
        receiptDetailRequest.setOrderDetailId(1L);
        receiptDetailRequest.setQuantityReceived(5);

        ReceiveOrderRequest receiveOrderRequest = new ReceiveOrderRequest();
        receiveOrderRequest.setReceivedDate(LocalDate.now());
        receiveOrderRequest.setReceivedDetails(Arrays.asList(receiptDetailRequest));

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderDetailRepository.findById(1L)).thenReturn(Optional.of(testOrderDetail));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(inventoryMovementRepository.save(any(InventoryMovement.class)))
                .thenAnswer(
                        invocation -> {
                            InventoryMovement movement = invocation.getArgument(0);
                            movement.setId(1L);
                            return movement;
                        });
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testOrder);

        // Act
        OrderReceiptResponse result = orderService.receiveOrder(1L, receiveOrderRequest);

        // Assert
        assertNotNull(result);
        assertEquals("PARTIAL", result.getStatus().toString());
        assertEquals(1L, result.getOrderId());
        assertEquals("PO001", result.getOrderNumber());
        assertEquals(1, result.getReceivedDetails().size());
        assertEquals(5, testOrderDetail.getQuantityReceived());

        verify(purchaseOrderRepository).findById(1L);
        verify(orderDetailRepository).findById(1L);
        verify(productRepository).save(testProduct);
        verify(inventoryMovementRepository).save(any(InventoryMovement.class));
    }

    // ========== Query Methods Tests ==========
    @Test
    void getOverdueOrders_ShouldReturnOverdueOrders() {
        // Arrange
        List<PurchaseOrder> overdueOrders = Arrays.asList(testOrder);
        when(purchaseOrderRepository.findOverdueOrders()).thenReturn(overdueOrders);

        // Act
        List<PurchaseOrder> result = orderService.getOverdueOrders();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder, result.get(0));
        verify(purchaseOrderRepository).findOverdueOrders();
    }

    @Test
    void getOrdersBySupplier_WithExistingSupplier_ShouldReturnOrders() {
        // Arrange
        List<PurchaseOrder> supplierOrders = Arrays.asList(testOrder);
        when(supplierRepository.existsById(1L)).thenReturn(true);
        when(purchaseOrderRepository.findBySupplierId(1L)).thenReturn(supplierOrders);

        // Act
        List<PurchaseOrder> result = orderService.getOrdersBySupplier(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder, result.get(0));
        verify(supplierRepository).existsById(1L);
        verify(purchaseOrderRepository).findBySupplierId(1L);
    }

    @Test
    void getOrdersBySupplier_WithNonExistentSupplier_ShouldThrowException() {
        // Arrange
        when(supplierRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(
                SupplierNotFoundException.class,
                () -> {
                    orderService.getOrdersBySupplier(999L);
                });

        verify(supplierRepository).existsById(999L);
        verify(purchaseOrderRepository, never()).findBySupplierId(anyLong());
    }

    @Test
    void getOrdersDueSoon_ShouldReturnOrdersDueSoon() {
        // Arrange
        List<PurchaseOrder> ordersDueSoon = Arrays.asList(testOrder);
        LocalDate futureDate = LocalDate.now().plusDays(7);
        when(purchaseOrderRepository.findOrdersDueSoon(futureDate)).thenReturn(ordersDueSoon);

        // Act
        List<PurchaseOrder> result = orderService.getOrdersDueSoon(7);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder, result.get(0));
        verify(purchaseOrderRepository).findOrdersDueSoon(futureDate);
    }

    @Test
    void getOrdersByDateRange_ShouldReturnOrdersInRange() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        List<PurchaseOrder> ordersInRange = Arrays.asList(testOrder);
        when(purchaseOrderRepository.findByOrderDateBetween(startDate, endDate))
                .thenReturn(ordersInRange);

        // Act
        List<PurchaseOrder> result = orderService.getOrdersByDateRange(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder, result.get(0));
        verify(purchaseOrderRepository).findByOrderDateBetween(startDate, endDate);
    }
}
