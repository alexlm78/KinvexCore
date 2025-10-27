package dev.kreaker.kinvex.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.kreaker.kinvex.dto.report.InventoryMovementReportDto;
import dev.kreaker.kinvex.dto.report.ReportFilterDto;
import dev.kreaker.kinvex.dto.report.StockLevelReportDto;
import dev.kreaker.kinvex.dto.report.SupplierPerformanceReportDto;
import dev.kreaker.kinvex.entity.InventoryMovement.MovementType;
import dev.kreaker.kinvex.entity.InventoryMovement.ReferenceType;
import dev.kreaker.kinvex.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST Controller for report generation and retrieval Requirements: 4.1, 4.2,
 * 4.4
 */
@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Report generation and retrieval endpoints")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Get inventory movement reports Requirement 4.1: Generate reports of
     * inbound inventory by time period Requirement 4.2: Generate reports of
     * outbound inventory by time period
     */
    @GetMapping("/inventory-movements")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Get inventory movement reports",
            description = "Generate reports of inventory movements with filtering options")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved inventory movement reports"),
        @ApiResponse(responseCode = "400", description = "Invalid filter parameters"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<InventoryMovementReportDto>> getInventoryMovements(
            @Parameter(description = "Start date for the report period (ISO format: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date for the report period (ISO format: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Product IDs to filter by")
            @RequestParam(required = false) List<Long> productIds,
            @Parameter(description = "Product codes to filter by")
            @RequestParam(required = false) List<String> productCodes,
            @Parameter(description = "Movement types to filter by (IN, OUT)")
            @RequestParam(required = false) List<MovementType> movementTypes,
            @Parameter(description = "Reference types to filter by")
            @RequestParam(required = false) List<ReferenceType> referenceTypes,
            @Parameter(description = "Source systems to filter by")
            @RequestParam(required = false) List<String> sourceSystems,
            @Parameter(description = "Maximum number of results to return")
            @RequestParam(required = false) Integer limit,
            @Parameter(description = "Include detailed information (user, reference details)")
            @RequestParam(defaultValue = "false") boolean detailed) {

        logger.info("Generating inventory movement report - startDate: {}, endDate: {}, detailed: {}",
                startDate, endDate, detailed);

        try {
            ReportFilterDto filter = buildReportFilter(startDate, endDate, productIds, productCodes,
                    null, null, movementTypes, referenceTypes,
                    sourceSystems, null, null, limit, null, null);

            List<InventoryMovementReportDto> reports;
            if (detailed) {
                reports = reportService.getDetailedInventoryMovementReport(filter);
            } else {
                reports = reportService.getInventoryMovementReport(filter);
            }

            logger.info("Successfully generated {} inventory movement reports", reports.size());
            return ResponseEntity.ok(reports);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for inventory movement report: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error generating inventory movement report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get stock level reports Requirement 4.2: Generate reports of stock levels
     * by time period
     */
    @GetMapping("/stock-levels")
    @PreAuthorize("hasAnyRole('VIEWER', 'OPERATOR', 'MANAGER', 'ADMIN')")
    @Operation(summary = "Get stock level reports",
            description = "Generate reports of current stock levels with optional movement data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved stock level reports"),
        @ApiResponse(responseCode = "400", description = "Invalid filter parameters"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<StockLevelReportDto>> getStockLevels(
            @Parameter(description = "Start date for movement analysis (ISO format: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date for movement analysis (ISO format: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Product IDs to filter by")
            @RequestParam(required = false) List<Long> productIds,
            @Parameter(description = "Product codes to filter by")
            @RequestParam(required = false) List<String> productCodes,
            @Parameter(description = "Category IDs to filter by")
            @RequestParam(required = false) List<Long> categoryIds,
            @Parameter(description = "Include only active products")
            @RequestParam(defaultValue = "true") Boolean activeProductsOnly) {

        logger.info("Generating stock level report - startDate: {}, endDate: {}, activeOnly: {}",
                startDate, endDate, activeProductsOnly);

        try {
            ReportFilterDto filter = buildReportFilter(startDate, endDate, productIds, productCodes,
                    null, categoryIds, null, null, null,
                    activeProductsOnly, null, null, null, null);

            List<StockLevelReportDto> reports = reportService.getStockLevelReport(filter);

            logger.info("Successfully generated {} stock level reports", reports.size());
            return ResponseEntity.ok(reports);

        } catch (Exception e) {
            logger.error("Error generating stock level report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get supplier performance reports Requirement 4.3: Show supplier
     * information for each movement Requirement 4.4: Allow filtering reports by
     * supplier
     */
    @GetMapping("/supplier-performance")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Get supplier performance reports",
            description = "Generate reports of supplier performance metrics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved supplier performance reports"),
        @ApiResponse(responseCode = "400", description = "Invalid filter parameters"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<SupplierPerformanceReportDto>> getSupplierPerformance(
            @Parameter(description = "Start date for the report period (ISO format: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date for the report period (ISO format: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Supplier IDs to filter by")
            @RequestParam(required = false) List<Long> supplierIds,
            @Parameter(description = "Include only active suppliers")
            @RequestParam(defaultValue = "true") Boolean activeSuppliersOnly) {

        logger.info("Generating supplier performance report - startDate: {}, endDate: {}, activeOnly: {}",
                startDate, endDate, activeSuppliersOnly);

        try {
            ReportFilterDto filter = buildReportFilter(startDate, endDate, null, null,
                    supplierIds, null, null, null, null,
                    null, activeSuppliersOnly, null, null, null);

            List<SupplierPerformanceReportDto> reports = reportService.getSupplierPerformanceReport(filter);

            logger.info("Successfully generated {} supplier performance reports", reports.size());
            return ResponseEntity.ok(reports);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for supplier performance report: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error generating supplier performance report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get product movement summary Requirement 4.4: Allow filtering reports by
     * product, supplier, or date ranges
     */
    @GetMapping("/product-movement-summary")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Get product movement summary",
            description = "Generate summary of product movements for analysis")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved product movement summary"),
        @ApiResponse(responseCode = "400", description = "Invalid filter parameters"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<Object[]>> getProductMovementSummary(
            @Parameter(description = "Start date for the report period (ISO format: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date for the report period (ISO format: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        logger.info("Generating product movement summary - startDate: {}, endDate: {}", startDate, endDate);

        try {
            ReportFilterDto filter = buildReportFilter(startDate, endDate, null, null,
                    null, null, null, null, null,
                    null, null, null, null, null);

            List<Object[]> summary = reportService.getProductMovementSummary(filter);

            logger.info("Successfully generated product movement summary with {} entries", summary.size());
            return ResponseEntity.ok(summary);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for product movement summary: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error generating product movement summary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get movement statistics by type Requirement 4.1: Generate reports of
     * inventory movements by time period
     */
    @GetMapping("/movement-statistics")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Get movement statistics by type",
            description = "Generate statistics of inventory movements grouped by type and reference")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved movement statistics"),
        @ApiResponse(responseCode = "400", description = "Invalid filter parameters"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<Object[]>> getMovementStatistics(
            @Parameter(description = "Start date for the report period (ISO format: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date for the report period (ISO format: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        logger.info("Generating movement statistics - startDate: {}, endDate: {}", startDate, endDate);

        try {
            ReportFilterDto filter = buildReportFilter(startDate, endDate, null, null,
                    null, null, null, null, null,
                    null, null, null, null, null);

            List<Object[]> statistics = reportService.getMovementStatisticsByType(filter);

            logger.info("Successfully generated movement statistics with {} entries", statistics.size());
            return ResponseEntity.ok(statistics);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for movement statistics: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error generating movement statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get daily movement summary Requirement 4.2: Generate reports of inventory
     * movements by time period
     */
    @GetMapping("/daily-movement-summary")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Get daily movement summary",
            description = "Generate daily summary of inventory movements")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved daily movement summary"),
        @ApiResponse(responseCode = "400", description = "Invalid filter parameters"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<Object[]>> getDailyMovementSummary(
            @Parameter(description = "Start date for the report period (ISO format: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date for the report period (ISO format: yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        logger.info("Generating daily movement summary - startDate: {}, endDate: {}", startDate, endDate);

        try {
            ReportFilterDto filter = buildReportFilter(startDate, endDate, null, null,
                    null, null, null, null, null,
                    null, null, null, null, null);

            List<Object[]> summary = reportService.getDailyMovementSummary(filter);

            logger.info("Successfully generated daily movement summary with {} entries", summary.size());
            return ResponseEntity.ok(summary);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for daily movement summary: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error generating daily movement summary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Helper method to build ReportFilterDto from request parameters
     */
    private ReportFilterDto buildReportFilter(
            LocalDateTime startDate, LocalDateTime endDate,
            List<Long> productIds, List<String> productCodes,
            List<Long> supplierIds, List<Long> categoryIds,
            List<MovementType> movementTypes, List<ReferenceType> referenceTypes,
            List<String> sourceSystems,
            Boolean activeProductsOnly, Boolean activeSuppliersOnly,
            Integer limit, String sortBy, String sortDirection) {

        ReportFilterDto filter = new ReportFilterDto();

        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setProductIds(productIds);
        filter.setProductCodes(productCodes);
        filter.setSupplierIds(supplierIds);
        filter.setCategoryIds(categoryIds);
        filter.setMovementTypes(movementTypes);
        filter.setReferenceTypes(referenceTypes);
        filter.setSourceSystems(sourceSystems);
        filter.setActiveProductsOnly(activeProductsOnly);
        filter.setActiveSuppliersOnly(activeSuppliersOnly);
        filter.setLimit(limit);
        filter.setSortBy(sortBy);
        filter.setSortDirection(sortDirection);

        return filter;
    }
}
