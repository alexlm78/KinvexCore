package dev.kreaker.kinvex.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kreaker.kinvex.dto.inventory.ExternalStockDeductionRequest;
import dev.kreaker.kinvex.dto.inventory.ExternalStockDeductionResponse;
import dev.kreaker.kinvex.exception.InsufficientStockException;
import dev.kreaker.kinvex.exception.ProductNotFoundException;
import dev.kreaker.kinvex.service.InventoryService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests de integración para ExternalBillingController.
 *
 * <p>Verifica los requerimientos 2.1 a 2.5 del sistema de facturación externa.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExternalBillingControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private InventoryService inventoryService;

    @Autowired private ObjectMapper objectMapper;

    private ExternalStockDeductionRequest validRequest;
    private ExternalStockDeductionResponse successResponse;

    @BeforeEach
    void setUp() {
        validRequest = new ExternalStockDeductionRequest();
        validRequest.setProductCode("TEST001");
        validRequest.setQuantity(5);
        validRequest.setSourceSystem("BILLING_SYSTEM");
        validRequest.setNotes("Venta desde sistema de facturación");

        successResponse =
                ExternalStockDeductionResponse.success(
                        "TEST001",
                        "Test Product",
                        5,
                        100,
                        95,
                        "BILLING_SYSTEM",
                        LocalDateTime.now(),
                        123L);
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void deductStock_WithValidRequest_ShouldReturnSuccess() throws Exception {
        // Arrange
        when(inventoryService.deductStockForExternalSystem(
                        any(ExternalStockDeductionRequest.class)))
                .thenReturn(successResponse);

        // Act & Assert
        mockMvc.perform(
                        post("/api/external/billing/stock/deduct")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.productCode").value("TEST001"))
                .andExpect(jsonPath("$.productName").value("Test Product"))
                .andExpect(jsonPath("$.quantityDeducted").value(5))
                .andExpect(jsonPath("$.previousStock").value(100))
                .andExpect(jsonPath("$.currentStock").value(95))
                .andExpect(jsonPath("$.sourceSystem").value("BILLING_SYSTEM"))
                .andExpect(jsonPath("$.movementId").value(123));
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void deductStock_WithInsufficientStock_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(inventoryService.deductStockForExternalSystem(
                        any(ExternalStockDeductionRequest.class)))
                .thenThrow(new InsufficientStockException(1L, "TEST001", 2, 5));

        // Act & Assert
        mockMvc.perform(
                        post("/api/external/billing/stock/deduct")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void deductStock_WithNonExistentProduct_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(inventoryService.deductStockForExternalSystem(
                        any(ExternalStockDeductionRequest.class)))
                .thenThrow(new ProductNotFoundException("código", "NONEXISTENT"));

        // Act & Assert
        mockMvc.perform(
                        post("/api/external/billing/stock/deduct")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void deductStock_WithInvalidQuantity_ShouldReturnBadRequest() throws Exception {
        // Arrange
        ExternalStockDeductionRequest invalidRequest = new ExternalStockDeductionRequest();
        invalidRequest.setProductCode("TEST001");
        invalidRequest.setQuantity(0); // Invalid quantity
        invalidRequest.setSourceSystem("BILLING_SYSTEM");

        // Act & Assert
        mockMvc.perform(
                        post("/api/external/billing/stock/deduct")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void deductStock_WithMissingProductCode_ShouldReturnBadRequest() throws Exception {
        // Arrange
        ExternalStockDeductionRequest invalidRequest = new ExternalStockDeductionRequest();
        invalidRequest.setQuantity(5);
        invalidRequest.setSourceSystem("BILLING_SYSTEM");
        // Missing productCode

        // Act & Assert
        mockMvc.perform(
                        post("/api/external/billing/stock/deduct")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deductStock_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(
                        post("/api/external/billing/stock/deduct")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void deductStock_WithInsufficientRole_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(
                        post("/api/external/billing/stock/deduct")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void deductStock_WithManagerRole_ShouldReturnSuccess() throws Exception {
        // Arrange
        when(inventoryService.deductStockForExternalSystem(
                        any(ExternalStockDeductionRequest.class)))
                .thenReturn(successResponse);

        // Act & Assert
        mockMvc.perform(
                        post("/api/external/billing/stock/deduct")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deductStock_WithAdminRole_ShouldReturnSuccess() throws Exception {
        // Arrange
        when(inventoryService.deductStockForExternalSystem(
                        any(ExternalStockDeductionRequest.class)))
                .thenReturn(successResponse);

        // Act & Assert
        mockMvc.perform(
                        post("/api/external/billing/stock/deduct")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
}
