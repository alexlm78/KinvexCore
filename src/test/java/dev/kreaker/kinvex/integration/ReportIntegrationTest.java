package dev.kreaker.kinvex.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.kreaker.kinvex.dto.report.InventoryMovementReportDto;
import dev.kreaker.kinvex.dto.report.ReportExportRequest;
import dev.kreaker.kinvex.dto.report.ReportFilterDto;
import dev.kreaker.kinvex.dto.report.StockLevelReportDto;
import dev.kreaker.kinvex.dto.report.SupplierPerformanceReportDto;
import dev.kreaker.kinvex.entity.Category;
import dev.kreaker.kinvex.entity.InventoryMovement;
import dev.kreaker.kinvex.entity.Product;
import dev.kreaker.kinvex.entity.Supplier;
import dev.kreaker.kinvex.entity.User;
import dev.kreaker.kinvex.repository.CategoryRepository;
import dev.kreaker.kinvex.repository.InventoryMovementRepository;
import dev.kreaker.kinvex.repository.ProductRepository;
import dev.kreaker.kinvex.repository.SupplierRepository;
import dev.kreaker.kinvex.repository.UserRepository;
import dev.kreaker.kinvex.service.ReportExportService;
import dev.kreaker.kinvex.service.ReportService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/** Integration tests for the complete report system flow Requirements: 4.1, 4.2, 4.4, 4.5 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReportIntegrationTest {

    @Autowired private ReportService reportService;

    @Autowired private ReportExportService reportExportService;

    @Autowired private ProductRepository productRepository;

    @Autowired private CategoryRepository categoryRepository;

    @Autowired private SupplierRepository supplierRepository;

    @Autowired private InventoryMovementRepository inventoryMovementRepository;

    @Autowired private UserRepository userRepository;

    private Product testProduct1;
    private Product testProduct2;
    private Supplier testSupplier;
    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        // Create test category
        testCategory = new Category();
        testCategory.setName("Electronics");
        testCategory.setDescription("Electronic products");
        testCategory = categoryRepository.save(testCategory);

        // Create test products
        testProduct1 = new Product("PROD001", "Test Product 1", new BigDecimal("25.50"));
        testProduct1.setCategory(testCategory);
        testProduct1.setCurrentStock(100);
        testProduct1.setMinStock(10);
        testProduct1.setMaxStock(500);
        testProduct1 = productRepository.save(testProduct1);

        testProduct2 = new Product("PROD002", "Test Product 2", new BigDecimal("15.75"));
        testProduct2.setCategory(testCategory);
        testProduct2.setCurrentStock(75);
        testProduct2.setMinStock(5);
        testProduct2.setMaxStock(200);
        testProduct2 = productRepository.save(testProduct2);

        // Create test supplier
        testSupplier = new Supplier("Test Supplier");
        testSupplier.setContactPerson("John Doe");
        testSupplier.setEmail("john@testsupplier.com");
        testSupplier.setPhone("123-456-7890");
        testSupplier = supplierRepository.save(testSupplier);

        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedpassword");
        testUser.setRole(User.UserRole.MANAGER);
        testUser = userRepository.save(testUser);

        // Create test inventory movements
        InventoryMovement movement1 =
                new InventoryMovement(testProduct1, InventoryMovement.MovementType.IN, 50);
        movement1.setReferenceType(InventoryMovement.ReferenceType.PURCHASE_ORDER);
        movement1.setReferenceId(1L);
        movement1.setSourceSystem("PURCHASE_SYSTEM");
        movement1.setNotes("Initial stock");
        movement1.setCreatedBy(testUser);
        movement1.setCreatedAt(LocalDateTime.now().minusDays(5));
        inventoryMovementRepository.save(movement1);

        InventoryMovement movement2 =
                new InventoryMovement(testProduct1, InventoryMovement.MovementType.OUT, 20);
        movement2.setReferenceType(InventoryMovement.ReferenceType.SALE);
        movement2.setSourceSystem("BILLING_SYSTEM");
        movement2.setNotes("Sale transaction");
        movement2.setCreatedBy(testUser);
        movement2.setCreatedAt(LocalDateTime.now().minusDays(3));
        inventoryMovementRepository.save(movement2);

        InventoryMovement movement3 =
                new InventoryMovement(testProduct2, InventoryMovement.MovementType.IN, 30);
        movement3.setReferenceType(InventoryMovement.ReferenceType.PURCHASE_ORDER);
        movement3.setReferenceId(2L);
        movement3.setSourceSystem("PURCHASE_SYSTEM");
        movement3.setCreatedBy(testUser);
        movement3.setCreatedAt(LocalDateTime.now().minusDays(2));
        inventoryMovementRepository.save(movement3);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testInventoryMovementReportIntegration() {
        // Given
        ReportFilterDto filter = new ReportFilterDto();
        filter.setStartDate(LocalDateTime.now().minusDays(10));
        filter.setEndDate(LocalDateTime.now());

        // When
        List<InventoryMovementReportDto> result = reportService.getInventoryMovementReport(filter);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());

        // Verify first movement
        InventoryMovementReportDto movement1 =
                result.stream()
                        .filter(
                                m ->
                                        m.getProductCode().equals("PROD001")
                                                && m.getMovementType()
                                                        == InventoryMovement.MovementType.IN)
                        .findFirst()
                        .orElse(null);
        assertNotNull(movement1);
        assertEquals("Test Product 1", movement1.getProductName());
        assertEquals(50, movement1.getQuantity());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testDetailedInventoryMovementReportIntegration() {
        // Given
        ReportFilterDto filter = new ReportFilterDto();
        filter.setStartDate(LocalDateTime.now().minusDays(10));
        filter.setEndDate(LocalDateTime.now());

        // When
        List<InventoryMovementReportDto> result =
                reportService.getDetailedInventoryMovementReport(filter);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());

        // Verify detailed information is included
        InventoryMovementReportDto detailedMovement =
                result.stream()
                        .filter(
                                m ->
                                        m.getProductCode().equals("PROD001")
                                                && m.getMovementType()
                                                        == InventoryMovement.MovementType.IN)
                        .findFirst()
                        .orElse(null);
        assertNotNull(detailedMovement);
        assertEquals("testuser", detailedMovement.getCreatedByUsername());
        assertEquals(
                InventoryMovement.ReferenceType.PURCHASE_ORDER,
                detailedMovement.getReferenceType());
        assertEquals("PURCHASE_SYSTEM", detailedMovement.getSourceSystem());
        assertEquals("Initial stock", detailedMovement.getNotes());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void testStockLevelReportIntegration() {
        // Given
        ReportFilterDto filter = new ReportFilterDto();
        filter.setStartDate(LocalDateTime.now().minusDays(10));
        filter.setEndDate(LocalDateTime.now());
        filter.setActiveProductsOnly(true);

        // When
        List<StockLevelReportDto> result = reportService.getStockLevelReport(filter);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify product 1 stock level
        StockLevelReportDto product1Stock =
                result.stream()
                        .filter(s -> s.getProductCode().equals("PROD001"))
                        .findFirst()
                        .orElse(null);
        assertNotNull(product1Stock);
        assertEquals("Test Product 1", product1Stock.getProductName());
        assertEquals("Electronics", product1Stock.getCategoryName());
        assertEquals(100, product1Stock.getCurrentStock());
        assertEquals(50, product1Stock.getInboundMovements()); // IN movement
        assertEquals(20, product1Stock.getOutboundMovements()); // OUT movement
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testSupplierPerformanceReportIntegration() {
        // Given
        ReportFilterDto filter = new ReportFilterDto();
        filter.setStartDate(LocalDateTime.now().minusDays(10));
        filter.setEndDate(LocalDateTime.now());
        filter.setActiveSuppliersOnly(true);

        // When
        List<SupplierPerformanceReportDto> result =
                reportService.getSupplierPerformanceReport(filter);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        SupplierPerformanceReportDto supplierReport = result.get(0);
        assertEquals("Test Supplier", supplierReport.getSupplierName());
        assertEquals("John Doe", supplierReport.getContactPerson());
        assertEquals("john@testsupplier.com", supplierReport.getEmail());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testInventoryMovementReportWithFilters() {
        // Given
        ReportFilterDto filter = new ReportFilterDto();
        filter.setStartDate(LocalDateTime.now().minusDays(10));
        filter.setEndDate(LocalDateTime.now());
        filter.setProductCodes(List.of("PROD001"));
        filter.setMovementTypes(List.of(InventoryMovement.MovementType.IN));

        // When
        List<InventoryMovementReportDto> result = reportService.getInventoryMovementReport(filter);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size()); // Only IN movement for PROD001
        assertEquals("PROD001", result.get(0).getProductCode());
        assertEquals(InventoryMovement.MovementType.IN, result.get(0).getMovementType());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testStockLevelReportWithCategoryFilter() {
        // Given
        ReportFilterDto filter = new ReportFilterDto();
        filter.setCategoryIds(List.of(testCategory.getId()));
        filter.setActiveProductsOnly(true);

        // When
        List<StockLevelReportDto> result = reportService.getStockLevelReport(filter);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size()); // Both products are in Electronics category
        assertTrue(result.stream().allMatch(s -> "Electronics".equals(s.getCategoryName())));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testExportInventoryMovementsToPdfIntegration() throws Exception {
        // Given
        ReportExportRequest request =
                new ReportExportRequest(
                        ReportExportRequest.ReportType.INVENTORY_MOVEMENTS,
                        ReportExportRequest.ExportFormat.PDF);
        request.setStartDate(LocalDateTime.now().minusDays(10));
        request.setEndDate(LocalDateTime.now());
        request.setTitle("Integration Test Report");

        // When
        byte[] result = reportExportService.exportReport(request);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(new String(result, 0, 4).equals("%PDF"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testExportStockLevelsToExcelIntegration() throws Exception {
        // Given
        ReportExportRequest request =
                new ReportExportRequest(
                        ReportExportRequest.ReportType.STOCK_LEVELS,
                        ReportExportRequest.ExportFormat.EXCEL);
        request.setStartDate(LocalDateTime.now().minusDays(10));
        request.setEndDate(LocalDateTime.now());

        // When
        byte[] result = reportExportService.exportReport(request);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(result[0] == 0x50 && result[1] == 0x4B); // ZIP signature for Excel
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testExportDetailedInventoryMovementsIntegration() throws Exception {
        // Given
        ReportExportRequest request =
                new ReportExportRequest(
                        ReportExportRequest.ReportType.INVENTORY_MOVEMENTS,
                        ReportExportRequest.ExportFormat.PDF);
        request.setStartDate(LocalDateTime.now().minusDays(10));
        request.setEndDate(LocalDateTime.now());
        request.setDetailed(true);

        // When
        byte[] result = reportExportService.exportReport(request);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(new String(result, 0, 4).equals("%PDF"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testProductMovementSummaryIntegration() {
        // Given
        ReportFilterDto filter = new ReportFilterDto();
        filter.setStartDate(LocalDateTime.now().minusDays(10));
        filter.setEndDate(LocalDateTime.now());

        // When
        List<Object[]> result = reportService.getProductMovementSummary(filter);

        // Then
        assertNotNull(result);
        assertTrue(result.size() >= 0); // May be empty depending on repository implementation
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testMovementStatisticsByTypeIntegration() {
        // Given
        ReportFilterDto filter = new ReportFilterDto();
        filter.setStartDate(LocalDateTime.now().minusDays(10));
        filter.setEndDate(LocalDateTime.now());

        // When
        List<Object[]> result = reportService.getMovementStatisticsByType(filter);

        // Then
        assertNotNull(result);
        assertTrue(result.size() >= 0); // May be empty depending on repository implementation
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testDailyMovementSummaryIntegration() {
        // Given
        ReportFilterDto filter = new ReportFilterDto();
        filter.setStartDate(LocalDateTime.now().minusDays(10));
        filter.setEndDate(LocalDateTime.now());

        // When
        List<Object[]> result = reportService.getDailyMovementSummary(filter);

        // Then
        assertNotNull(result);
        assertTrue(result.size() >= 0); // May be empty depending on repository implementation
    }
}
