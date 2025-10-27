package dev.kreaker.kinvex.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import dev.kreaker.kinvex.dto.report.InventoryMovementReportDto;
import dev.kreaker.kinvex.dto.report.ReportExportRequest;
import dev.kreaker.kinvex.dto.report.StockLevelReportDto;
import dev.kreaker.kinvex.dto.report.SupplierPerformanceReportDto;
import dev.kreaker.kinvex.entity.InventoryMovement.MovementType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for ReportExportService Requirement 4.5: Export reports in PDF and Excel formats */
@ExtendWith(MockitoExtension.class)
class ReportExportServiceTest {

    @Mock private ReportService reportService;

    @InjectMocks private ReportExportService reportExportService;

    private List<InventoryMovementReportDto> sampleMovements;
    private List<StockLevelReportDto> sampleStockLevels;
    private List<SupplierPerformanceReportDto> sampleSuppliers;

    @BeforeEach
    void setUp() {
        // Sample inventory movements
        sampleMovements =
                Arrays.asList(
                        new InventoryMovementReportDto(
                                1L,
                                "PROD001",
                                "Test Product 1",
                                MovementType.IN,
                                10,
                                LocalDateTime.now(),
                                BigDecimal.valueOf(25.50)),
                        new InventoryMovementReportDto(
                                2L,
                                "PROD002",
                                "Test Product 2",
                                MovementType.OUT,
                                5,
                                LocalDateTime.now(),
                                BigDecimal.valueOf(15.75)));

        // Sample stock levels
        sampleStockLevels =
                Arrays.asList(
                        new StockLevelReportDto(
                                1L,
                                "PROD001",
                                "Test Product 1",
                                "Electronics",
                                100,
                                10,
                                500,
                                BigDecimal.valueOf(25.50),
                                50,
                                20,
                                LocalDateTime.now()),
                        new StockLevelReportDto(
                                2L,
                                "PROD002",
                                "Test Product 2",
                                "Books",
                                75,
                                5,
                                200,
                                BigDecimal.valueOf(15.75),
                                30,
                                15,
                                LocalDateTime.now()));

        // Sample suppliers
        sampleSuppliers =
                Arrays.asList(
                        new SupplierPerformanceReportDto(
                                1L,
                                "Supplier A",
                                "John Doe",
                                "john@supplier-a.com",
                                "123-456-7890",
                                10,
                                8,
                                1,
                                1,
                                BigDecimal.valueOf(1000.00),
                                BigDecimal.valueOf(100.00),
                                5.0,
                                LocalDateTime.now()),
                        new SupplierPerformanceReportDto(
                                2L,
                                "Supplier B",
                                "Jane Smith",
                                "jane@supplier-b.com",
                                "098-765-4321",
                                15,
                                12,
                                2,
                                1,
                                BigDecimal.valueOf(2000.00),
                                BigDecimal.valueOf(133.33),
                                3.0,
                                LocalDateTime.now()));
    }

    @Test
    void testExportInventoryMovementsToPdf() throws Exception {
        // Arrange
        ReportExportRequest request =
                new ReportExportRequest(
                        ReportExportRequest.ReportType.INVENTORY_MOVEMENTS,
                        ReportExportRequest.ExportFormat.PDF);
        request.setStartDate(LocalDateTime.now().minusDays(30));
        request.setEndDate(LocalDateTime.now());
        request.setTitle("Test Inventory Movements Report");

        when(reportService.getInventoryMovementReport(any())).thenReturn(sampleMovements);

        // Act
        byte[] result = reportExportService.exportReport(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        // PDF files start with %PDF
        assertTrue(new String(result, 0, 4).equals("%PDF"));
    }

    @Test
    void testExportInventoryMovementsToExcel() throws Exception {
        // Arrange
        ReportExportRequest request =
                new ReportExportRequest(
                        ReportExportRequest.ReportType.INVENTORY_MOVEMENTS,
                        ReportExportRequest.ExportFormat.EXCEL);
        request.setStartDate(LocalDateTime.now().minusDays(30));
        request.setEndDate(LocalDateTime.now());

        when(reportService.getInventoryMovementReport(any())).thenReturn(sampleMovements);

        // Act
        byte[] result = reportExportService.exportReport(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        // Excel files start with PK (ZIP signature)
        assertTrue(result[0] == 0x50 && result[1] == 0x4B);
    }

    @Test
    void testExportStockLevelsToPdf() throws Exception {
        // Arrange
        ReportExportRequest request =
                new ReportExportRequest(
                        ReportExportRequest.ReportType.STOCK_LEVELS,
                        ReportExportRequest.ExportFormat.PDF);
        request.setTitle("Test Stock Levels Report");

        when(reportService.getStockLevelReport(any())).thenReturn(sampleStockLevels);

        // Act
        byte[] result = reportExportService.exportReport(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(new String(result, 0, 4).equals("%PDF"));
    }

    @Test
    void testExportStockLevelsToExcel() throws Exception {
        // Arrange
        ReportExportRequest request =
                new ReportExportRequest(
                        ReportExportRequest.ReportType.STOCK_LEVELS,
                        ReportExportRequest.ExportFormat.EXCEL);

        when(reportService.getStockLevelReport(any())).thenReturn(sampleStockLevels);

        // Act
        byte[] result = reportExportService.exportReport(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(result[0] == 0x50 && result[1] == 0x4B);
    }

    @Test
    void testExportSupplierPerformanceToPdf() throws Exception {
        // Arrange
        ReportExportRequest request =
                new ReportExportRequest(
                        ReportExportRequest.ReportType.SUPPLIER_PERFORMANCE,
                        ReportExportRequest.ExportFormat.PDF);
        request.setStartDate(LocalDateTime.now().minusDays(30));
        request.setEndDate(LocalDateTime.now());
        request.setTitle("Test Supplier Performance Report");

        when(reportService.getSupplierPerformanceReport(any())).thenReturn(sampleSuppliers);

        // Act
        byte[] result = reportExportService.exportReport(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(new String(result, 0, 4).equals("%PDF"));
    }

    @Test
    void testExportSupplierPerformanceToExcel() throws Exception {
        // Arrange
        ReportExportRequest request =
                new ReportExportRequest(
                        ReportExportRequest.ReportType.SUPPLIER_PERFORMANCE,
                        ReportExportRequest.ExportFormat.EXCEL);
        request.setStartDate(LocalDateTime.now().minusDays(30));
        request.setEndDate(LocalDateTime.now());

        when(reportService.getSupplierPerformanceReport(any())).thenReturn(sampleSuppliers);

        // Act
        byte[] result = reportExportService.exportReport(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(result[0] == 0x50 && result[1] == 0x4B);
    }

    @Test
    void testGenerateFilename() {
        // Arrange
        ReportExportRequest pdfRequest =
                new ReportExportRequest(
                        ReportExportRequest.ReportType.INVENTORY_MOVEMENTS,
                        ReportExportRequest.ExportFormat.PDF);

        ReportExportRequest excelRequest =
                new ReportExportRequest(
                        ReportExportRequest.ReportType.STOCK_LEVELS,
                        ReportExportRequest.ExportFormat.EXCEL);

        // Act
        String pdfFilename = reportExportService.generateFilename(pdfRequest);
        String excelFilename = reportExportService.generateFilename(excelRequest);

        // Assert
        assertNotNull(pdfFilename);
        assertTrue(pdfFilename.contains("Movimientos_Inventario"));
        assertTrue(pdfFilename.endsWith(".pdf"));

        assertNotNull(excelFilename);
        assertTrue(excelFilename.contains("Niveles_Stock"));
        assertTrue(excelFilename.endsWith(".xlsx"));
    }

    @Test
    void testExportDetailedInventoryMovements() throws Exception {
        // Arrange
        ReportExportRequest request =
                new ReportExportRequest(
                        ReportExportRequest.ReportType.INVENTORY_MOVEMENTS,
                        ReportExportRequest.ExportFormat.PDF);
        request.setDetailed(true);
        request.setStartDate(LocalDateTime.now().minusDays(30));
        request.setEndDate(LocalDateTime.now());

        when(reportService.getDetailedInventoryMovementReport(any())).thenReturn(sampleMovements);

        // Act
        byte[] result = reportExportService.exportReport(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(new String(result, 0, 4).equals("%PDF"));
    }

    @Test
    void testExportEmptyInventoryMovementsToPdf() throws Exception {
        // Arrange
        ReportExportRequest request =
                new ReportExportRequest(
                        ReportExportRequest.ReportType.INVENTORY_MOVEMENTS,
                        ReportExportRequest.ExportFormat.PDF);
        request.setStartDate(LocalDateTime.now().minusDays(30));
        request.setEndDate(LocalDateTime.now());

        when(reportService.getInventoryMovementReport(any())).thenReturn(Arrays.asList());

        // Act
        byte[] result = reportExportService.exportReport(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(new String(result, 0, 4).equals("%PDF"));
    }

    @Test
    void testExportEmptyStockLevelsToExcel() throws Exception {
        // Arrange
        ReportExportRequest request =
                new ReportExportRequest(
                        ReportExportRequest.ReportType.STOCK_LEVELS,
                        ReportExportRequest.ExportFormat.EXCEL);

        when(reportService.getStockLevelReport(any())).thenReturn(Arrays.asList());

        // Act
        byte[] result = reportExportService.exportReport(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(result[0] == 0x50 && result[1] == 0x4B);
    }

    @Test
    void testExportEmptySupplierPerformanceToPdf() throws Exception {
        // Arrange
        ReportExportRequest request =
                new ReportExportRequest(
                        ReportExportRequest.ReportType.SUPPLIER_PERFORMANCE,
                        ReportExportRequest.ExportFormat.PDF);
        request.setStartDate(LocalDateTime.now().minusDays(30));
        request.setEndDate(LocalDateTime.now());

        when(reportService.getSupplierPerformanceReport(any())).thenReturn(Arrays.asList());

        // Act
        byte[] result = reportExportService.exportReport(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(new String(result, 0, 4).equals("%PDF"));
    }

    @Test
    void testExportWithCustomTitle() throws Exception {
        // Arrange
        ReportExportRequest request =
                new ReportExportRequest(
                        ReportExportRequest.ReportType.INVENTORY_MOVEMENTS,
                        ReportExportRequest.ExportFormat.PDF);
        request.setTitle("Custom Report Title");
        request.setStartDate(LocalDateTime.now().minusDays(30));
        request.setEndDate(LocalDateTime.now());

        when(reportService.getInventoryMovementReport(any())).thenReturn(sampleMovements);

        // Act
        byte[] result = reportExportService.exportReport(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(new String(result, 0, 4).equals("%PDF"));
    }

    @Test
    void testExportWithDateRangeInFilename() {
        // Arrange
        ReportExportRequest pdfRequest =
                new ReportExportRequest(
                        ReportExportRequest.ReportType.INVENTORY_MOVEMENTS,
                        ReportExportRequest.ExportFormat.PDF);
        pdfRequest.setStartDate(LocalDateTime.of(2024, 1, 1, 0, 0));
        pdfRequest.setEndDate(LocalDateTime.of(2024, 1, 31, 23, 59));

        ReportExportRequest excelRequest =
                new ReportExportRequest(
                        ReportExportRequest.ReportType.SUPPLIER_PERFORMANCE,
                        ReportExportRequest.ExportFormat.EXCEL);
        excelRequest.setStartDate(LocalDateTime.of(2024, 2, 1, 0, 0));
        excelRequest.setEndDate(LocalDateTime.of(2024, 2, 29, 23, 59));

        // Act
        String pdfFilename = reportExportService.generateFilename(pdfRequest);
        String excelFilename = reportExportService.generateFilename(excelRequest);

        // Assert
        assertNotNull(pdfFilename);
        assertTrue(pdfFilename.contains("Movimientos_Inventario"));
        assertTrue(pdfFilename.endsWith(".pdf"));
        assertTrue(pdfFilename.matches(".*\\d{8}_\\d{4}\\.pdf"));

        assertNotNull(excelFilename);
        assertTrue(excelFilename.contains("Desempeño_Proveedores"));
        assertTrue(excelFilename.endsWith(".xlsx"));
        assertTrue(excelFilename.matches(".*\\d{8}_\\d{4}\\.xlsx"));
    }

    @Test
    void testExportLargeDatasetToPdf() throws Exception {
        // Arrange - Create a large dataset
        List<InventoryMovementReportDto> largeMovements = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            largeMovements.add(
                    new InventoryMovementReportDto(
                            (long) i,
                            "PROD" + String.format("%03d", i),
                            "Test Product " + i,
                            i % 2 == 0 ? MovementType.IN : MovementType.OUT,
                            i * 10,
                            LocalDateTime.now().minusDays(i % 30),
                            BigDecimal.valueOf(i * 1.5)));
        }

        ReportExportRequest request =
                new ReportExportRequest(
                        ReportExportRequest.ReportType.INVENTORY_MOVEMENTS,
                        ReportExportRequest.ExportFormat.PDF);
        request.setStartDate(LocalDateTime.now().minusDays(30));
        request.setEndDate(LocalDateTime.now());

        when(reportService.getInventoryMovementReport(any())).thenReturn(largeMovements);

        // Act
        byte[] result = reportExportService.exportReport(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(new String(result, 0, 4).equals("%PDF"));
    }

    @Test
    void testExportLargeDatasetToExcel() throws Exception {
        // Arrange - Create a large dataset
        List<StockLevelReportDto> largeStockLevels = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            largeStockLevels.add(
                    new StockLevelReportDto(
                            (long) i,
                            "PROD" + String.format("%03d", i),
                            "Test Product " + i,
                            "Category " + (i % 10),
                            i * 100,
                            i * 10,
                            i * 500,
                            BigDecimal.valueOf(i * 1.5),
                            i * 50,
                            i * 20,
                            LocalDateTime.now()));
        }

        ReportExportRequest request =
                new ReportExportRequest(
                        ReportExportRequest.ReportType.STOCK_LEVELS,
                        ReportExportRequest.ExportFormat.EXCEL);

        when(reportService.getStockLevelReport(any())).thenReturn(largeStockLevels);

        // Act
        byte[] result = reportExportService.exportReport(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(result[0] == 0x50 && result[1] == 0x4B);
    }
}
