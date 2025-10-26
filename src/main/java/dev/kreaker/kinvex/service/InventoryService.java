package dev.kreaker.kinvex.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.kreaker.kinvex.dto.inventory.CreateProductRequest;
import dev.kreaker.kinvex.dto.inventory.ExternalStockDeductionRequest;
import dev.kreaker.kinvex.dto.inventory.ExternalStockDeductionResponse;
import dev.kreaker.kinvex.dto.inventory.ProductSearchCriteria;
import dev.kreaker.kinvex.dto.inventory.StockUpdateRequest;
import dev.kreaker.kinvex.dto.inventory.UpdateProductRequest;
import dev.kreaker.kinvex.entity.Category;
import dev.kreaker.kinvex.entity.InventoryMovement;
import dev.kreaker.kinvex.entity.InventoryMovement.MovementType;
import dev.kreaker.kinvex.entity.Product;
import dev.kreaker.kinvex.entity.User;
import dev.kreaker.kinvex.exception.DuplicateProductCodeException;
import dev.kreaker.kinvex.exception.InsufficientStockException;
import dev.kreaker.kinvex.exception.ProductNotFoundException;
import dev.kreaker.kinvex.repository.CategoryRepository;
import dev.kreaker.kinvex.repository.InventoryMovementRepository;
import dev.kreaker.kinvex.repository.ProductRepository;
import dev.kreaker.kinvex.repository.UserRepository;

/**
 * Servicio de inventario que maneja operaciones CRUD de productos,
 * actualización de stock con validaciones y consultas por diferentes criterios.
 *
 * <p>
 * Implementa los requerimientos 1.1, 1.2, 1.3, 1.5 del sistema Kinvex.
 */
@Service
@Transactional
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final UserRepository userRepository;

    public InventoryService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            InventoryMovementRepository inventoryMovementRepository,
            UserRepository userRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.userRepository = userRepository;
    }

    // ========== CRUD Operations ==========
    /**
     * Crea un nuevo producto en el inventario. Requerimiento 1.1: Crear
     * productos con código único, nombre, descripción, precio y stock inicial
     *
     * @param request Datos del producto a crear
     * @return Producto creado
     * @throws DuplicateProductCodeException si el código ya existe
     */
    public Product createProduct(CreateProductRequest request) {
        logger.info("Creando producto con código: {}", request.getCode());

        // Validar que el código no exista (Requerimiento 1.4)
        if (productRepository.existsByCode(request.getCode())) {
            throw new DuplicateProductCodeException(request.getCode());
        }

        // Crear producto
        Product product = new Product();
        product.setCode(request.getCode());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setUnitPrice(request.getUnitPrice());
        product.setCurrentStock(request.getInitialStock());
        product.setMinStock(request.getMinStock());
        product.setMaxStock(request.getMaxStock());

        // Asignar categoría si se especifica
        if (request.getCategoryId() != null) {
            Category category
                    = categoryRepository
                            .findById(request.getCategoryId())
                            .orElseThrow(
                                    ()
                                    -> new RuntimeException(
                                            "Categoría no encontrada: "
                                            + request.getCategoryId()));
            product.setCategory(category);
        }

        // Guardar producto
        Product savedProduct = productRepository.save(product);

        // Crear movimiento de inventario inicial si hay stock inicial
        if (request.getInitialStock() > 0) {
            createInventoryMovement(
                    savedProduct,
                    MovementType.IN,
                    request.getInitialStock(),
                    InventoryMovement.ReferenceType.ADJUSTMENT,
                    null,
                    "SYSTEM",
                    "Stock inicial del producto");
        }

        logger.info(
                "Producto creado exitosamente: {} (ID: {})",
                savedProduct.getCode(),
                savedProduct.getId());
        return savedProduct;
    }

    /**
     * Actualiza un producto existente. Requerimiento 1.2: Actualizar
     * información de productos existentes
     *
     * @param productId ID del producto a actualizar
     * @param request Datos actualizados del producto
     * @return Producto actualizado
     * @throws ProductNotFoundException si el producto no existe
     */
    public Product updateProduct(Long productId, UpdateProductRequest request) {
        logger.info("Actualizando producto ID: {}", productId);

        Product product
                = productRepository
                        .findById(productId)
                        .orElseThrow(() -> new ProductNotFoundException(productId));

        // Actualizar campos
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setUnitPrice(request.getUnitPrice());
        product.setMinStock(request.getMinStock());
        product.setMaxStock(request.getMaxStock());

        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }

        // Actualizar categoría si se especifica
        if (request.getCategoryId() != null) {
            Category category
                    = categoryRepository
                            .findById(request.getCategoryId())
                            .orElseThrow(
                                    ()
                                    -> new RuntimeException(
                                            "Categoría no encontrada: "
                                            + request.getCategoryId()));
            product.setCategory(category);
        }

        Product updatedProduct = productRepository.save(product);
        logger.info("Producto actualizado exitosamente: {}", updatedProduct.getCode());
        return updatedProduct;
    }

    /**
     * Obtiene un producto por su ID.
     *
     * @param productId ID del producto
     * @return Producto encontrado
     * @throws ProductNotFoundException si el producto no existe
     */
    @Transactional(readOnly = true)
    public Product getProductById(Long productId) {
        return productRepository
                .findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    /**
     * Obtiene un producto por su código. Requerimiento 1.3: Consultar productos
     * por código
     *
     * @param code Código del producto
     * @return Producto encontrado
     * @throws ProductNotFoundException si el producto no existe
     */
    @Transactional(readOnly = true)
    public Product getProductByCode(String code) {
        return productRepository
                .findByCode(code)
                .orElseThrow(() -> new ProductNotFoundException("código", code));
    }

    /**
     * Obtiene todos los productos activos con paginación.
     *
     * @param pageable Configuración de paginación
     * @return Página de productos
     */
    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable);
    }

    /**
     * Elimina (desactiva) un producto.
     *
     * @param productId ID del producto a eliminar
     * @throws ProductNotFoundException si el producto no existe
     */
    public void deleteProduct(Long productId) {
        logger.info("Eliminando producto ID: {}", productId);

        Product product
                = productRepository
                        .findById(productId)
                        .orElseThrow(() -> new ProductNotFoundException(productId));

        product.setActive(false);
        productRepository.save(product);

        logger.info("Producto eliminado (desactivado): {}", product.getCode());
    }

    // ========== Stock Management ==========
    /**
     * Incrementa el stock de un producto. Requerimiento 1.5: Mantener historial
     * de cambios en la información de productos
     *
     * @param productId ID del producto
     * @param request Datos de la actualización de stock
     * @return Movimiento de inventario creado
     * @throws ProductNotFoundException si el producto no existe
     */
    public InventoryMovement increaseStock(Long productId, StockUpdateRequest request) {
        logger.info(
                "Incrementando stock del producto ID: {} en {} unidades",
                productId,
                request.getQuantity());

        Product product
                = productRepository
                        .findById(productId)
                        .orElseThrow(() -> new ProductNotFoundException(productId));

        // Actualizar stock
        product.increaseStock(request.getQuantity());
        productRepository.save(product);

        // Crear movimiento de inventario
        InventoryMovement movement
                = createInventoryMovement(
                        product,
                        MovementType.IN,
                        request.getQuantity(),
                        request.getReferenceType(),
                        request.getReferenceId(),
                        request.getSourceSystem(),
                        request.getNotes());

        logger.info("Stock incrementado exitosamente. Nuevo stock: {}", product.getCurrentStock());
        return movement;
    }

    /**
     * Decrementa el stock de un producto con validaciones. Requerimiento 1.5:
     * Mantener historial de cambios en la información de productos
     *
     * @param productId ID del producto
     * @param request Datos de la actualización de stock
     * @return Movimiento de inventario creado
     * @throws ProductNotFoundException si el producto no existe
     * @throws InsufficientStockException si no hay suficiente stock
     */
    public InventoryMovement decreaseStock(Long productId, StockUpdateRequest request) {
        logger.info(
                "Decrementando stock del producto ID: {} en {} unidades",
                productId,
                request.getQuantity());

        Product product
                = productRepository
                        .findById(productId)
                        .orElseThrow(() -> new ProductNotFoundException(productId));

        // Validar stock disponible
        if (!product.hasAvailableStock(request.getQuantity())) {
            throw new InsufficientStockException(
                    product.getId(),
                    product.getCode(),
                    product.getCurrentStock(),
                    request.getQuantity());
        }

        // Actualizar stock
        product.decreaseStock(request.getQuantity());
        productRepository.save(product);

        // Crear movimiento de inventario
        InventoryMovement movement
                = createInventoryMovement(
                        product,
                        MovementType.OUT,
                        request.getQuantity(),
                        request.getReferenceType(),
                        request.getReferenceId(),
                        request.getSourceSystem(),
                        request.getNotes());

        logger.info("Stock decrementado exitosamente. Nuevo stock: {}", product.getCurrentStock());
        return movement;
    }

    /**
     * Ajusta el stock de un producto a una cantidad específica.
     *
     * @param productId ID del producto
     * @param newStock Nueva cantidad de stock
     * @param notes Notas del ajuste
     * @return Movimiento de inventario creado (si hay cambio)
     * @throws ProductNotFoundException si el producto no existe
     */
    public InventoryMovement adjustStock(Long productId, Integer newStock, String notes) {
        logger.info("Ajustando stock del producto ID: {} a {} unidades", productId, newStock);

        Product product
                = productRepository
                        .findById(productId)
                        .orElseThrow(() -> new ProductNotFoundException(productId));

        Integer currentStock = product.getCurrentStock();
        Integer difference = newStock - currentStock;

        if (difference == 0) {
            logger.info("No hay cambios en el stock del producto: {}", product.getCode());
            return null;
        }

        // Actualizar stock
        product.setCurrentStock(newStock);
        productRepository.save(product);

        // Crear movimiento de inventario
        MovementType movementType = difference > 0 ? MovementType.IN : MovementType.OUT;
        Integer quantity = Math.abs(difference);

        InventoryMovement movement
                = createInventoryMovement(
                        product,
                        movementType,
                        quantity,
                        InventoryMovement.ReferenceType.ADJUSTMENT,
                        null,
                        "SYSTEM",
                        notes != null ? notes : "Ajuste de inventario");

        logger.info(
                "Stock ajustado exitosamente. Stock anterior: {}, nuevo stock: {}",
                currentStock,
                newStock);
        return movement;
    }

    // ========== Search and Query Methods ==========
    /**
     * Busca productos por diferentes criterios. Requerimiento 1.3: Consultar
     * productos por código, nombre o categoría
     *
     * @param criteria Criterios de búsqueda
     * @param pageable Configuración de paginación
     * @return Página de productos que coinciden con los criterios
     */
    @Transactional(readOnly = true)
    public Page<Product> searchProducts(ProductSearchCriteria criteria, Pageable pageable) {
        logger.debug("Buscando productos con criterios: {}", criteria);

        // Si no hay criterios específicos, retornar productos activos
        if (isEmptyCriteria(criteria)) {
            return productRepository.findByActiveTrue(pageable);
        }

        // Implementar búsqueda básica por nombre
        if (criteria.getName() != null && !criteria.getName().trim().isEmpty()) {
            return productRepository.findByNameContainingIgnoreCase(
                    criteria.getName().trim(), pageable);
        }

        // Para criterios más complejos, usar método personalizado
        return searchProductsWithComplexCriteria(criteria, pageable);
    }

    /**
     * Busca productos por nombre. Requerimiento 1.3: Consultar productos por
     * nombre
     *
     * @param name Nombre o parte del nombre del producto
     * @return Lista de productos que coinciden
     */
    @Transactional(readOnly = true)
    public List<Product> findProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Busca productos por código. Requerimiento 1.3: Consultar productos por
     * código
     *
     * @param code Código o parte del código del producto
     * @return Lista de productos que coinciden
     */
    @Transactional(readOnly = true)
    public List<Product> findProductsByCode(String code) {
        return productRepository.findByCodeContainingIgnoreCase(code);
    }

    /**
     * Busca productos por categoría. Requerimiento 1.3: Consultar productos por
     * categoría
     *
     * @param categoryId ID de la categoría
     * @return Lista de productos de la categoría
     */
    @Transactional(readOnly = true)
    public List<Product> findProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    /**
     * Obtiene productos con stock bajo.
     *
     * @return Lista de productos con stock menor o igual al mínimo
     */
    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    /**
     * Obtiene productos sin stock.
     *
     * @return Lista de productos con stock cero
     */
    @Transactional(readOnly = true)
    public List<Product> getOutOfStockProducts() {
        return productRepository.findOutOfStockProducts();
    }

    /**
     * Obtiene productos con exceso de stock.
     *
     * @return Lista de productos con stock mayor al máximo
     */
    @Transactional(readOnly = true)
    public List<Product> getOverStockProducts() {
        return productRepository.findOverStockProducts();
    }

    /**
     * Busca productos por rango de precios.
     *
     * @param minPrice Precio mínimo
     * @param maxPrice Precio máximo
     * @return Lista de productos en el rango de precios
     */
    @Transactional(readOnly = true)
    public List<Product> findProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findProductsByPriceRange(minPrice, maxPrice);
    }

    /**
     * Busca productos por rango de stock.
     *
     * @param minStock Stock mínimo
     * @param maxStock Stock máximo
     * @return Lista de productos en el rango de stock
     */
    @Transactional(readOnly = true)
    public List<Product> findProductsByStockRange(Integer minStock, Integer maxStock) {
        return productRepository.findProductsByStockRange(minStock, maxStock);
    }

    // ========== External API Methods ==========
    /**
     * Descuenta stock para sistemas externos de facturación.
     *
     * Implementa los requerimientos 2.1, 2.2, 2.3, 2.4: - 2.1: Endpoint REST
     * para descuento de inventario que reciba código de producto y cantidad -
     * 2.2: Reducir el stock del producto especificado cuando se reciba una
     * solicitud válida - 2.3: Retornar error HTTP 400 si el stock disponible es
     * insuficiente - 2.4: Registrar cada movimiento de salida con timestamp,
     * producto, cantidad y sistema origen
     *
     * @param request Solicitud de descuento de stock desde sistema externo
     * @return Respuesta con detalles del descuento realizado
     * @throws ProductNotFoundException si el producto no existe
     * @throws InsufficientStockException si no hay suficiente stock
     */
    public ExternalStockDeductionResponse deductStockForExternalSystem(ExternalStockDeductionRequest request) {
        logger.info("Procesando descuento de stock externo para producto: {} desde sistema: {}",
                request.getProductCode(), request.getSourceSystem());

        // Buscar producto por código (Requerimiento 2.1)
        Product product = productRepository
                .findByCode(request.getProductCode())
                .orElseThrow(() -> new ProductNotFoundException("código", request.getProductCode()));

        // Validar que el producto esté activo
        if (!product.getActive()) {
            throw new ProductNotFoundException("código", request.getProductCode());
        }

        // Guardar stock anterior para la respuesta
        Integer previousStock = product.getCurrentStock();

        // Validar stock disponible (Requerimiento 2.3)
        if (!product.hasAvailableStock(request.getQuantity())) {
            throw new InsufficientStockException(
                    product.getId(),
                    product.getCode(),
                    product.getCurrentStock(),
                    request.getQuantity());
        }

        // Reducir stock (Requerimiento 2.2)
        product.decreaseStock(request.getQuantity());
        productRepository.save(product);

        // Registrar movimiento de inventario (Requerimiento 2.4)
        InventoryMovement movement = createInventoryMovement(
                product,
                MovementType.OUT,
                request.getQuantity(),
                InventoryMovement.ReferenceType.SALE,
                null,
                request.getSourceSystem() != null ? request.getSourceSystem() : "EXTERNAL_BILLING",
                request.getNotes() != null ? request.getNotes() : "Descuento desde sistema de facturación externo");

        logger.info("Stock deducido exitosamente para producto: {}. Stock anterior: {}, nuevo stock: {}",
                product.getCode(), previousStock, product.getCurrentStock());

        // Crear respuesta con timestamp y detalles del movimiento (Requerimiento 2.4)
        return ExternalStockDeductionResponse.success(
                product.getCode(),
                product.getName(),
                request.getQuantity(),
                previousStock,
                product.getCurrentStock(),
                movement.getSourceSystem(),
                movement.getCreatedAt(),
                movement.getId());
    }

    // ========== Helper Methods ==========
    /**
     * Crea un movimiento de inventario.
     *
     * @param product Producto
     * @param movementType Tipo de movimiento
     * @param quantity Cantidad
     * @param referenceType Tipo de referencia
     * @param referenceId ID de referencia
     * @param sourceSystem Sistema origen
     * @param notes Notas
     * @return Movimiento de inventario creado
     */
    private InventoryMovement createInventoryMovement(
            Product product,
            MovementType movementType,
            Integer quantity,
            InventoryMovement.ReferenceType referenceType,
            Long referenceId,
            String sourceSystem,
            String notes) {

        InventoryMovement movement = new InventoryMovement();
        movement.setProduct(product);
        movement.setMovementType(movementType);
        movement.setQuantity(quantity);
        movement.setReferenceType(referenceType);
        movement.setReferenceId(referenceId);
        movement.setSourceSystem(sourceSystem);
        movement.setNotes(notes);

        // Obtener usuario actual si está disponible
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getName() != null) {
                Optional<User> user = userRepository.findByUsername(authentication.getName());
                user.ifPresent(movement::setCreatedBy);
            }
        } catch (Exception e) {
            logger.debug(
                    "No se pudo obtener el usuario actual para el movimiento de inventario", e);
        }

        return inventoryMovementRepository.save(movement);
    }

    /**
     * Verifica si los criterios de búsqueda están vacíos.
     */
    private boolean isEmptyCriteria(ProductSearchCriteria criteria) {
        return criteria.getCode() == null
                && criteria.getName() == null
                && criteria.getCategoryId() == null
                && criteria.getMinPrice() == null
                && criteria.getMaxPrice() == null
                && criteria.getMinStock() == null
                && criteria.getMaxStock() == null
                && criteria.getLowStock() == null
                && criteria.getOutOfStock() == null;
    }

    /**
     * Implementa búsqueda con criterios complejos. Esta es una implementación
     * básica que puede expandirse según necesidades.
     */
    private Page<Product> searchProductsWithComplexCriteria(
            ProductSearchCriteria criteria, Pageable pageable) {
        // Implementación básica - puede expandirse con Specifications o Criteria API

        if (criteria.getLowStock() != null && criteria.getLowStock()) {
            // Para low stock, retornamos productos activos ya que no hay paginación específica
            return productRepository.findByActiveTrue(pageable);
        } else if (criteria.getOutOfStock() != null && criteria.getOutOfStock()) {
            // Para out of stock, retornamos productos activos ya que no hay paginación específica
            return productRepository.findByActiveTrue(pageable);
        } else if (criteria.getCategoryId() != null) {
            // Buscar por categoría con paginación
            Category category = categoryRepository.findById(criteria.getCategoryId()).orElse(null);
            if (category != null) {
                return productRepository.findByCategoryAndActiveTrue(category, pageable);
            }
        } else if (criteria.getCode() != null) {
            // Para búsqueda por código, usar búsqueda por nombre como fallback con paginación
            return productRepository.findByNameContainingIgnoreCase(criteria.getCode(), pageable);
        }

        // Fallback a productos activos
        return productRepository.findByActiveTrue(pageable);
    }
}
