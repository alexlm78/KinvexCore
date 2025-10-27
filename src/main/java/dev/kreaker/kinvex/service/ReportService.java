package dev.kreaker.kinvex.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.kreaker.kinvex.dto.report.InventoryMovementReportDto;
import dev.kreaker.kinvex.dto.report.ReportFilterDto;
import dev.kreaker.kinvex.dto.report.StockLevelReportDto;
import dev.kreaker.kinvex.dto.report.SupplierPerformanceReportDto;
import dev.kreaker.kinvex.entity.InventoryMovement;
import dev.kreaker.kinvex.entity.Product;
import dev.kreaker.kinvex.entity.Supplier;
import dev.kreaker.kinvex.repository.InventoryMovementRepository;
import dev.kreaker.kinvex.repository.ProductRepository;
import dev.kreaker.kinvex.repository.SupplierRepository;

/**
 * Service for generating various inventory and supplier reports Requirements:
 * 4.1, 4.2, 4.3, 4.4
 */
@Service
@Transactional(readOnly = true)
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    private final InventoryMovementRepository inventoryMovementRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;

    public ReportService(
            InventoryMovementRepository inventoryMovementRepository,
            ProductRepository productRepository,
            SupplierRepository supplierRepository) {
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
    }

    /**
     * Generate inventory movement reports Requirement 4.1: Generate reports of
     * inbound inventory by time period Requirement 4.2: Generate reports of
     * outbound inventory by time period
     */
    public List<InventoryMovementReportDto> getInventoryMovementReport(ReportFilterDto filter) {
        logger.info("Generating inventory movement report with filter: {}", filter);

        if (!filter.isValidDateRange()) {
            throw new IllegalArgumentException("Valid date range is required for inventory movement reports");
        }

        List<InventoryMovement> movements = inventoryMovementRepository
                .findByCreatedAtBetween(filter.getStartDate(), filter.getEndDate());

        return movements.stream()
                .filter(movement -> applyMovementFilters(movement, filter))
                .map(this::convertToInventoryMovementReportDto)
                .collect(Collectors.toList());
    }

    /**
     * Generate detailed inventory movement report with product and user
     * information Requirement 4.3: Show origin information for each movement
     */
    public List<InventoryMovementReportDto> getDetailedInventoryMovementReport(ReportFilterDto filter) {
        logger.info("Generating detailed inventory movement report with filter: {}", filter);

        if (!filter.isValidDateRange()) {
            throw new IllegalArgumentException("Valid date range is required for detailed inventory movement reports");
        }

        List<InventoryMovement> movements = inventoryMovementRepository
                .findMovementsBetween(filter.getStartDate(), filter.getEndDate());

        List<InventoryMovementReportDto> reportData = movements.stream()
                .filter(movement -> applyMovementFilters(movement, filter))
                .map(this::convertToDetailedInventoryMovementReportDto)
                .collect(Collectors.toList());

        // Apply limit if specified
        if (filter.getLimit() != null && filter.getLimit() > 0) {
            return reportData.stream()
                    .limit(filter.getLimit())
                    .collect(Collectors.toList());
        }

        return reportData;
    }

    /**
     * Generate stock level reports Requirement 4.2: Generate reports of stock
     * levels by time period
     */
    public List<StockLevelReportDto> getStockLevelReport(ReportFilterDto filter) {
        logger.info("Generating stock level report with filter: {}", filter);

        List<Product> products;
        if (filter.getActiveProductsOnly() != null && filter.getActiveProductsOnly()) {
            products = productRepository.findByActiveTrue();
        } else {
            products = productRepository.findAll();
        }

        return products.stream()
                .filter(product -> applyProductFilters(product, filter))
                .map(product -> convertToStockLevelReportDto(product, filter))
                .collect(Collectors.toList());
    }

    /**
     * Generate supplier performance reports Requirement 4.3: Show supplier
     * information for each movement Requirement 4.4: Allow filtering reports by
     * supplier
     */
    public List<SupplierPerformanceReportDto> getSupplierPerformanceReport(ReportFilterDto filter) {
        logger.info("Generating supplier performance report with filter: {}", filter);

        if (!filter.isValidDateRange()) {
            throw new IllegalArgumentException("Valid date range is required for supplier performance reports");
        }

        List<Supplier> suppliers;
        if (filter.hasSupplierFilter()) {
            suppliers = supplierRepository.findAllById(filter.getSupplierIds());
        } else if (filter.getActiveSuppliersOnly() != null && filter.getActiveSuppliersOnly()) {
            suppliers = supplierRepository.findByActiveTrue();
        } else {
            suppliers = supplierRepository.findAll();
        }

        return suppliers.stream()
                .map(supplier -> convertToSupplierPerformanceReportDto(supplier, filter))
                .collect(Collectors.toList());
    }

    /**
     * Get product movement summary for a specific time period Requirement 4.4:
     * Allow filtering reports by product, supplier, or date ranges
     */
    public List<Object[]> getProductMovementSummary(ReportFilterDto filter) {
        logger.info("Generating product movement summary with filter: {}", filter);

        if (!filter.isValidDateRange()) {
            throw new IllegalArgumentException("Valid date range is required for product movement summary");
        }

        return inventoryMovementRepository
                .findProductMovementSummaryBetween(filter.getStartDate(), filter.getEndDate());
    }

    /**
     * Get movement statistics by type and reference
     */
    public List<Object[]> getMovementStatisticsByType(ReportFilterDto filter) {
        logger.info("Generating movement statistics by type with filter: {}", filter);

        if (!filter.isValidDateRange()) {
            throw new IllegalArgumentException("Valid date range is required for movement statistics");
        }

        return inventoryMovementRepository
                .findMovementStatisticsByTypeBetween(filter.getStartDate(), filter.getEndDate());
    }

    /**
     * Get daily movement summary
     */
    public List<Object[]> getDailyMovementSummary(ReportFilterDto filter) {
        logger.info("Generating daily movement summary with filter: {}", filter);

        if (!filter.isValidDateRange()) {
            throw new IllegalArgumentException("Valid date range is required for daily movement summary");
        }

        return inventoryMovementRepository
                .findDailyMovementSummaryBetween(filter.getStartDate(), filter.getEndDate());
    }

    // Private helper methods
    private boolean applyMovementFilters(InventoryMovement movement, ReportFilterDto filter) {
        // Filter by product IDs
        if (filter.hasProductFilter() && filter.getProductIds() != null) {
            if (!filter.getProductIds().contains(movement.getProduct().getId())) {
                return false;
            }
        }

        // Filter by product codes
        if (filter.hasProductFilter() && filter.getProductCodes() != null) {
            if (!filter.getProductCodes().contains(movement.getProduct().getCode())) {
                return false;
            }
        }

        // Filter by movement types
        if (filter.hasMovementTypeFilter()) {
            if (!filter.getMovementTypes().contains(movement.getMovementType())) {
                return false;
            }
        }

        // Filter by reference types
        if (filter.hasReferenceTypeFilter()) {
            if (!filter.getReferenceTypes().contains(movement.getReferenceType())) {
                return false;
            }
        }

        // Filter by source systems
        if (filter.hasSourceSystemFilter()) {
            if (!filter.getSourceSystems().contains(movement.getSourceSystem())) {
                return false;
            }
        }

        return true;
    }

    private boolean applyProductFilters(Product product, ReportFilterDto filter) {
        // Filter by product IDs
        if (filter.hasProductFilter() && filter.getProductIds() != null) {
            if (!filter.getProductIds().contains(product.getId())) {
                return false;
            }
        }

        // Filter by product codes
        if (filter.hasProductFilter() && filter.getProductCodes() != null) {
            if (!filter.getProductCodes().contains(product.getCode())) {
                return false;
            }
        }

        // Filter by category IDs
        if (filter.hasCategoryFilter()) {
            if (product.getCategory() == null
                    || !filter.getCategoryIds().contains(product.getCategory().getId())) {
                return false;
            }
        }

        return true;
    }

    private InventoryMovementReportDto convertToInventoryMovementReportDto(InventoryMovement movement) {
        return new InventoryMovementReportDto(
                movement.getId(),
                movement.getProduct().getCode(),
                movement.getProduct().getName(),
                movement.getMovementType(),
                movement.getQuantity(),
                movement.getCreatedAt(),
                movement.getProduct().getUnitPrice()
        );
    }

    private InventoryMovementReportDto convertToDetailedInventoryMovementReportDto(InventoryMovement movement) {
        return new InventoryMovementReportDto(
                movement.getId(),
                movement.getProduct().getCode(),
                movement.getProduct().getName(),
                movement.getMovementType(),
                movement.getQuantity(),
                movement.getReferenceType(),
                movement.getReferenceId(),
                movement.getSourceSystem(),
                movement.getNotes(),
                movement.getCreatedBy() != null ? movement.getCreatedBy().getUsername() : null,
                movement.getCreatedAt(),
                movement.getProduct().getUnitPrice()
        );
    }

    private StockLevelReportDto convertToStockLevelReportDto(Product product, ReportFilterDto filter) {
        // Get movement data for the period if date range is provided
        Integer inboundMovements = 0;
        Integer outboundMovements = 0;
        LocalDateTime lastMovementDate = null;

        if (filter.hasDateRange()) {
            List<InventoryMovement> movements = inventoryMovementRepository
                    .findByProductAndCreatedAtBetween(product, filter.getStartDate(), filter.getEndDate());

            inboundMovements = movements.stream()
                    .filter(m -> m.getMovementType() == InventoryMovement.MovementType.IN)
                    .mapToInt(InventoryMovement::getQuantity)
                    .sum();

            outboundMovements = movements.stream()
                    .filter(m -> m.getMovementType() == InventoryMovement.MovementType.OUT)
                    .mapToInt(InventoryMovement::getQuantity)
                    .sum();

            lastMovementDate = movements.stream()
                    .map(InventoryMovement::getCreatedAt)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
        }

        return new StockLevelReportDto(
                product.getId(),
                product.getCode(),
                product.getName(),
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getCurrentStock(),
                product.getMinStock(),
                product.getMaxStock(),
                product.getUnitPrice(),
                inboundMovements,
                outboundMovements,
                lastMovementDate
        );
    }

    private SupplierPerformanceReportDto convertToSupplierPerformanceReportDto(Supplier supplier, ReportFilterDto filter) {
        // Get supplier performance data from purchase orders
        List<Object[]> performanceData = supplierRepository
                .findSupplierPerformanceBetween(filter.getStartDate(), filter.getEndDate());

        // Find data for this supplier
        Object[] supplierData = performanceData.stream()
                .filter(data -> ((Supplier) data[0]).getId().equals(supplier.getId()))
                .findFirst()
                .orElse(null);

        if (supplierData != null) {
            return new SupplierPerformanceReportDto(
                    supplier.getId(),
                    supplier.getName(),
                    supplier.getContactPerson(),
                    supplier.getEmail(),
                    supplier.getPhone(),
                    ((Number) supplierData[1]).intValue(), // totalOrders
                    ((Number) supplierData[3]).intValue(), // completedOrders
                    0, // pendingOrders - would need additional query
                    ((Number) supplierData[4]).intValue(), // cancelledOrders
                    (BigDecimal) supplierData[2], // totalOrderValue (averageOrderValue from query)
                    (BigDecimal) supplierData[2], // averageOrderValue
                    null, // averageDeliveryDays - would need additional query
                    null // lastOrderDate - would need additional query
            );
        } else {
            // Supplier with no orders in the period
            return new SupplierPerformanceReportDto(
                    supplier.getId(),
                    supplier.getName(),
                    supplier.getContactPerson(),
                    supplier.getEmail(),
                    supplier.getPhone()
            );
        }
    }
}
