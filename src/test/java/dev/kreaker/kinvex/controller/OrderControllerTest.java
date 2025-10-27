package dev.kreaker.kinvex.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kreaker.kinvex.dto.order.CreateOrderRequest;
import dev.kreaker.kinvex.dto.order.OrderDetailReceiptRequest;
import dev.kreaker.kinvex.dto.order.OrderDetailReceiptResponse;
import dev.kreaker.kinvex.dto.order.OrderDetailRequest;
import dev.kreaker.kinvex.dto.order.OrderReceiptResponse;
import dev.kreaker.kinvex.dto.order.ReceiveOrderRequest;
import dev.kreaker.kinvex.dto.order.UpdateOrderStatusRequest;
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
import dev.kreaker.kinvex.service.OrderService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests de integración para OrderController.
 *
 * Verifica los endpoints REST para gestión de órdenes de compra según los
 * requerimientos 3.3 y 3.4.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private PurchaseOrder testOrder;
    private Supplier testSupplier;
    private Product testProduct;
    private User testUser;
    private OrderDetail testOrderDetail;
    private CreateOrderRequest createOrderRequest;
    private UpdateOrderStatusRequest updateStatusRequest;
    private ReceiveOrderRequest receiveOrderRequest;

    @BeforeEach
    void setUp() {
        // Setup test entities
        testSupplier = new Supplier("Test Supplier", "John Doe", "supplier@example.com", "123-456-7890");
        testSupplier.setId(1L);

        testProduct = new Product("PROD001", "Test Product", new BigDecimal("10.00"));
        testProduct.setId(1L);
        testProduct.setCurrentStock(100);

        testUser = new User("testuser", "test@example.com", "hashedpassword", User.UserRole.OPERATOR);
        testUser.setId(1L);

        testOrder = new PurchaseOrder("PO001", testSupplier, LocalDate.now(), LocalDate.now().plusDays(7), testUser);
        testOrder.setId(1L);
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setTotalAmount(new BigDecimal("100.00"));

        testOrderDetail = new OrderDetail(testOrder, testProduct, 10, new BigDecimal("10.00"));
        testOrderDetail.setId(1L);
        testOrder.getOrderDetails().add(testOrderDetail);

        // Setup DTOs
        OrderDetailRequest orderDetailRequest = new OrderDetailRequest();
        orderDetailRequest.setProductId(1L);
        orderDetailRequest.setQuantityOrdered(10);
        orderDetailRequest.setUnitPrice(new BigDecimal("10.00"));

        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setOrderNumber("PO001");
        createOrderRequest.setSupplierId(1L);
        createOrderRequest.setOrderDate(LocalDate.now());
        createOrderRequest.setExpectedDate(LocalDate.now().plusDays(7));
        createOrderRequest.setOrderDetails(Arrays.asList(orderDetailRequest));
        createOrderRequest.setNotes("Test order");

        updateStatusRequest = new UpdateOrderStatusRequest();
        updateStatusRequest.setStatus(OrderStatus.CONFIRMED);
        updateStatusRequest.setNotes("Status updated");

        OrderDetailReceiptRequest receiptDetailRequest = new OrderDetailReceiptRequest();
        receiptDetailRequest.setOrderDetailId(1L);
        receiptDetailRequest.setQuantityReceived(5);

        receiveOrderRequest = new ReceiveOrderRequest();
        receiveOrderRequest.setReceivedDate(LocalDate.now());
        receiveOrderRequest.setReceivedDetails(Arrays.asList(receiptDetailRequest));
        receiveOrderRequest.setNotes("Partial receipt");
    }

    // ========== Order Creation Tests ==========
    @Test
    @WithMockUser(roles = "OPERATOR")
    void createOrder_WithValidData_ShouldReturnCreatedOrder() throws Exception {
        // Arrange
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(testOrder);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.orderNumber", is("PO001")))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.totalAmount", is(100.00)));
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void createOrder_WithDuplicateOrderNumber_ShouldReturnConflict() throws Exception {
        // Arrange
        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenThrow(new DuplicateOrderNumberException("PO001"));

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void createOrder_WithNonExistentSupplier_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenThrow(new SupplierNotFoundException(1L));

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void createOrder_WithNonExistentProduct_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenThrow(new ProductNotFoundException(1L));

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void createOrder_WithViewerRole_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isForbidden());
    }

    // ========== Order Retrieval Tests ==========
    @Test
    @WithMockUser(roles = "VIEWER")
    void getAllOrders_ShouldReturnPagedOrders() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<PurchaseOrder> orderPage = new PageImpl<>(Arrays.asList(testOrder), pageable, 1);
        when(orderService.getAllOrders(any(Pageable.class))).thenReturn(orderPage);

        // Act & Assert
        mockMvc.perform(get("/api/orders")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].orderNumber", is("PO001")))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void getOrderById_WithExistingId_ShouldReturnOrder() throws Exception {
        // Arrange
        when(orderService.getOrderById(1L)).thenReturn(testOrder);

        // Act & Assert
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.orderNumber", is("PO001")))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void getOrderById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(orderService.getOrderById(999L)).thenThrow(new OrderNotFoundException(999L));

        // Act & Assert
        mockMvc.perform(get("/api/orders/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void getOrderByNumber_WithExistingNumber_ShouldReturnOrder() throws Exception {
        // Arrange
        when(orderService.getOrderByNumber("PO001")).thenReturn(testOrder);

        // Act & Assert
        mockMvc.perform(get("/api/orders/number/PO001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.orderNumber", is("PO001")));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void getOrdersByStatus_ShouldReturnFilteredOrders() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<PurchaseOrder> orderPage = new PageImpl<>(Arrays.asList(testOrder), pageable, 1);
        when(orderService.getOrdersByStatus(eq(OrderStatus.PENDING), any(Pageable.class))).thenReturn(orderPage);

        // Act & Assert
        mockMvc.perform(get("/api/orders/status/PENDING")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status", is("PENDING")));
    }

    // ========== Order Status Management Tests ==========
    @Test
    @WithMockUser(roles = "OPERATOR")
    void updateOrderStatus_WithValidTransition_ShouldReturnUpdatedOrder() throws Exception {
        // Arrange
        testOrder.setStatus(OrderStatus.CONFIRMED);
        when(orderService.updateOrderStatus(eq(1L), any(UpdateOrderStatusRequest.class))).thenReturn(testOrder);

        // Act & Assert
        mockMvc.perform(put("/api/orders/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateStatusRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("CONFIRMED")));
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void updateOrderStatus_WithInvalidTransition_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(orderService.updateOrderStatus(eq(1L), any(UpdateOrderStatusRequest.class)))
                .thenThrow(new InvalidOrderOperationException("Invalid status transition"));

        // Act & Assert
        mockMvc.perform(put("/api/orders/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateStatusRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void updateOrderStatus_WithViewerRole_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/orders/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateStatusRequest)))
                .andExpect(status().isForbidden());
    }

    // ========== Order Reception Tests ==========
    @Test
    @WithMockUser(roles = "OPERATOR")
    void receiveOrder_WithValidData_ShouldReturnReceiptResponse() throws Exception {
        // Arrange
        OrderDetailReceiptResponse receiptDetail = new OrderDetailReceiptResponse(
                1L, 1L, "PROD001", "Test Product", 10, 0, 5, 5, 5, false);
        OrderReceiptResponse receiptResponse = OrderReceiptResponse.success(
                1L, "PO001", OrderStatus.PARTIAL, LocalDate.now(), "Partial receipt",
                Arrays.asList(receiptDetail), false);

        when(orderService.receiveOrder(eq(1L), any(ReceiveOrderRequest.class))).thenReturn(receiptResponse);

        // Act & Assert
        mockMvc.perform(post("/api/orders/1/receive")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(receiveOrderRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("PARTIAL")))
                .andExpect(jsonPath("$.orderId", is(1)))
                .andExpect(jsonPath("$.orderNumber", is("PO001")))
                .andExpect(jsonPath("$.receivedDetails", hasSize(1)))
                .andExpect(jsonPath("$.fullyReceived", is(false)));
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void receiveOrder_WithCancelledOrder_ShouldReturnConflict() throws Exception {
        // Arrange
        when(orderService.receiveOrder(eq(1L), any(ReceiveOrderRequest.class)))
                .thenThrow(new OrderStateConflictException("Cannot receive products from cancelled order"));

        // Act & Assert
        mockMvc.perform(post("/api/orders/1/receive")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(receiveOrderRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void receiveOrder_WithNonExistentOrder_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(orderService.receiveOrder(eq(999L), any(ReceiveOrderRequest.class)))
                .thenThrow(new OrderNotFoundException(999L));

        // Act & Assert
        mockMvc.perform(post("/api/orders/999/receive")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(receiveOrderRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void receiveOrder_WithViewerRole_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/orders/1/receive")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(receiveOrderRequest)))
                .andExpect(status().isForbidden());
    }

    // ========== Query Operations Tests ==========
    @Test
    @WithMockUser(roles = "VIEWER")
    void getOrdersBySupplier_ShouldReturnSupplierOrders() throws Exception {
        // Arrange
        List<PurchaseOrder> supplierOrders = Arrays.asList(testOrder);
        when(orderService.getOrdersBySupplier(1L)).thenReturn(supplierOrders);

        // Act & Assert
        mockMvc.perform(get("/api/orders/supplier/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].orderNumber", is("PO001")));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void getOrdersBySupplier_WithNonExistentSupplier_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(orderService.getOrdersBySupplier(999L)).thenThrow(new SupplierNotFoundException(999L));

        // Act & Assert
        mockMvc.perform(get("/api/orders/supplier/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void getOverdueOrders_ShouldReturnOverdueOrders() throws Exception {
        // Arrange
        List<PurchaseOrder> overdueOrders = Arrays.asList(testOrder);
        when(orderService.getOverdueOrders()).thenReturn(overdueOrders);

        // Act & Assert
        mockMvc.perform(get("/api/orders/overdue"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void getOverdueOrders_WithViewerRole_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orders/overdue"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void getOrdersDueSoon_ShouldReturnOrdersDueSoon() throws Exception {
        // Arrange
        List<PurchaseOrder> ordersDueSoon = Arrays.asList(testOrder);
        when(orderService.getOrdersDueSoon(anyInt())).thenReturn(ordersDueSoon);

        // Act & Assert
        mockMvc.perform(get("/api/orders/due-soon")
                .param("daysAhead", "7"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void getOrdersByDateRange_ShouldReturnOrdersInRange() throws Exception {
        // Arrange
        List<PurchaseOrder> ordersInRange = Arrays.asList(testOrder);
        when(orderService.getOrdersByDateRange(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(ordersInRange);

        // Act & Assert
        mockMvc.perform(get("/api/orders/date-range")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    void getAllOrders_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createOrder_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createOrderRequest)))
                .andExpect(status().isUnauthorized());
    }
}
