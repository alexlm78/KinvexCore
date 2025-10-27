package dev.kreaker.kinvex.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import dev.kreaker.kinvex.dto.report.InventoryMovementReportDto;
import dev.kreaker.kinvex.dto.report.ReportFilterDto;
import dev.kreaker.kinvex.dto.report.StockLevelReportDto;
import dev.kreaker.kinvex.dto.report.SupplierPerformanceReportDto;
import dev.kreaker.kinvex.entity.Category;
import dev.kreaker.kinvex.entity.InventoryMovement;
import dev.kreaker.kinvex.entity.Product;
import dev.kreaker.kinvex.entity.Supplier;
import dev.kreaker.kinvex.entity.User;
import dev.kreaker.kinvex.repository.InventoryMovementRepository;
import dev.kreaker.kinvex.repository.ProductRepository;
import dev.kreaker.kinvex.repository.SupplierRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private InventoryMovementRepository inventoryMovementRepository;

    @Mock private ProductRepository productRepository;

    @Mock private SupplierRepository supplierRepository;

    @InjectMocks private ReportService reportService;

    private Product testProduct;
    private InventoryMovement testMovement;
    private Supplier testSupplier;
    private ReportFilterDto testFilter;

    @BeforeEach
    void setUp() {
        // Create test product
        testProduct = new Product("TEST001", "Test Product", new BigDecimal("10.00"));
        testProduct.setId(1L);
        testProduct.setCurrentStock(100);
        testProduct.setMinStock(10);
        testProduct.setMaxStock(500);

        Category category = new Category();
        category.setId(1L);
        category.setName("Test Category");
        testProduct.setCategory(category);

        // Create test user
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        // Create test movement
        testMovement = new InventoryMovement(testProduct, InventoryMovement.MovementType.IN, 50);
        testMovement.setId(1L);
        testMovement.setCreatedBy(testUser);
        testMovement.setCreatedAt(LocalDateTime.now());

        // Create test supplier
        testSupplier = new Supplier("Test Supplier");
        testSupplier.setId(1L);
        testSupplier.setContactPerson("John Doe");
        testSupplier.setEmail("john@testsupplier.com");
        testSupplier.setPhone("123-456-7890");

        // Create test filter
        testFilter = new ReportFilterDto();
        testFilter.setStartDate(LocalDateTime.now().minusDays(30));
        testFilter.setEndDate(LocalDateTime.now());
    }

    @Test
    void testGetInventoryMovementReport() {
        // Given
        when(inventoryMovementRepository.findByCreatedAtBetween(any(), any()))
                .thenReturn(Arrays.asList(testMovement));

        // When
        List<InventoryMovementReportDto> result =
                reportService.getInventoryMovementReport(testFilter);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        InventoryMovementReportDto reportDto = result.get(0);
        assertEquals(testMovement.getId(), reportDto.getMovementId());
        assertEquals(testProduct.getCode(), reportDto.getProductCode());
        assertEquals(testProduct.getName(), reportDto.getProductName());
        assertEquals(testMovement.getMovementType(), reportDto.getMovementType());
        assertEquals(testMovement.getQuantity(), reportDto.getQuantity());
    }

    @Test
    void testGetDetailedInventoryMovementReport() {
        // Given
        when(inventoryMovementRepository.findMovementsBetween(any(), any()))
                .thenReturn(Arrays.asList(testMovement));

        // When
        List<InventoryMovementReportDto> result =
                reportService.getDetailedInventoryMovementReport(testFilter);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        InventoryMovementReportDto reportDto = result.get(0);
        assertEquals(testMovement.getId(), reportDto.getMovementId());
        assertEquals(testProduct.getCode(), reportDto.getProductCode());
        assertEquals(testProduct.getName(), reportDto.getProductName());
        assertEquals("testuser", reportDto.getCreatedByUsername());
    }

    @Test
    void testGetStockLevelReport() {
        // Given
        when(productRepository.findByActiveTrue()).thenReturn(Arrays.asList(testProduct));
        when(inventoryMovementRepository.findByProductAndCreatedAtBetween(any(), any(), any()))
                .thenReturn(Arrays.asList(testMovement));

        // When
        List<StockLevelReportDto> result = reportService.getStockLevelReport(testFilter);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        StockLevelReportDto reportDto = result.get(0);
        assertEquals(testProduct.getId(), reportDto.getProductId());
        assertEquals(testProduct.getCode(), reportDto.getProductCode());
        assertEquals(testProduct.getName(), reportDto.getProductName());
        assertEquals(testProduct.getCurrentStock(), reportDto.getCurrentStock());
        assertEquals("Test Category", reportDto.getCategoryName());
    }

    @Test
    void testGetSupplierPerformanceReport() {
        // Given
        when(supplierRepository.findByActiveTrue()).thenReturn(Arrays.asList(testSupplier));
        when(supplierRepository.findSupplierPerformanceBetween(any(), any()))
                .thenReturn(Arrays.asList());

        // When
        List<SupplierPerformanceReportDto> result =
                reportService.getSupplierPerformanceReport(testFilter);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        SupplierPerformanceReportDto reportDto = result.get(0);
        assertEquals(testSupplier.getId(), reportDto.getSupplierId());
        assertEquals(testSupplier.getName(), reportDto.getSupplierName());
        assertEquals(testSupplier.getContactPerson(), reportDto.getContactPerson());
        assertEquals(testSupplier.getEmail(), reportDto.getEmail());
    }

    @Test
    void testGetInventoryMovementReportWithInvalidDateRange() {
        // Given
        ReportFilterDto invalidFilter = new ReportFilterDto();
        invalidFilter.setStartDate(null);
        invalidFilter.setEndDate(null);

        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    reportService.getInventoryMovementReport(invalidFilter);
                });
    }

    @Test
    void testGetSupplierPerformanceReportWithInvalidDateRange() {
        // Given
        ReportFilterDto invalidFilter = new ReportFilterDto();
        invalidFilter.setStartDate(null);
        invalidFilter.setEndDate(null);

        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    reportService.getSupplierPerformanceReport(invalidFilter);
                });
    }

    @Test
    void testGetProductMovementSummary() {
        // Given
        Object[] summaryData = {testProduct, 100L, 50L, 50L};
        when(inventoryMovementRepository.findProductMovementSummaryBetween(any(), any()))
                .thenReturn(Collections.singletonList(summaryData));

        // When
        List<Object[]> result = reportService.getProductMovementSummary(testFilter);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProduct, result.get(0)[0]);
    }

    @Test
    void testGetMovementStatisticsByType() {
        // Given
        Object[] statsData = {
            InventoryMovement.MovementType.IN,
            InventoryMovement.ReferenceType.PURCHASE_ORDER,
            5L,
            250L
        };
        when(inventoryMovementRepository.findMovementStatisticsByTypeBetween(any(), any()))
                .thenReturn(Collections.singletonList(statsData));

        // When
        List<Object[]> result = reportService.getMovementStatisticsByType(testFilter);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(InventoryMovement.MovementType.IN, result.get(0)[0]);
    }

    @Test
    void testGetDailyMovementSummary() {
        // Given
        Object[] dailyData = {LocalDateTime.now().toLocalDate(), 100L, 50L};
        when(inventoryMovementRepository.findDailyMovementSummaryBetween(any(), any()))
                .thenReturn(Collections.singletonList(dailyData));

        // When
        List<Object[]> result = reportService.getDailyMovementSummary(testFilter);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(LocalDateTime.now().toLocalDate(), result.get(0)[0]);
    }

    @Test
    void testGetInventoryMovementReportWithFilters() {
        // Given
        ReportFilterDto filterWithProductIds = new ReportFilterDto();
        filterWithProductIds.setStartDate(LocalDateTime.now().minusDays(30));
        filterWithProductIds.setEndDate(LocalDateTime.now());
        filterWithProductIds.setProductIds(Arrays.asList(1L, 2L));
        filterWithProductIds.setMovementTypes(Arrays.asList(InventoryMovement.MovementType.IN));

        when(inventoryMovementRepository.findByCreatedAtBetween(any(), any()))
                .thenReturn(Arrays.asList(testMovement));

        // When
        List<InventoryMovementReportDto> result =
                reportService.getInventoryMovementReport(filterWithProductIds);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetStockLevelReportWithCategoryFilter() {
        // Given
        ReportFilterDto filterWithCategory = new ReportFilterDto();
        filterWithCategory.setCategoryIds(Arrays.asList(1L));
        filterWithCategory.setActiveProductsOnly(true);

        when(productRepository.findByActiveTrue()).thenReturn(Arrays.asList(testProduct));

        // When
        List<StockLevelReportDto> result = reportService.getStockLevelReport(filterWithCategory);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Category", result.get(0).getCategoryName());
    }

    @Test
    void testGetSupplierPerformanceReportWithSupplierFilter() {
        // Given
        ReportFilterDto filterWithSuppliers = new ReportFilterDto();
        filterWithSuppliers.setStartDate(LocalDateTime.now().minusDays(30));
        filterWithSuppliers.setEndDate(LocalDateTime.now());
        filterWithSuppliers.setSupplierIds(Arrays.asList(1L));

        when(supplierRepository.findAllById(any())).thenReturn(Arrays.asList(testSupplier));
        when(supplierRepository.findSupplierPerformanceBetween(any(), any()))
                .thenReturn(Arrays.asList());

        // When
        List<SupplierPerformanceReportDto> result =
                reportService.getSupplierPerformanceReport(filterWithSuppliers);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Supplier", result.get(0).getSupplierName());
    }

    @Test
    void testGetDetailedInventoryMovementReportWithLimit() {
        // Given
        ReportFilterDto filterWithLimit = new ReportFilterDto();
        filterWithLimit.setStartDate(LocalDateTime.now().minusDays(30));
        filterWithLimit.setEndDate(LocalDateTime.now());
        filterWithLimit.setLimit(1);

        InventoryMovement movement2 =
                new InventoryMovement(testProduct, InventoryMovement.MovementType.OUT, 25);
        movement2.setId(2L);
        movement2.setCreatedAt(LocalDateTime.now());

        when(inventoryMovementRepository.findMovementsBetween(any(), any()))
                .thenReturn(Arrays.asList(testMovement, movement2));

        // When
        List<InventoryMovementReportDto> result =
                reportService.getDetailedInventoryMovementReport(filterWithLimit);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size()); // Limited to 1 result
    }

    @Test
    void testGetStockLevelReportWithInactiveProducts() {
        // Given
        ReportFilterDto filterInactiveProducts = new ReportFilterDto();
        filterInactiveProducts.setActiveProductsOnly(false);

        Product inactiveProduct =
                new Product("INACTIVE001", "Inactive Product", new BigDecimal("5.00"));
        inactiveProduct.setId(2L);
        inactiveProduct.setActive(false);

        when(productRepository.findAll()).thenReturn(Arrays.asList(testProduct, inactiveProduct));

        // When
        List<StockLevelReportDto> result =
                reportService.getStockLevelReport(filterInactiveProducts);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size()); // Should include both active and inactive products
    }

    @Test
    void testGetSupplierPerformanceReportWithPerformanceData() {
        // Given
        Object[] performanceData = {testSupplier, 5, new BigDecimal("500.00"), 4, 1};
        when(supplierRepository.findByActiveTrue()).thenReturn(Arrays.asList(testSupplier));
        when(supplierRepository.findSupplierPerformanceBetween(any(), any()))
                .thenReturn(Collections.singletonList(performanceData));

        // When
        List<SupplierPerformanceReportDto> result =
                reportService.getSupplierPerformanceReport(testFilter);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        SupplierPerformanceReportDto reportDto = result.get(0);
        assertEquals(5, reportDto.getTotalOrders());
        assertEquals(4, reportDto.getCompletedOrders());
        assertEquals(1, reportDto.getCancelledOrders());
    }

    @Test
    void testGetProductMovementSummaryWithInvalidDateRange() {
        // Given
        ReportFilterDto invalidFilter = new ReportFilterDto();
        invalidFilter.setStartDate(null);
        invalidFilter.setEndDate(null);

        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    reportService.getProductMovementSummary(invalidFilter);
                });
    }

    @Test
    void testGetMovementStatisticsByTypeWithInvalidDateRange() {
        // Given
        ReportFilterDto invalidFilter = new ReportFilterDto();
        invalidFilter.setStartDate(null);
        invalidFilter.setEndDate(null);

        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    reportService.getMovementStatisticsByType(invalidFilter);
                });
    }

    @Test
    void testGetDailyMovementSummaryWithInvalidDateRange() {
        // Given
        ReportFilterDto invalidFilter = new ReportFilterDto();
        invalidFilter.setStartDate(null);
        invalidFilter.setEndDate(null);

        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    reportService.getDailyMovementSummary(invalidFilter);
                });
    }
}
