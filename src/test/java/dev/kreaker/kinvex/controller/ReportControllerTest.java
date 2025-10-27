package dev.kreaker.kinvex.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kreaker.kinvex.dto.report.InventoryMovementReportDto;
import dev.kreaker.kinvex.dto.report.ReportExportRequest;
import dev.kreaker.kinvex.dto.report.StockLevelReportDto;
import dev.kreaker.kinvex.dto.report.SupplierPerformanceReportDto;
import dev.kreaker.kinvex.entity.InventoryMovement.MovementType;
import dev.kreaker.kinvex.service.ReportExportService;
import dev.kreaker.kinvex.service.ReportService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/** Unit tests for ReportController Requirements: 4.1, 4.2, 4.4, 4.5 */
@WebMvcTest(ReportController.class)
@EnableMethodSecurity(prePostEnabled = true)
class ReportControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private ReportService reportService;

    @MockBean private ReportExportService reportExportService;

    @MockBean private dev.kreaker.kinvex.security.JwtTokenProvider jwtTokenProvider;

    @Autowired private ObjectMapper objectMapper;

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
    @WithMockUser(roles = "MANAGER")
    void testGetInventoryMovements() throws Exception {
        // Given
        when(reportService.getInventoryMovementReport(any())).thenReturn(sampleMovements);

        // When & Then
        mockMvc.perform(
                        get("/api/reports/inventory-movements")
                                .param("startDate", "2024-01-01T00:00:00")
                                .param("endDate", "2024-01-31T23:59:59")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].productCode", is("PROD001")))
                .andExpect(jsonPath("$[0].productName", is("Test Product 1")))
                .andExpect(jsonPath("$[0].movementType", is("IN")))
                .andExpect(jsonPath("$[0].quantity", is(10)));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetDetailedInventoryMovements() throws Exception {
        // Given
        when(reportService.getDetailedInventoryMovementReport(any())).thenReturn(sampleMovements);

        // When & Then
        mockMvc.perform(
                        get("/api/reports/inventory-movements")
                                .param("startDate", "2024-01-01T00:00:00")
                                .param("endDate", "2024-01-31T23:59:59")
                                .param("detailed", "true")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].productCode", is("PROD001")));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void testGetStockLevels() throws Exception {
        // Given
        when(reportService.getStockLevelReport(any())).thenReturn(sampleStockLevels);

        // When & Then
        mockMvc.perform(get("/api/reports/stock-levels").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].productCode", is("PROD001")))
                .andExpect(jsonPath("$[0].productName", is("Test Product 1")))
                .andExpect(jsonPath("$[0].currentStock", is(100)))
                .andExpect(jsonPath("$[0].categoryName", is("Electronics")));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetSupplierPerformance() throws Exception {
        // Given
        when(reportService.getSupplierPerformanceReport(any())).thenReturn(sampleSuppliers);

        // When & Then
        mockMvc.perform(
                        get("/api/reports/supplier-performance")
                                .param("startDate", "2024-01-01T00:00:00")
                                .param("endDate", "2024-01-31T23:59:59")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].supplierName", is("Supplier A")))
                .andExpect(jsonPath("$[0].contactPerson", is("John Doe")))
                .andExpect(jsonPath("$[0].totalOrders", is(10)))
                .andExpect(jsonPath("$[0].completedOrders", is(8)));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetProductMovementSummary() throws Exception {
        // Given
        Object[] summaryData = {"PROD001", "Test Product 1", 100L, 50L, 50L};
        when(reportService.getProductMovementSummary(any()))
                .thenReturn(Collections.singletonList(summaryData));

        // When & Then
        mockMvc.perform(
                        get("/api/reports/product-movement-summary")
                                .param("startDate", "2024-01-01T00:00:00")
                                .param("endDate", "2024-01-31T23:59:59")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetMovementStatistics() throws Exception {
        // Given
        Object[] statsData = {MovementType.IN, "PURCHASE_ORDER", 5L, 250L};
        when(reportService.getMovementStatisticsByType(any()))
                .thenReturn(Collections.singletonList(statsData));

        // When & Then
        mockMvc.perform(
                        get("/api/reports/movement-statistics")
                                .param("startDate", "2024-01-01T00:00:00")
                                .param("endDate", "2024-01-31T23:59:59")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetDailyMovementSummary() throws Exception {
        // Given
        Object[] dailyData = {"2024-01-15", 100L, 50L};
        when(reportService.getDailyMovementSummary(any()))
                .thenReturn(Collections.singletonList(dailyData));

        // When & Then
        mockMvc.perform(
                        get("/api/reports/daily-movement-summary")
                                .param("startDate", "2024-01-01T00:00:00")
                                .param("endDate", "2024-01-31T23:59:59")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testExportReportPdf() throws Exception {
        // Given
        ReportExportRequest request =
                new ReportExportRequest(
                        ReportExportRequest.ReportType.INVENTORY_MOVEMENTS,
                        ReportExportRequest.ExportFormat.PDF);
        request.setStartDate(LocalDateTime.of(2024, 1, 1, 0, 0));
        request.setEndDate(LocalDateTime.of(2024, 1, 31, 23, 59));

        byte[] pdfData = "%PDF-1.4 test content".getBytes();
        when(reportExportService.exportReport(any())).thenReturn(pdfData);
        when(reportExportService.generateFilename(any()))
                .thenReturn("inventory_movements_20240115_1030.pdf");

        // When & Then
        mockMvc.perform(
                        post("/api/reports/export")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(
                        header().string(
                                        "Content-Disposition",
                                        "form-data; name=\"attachment\"; filename=\"inventory_movements_20240115_1030.pdf\""))
                .andExpect(content().bytes(pdfData));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testExportReportExcel() throws Exception {
        // Given
        ReportExportRequest request =
                new ReportExportRequest(
                        ReportExportRequest.ReportType.STOCK_LEVELS,
                        ReportExportRequest.ExportFormat.EXCEL);

        byte[] excelData = new byte[] {0x50, 0x4B, 0x03, 0x04}; // ZIP signature for Excel
        when(reportExportService.exportReport(any())).thenReturn(excelData);
        when(reportExportService.generateFilename(any()))
                .thenReturn("stock_levels_20240115_1030.xlsx");

        // When & Then
        mockMvc.perform(
                        post("/api/reports/export")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(
                        content()
                                .contentType(
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(
                        header().string(
                                        "Content-Disposition",
                                        "form-data; name=\"attachment\"; filename=\"stock_levels_20240115_1030.xlsx\""))
                .andExpect(content().bytes(excelData));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void testInventoryMovementsAccessDenied() throws Exception {
        // When & Then - VIEWER role should not have access to inventory movements
        mockMvc.perform(
                        get("/api/reports/inventory-movements")
                                .param("startDate", "2024-01-01T00:00:00")
                                .param("endDate", "2024-01-31T23:59:59")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void testExportAccessDenied() throws Exception {
        // Given
        ReportExportRequest request =
                new ReportExportRequest(
                        ReportExportRequest.ReportType.INVENTORY_MOVEMENTS,
                        ReportExportRequest.ExportFormat.PDF);

        // When & Then - VIEWER role should not have access to export
        mockMvc.perform(
                        post("/api/reports/export")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testExportReportWithInvalidRequest() throws Exception {
        // Given - request without required fields
        ReportExportRequest request = new ReportExportRequest();
        // Missing reportType and format

        // When & Then
        mockMvc.perform(
                        post("/api/reports/export")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testInventoryMovementsWithFilters() throws Exception {
        // Given
        when(reportService.getInventoryMovementReport(any())).thenReturn(sampleMovements);

        // When & Then
        mockMvc.perform(
                        get("/api/reports/inventory-movements")
                                .param("startDate", "2024-01-01T00:00:00")
                                .param("endDate", "2024-01-31T23:59:59")
                                .param("productIds", "1", "2")
                                .param("productCodes", "PROD001", "PROD002")
                                .param("movementTypes", "IN", "OUT")
                                .param("sourceSystems", "BILLING_SYSTEM")
                                .param("limit", "100")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testSupplierPerformanceWithFilters() throws Exception {
        // Given
        when(reportService.getSupplierPerformanceReport(any())).thenReturn(sampleSuppliers);

        // When & Then
        mockMvc.perform(
                        get("/api/reports/supplier-performance")
                                .param("startDate", "2024-01-01T00:00:00")
                                .param("endDate", "2024-01-31T23:59:59")
                                .param("supplierIds", "1", "2")
                                .param("activeSuppliersOnly", "true")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void testStockLevelsWithFilters() throws Exception {
        // Given
        when(reportService.getStockLevelReport(any())).thenReturn(sampleStockLevels);

        // When & Then
        mockMvc.perform(
                        get("/api/reports/stock-levels")
                                .param("startDate", "2024-01-01T00:00:00")
                                .param("endDate", "2024-01-31T23:59:59")
                                .param("productIds", "1")
                                .param("categoryIds", "1")
                                .param("activeProductsOnly", "true")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testUnauthenticatedAccess() throws Exception {
        // When & Then - No authentication should result in 401
        mockMvc.perform(
                        get("/api/reports/inventory-movements")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testInventoryMovementsServiceException() throws Exception {
        // Given
        when(reportService.getInventoryMovementReport(any()))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(
                        get("/api/reports/inventory-movements")
                                .param("startDate", "2024-01-01T00:00:00")
                                .param("endDate", "2024-01-31T23:59:59")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testExportServiceException() throws Exception {
        // Given
        ReportExportRequest request =
                new ReportExportRequest(
                        ReportExportRequest.ReportType.INVENTORY_MOVEMENTS,
                        ReportExportRequest.ExportFormat.PDF);
        request.setStartDate(LocalDateTime.of(2024, 1, 1, 0, 0));
        request.setEndDate(LocalDateTime.of(2024, 1, 31, 23, 59));

        when(reportExportService.exportReport(any()))
                .thenThrow(new RuntimeException("Export error"));

        // When & Then
        mockMvc.perform(
                        post("/api/reports/export")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }
}
