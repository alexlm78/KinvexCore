package dev.kreaker.kinvex.controller;

import dev.kreaker.kinvex.dto.inventory.CreateProductRequest;
import dev.kreaker.kinvex.dto.inventory.ProductSearchCriteria;
import dev.kreaker.kinvex.dto.inventory.StockUpdateRequest;
import dev.kreaker.kinvex.dto.inventory.UpdateProductRequest;
import dev.kreaker.kinvex.entity.InventoryMovement;
import dev.kreaker.kinvex.entity.Product;
import dev.kreaker.kinvex.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para operaciones de inventario y gestión de productos.
 *
 * <p>Implementa los requerimientos 1.1, 1.2, 1.3 del sistema Kinvex: - 1.1: Crear productos con
 * código único, nombre, descripción, precio y stock inicial - 1.2: Actualizar información de
 * productos existentes - 1.3: Consultar productos por código, nombre o categoría
 */
@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory", description = "Operaciones de gestión de inventario y productos")
public class InventoryController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // ========== Product CRUD Operations ==========
    /**
     * Obtiene todos los productos con paginación y filtros. Requerimiento 1.3: Consultar productos
     * por código, nombre o categoría
     *
     * @param pageable Configuración de paginación y ordenamiento
     * @param code Filtro por código de producto (opcional)
     * @param name Filtro por nombre de producto (opcional)
     * @param categoryId Filtro por ID de categoría (opcional)
     * @param minPrice Filtro por precio mínimo (opcional)
     * @param maxPrice Filtro por precio máximo (opcional)
     * @param minStock Filtro por stock mínimo (opcional)
     * @param maxStock Filtro por stock máximo (opcional)
     * @param lowStock Filtro para productos con stock bajo (opcional)
     * @param outOfStock Filtro para productos sin stock (opcional)
     * @param active Filtro por productos activos/inactivos (opcional)
     * @return Página de productos que coinciden con los filtros
     */
    @GetMapping("/products")
    @Operation(
            summary = "Obtener productos con paginación y filtros",
            description =
                    "Retorna una página de productos aplicando filtros opcionales de búsqueda")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Productos obtenidos exitosamente"),
                @ApiResponse(
                        responseCode = "400",
                        description = "Parámetros de consulta inválidos"),
                @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PreAuthorize("hasAnyRole('VIEWER', 'OPERATOR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Page<Product>> getProducts(
            @PageableDefault(size = 20, sort = "name") Pageable pageable,
            @Parameter(description = "Filtro por código de producto")
                    @RequestParam(required = false)
                    String code,
            @Parameter(description = "Filtro por nombre de producto")
                    @RequestParam(required = false)
                    String name,
            @Parameter(description = "Filtro por ID de categoría") @RequestParam(required = false)
                    Long categoryId,
            @Parameter(description = "Filtro por precio mínimo") @RequestParam(required = false)
                    BigDecimal minPrice,
            @Parameter(description = "Filtro por precio máximo") @RequestParam(required = false)
                    BigDecimal maxPrice,
            @Parameter(description = "Filtro por stock mínimo") @RequestParam(required = false)
                    Integer minStock,
            @Parameter(description = "Filtro por stock máximo") @RequestParam(required = false)
                    Integer maxStock,
            @Parameter(description = "Filtro para productos con stock bajo")
                    @RequestParam(required = false)
                    Boolean lowStock,
            @Parameter(description = "Filtro para productos sin stock")
                    @RequestParam(required = false)
                    Boolean outOfStock,
            @Parameter(description = "Filtro por productos activos/inactivos")
                    @RequestParam(required = false)
                    Boolean active) {

        logger.debug(
                "Obteniendo productos con filtros - página: {}, tamaño: {}",
                pageable.getPageNumber(),
                pageable.getPageSize());

        // Crear criterios de búsqueda
        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setCode(code);
        criteria.setName(name);
        criteria.setCategoryId(categoryId);
        criteria.setMinPrice(minPrice);
        criteria.setMaxPrice(maxPrice);
        criteria.setMinStock(minStock);
        criteria.setMaxStock(maxStock);
        criteria.setLowStock(lowStock);
        criteria.setOutOfStock(outOfStock);
        criteria.setActive(active);

        Page<Product> products = inventoryService.searchProducts(criteria, pageable);

        logger.debug(
                "Productos encontrados: {} de {} total",
                products.getNumberOfElements(),
                products.getTotalElements());

        return ResponseEntity.ok(products);
    }

    /**
     * Obtiene un producto por su ID.
     *
     * @param id ID del producto
     * @return Producto encontrado
     */
    @GetMapping("/products/{id}")
    @Operation(
            summary = "Obtener producto por ID",
            description = "Retorna un producto específico por su identificador")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Producto encontrado"),
                @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
                @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PreAuthorize("hasAnyRole('VIEWER', 'OPERATOR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Product> getProductById(
            @Parameter(description = "ID del producto") @PathVariable Long id) {

        logger.debug("Obteniendo producto por ID: {}", id);
        Product product = inventoryService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    /**
     * Obtiene un producto por su código. Requerimiento 1.3: Consultar productos por código
     *
     * @param code Código del producto
     * @return Producto encontrado
     */
    @GetMapping("/products/by-code/{code}")
    @Operation(
            summary = "Obtener producto por código",
            description = "Retorna un producto específico por su código único")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Producto encontrado"),
                @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
                @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PreAuthorize("hasAnyRole('VIEWER', 'OPERATOR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Product> getProductByCode(
            @Parameter(description = "Código del producto") @PathVariable String code) {

        logger.debug("Obteniendo producto por código: {}", code);
        Product product = inventoryService.getProductByCode(code);
        return ResponseEntity.ok(product);
    }

    /**
     * Crea un nuevo producto. Requerimiento 1.1: Crear productos con código único, nombre,
     * descripción, precio y stock inicial
     *
     * @param request Datos del producto a crear
     * @return Producto creado
     */
    @PostMapping("/products")
    @Operation(
            summary = "Crear nuevo producto",
            description = "Crea un nuevo producto en el inventario con código único")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "201", description = "Producto creado exitosamente"),
                @ApiResponse(
                        responseCode = "400",
                        description = "Datos de entrada inválidos o código duplicado"),
                @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PreAuthorize("hasAnyRole('OPERATOR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody CreateProductRequest request) {

        logger.info("Creando nuevo producto con código: {}", request.getCode());
        Product product = inventoryService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    /**
     * Actualiza un producto existente. Requerimiento 1.2: Actualizar información de productos
     * existentes
     *
     * @param id ID del producto a actualizar
     * @param request Datos actualizados del producto
     * @return Producto actualizado
     */
    @PutMapping("/products/{id}")
    @Operation(
            summary = "Actualizar producto",
            description = "Actualiza la información de un producto existente")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Producto actualizado exitosamente"),
                @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
                @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
                @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PreAuthorize("hasAnyRole('OPERATOR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Product> updateProduct(
            @Parameter(description = "ID del producto") @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {

        logger.info("Actualizando producto ID: {}", id);
        Product product = inventoryService.updateProduct(id, request);
        return ResponseEntity.ok(product);
    }

    /**
     * Elimina (desactiva) un producto.
     *
     * @param id ID del producto a eliminar
     * @return Respuesta vacía
     */
    @DeleteMapping("/products/{id}")
    @Operation(
            summary = "Eliminar producto",
            description = "Desactiva un producto del inventario (eliminación lógica)")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente"),
                @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
                @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID del producto") @PathVariable Long id) {

        logger.info("Eliminando producto ID: {}", id);
        inventoryService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // ========== Stock Management Operations ==========
    /**
     * Incrementa el stock de un producto.
     *
     * @param id ID del producto
     * @param request Datos de la actualización de stock
     * @return Movimiento de inventario creado
     */
    @PostMapping("/products/{id}/stock/increase")
    @Operation(
            summary = "Incrementar stock",
            description = "Incrementa el stock de un producto y registra el movimiento")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Stock incrementado exitosamente"),
                @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
                @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
                @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PreAuthorize("hasAnyRole('OPERATOR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<InventoryMovement> increaseStock(
            @Parameter(description = "ID del producto") @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request) {

        logger.info(
                "Incrementando stock del producto ID: {} en {} unidades",
                id,
                request.getQuantity());
        InventoryMovement movement = inventoryService.increaseStock(id, request);
        return ResponseEntity.ok(movement);
    }

    /**
     * Decrementa el stock de un producto.
     *
     * @param id ID del producto
     * @param request Datos de la actualización de stock
     * @return Movimiento de inventario creado
     */
    @PostMapping("/products/{id}/stock/decrease")
    @Operation(
            summary = "Decrementar stock",
            description =
                    "Decrementa el stock de un producto con validaciones y registra el movimiento")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Stock decrementado exitosamente"),
                @ApiResponse(
                        responseCode = "400",
                        description = "Stock insuficiente o datos inválidos"),
                @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
                @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PreAuthorize("hasAnyRole('OPERATOR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<InventoryMovement> decreaseStock(
            @Parameter(description = "ID del producto") @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request) {

        logger.info(
                "Decrementando stock del producto ID: {} en {} unidades",
                id,
                request.getQuantity());
        InventoryMovement movement = inventoryService.decreaseStock(id, request);
        return ResponseEntity.ok(movement);
    }

    /**
     * Ajusta el stock de un producto a una cantidad específica.
     *
     * @param id ID del producto
     * @param newStock Nueva cantidad de stock
     * @param notes Notas del ajuste (opcional)
     * @return Movimiento de inventario creado o respuesta vacía si no hay cambios
     */
    @PostMapping("/products/{id}/stock/adjust")
    @Operation(
            summary = "Ajustar stock",
            description = "Ajusta el stock de un producto a una cantidad específica")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Stock ajustado exitosamente"),
                @ApiResponse(responseCode = "204", description = "No hay cambios en el stock"),
                @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
                @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
                @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<InventoryMovement> adjustStock(
            @Parameter(description = "ID del producto") @PathVariable Long id,
            @Parameter(description = "Nueva cantidad de stock") @RequestParam Integer newStock,
            @Parameter(description = "Notas del ajuste") @RequestParam(required = false)
                    String notes) {

        logger.info("Ajustando stock del producto ID: {} a {} unidades", id, newStock);
        InventoryMovement movement = inventoryService.adjustStock(id, newStock, notes);

        if (movement == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(movement);
    }

    // ========== Special Query Operations ==========
    /**
     * Obtiene productos con stock bajo.
     *
     * @return Lista de productos con stock menor o igual al mínimo
     */
    @GetMapping("/products/low-stock")
    @Operation(
            summary = "Obtener productos con stock bajo",
            description = "Retorna productos cuyo stock actual es menor o igual al stock mínimo")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Productos con stock bajo obtenidos"),
                @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PreAuthorize("hasAnyRole('VIEWER', 'OPERATOR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<Product>> getLowStockProducts() {
        logger.debug("Obteniendo productos con stock bajo");
        List<Product> products = inventoryService.getLowStockProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Obtiene productos sin stock.
     *
     * @return Lista de productos con stock cero
     */
    @GetMapping("/products/out-of-stock")
    @Operation(
            summary = "Obtener productos sin stock",
            description = "Retorna productos cuyo stock actual es cero")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Productos sin stock obtenidos"),
                @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PreAuthorize("hasAnyRole('VIEWER', 'OPERATOR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<Product>> getOutOfStockProducts() {
        logger.debug("Obteniendo productos sin stock");
        List<Product> products = inventoryService.getOutOfStockProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Obtiene productos con exceso de stock.
     *
     * @return Lista de productos con stock mayor al máximo
     */
    @GetMapping("/products/over-stock")
    @Operation(
            summary = "Obtener productos con exceso de stock",
            description = "Retorna productos cuyo stock actual es mayor al stock máximo")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Productos con exceso de stock obtenidos"),
                @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PreAuthorize("hasAnyRole('VIEWER', 'OPERATOR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<Product>> getOverStockProducts() {
        logger.debug("Obteniendo productos con exceso de stock");
        List<Product> products = inventoryService.getOverStockProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Busca productos por rango de precios.
     *
     * @param minPrice Precio mínimo
     * @param maxPrice Precio máximo
     * @return Lista de productos en el rango de precios
     */
    @GetMapping("/products/by-price-range")
    @Operation(
            summary = "Buscar productos por rango de precios",
            description = "Retorna productos cuyo precio está dentro del rango especificado")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Productos encontrados"),
                @ApiResponse(responseCode = "400", description = "Parámetros de precio inválidos"),
                @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PreAuthorize("hasAnyRole('VIEWER', 'OPERATOR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<Product>> getProductsByPriceRange(
            @Parameter(description = "Precio mínimo") @RequestParam BigDecimal minPrice,
            @Parameter(description = "Precio máximo") @RequestParam BigDecimal maxPrice) {

        logger.debug("Buscando productos por rango de precios: {} - {}", minPrice, maxPrice);
        List<Product> products = inventoryService.findProductsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }

    /**
     * Busca productos por rango de stock.
     *
     * @param minStock Stock mínimo
     * @param maxStock Stock máximo
     * @return Lista de productos en el rango de stock
     */
    @GetMapping("/products/by-stock-range")
    @Operation(
            summary = "Buscar productos por rango de stock",
            description = "Retorna productos cuyo stock está dentro del rango especificado")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Productos encontrados"),
                @ApiResponse(responseCode = "400", description = "Parámetros de stock inválidos"),
                @ApiResponse(responseCode = "403", description = "Acceso denegado")
            })
    @PreAuthorize("hasAnyRole('VIEWER', 'OPERATOR', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<Product>> getProductsByStockRange(
            @Parameter(description = "Stock mínimo") @RequestParam Integer minStock,
            @Parameter(description = "Stock máximo") @RequestParam Integer maxStock) {

        logger.debug("Buscando productos por rango de stock: {} - {}", minStock, maxStock);
        List<Product> products = inventoryService.findProductsByStockRange(minStock, maxStock);
        return ResponseEntity.ok(products);
    }
}
