package dev.kreaker.kinvex.dto.alert;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

/** DTO para respuesta de resumen de alertas. */
public class AlertSummaryResponse {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private int totalOverdueOrders;
    private int totalOrdersDueSoon;
    private List<OrderAlertResponse> overdueOrders;
    private List<OrderAlertResponse> ordersDueSoon;
    private String summary;

    // Constructors
    public AlertSummaryResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public AlertSummaryResponse(
            List<OrderAlertResponse> overdueOrders, List<OrderAlertResponse> ordersDueSoon) {
        this();
        this.overdueOrders = overdueOrders;
        this.ordersDueSoon = ordersDueSoon;
        this.totalOverdueOrders = overdueOrders != null ? overdueOrders.size() : 0;
        this.totalOrdersDueSoon = ordersDueSoon != null ? ordersDueSoon.size() : 0;
        this.summary = buildSummary();
    }

    // Static factory methods
    public static AlertSummaryResponse create(
            List<OrderAlertResponse> overdueOrders, List<OrderAlertResponse> ordersDueSoon) {
        return new AlertSummaryResponse(overdueOrders, ordersDueSoon);
    }

    public static AlertSummaryResponse empty() {
        AlertSummaryResponse response = new AlertSummaryResponse();
        response.totalOverdueOrders = 0;
        response.totalOrdersDueSoon = 0;
        response.summary = "No hay alertas pendientes";
        return response;
    }

    // Helper methods
    private String buildSummary() {
        StringBuilder sb = new StringBuilder();

        if (totalOverdueOrders > 0) {
            sb.append(totalOverdueOrders).append(" orden(es) vencida(s)");
        }

        if (totalOrdersDueSoon > 0) {
            if (sb.length() > 0) {
                sb.append(" y ");
            }
            sb.append(totalOrdersDueSoon).append(" orden(es) que vence(n) pronto");
        }

        if (sb.length() == 0) {
            sb.append("No hay alertas pendientes");
        }

        return sb.toString();
    }

    public boolean hasAlerts() {
        return totalOverdueOrders > 0 || totalOrdersDueSoon > 0;
    }

    // Getters and Setters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getTotalOverdueOrders() {
        return totalOverdueOrders;
    }

    public void setTotalOverdueOrders(int totalOverdueOrders) {
        this.totalOverdueOrders = totalOverdueOrders;
    }

    public int getTotalOrdersDueSoon() {
        return totalOrdersDueSoon;
    }

    public void setTotalOrdersDueSoon(int totalOrdersDueSoon) {
        this.totalOrdersDueSoon = totalOrdersDueSoon;
    }

    public List<OrderAlertResponse> getOverdueOrders() {
        return overdueOrders;
    }

    public void setOverdueOrders(List<OrderAlertResponse> overdueOrders) {
        this.overdueOrders = overdueOrders;
        this.totalOverdueOrders = overdueOrders != null ? overdueOrders.size() : 0;
        this.summary = buildSummary();
    }

    public List<OrderAlertResponse> getOrdersDueSoon() {
        return ordersDueSoon;
    }

    public void setOrdersDueSoon(List<OrderAlertResponse> ordersDueSoon) {
        this.ordersDueSoon = ordersDueSoon;
        this.totalOrdersDueSoon = ordersDueSoon != null ? ordersDueSoon.size() : 0;
        this.summary = buildSummary();
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Override
    public String toString() {
        return "AlertSummaryResponse{"
                + "timestamp="
                + timestamp
                + ", totalOverdueOrders="
                + totalOverdueOrders
                + ", totalOrdersDueSoon="
                + totalOrdersDueSoon
                + ", summary='"
                + summary
                + '\''
                + '}';
    }
}
