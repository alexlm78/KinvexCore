package dev.kreaker.kinvex.dto.report;

import java.time.LocalDateTime;
import java.util.List;

import dev.kreaker.kinvex.entity.InventoryMovement.MovementType;
import dev.kreaker.kinvex.entity.InventoryMovement.ReferenceType;

/**
 * DTO for report filtering parameters Requirement 4.4: Allow filtering reports
 * by product, supplier, or date ranges
 */
public class ReportFilterDto {

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<Long> productIds;
    private List<String> productCodes;
    private List<Long> supplierIds;
    private List<Long> categoryIds;
    private List<MovementType> movementTypes;
    private List<ReferenceType> referenceTypes;
    private List<String> sourceSystems;
    private Boolean activeProductsOnly;
    private Boolean activeSuppliersOnly;
    private Integer limit;
    private String sortBy;
    private String sortDirection;

    // Default constructor
    public ReportFilterDto() {
        this.activeProductsOnly = true;
        this.activeSuppliersOnly = true;
        this.sortDirection = "DESC";
    }

    // Constructor with date range
    public ReportFilterDto(LocalDateTime startDate, LocalDateTime endDate) {
        this();
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
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

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    // Helper methods
    public boolean hasDateRange() {
        return startDate != null && endDate != null;
    }

    public boolean hasProductFilter() {
        return (productIds != null && !productIds.isEmpty())
                || (productCodes != null && !productCodes.isEmpty());
    }

    public boolean hasSupplierFilter() {
        return supplierIds != null && !supplierIds.isEmpty();
    }

    public boolean hasCategoryFilter() {
        return categoryIds != null && !categoryIds.isEmpty();
    }

    public boolean hasMovementTypeFilter() {
        return movementTypes != null && !movementTypes.isEmpty();
    }

    public boolean hasReferenceTypeFilter() {
        return referenceTypes != null && !referenceTypes.isEmpty();
    }

    public boolean hasSourceSystemFilter() {
        return sourceSystems != null && !sourceSystems.isEmpty();
    }

    public boolean isValidDateRange() {
        return hasDateRange() && startDate.isBefore(endDate);
    }

    @Override
    public String toString() {
        return "ReportFilterDto{"
                + "startDate=" + startDate
                + ", endDate=" + endDate
                + ", productIds=" + productIds
                + ", supplierIds=" + supplierIds
                + ", movementTypes=" + movementTypes
                + ", activeProductsOnly=" + activeProductsOnly
                + ", limit=" + limit
                + ", sortBy='" + sortBy + '\''
                + ", sortDirection='" + sortDirection + '\''
                + '}';
    }
}
