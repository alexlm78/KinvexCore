package dev.kreaker.kinvex.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.kreaker.kinvex.entity.PurchaseOrder;
import dev.kreaker.kinvex.entity.Supplier;
import dev.kreaker.kinvex.repository.PurchaseOrderRepository;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertService Tests")
class AlertServiceTest {

    @Mock private PurchaseOrderRepository purchaseOrderRepository;

    @Mock private NotificationService notificationService;

    @InjectMocks private AlertService alertService;

    private PurchaseOrder overdueOrder;
    private PurchaseOrder dueSoonOrder;
    private Supplier supplier;

    @BeforeEach
    void setUp() {
        supplier = new Supplier();
        supplier.setId(1L);
        supplier.setName("Test Supplier");

        // Orden vencida (vencida hace 5 días)
        overdueOrder = new PurchaseOrder();
        overdueOrder.setId(1L);
        overdueOrder.setOrderNumber("ORD-001");
        overdueOrder.setSupplier(supplier);
        overdueOrder.setStatus(PurchaseOrder.OrderStatus.CONFIRMED);
        overdueOrder.setOrderDate(LocalDate.now().minusDays(10));
        overdueOrder.setExpectedDate(LocalDate.now().minusDays(5));

        // Orden que vence pronto (vence en 2 días)
        dueSoonOrder = new PurchaseOrder();
        dueSoonOrder.setId(2L);
        dueSoonOrder.setOrderNumber("ORD-002");
        dueSoonOrder.setSupplier(supplier);
        dueSoonOrder.setStatus(PurchaseOrder.OrderStatus.CONFIRMED);
        dueSoonOrder.setOrderDate(LocalDate.now().minusDays(5));
        dueSoonOrder.setExpectedDate(LocalDate.now().plusDays(2));
    }

    @Test
    @DisplayName("Debe obtener órdenes vencidas correctamente")
    void shouldGetOverdueOrders() {
        // Given
        List<PurchaseOrder> expectedOrders = Arrays.asList(overdueOrder);
        when(purchaseOrderRepository.findOverdueOrders()).thenReturn(expectedOrders);

        // When
        List<PurchaseOrder> result = alertService.getOverdueOrders();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ORD-001", result.get(0).getOrderNumber());
        assertTrue(result.get(0).isOverdue());

        verify(purchaseOrderRepository).findOverdueOrders();
    }

    @Test
    @DisplayName("Debe obtener órdenes que vencen pronto correctamente")
    void shouldGetOrdersDueSoon() {
        // Given
        int daysAhead = 3;
        List<PurchaseOrder> expectedOrders = Arrays.asList(dueSoonOrder);
        when(purchaseOrderRepository.findOrdersDueSoon(any(LocalDate.class)))
                .thenReturn(expectedOrders);

        // When
        List<PurchaseOrder> result = alertService.getOrdersDueSoon(daysAhead);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ORD-002", result.get(0).getOrderNumber());
        assertFalse(result.get(0).isOverdue());

        verify(purchaseOrderRepository).findOrdersDueSoon(any(LocalDate.class));
    }

    @Test
    @DisplayName("Debe procesar alertas de órdenes vencidas manualmente")
    void shouldCheckOverdueOrdersManually() {
        // Given
        List<PurchaseOrder> overdueOrders = Arrays.asList(overdueOrder);
        when(purchaseOrderRepository.findOverdueOrders()).thenReturn(overdueOrders);

        // When
        int result = alertService.checkOverdueOrdersManually();

        // Then
        assertEquals(1, result);
        verify(purchaseOrderRepository).findOverdueOrders();
        verify(notificationService).sendOverdueOrderAlert(any(PurchaseOrder.class), anyLong());
        verify(notificationService).sendOverdueOrdersSummary(anyList());
    }

    @Test
    @DisplayName("Debe procesar alertas de órdenes que vencen pronto manualmente")
    void shouldCheckOrdersDueSoonManually() {
        // Given
        int daysAhead = 3;
        List<PurchaseOrder> ordersDueSoon = Arrays.asList(dueSoonOrder);
        when(purchaseOrderRepository.findOrdersDueSoon(any(LocalDate.class)))
                .thenReturn(ordersDueSoon);

        // When
        int result = alertService.checkOrdersDueSoonManually(daysAhead);

        // Then
        assertEquals(1, result);
        verify(purchaseOrderRepository).findOrdersDueSoon(any(LocalDate.class));
        verify(notificationService).sendOrderDueSoonAlert(any(PurchaseOrder.class), anyLong());
        verify(notificationService).sendOrdersDueSoonSummary(anyList());
    }

    @Test
    @DisplayName("Debe manejar correctamente cuando no hay órdenes vencidas")
    void shouldHandleNoOverdueOrders() {
        // Given
        when(purchaseOrderRepository.findOverdueOrders()).thenReturn(Collections.emptyList());

        // When
        List<PurchaseOrder> result = alertService.getOverdueOrders();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(purchaseOrderRepository).findOverdueOrders();
    }

    @Test
    @DisplayName("Debe manejar correctamente cuando no hay órdenes que vencen pronto")
    void shouldHandleNoOrdersDueSoon() {
        // Given
        int daysAhead = 7;
        when(purchaseOrderRepository.findOrdersDueSoon(any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // When
        List<PurchaseOrder> result = alertService.getOrdersDueSoon(daysAhead);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(purchaseOrderRepository).findOrdersDueSoon(any(LocalDate.class));
    }

    @Test
    @DisplayName("Debe procesar verificación manual sin órdenes")
    void shouldHandleManualCheckWithNoOrders() {
        // Given
        when(purchaseOrderRepository.findOverdueOrders()).thenReturn(Collections.emptyList());

        // When
        int result = alertService.checkOverdueOrdersManually();

        // Then
        assertEquals(0, result);
        verify(purchaseOrderRepository).findOverdueOrders();
        verify(notificationService, times(0))
                .sendOverdueOrderAlert(any(PurchaseOrder.class), anyLong());
        verify(notificationService, times(0)).sendOverdueOrdersSummary(anyList());
    }
}
