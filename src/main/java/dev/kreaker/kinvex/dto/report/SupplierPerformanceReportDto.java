package dev.kreaker.kinvex.dto.report;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for supplier performance reports Requirement 4.3: Show supplier
 * information for each movement Requirement 4.4: Allow filtering reports by
 * supplier
 */
public class SupplierPerformanceReportDto {

    private Long supplierId;
    private String supplierName;
    private String contactPerson;
    private String email;
    private String phone;
    private Integer totalOrders;
    private Integer completedOrders;
    private Integer pendingOrders;
    private Integer cancelledOrders;
    private BigDecimal totalOrderValue;
    private BigDecimal averageOrderValue;
    private Double completionRate;
    private Double averageDeliveryDays;
    private Integer overdueOrders;
    private LocalDateTime lastOrderDate;
    private LocalDateTime reportPeriodStart;
    private LocalDateTime reportPeriodEnd;
    private LocalDateTime reportGeneratedAt;

    // Default constructor
    public SupplierPerformanceReportDto() {
        this.reportGeneratedAt = LocalDateTime.now();
    }

    // Constructor with essential fields
    public SupplierPerformanceReportDto(
            Long supplierId,
            String supplierName,
            String contactPerson,
            String email,
            String phone) {
        this();
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phone = phone;
    }

    // Constructor with performance metrics
    public SupplierPerformanceReportDto(
            Long supplierId,
            String supplierName,
            String contactPerson,
            String email,
            String phone,
            Integer totalOrders,
            Integer completedOrders,
            Integer pendingOrders,
            Integer cancelledOrders,
            BigDecimal totalOrderValue,
            BigDecimal averageOrderValue,
            Double averageDeliveryDays,
            LocalDateTime lastOrderDate) {
        this(supplierId, supplierName, contactPerson, email, phone);
        this.totalOrders = totalOrders != null ? totalOrders : 0;
        this.completedOrders = completedOrders != null ? completedOrders : 0;
        this.pendingOrders = pendingOrders != null ? pendingOrders : 0;
        this.cancelledOrders = cancelledOrders != null ? cancelledOrders : 0;
        this.totalOrderValue = totalOrderValue != null ? totalOrderValue : BigDecimal.ZERO;
        this.averageOrderValue = averageOrderValue != null ? averageOrderValue : BigDecimal.ZERO;
        this.averageDeliveryDays = averageDeliveryDays;
        this.lastOrderDate = lastOrderDate;
        this.completionRate = calculateCompletionRate();
    }

    // Getters and Setters
    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
        this.completionRate = calculateCompletionRate();
    }

    public Integer getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(Integer completedOrders) {
        this.completedOrders = completedOrders;
        this.completionRate = calculateCompletionRate();
    }

    public Integer getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(Integer pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public Integer getCancelledOrders() {
        return cancelledOrders;
    }

    public void setCancelledOrders(Integer cancelledOrders) {
        this.cancelledOrders = cancelledOrders;
    }

    public BigDecimal getTotalOrderValue() {
        return totalOrderValue;
    }

    public void setTotalOrderValue(BigDecimal totalOrderValue) {
        this.totalOrderValue = totalOrderValue;
    }

    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }

    public void setAverageOrderValue(BigDecimal averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }

    public Double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(Double completionRate) {
        this.completionRate = completionRate;
    }

    public Double getAverageDeliveryDays() {
        return averageDeliveryDays;
    }

    public void setAverageDeliveryDays(Double averageDeliveryDays) {
        this.averageDeliveryDays = averageDeliveryDays;
    }

    public Integer getOverdueOrders() {
        return overdueOrders;
    }

    public void setOverdueOrders(Integer overdueOrders) {
        this.overdueOrders = overdueOrders;
    }

    public LocalDateTime getLastOrderDate() {
        return lastOrderDate;
    }

    public void setLastOrderDate(LocalDateTime lastOrderDate) {
        this.lastOrderDate = lastOrderDate;
    }

    public LocalDateTime getReportPeriodStart() {
        return reportPeriodStart;
    }

    public void setReportPeriodStart(LocalDateTime reportPeriodStart) {
        this.reportPeriodStart = reportPeriodStart;
    }

    public LocalDateTime getReportPeriodEnd() {
        return reportPeriodEnd;
    }

    public void setReportPeriodEnd(LocalDateTime reportPeriodEnd) {
        this.reportPeriodEnd = reportPeriodEnd;
    }

    public LocalDateTime getReportGeneratedAt() {
        return reportGeneratedAt;
    }

    public void setReportGeneratedAt(LocalDateTime reportGeneratedAt) {
        this.reportGeneratedAt = reportGeneratedAt;
    }

    // Helper methods
    private Double calculateCompletionRate() {
        if (totalOrders == null || totalOrders == 0) {
            return 0.0;
        }
        if (completedOrders == null) {
            return 0.0;
        }
        return (completedOrders.doubleValue() / totalOrders.doubleValue()) * 100.0;
    }

    public boolean isReliableSupplier() {
        return completionRate != null && completionRate >= 90.0;
    }

    public boolean hasRecentActivity() {
        return lastOrderDate != null
                && lastOrderDate.isAfter(LocalDateTime.now().minusMonths(3));
    }

    public boolean hasOverdueOrders() {
        return overdueOrders != null && overdueOrders > 0;
    }

    public PerformanceRating getPerformanceRating() {
        if (completionRate == null) {
            return PerformanceRating.UNKNOWN;
        }

        if (completionRate >= 95.0) {
            return PerformanceRating.EXCELLENT;
        } else if (completionRate >= 85.0) {
            return PerformanceRating.GOOD;
        } else if (completionRate >= 70.0) {
            return PerformanceRating.AVERAGE;
        } else {
            return PerformanceRating.POOR;
        }
    }

    @Override
    public String toString() {
        return "SupplierPerformanceReportDto{"
                + "supplierId=" + supplierId
                + ", supplierName='" + supplierName + '\''
                + ", totalOrders=" + totalOrders
                + ", completedOrders=" + completedOrders
                + ", completionRate=" + completionRate
                + ", totalOrderValue=" + totalOrderValue
                + ", reportGeneratedAt=" + reportGeneratedAt
                + '}';
    }

    public enum PerformanceRating {
        EXCELLENT,
        GOOD,
        AVERAGE,
        POOR,
        UNKNOWN
    }
}
