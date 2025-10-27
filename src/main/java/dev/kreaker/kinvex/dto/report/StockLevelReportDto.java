package dev.kreaker.kinvex.dto.report;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** DTO for stock level reports Requirement 4.2: Generate reports of stock levels by time period */
public class StockLevelReportDto {

    private Long productId;
    private String productCode;
    private String productName;
    private String categoryName;
    private Integer currentStock;
    private Integer minStock;
    private Integer maxStock;
    private BigDecimal unitPrice;
    private BigDecimal stockValue;
    private StockStatus stockStatus;
    private Integer inboundMovements;
    private Integer outboundMovements;
    private Integer netMovements;
    private LocalDateTime lastMovementDate;
    private LocalDateTime reportGeneratedAt;

    // Default constructor
    public StockLevelReportDto() {
        this.reportGeneratedAt = LocalDateTime.now();
    }

    // Constructor with essential fields
    public StockLevelReportDto(
            Long productId,
            String productCode,
            String productName,
            String categoryName,
            Integer currentStock,
            Integer minStock,
            Integer maxStock,
            BigDecimal unitPrice) {
        this();
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.categoryName = categoryName;
        this.currentStock = currentStock;
        this.minStock = minStock;
        this.maxStock = maxStock;
        this.unitPrice = unitPrice;
        this.stockValue = calculateStockValue();
        this.stockStatus = determineStockStatus();
    }

    // Full constructor
    public StockLevelReportDto(
            Long productId,
            String productCode,
            String productName,
            String categoryName,
            Integer currentStock,
            Integer minStock,
            Integer maxStock,
            BigDecimal unitPrice,
            Integer inboundMovements,
            Integer outboundMovements,
            LocalDateTime lastMovementDate) {
        this(
                productId,
                productCode,
                productName,
                categoryName,
                currentStock,
                minStock,
                maxStock,
                unitPrice);
        this.inboundMovements = inboundMovements != null ? inboundMovements : 0;
        this.outboundMovements = outboundMovements != null ? outboundMovements : 0;
        this.netMovements = this.inboundMovements - this.outboundMovements;
        this.lastMovementDate = lastMovementDate;
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
        this.stockValue = calculateStockValue();
        this.stockStatus = determineStockStatus();
    }

    public Integer getMinStock() {
        return minStock;
    }

    public void setMinStock(Integer minStock) {
        this.minStock = minStock;
        this.stockStatus = determineStockStatus();
    }

    public Integer getMaxStock() {
        return maxStock;
    }

    public void setMaxStock(Integer maxStock) {
        this.maxStock = maxStock;
        this.stockStatus = determineStockStatus();
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        this.stockValue = calculateStockValue();
    }

    public BigDecimal getStockValue() {
        return stockValue;
    }

    public void setStockValue(BigDecimal stockValue) {
        this.stockValue = stockValue;
    }

    public StockStatus getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(StockStatus stockStatus) {
        this.stockStatus = stockStatus;
    }

    public Integer getInboundMovements() {
        return inboundMovements;
    }

    public void setInboundMovements(Integer inboundMovements) {
        this.inboundMovements = inboundMovements;
        updateNetMovements();
    }

    public Integer getOutboundMovements() {
        return outboundMovements;
    }

    public void setOutboundMovements(Integer outboundMovements) {
        this.outboundMovements = outboundMovements;
        updateNetMovements();
    }

    public Integer getNetMovements() {
        return netMovements;
    }

    public void setNetMovements(Integer netMovements) {
        this.netMovements = netMovements;
    }

    public LocalDateTime getLastMovementDate() {
        return lastMovementDate;
    }

    public void setLastMovementDate(LocalDateTime lastMovementDate) {
        this.lastMovementDate = lastMovementDate;
    }

    public LocalDateTime getReportGeneratedAt() {
        return reportGeneratedAt;
    }

    public void setReportGeneratedAt(LocalDateTime reportGeneratedAt) {
        this.reportGeneratedAt = reportGeneratedAt;
    }

    // Helper methods
    private BigDecimal calculateStockValue() {
        if (unitPrice != null && currentStock != null) {
            return unitPrice.multiply(BigDecimal.valueOf(currentStock));
        }
        return BigDecimal.ZERO;
    }

    private StockStatus determineStockStatus() {
        if (currentStock == null) {
            return StockStatus.UNKNOWN;
        }

        if (currentStock == 0) {
            return StockStatus.OUT_OF_STOCK;
        }

        if (minStock != null && currentStock <= minStock) {
            return StockStatus.LOW_STOCK;
        }

        if (maxStock != null && currentStock > maxStock) {
            return StockStatus.OVERSTOCK;
        }

        return StockStatus.NORMAL;
    }

    private void updateNetMovements() {
        if (inboundMovements != null && outboundMovements != null) {
            this.netMovements = inboundMovements - outboundMovements;
        }
    }

    public boolean isLowStock() {
        return stockStatus == StockStatus.LOW_STOCK;
    }

    public boolean isOutOfStock() {
        return stockStatus == StockStatus.OUT_OF_STOCK;
    }

    public boolean isOverStock() {
        return stockStatus == StockStatus.OVERSTOCK;
    }

    @Override
    public String toString() {
        return "StockLevelReportDto{"
                + "productId="
                + productId
                + ", productCode='"
                + productCode
                + '\''
                + ", productName='"
                + productName
                + '\''
                + ", currentStock="
                + currentStock
                + ", stockStatus="
                + stockStatus
                + ", stockValue="
                + stockValue
                + ", reportGeneratedAt="
                + reportGeneratedAt
                + '}';
    }

    public enum StockStatus {
        NORMAL,
        LOW_STOCK,
        OUT_OF_STOCK,
        OVERSTOCK,
        UNKNOWN
    }
}
