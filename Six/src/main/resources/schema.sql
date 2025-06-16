-- Tabla de movimientos de stock
CREATE TABLE IF NOT EXISTS movimientos_stock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    variante_id BIGINT NOT NULL,
    usuario_id BIGINT,
    cantidad INT NOT NULL,
    tipo VARCHAR(10) NOT NULL,
    motivo VARCHAR(50) NOT NULL,
    motivo_detalle VARCHAR(100),
    fecha DATETIME NOT NULL,
    observaciones VARCHAR(500),
    FOREIGN KEY (variante_id) REFERENCES variantes_producto(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- Actualizar la estructura si la tabla ya existe
ALTER TABLE movimientos_stock 
    MODIFY COLUMN motivo VARCHAR(50),
    MODIFY COLUMN motivo_detalle VARCHAR(100),
    MODIFY COLUMN observaciones VARCHAR(500);

-- Tabla de devoluciones_venta
CREATE TABLE IF NOT EXISTS devoluciones_venta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cantidad INT NOT NULL,
    codigo VARCHAR(50),
    estado VARCHAR(20) NOT NULL,
    fecha_devolucion DATETIME NOT NULL,
    monto_devuelto DECIMAL(10,2) NOT NULL,
    motivo VARCHAR(50),
    detalle_venta_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    venta_id BIGINT NOT NULL,
    monto_total DECIMAL(10,2) NOT NULL,
    observaciones VARCHAR(500),
    FOREIGN KEY (detalle_venta_id) REFERENCES detalles_venta(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (venta_id) REFERENCES ventas(id)
);

-- Tabla de devoluciones_lote
CREATE TABLE IF NOT EXISTS devoluciones_lote (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    estado VARCHAR(20) NOT NULL,
    fecha_devolucion DATETIME NOT NULL,
    motivo VARCHAR(100),
    comentarios VARCHAR(1000),
    valor_total DECIMAL(10,2) NOT NULL,
    proveedor_id BIGINT,
    usuario_id BIGINT,
    FOREIGN KEY (lote_id) REFERENCES lotes_producto(id),
    FOREIGN KEY (proveedor_id) REFERENCES proveedores(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- Tabla de proveedores
CREATE TABLE IF NOT EXISTS proveedores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    ruc VARCHAR(20),
    direccion VARCHAR(200),
    telefono VARCHAR(20),
    email VARCHAR(100),
    contacto VARCHAR(100),
    telefono_contacto VARCHAR(20),
    observaciones VARCHAR(500),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_registro DATETIME NOT NULL,
    ultima_actualizacion DATETIME
);

-- Modificar la tabla lotes_producto para usar una clave foránea a proveedores
ALTER TABLE lotes_producto
    DROP COLUMN IF EXISTS proveedor,
    ADD COLUMN IF NOT EXISTS proveedor_id BIGINT,
    ADD FOREIGN KEY IF NOT EXISTS (proveedor_id) REFERENCES proveedores(id);

-- Agregar las tablas para el sistema de alertas

-- Tabla de alertas
CREATE TABLE IF NOT EXISTS alertas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL,
    titulo VARCHAR(100) NOT NULL,
    mensaje VARCHAR(500) NOT NULL,
    fecha_creacion DATETIME NOT NULL,
    fecha_lectura DATETIME,
    leida BOOLEAN NOT NULL DEFAULT false,
    prioridad VARCHAR(20) NOT NULL,
    producto_id BIGINT,
    variante_id BIGINT,
    usuario_id BIGINT,
    stock_actual INT,
    umbral INT,
    accion_requerida VARCHAR(200),
    FOREIGN KEY (producto_id) REFERENCES productos(id),
    FOREIGN KEY (variante_id) REFERENCES variantes_producto(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- Tabla de configuración de alertas
CREATE TABLE IF NOT EXISTS configuracion_alertas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alertas_stock_habilitadas BOOLEAN NOT NULL DEFAULT true,
    alertas_devoluciones_habilitadas BOOLEAN NOT NULL DEFAULT true,
    alertas_ventas_habilitadas BOOLEAN NOT NULL DEFAULT true,
    umbral_stock_bajo INT NOT NULL DEFAULT 10,
    umbral_stock_critico INT NOT NULL DEFAULT 5,
    pedidos_automaticos_habilitados BOOLEAN NOT NULL DEFAULT false,
    umbral_pedido_automatico INT NOT NULL DEFAULT 3,
    cantidad_pedido_automatico INT NOT NULL DEFAULT 10,
    notificaciones_email_habilitadas BOOLEAN NOT NULL DEFAULT false,
    notificaciones_push_habilitadas BOOLEAN NOT NULL DEFAULT true,
    emails_notificacion VARCHAR(500),
    enviar_digesto_diario BOOLEAN NOT NULL DEFAULT false,
    solo_alertas_criticas BOOLEAN NOT NULL DEFAULT false,
    categoria_id BIGINT,
    producto_id BIGINT,
    fecha_creacion DATETIME NOT NULL,
    fecha_actualizacion DATETIME,
    usuario_id BIGINT,
    FOREIGN KEY (categoria_id) REFERENCES categorias(id),
    FOREIGN KEY (producto_id) REFERENCES productos(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
); 