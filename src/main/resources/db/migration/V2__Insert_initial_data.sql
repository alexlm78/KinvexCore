-- Kinvex Inventory System - Initial Data
-- Version: 2.0
-- Description: Inserts initial data including default admin user and basic categories

-- Insertar usuario administrador por defecto
-- Contraseña: admin123 (debe cambiarse en producción)
-- Hash generado con BCrypt strength 12
INSERT INTO users (username, email, password_hash, role, active) VALUES
('admin', 'admin@kinvex.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj/VcQjyN/L6', 'ADMIN', true);

-- Insertar categorías básicas del sistema
INSERT INTO categories (name, description) VALUES
('Electrónicos', 'Productos electrónicos y tecnológicos'),
('Oficina', 'Suministros y equipos de oficina'),
('Limpieza', 'Productos de limpieza y mantenimiento'),
('Seguridad', 'Equipos y suministros de seguridad'),
('Herramientas', 'Herramientas y equipos de trabajo');

-- Insertar subcategorías
INSERT INTO categories (name, description, parent_id) VALUES
('Computadoras', 'Equipos de cómputo', (SELECT id FROM categories WHERE name = 'Electrónicos')),
('Periféricos', 'Dispositivos periféricos', (SELECT id FROM categories WHERE name = 'Electrónicos')),
('Papelería', 'Artículos de papelería', (SELECT id FROM categories WHERE name = 'Oficina')),
('Mobiliario', 'Muebles de oficina', (SELECT id FROM categories WHERE name = 'Oficina')),
('Productos Químicos', 'Químicos de limpieza', (SELECT id FROM categories WHERE name = 'Limpieza')),
('Equipos de Limpieza', 'Máquinas y equipos', (SELECT id FROM categories WHERE name = 'Limpieza')),
('Cámaras', 'Sistemas de videovigilancia', (SELECT id FROM categories WHERE name = 'Seguridad')),
('Control de Acceso', 'Sistemas de acceso', (SELECT id FROM categories WHERE name = 'Seguridad')),
('Herramientas Manuales', 'Herramientas de mano', (SELECT id FROM categories WHERE name = 'Herramientas')),
('Herramientas Eléctricas', 'Herramientas con motor', (SELECT id FROM categories WHERE name = 'Herramientas'));

-- Insertar proveedores de ejemplo
INSERT INTO suppliers (name, contact_person, email, phone, address, active) VALUES
('TechSupply Corp', 'Juan Pérez', 'ventas@techsupply.com', '+1-555-0101', '123 Tech Street, Ciudad Tech', true),
('Office Solutions', 'María García', 'contacto@officesolutions.com', '+1-555-0102', '456 Office Ave, Business City', true),
('CleanPro Supplies', 'Carlos López', 'pedidos@cleanpro.com', '+1-555-0103', '789 Clean Blvd, Industrial Zone', true),
('Security Systems Inc', 'Ana Rodríguez', 'info@securitysystems.com', '+1-555-0104', '321 Security Lane, Safe Town', true),
('Tools & Equipment Co', 'Roberto Martínez', 'sales@toolsequipment.com', '+1-555-0105', '654 Tools Road, Workshop City', true);

-- Insertar productos de ejemplo para demostración
INSERT INTO products (code, name, description, category_id, unit_price, current_stock, min_stock, max_stock, active) VALUES
-- Electrónicos - Computadoras
('COMP-001', 'Laptop Dell Inspiron 15', 'Laptop Dell Inspiron 15 3000, Intel i5, 8GB RAM, 256GB SSD',
 (SELECT id FROM categories WHERE name = 'Computadoras'), 899.99, 5, 2, 20, true),
('COMP-002', 'Desktop HP ProDesk', 'Desktop HP ProDesk 400 G7, Intel i7, 16GB RAM, 512GB SSD',
 (SELECT id FROM categories WHERE name = 'Computadoras'), 1299.99, 3, 1, 10, true),

-- Electrónicos - Periféricos
('PERI-001', 'Mouse Logitech MX Master', 'Mouse inalámbrico Logitech MX Master 3',
 (SELECT id FROM categories WHERE name = 'Periféricos'), 99.99, 15, 5, 50, true),
('PERI-002', 'Teclado Mecánico Corsair', 'Teclado mecánico Corsair K95 RGB',
 (SELECT id FROM categories WHERE name = 'Periféricos'), 199.99, 8, 3, 25, true),

-- Oficina - Papelería
('PAP-001', 'Papel Bond A4', 'Resma de papel bond A4 75g, 500 hojas',
 (SELECT id FROM categories WHERE name = 'Papelería'), 4.99, 100, 20, 500, true),
('PAP-002', 'Bolígrafos BIC Azul', 'Caja de bolígrafos BIC azul, 50 unidades',
 (SELECT id FROM categories WHERE name = 'Papelería'), 12.99, 25, 10, 100, true),

-- Limpieza - Productos Químicos
('LIMP-001', 'Desinfectante Multiusos', 'Desinfectante multiusos 1 litro',
 (SELECT id FROM categories WHERE name = 'Productos Químicos'), 8.99, 30, 10, 100, true),
('LIMP-002', 'Detergente Industrial', 'Detergente industrial concentrado 5 litros',
 (SELECT id FROM categories WHERE name = 'Productos Químicos'), 24.99, 12, 5, 50, true);

-- Insertar algunos movimientos de inventario iniciales (stock inicial)
INSERT INTO inventory_movements (product_id, movement_type, quantity, reference_type, notes, created_by) VALUES
((SELECT id FROM products WHERE code = 'COMP-001'), 'IN', 5, 'ADJUSTMENT', 'Stock inicial del sistema',
 (SELECT id FROM users WHERE username = 'admin')),
((SELECT id FROM products WHERE code = 'COMP-002'), 'IN', 3, 'ADJUSTMENT', 'Stock inicial del sistema',
 (SELECT id FROM users WHERE username = 'admin')),
((SELECT id FROM products WHERE code = 'PERI-001'), 'IN', 15, 'ADJUSTMENT', 'Stock inicial del sistema',
 (SELECT id FROM users WHERE username = 'admin')),
((SELECT id FROM products WHERE code = 'PERI-002'), 'IN', 8, 'ADJUSTMENT', 'Stock inicial del sistema',
 (SELECT id FROM users WHERE username = 'admin')),
((SELECT id FROM products WHERE code = 'PAP-001'), 'IN', 100, 'ADJUSTMENT', 'Stock inicial del sistema',
 (SELECT id FROM users WHERE username = 'admin')),
((SELECT id FROM products WHERE code = 'PAP-002'), 'IN', 25, 'ADJUSTMENT', 'Stock inicial del sistema',
 (SELECT id FROM users WHERE username = 'admin')),
((SELECT id FROM products WHERE code = 'LIMP-001'), 'IN', 30, 'ADJUSTMENT', 'Stock inicial del sistema',
 (SELECT id FROM users WHERE username = 'admin')),
((SELECT id FROM products WHERE code = 'LIMP-002'), 'IN', 12, 'ADJUSTMENT', 'Stock inicial del sistema',
 (SELECT id FROM users WHERE username = 'admin'));

-- Insertar registro de auditoría para la creación inicial
INSERT INTO audit_logs (user_id, action, entity_type, entity_id, new_values, ip_address) VALUES
((SELECT id FROM users WHERE username = 'admin'), 'SYSTEM_INIT', 'SYSTEM', 1,
 '{"action": "initial_data_load", "description": "Sistema inicializado con datos base"}', '127.0.0.1');
