package dev.kreaker.kinvex.dto.report;

import java.time.LocalDateTime;
import java.util.List;

import dev.kreaker.kinvex.entity.InventoryMovement.MovementType;
import dev.kreaker.kinvex.entity.InventoryMovement.ReferenceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for report export requests Requirement 4.5: Export reports in PDF and
 * Excel formats
 */
@Schema(description = "Request object for exporting reports")
public class ReportExportRequest {

    @NotNull
    @Schema(description = "Type of report to export", example = "INVENTORY_MOVEMENTS")
    private ReportType reportType;

    @NotNull
    @Schema(description = "Export format", example = "PDF")
    private ExportFormat format;

    @Schema(description = "Start date for the report period")
    private LocalDateTime startDate;

    @Schema(description = "End date for the report period")
    private LocalDateTime endDate;

    @Schema(description = "Product IDs to filter by")
    private List<Long> productIds;

    @Schema(description = "Product codes to filter by")
    private List<String> productCodes;

    @Schema(description = "Supplier IDs to filter by")
    private List<Long> supplierIds;

    @Schema(description = "Category IDs to filter by")
    private List<Long> categoryIds;

    @Schema(description = "Movement types to filter by")
    private List<MovementType> movementTypes;

    @Schema(description = "Reference types to filter by")
    private List<ReferenceType> referenceTypes;

    @Schema(description = "Source systems to filter by")
    private List<String> sourceSystems;

    @Schema(description = "Include only active products")
    private Boolean activeProductsOnly;

    @Schema(description = "Include only active suppliers")
    private Boolean activeSuppliersOnly;

    @Schema(description = "Maximum number of results to include")
    private Integer limit;

    @Schema(description = "Include detailed information")
    private Boolean detailed;

    @Schema(description = "Report title for export")
    private String title;

    public enum ReportType {
        INVENTORY_MOVEMENTS,
        STOCK_LEVELS,
        SUPPLIER_PERFORMANCE
    }

    public enum ExportFormat {
        PDF,
        EXCEL
    }

    // Constructors
    public ReportExportRequest() {
    }

    public ReportExportRequest(ReportType reportType, ExportFormat format) {
        this.reportType = reportType;
        this.format = format;
    }

    // Getters and Setters
    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public ExportFormat getFormat() {
        return format;
    }

    public void setFormat(ExportFormat format) {
        this.format = format;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public List<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Long> productIds) {
        this.productIds = productIds;
    }

    public List<String> getProductCodes() {
        return productCodes;
    }

    public void setProductCodes(List<String> productCodes) {
        this.productCodes = productCodes;
    }

    public List<Long> getSupplierIds() {
        return supplierIds;
    }

    public void setSupplierIds(List<Long> supplierIds) {
        this.supplierIds = supplierIds;
    }

    public List<Long> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public List<MovementType> getMovementTypes() {
        return movementTypes;
    }

    public void setMovementTypes(List<MovementType> movementTypes) {
        this.movementTypes = movementTypes;
    }

    public List<ReferenceType> getReferenceTypes() {
        return referenceTypes;
    }

    public void setReferenceTypes(List<ReferenceType> referenceTypes) {
        this.referenceTypes = referenceTypes;
    }

    public List<String> getSourceSystems() {
        return sourceSystems;
    }

    public void setSourceSystems(List<String> sourceSystems) {
        this.sourceSystems = sourceSystems;
    }

    public Boolean getActiveProductsOnly() {
        return activeProductsOnly;
    }

    public void setActiveProductsOnly(Boolean activeProductsOnly) {
        this.activeProductsOnly = activeProductsOnly;
    }

    public Boolean getActiveSuppliersOnly() {
        return activeSuppliersOnly;
    }

    public void setActiveSuppliersOnly(Boolean activeSuppliersOnly) {
        this.activeSuppliersOnly = activeSuppliersOnly;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Boolean getDetailed() {
        return detailed;
    }

    public void setDetailed(Boolean detailed) {
        this.detailed = detailed;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Convert to ReportFilterDto for service layer
     */
    public ReportFilterDto toReportFilter() {
        ReportFilterDto filter = new ReportFilterDto();
        filter.setStartDate(this.startDate);
        filter.setEndDate(this.endDate);
        filter.setProductIds(this.productIds);
        filter.setProductCodes(this.productCodes);
        filter.setSupplierIds(this.supplierIds);
        filter.setCategoryIds(this.categoryIds);
        filter.setMovementTypes(this.movementTypes);
        filter.setReferenceTypes(this.referenceTypes);
        filter.setSourceSystems(this.sourceSystems);
        filter.setActiveProductsOnly(this.activeProductsOnly);
        filter.setActiveSuppliersOnly(this.activeSuppliersOnly);
        filter.setLimit(this.limit);
        return filter;
    }

    @Override
    public String toString() {
        return "ReportExportRequest{"
                + "reportType=" + reportType
                + ", format=" + format
                + ", startDate=" + startDate
                + ", endDate=" + endDate
                + ", title='" + title + '\''
                + '}';
    }
}
