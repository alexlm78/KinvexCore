package dev.kreaker.kinvex.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kreaker.kinvex.dto.inventory.CreateProductRequest;
import dev.kreaker.kinvex.dto.inventory.UpdateProductRequest;
import dev.kreaker.kinvex.entity.Product;
import dev.kreaker.kinvex.service.InventoryService;
import java.math.BigDecimal;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Tests de integración para InventoryController. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InventoryControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private InventoryService inventoryService;

    @Autowired private ObjectMapper objectMapper;

    private Product testProduct;
    private CreateProductRequest createRequest;
    private UpdateProductRequest updateRequest;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setCode("TEST001");
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setUnitPrice(new BigDecimal("10.99"));
        testProduct.setCurrentStock(100);
        testProduct.setMinStock(10);
        testProduct.setMaxStock(500);
        testProduct.setActive(true);

        createRequest = new CreateProductRequest();
        createRequest.setCode("TEST001");
        createRequest.setName("Test Product");
        createRequest.setDescription("Test Description");
        createRequest.setUnitPrice(new BigDecimal("10.99"));
        createRequest.setInitialStock(100);
        createRequest.setMinStock(10);
        createRequest.setMaxStock(500);

        updateRequest = new UpdateProductRequest();
        updateRequest.setName("Updated Product");
        updateRequest.setDescription("Updated Description");
        updateRequest.setUnitPrice(new BigDecimal("12.99"));
        updateRequest.setMinStock(15);
        updateRequest.setMaxStock(600);
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void getProducts_ShouldReturnPagedProducts() throws Exception {
        // Arrange
        Page<Product> productPage =
                new PageImpl<>(Arrays.asList(testProduct), PageRequest.of(0, 20), 1);
        when(inventoryService.searchProducts(any(), any())).thenReturn(productPage);

        // Act & Assert
        mockMvc.perform(get("/api/inventory/products").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].code").value("TEST001"))
                .andExpect(jsonPath("$.content[0].name").value("Test Product"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void getProductById_ShouldReturnProduct() throws Exception {
        // Arrange
        when(inventoryService.getProductById(1L)).thenReturn(testProduct);

        // Act & Assert
        mockMvc.perform(get("/api/inventory/products/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("TEST001"))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void getProductByCode_ShouldReturnProduct() throws Exception {
        // Arrange
        when(inventoryService.getProductByCode("TEST001")).thenReturn(testProduct);

        // Act & Assert
        mockMvc.perform(
                        get("/api/inventory/products/by-code/TEST001")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("TEST001"))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void createProduct_ShouldReturnCreatedProduct() throws Exception {
        // Arrange
        when(inventoryService.createProduct(any(CreateProductRequest.class)))
                .thenReturn(testProduct);

        // Act & Assert
        mockMvc.perform(
                        post("/api/inventory/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("TEST001"))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void updateProduct_ShouldReturnUpdatedProduct() throws Exception {
        // Arrange
        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setCode("TEST001");
        updatedProduct.setName("Updated Product");
        updatedProduct.setDescription("Updated Description");
        updatedProduct.setUnitPrice(new BigDecimal("12.99"));
        updatedProduct.setCurrentStock(100);
        updatedProduct.setMinStock(15);
        updatedProduct.setMaxStock(600);
        updatedProduct.setActive(true);

        when(inventoryService.updateProduct(eq(1L), any(UpdateProductRequest.class)))
                .thenReturn(updatedProduct);

        // Act & Assert
        mockMvc.perform(
                        put("/api/inventory/products/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Product"))
                .andExpect(jsonPath("$.unitPrice").value(12.99));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void getLowStockProducts_ShouldReturnLowStockProducts() throws Exception {
        // Arrange
        when(inventoryService.getLowStockProducts()).thenReturn(Arrays.asList(testProduct));

        // Act & Assert
        mockMvc.perform(
                        get("/api/inventory/products/low-stock")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].code").value("TEST001"));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void getOutOfStockProducts_ShouldReturnOutOfStockProducts() throws Exception {
        // Arrange
        when(inventoryService.getOutOfStockProducts()).thenReturn(Arrays.asList(testProduct));

        // Act & Assert
        mockMvc.perform(
                        get("/api/inventory/products/out-of-stock")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].code").value("TEST001"));
    }

    @Test
    void getProducts_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/inventory/products").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void createProduct_WithViewerRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(
                        post("/api/inventory/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }
}
