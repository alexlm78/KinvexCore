package dev.kreaker.kinvex.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.kreaker.kinvex.dto.inventory.CreateProductRequest;
import dev.kreaker.kinvex.dto.inventory.ExternalStockDeductionRequest;
import dev.kreaker.kinvex.dto.inventory.ExternalStockDeductionResponse;
import dev.kreaker.kinvex.dto.inventory.StockUpdateRequest;
import dev.kreaker.kinvex.dto.inventory.UpdateProductRequest;
import dev.kreaker.kinvex.entity.Category;
import dev.kreaker.kinvex.entity.InventoryMovement;
import dev.kreaker.kinvex.entity.Product;
import dev.kreaker.kinvex.exception.DuplicateProductCodeException;
import dev.kreaker.kinvex.exception.InsufficientStockException;
import dev.kreaker.kinvex.exception.ProductNotFoundException;
import dev.kreaker.kinvex.repository.CategoryRepository;
import dev.kreaker.kinvex.repository.InventoryMovementRepository;
import dev.kreaker.kinvex.repository.ProductRepository;
import dev.kreaker.kinvex.repository.UserRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitarios para InventoryService.
 *
 * <p>Verifica la lógica de negocio de inventario según los requerimientos 1.4 y 2.3.
 */
@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock private ProductRepository productRepository;

    @Mock private CategoryRepository categoryRepository;

    @Mock private InventoryMovementRepository inventoryMovementRepository;

    @Mock private UserRepository userRepository;

    @InjectMocks private InventoryService inventoryService;

    private Product testProduct;
    private Category testCategory;
    private CreateProductRequest createRequest;
    private UpdateProductRequest updateRequest;
    private StockUpdateRequest stockUpdateRequest;
    private ExternalStockDeductionRequest externalRequest;

    @BeforeEach
    void setUp() {
        // Setup test product
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

        // Setup test category
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Test Category");

        // Setup create request
        createRequest = new CreateProductRequest();
        createRequest.setCode("NEW001");
        createRequest.setName("New Product");
        createRequest.setDescription("New Description");
        createRequest.setUnitPrice(new BigDecimal("15.99"));
        createRequest.setInitialStock(50);
        createRequest.setMinStock(5);
        createRequest.setMaxStock(200);
        createRequest.setCategoryId(1L);

        // Setup update request
        updateRequest = new UpdateProductRequest();
        updateRequest.setName("Updated Product");
        updateRequest.setDescription("Updated Description");
        updateRequest.setUnitPrice(new BigDecimal("12.99"));
        updateRequest.setMinStock(15);
        updateRequest.setMaxStock(600);

        // Setup stock update request
        stockUpdateRequest = new StockUpdateRequest();
        stockUpdateRequest.setQuantity(10);
        stockUpdateRequest.setReferenceType(InventoryMovement.ReferenceType.ADJUSTMENT);
        stockUpdateRequest.setSourceSystem("TEST_SYSTEM");
        stockUpdateRequest.setNotes("Test adjustment");

        // Setup external request
        externalRequest = new ExternalStockDeductionRequest();
        externalRequest.setProductCode("TEST001");
        externalRequest.setQuantity(5);
        externalRequest.setSourceSystem("BILLING_SYSTEM");
        externalRequest.setNotes("External sale");
    }

    // ========== Product Creation Tests ==========
    @Test
    void createProduct_WithValidData_ShouldCreateProduct() {
        // Arrange
        when(productRepository.existsByCode("NEW001")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class)))
                .thenAnswer(
                        invocation -> {
                            Product product = invocation.getArgument(0);
                            product.setId(2L);
                            return product;
                        });
        when(inventoryMovementRepository.save(any(InventoryMovement.class)))
                .thenAnswer(
                        invocation -> {
                            InventoryMovement movement = invocation.getArgument(0);
                            movement.setId(1L);
                            return movement;
                        });

        // Act
        Product result = inventoryService.createProduct(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals("NEW001", result.getCode());
        assertEquals("New Product", result.getName());
        assertEquals(new BigDecimal("15.99"), result.getUnitPrice());
        assertEquals(50, result.getCurrentStock());
        assertEquals(testCategory, result.getCategory());

        verify(productRepository).existsByCode("NEW001");
        verify(categoryRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
        verify(inventoryMovementRepository).save(any(InventoryMovement.class));
    }

    @Test
    void createProduct_WithDuplicateCode_ShouldThrowException() {
        // Arrange
        when(productRepository.existsByCode("NEW001")).thenReturn(true);

        // Act & Assert
        assertThrows(
                DuplicateProductCodeException.class,
                () -> {
                    inventoryService.createProduct(createRequest);
                });

        verify(productRepository).existsByCode("NEW001");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void createProduct_WithZeroInitialStock_ShouldNotCreateMovement() {
        // Arrange
        createRequest.setInitialStock(0);
        when(productRepository.existsByCode("NEW001")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class)))
                .thenAnswer(
                        invocation -> {
                            Product product = invocation.getArgument(0);
                            product.setId(2L);
                            return product;
                        });

        // Act
        Product result = inventoryService.createProduct(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getCurrentStock());
        verify(inventoryMovementRepository, never()).save(any(InventoryMovement.class));
    }

    // ========== Product Update Tests ==========
    @Test
    void updateProduct_WithValidData_ShouldUpdateProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        Product result = inventoryService.updateProduct(1L, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Product", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(new BigDecimal("12.99"), result.getUnitPrice());
        assertEquals(15, result.getMinStock());
        assertEquals(600, result.getMaxStock());

        verify(productRepository).findById(1L);
        verify(productRepository).save(testProduct);
    }

    @Test
    void updateProduct_WithNonExistentId_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ProductNotFoundException.class,
                () -> {
                    inventoryService.updateProduct(999L, updateRequest);
                });

        verify(productRepository).findById(999L);
        verify(productRepository, never()).save(any(Product.class));
    }

    // ========== Stock Management Tests ==========
    @Test
    void increaseStock_WithValidQuantity_ShouldIncreaseStock() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(inventoryMovementRepository.save(any(InventoryMovement.class)))
                .thenAnswer(
                        invocation -> {
                            InventoryMovement movement = invocation.getArgument(0);
                            movement.setId(1L);
                            return movement;
                        });

        // Act
        InventoryMovement result = inventoryService.increaseStock(1L, stockUpdateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(110, testProduct.getCurrentStock()); // 100 + 10
        assertEquals(InventoryMovement.MovementType.IN, result.getMovementType());
        assertEquals(10, result.getQuantity());

        verify(productRepository).findById(1L);
        verify(productRepository).save(testProduct);
        verify(inventoryMovementRepository).save(any(InventoryMovement.class));
    }

    @Test
    void decreaseStock_WithSufficientStock_ShouldDecreaseStock() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(inventoryMovementRepository.save(any(InventoryMovement.class)))
                .thenAnswer(
                        invocation -> {
                            InventoryMovement movement = invocation.getArgument(0);
                            movement.setId(1L);
                            return movement;
                        });

        // Act
        InventoryMovement result = inventoryService.decreaseStock(1L, stockUpdateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(90, testProduct.getCurrentStock()); // 100 - 10
        assertEquals(InventoryMovement.MovementType.OUT, result.getMovementType());
        assertEquals(10, result.getQuantity());

        verify(productRepository).findById(1L);
        verify(productRepository).save(testProduct);
        verify(inventoryMovementRepository).save(any(InventoryMovement.class));
    }

    @Test
    void decreaseStock_WithInsufficientStock_ShouldThrowException() {
        // Arrange
        testProduct.setCurrentStock(5); // Less than requested quantity
        stockUpdateRequest.setQuantity(10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act & Assert
        assertThrows(
                InsufficientStockException.class,
                () -> {
                    inventoryService.decreaseStock(1L, stockUpdateRequest);
                });

        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
        verify(inventoryMovementRepository, never()).save(any(InventoryMovement.class));
    }

    @Test
    void adjustStock_WithDifferentStock_ShouldCreateMovement() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(inventoryMovementRepository.save(any(InventoryMovement.class)))
                .thenAnswer(
                        invocation -> {
                            InventoryMovement movement = invocation.getArgument(0);
                            movement.setId(1L);
                            return movement;
                        });

        // Act
        InventoryMovement result = inventoryService.adjustStock(1L, 120, "Stock adjustment");

        // Assert
        assertNotNull(result);
        assertEquals(120, testProduct.getCurrentStock());
        assertEquals(InventoryMovement.MovementType.IN, result.getMovementType());
        assertEquals(20, result.getQuantity()); // 120 - 100

        verify(productRepository).findById(1L);
        verify(productRepository).save(testProduct);
        verify(inventoryMovementRepository).save(any(InventoryMovement.class));
    }

    @Test
    void adjustStock_WithSameStock_ShouldReturnNull() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act
        InventoryMovement result = inventoryService.adjustStock(1L, 100, "No change");

        // Assert
        assertNull(result);
        assertEquals(100, testProduct.getCurrentStock()); // No change

        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
        verify(inventoryMovementRepository, never()).save(any(InventoryMovement.class));
    }

    // ========== External API Tests ==========
    @Test
    void deductStockForExternalSystem_WithValidRequest_ShouldReturnSuccess() {
        // Arrange
        when(productRepository.findByCode("TEST001")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(inventoryMovementRepository.save(any(InventoryMovement.class)))
                .thenAnswer(
                        invocation -> {
                            InventoryMovement movement = invocation.getArgument(0);
                            movement.setId(1L);
                            return movement;
                        });

        // Act
        ExternalStockDeductionResponse result =
                inventoryService.deductStockForExternalSystem(externalRequest);

        // Assert
        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals("TEST001", result.getProductCode());
        assertEquals("Test Product", result.getProductName());
        assertEquals(5, result.getQuantityDeducted());
        assertEquals(100, result.getPreviousStock());
        assertEquals(95, result.getCurrentStock());
        assertEquals("BILLING_SYSTEM", result.getSourceSystem());
        assertEquals(1L, result.getMovementId());

        verify(productRepository).findByCode("TEST001");
        verify(productRepository).save(testProduct);
        verify(inventoryMovementRepository).save(any(InventoryMovement.class));
    }

    @Test
    void deductStockForExternalSystem_WithInsufficientStock_ShouldThrowException() {
        // Arrange
        testProduct.setCurrentStock(3); // Less than requested quantity
        when(productRepository.findByCode("TEST001")).thenReturn(Optional.of(testProduct));

        // Act & Assert
        InsufficientStockException exception =
                assertThrows(
                        InsufficientStockException.class,
                        () -> {
                            inventoryService.deductStockForExternalSystem(externalRequest);
                        });

        assertEquals(1L, exception.getProductId());
        assertEquals("TEST001", exception.getProductCode());
        assertEquals(3, exception.getAvailableStock());
        assertEquals(5, exception.getRequestedQuantity());

        verify(productRepository).findByCode("TEST001");
        verify(productRepository, never()).save(any(Product.class));
        verify(inventoryMovementRepository, never()).save(any(InventoryMovement.class));
    }

    @Test
    void deductStockForExternalSystem_WithNonExistentProduct_ShouldThrowException() {
        // Arrange
        when(productRepository.findByCode("NONEXISTENT")).thenReturn(Optional.empty());
        externalRequest.setProductCode("NONEXISTENT");

        // Act & Assert
        assertThrows(
                ProductNotFoundException.class,
                () -> {
                    inventoryService.deductStockForExternalSystem(externalRequest);
                });

        verify(productRepository).findByCode("NONEXISTENT");
        verify(productRepository, never()).save(any(Product.class));
        verify(inventoryMovementRepository, never()).save(any(InventoryMovement.class));
    }

    @Test
    void deductStockForExternalSystem_WithInactiveProduct_ShouldThrowException() {
        // Arrange
        testProduct.setActive(false);
        when(productRepository.findByCode("TEST001")).thenReturn(Optional.of(testProduct));

        // Act & Assert
        assertThrows(
                ProductNotFoundException.class,
                () -> {
                    inventoryService.deductStockForExternalSystem(externalRequest);
                });

        verify(productRepository).findByCode("TEST001");
        verify(productRepository, never()).save(any(Product.class));
        verify(inventoryMovementRepository, never()).save(any(InventoryMovement.class));
    }

    // ========== Product Retrieval Tests ==========
    @Test
    void getProductById_WithExistingId_ShouldReturnProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act
        Product result = inventoryService.getProductById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testProduct, result);
        verify(productRepository).findById(1L);
    }

    @Test
    void getProductById_WithNonExistentId_ShouldThrowException() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ProductNotFoundException.class,
                () -> {
                    inventoryService.getProductById(999L);
                });

        verify(productRepository).findById(999L);
    }

    @Test
    void getProductByCode_WithExistingCode_ShouldReturnProduct() {
        // Arrange
        when(productRepository.findByCode("TEST001")).thenReturn(Optional.of(testProduct));

        // Act
        Product result = inventoryService.getProductByCode("TEST001");

        // Assert
        assertNotNull(result);
        assertEquals(testProduct, result);
        verify(productRepository).findByCode("TEST001");
    }

    @Test
    void getProductByCode_WithNonExistentCode_ShouldThrowException() {
        // Arrange
        when(productRepository.findByCode("NONEXISTENT")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                ProductNotFoundException.class,
                () -> {
                    inventoryService.getProductByCode("NONEXISTENT");
                });

        verify(productRepository).findByCode("NONEXISTENT");
    }

    @Test
    void deleteProduct_WithExistingId_ShouldDeactivateProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        inventoryService.deleteProduct(1L);

        // Assert
        assertFalse(testProduct.getActive());
        verify(productRepository).findById(1L);
        verify(productRepository).save(testProduct);
    }
}
