-- =============================================================================
-- BASE DE DATOS: S.P.T. - CLIENTE NUEVO (TALLER MECÁNICO)
-- =============================================================================
-- Script de creación desde cero, listo para ejecutar en un servidor MySQL 8.0+.
-- Reutiliza el schema base del sistema S.P.T. original con las siguientes
-- diferencias adaptadas al nuevo cliente:
--
--   * SIN módulo de stock (no se crean stock_pieza ni movimiento_stock).
--   * `presupuesto` incluye `ocultar_precios_items` y NO tiene numero/tipo_factura.
--   * `orden_trabajo` NO tiene numero/tipo_factura.
--   * `cliente` incluye `whatsapp`.
--   * Nuevas tablas: empleado, pago_empleado, ot_empleado, factura,
--     mensaje_whatsapp, evento, configuracion_taller.
--   * Vistas de saldo y reportes de ganancias / mano de obra.
--
-- Las tablas se crean en orden de dependencia de FKs, por lo que NO hace falta
-- desactivar FOREIGN_KEY_CHECKS.
-- =============================================================================

-- Opcional: crear la base de datos. Comentar si ya existe una creada.
CREATE DATABASE IF NOT EXISTS `spt_taller`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;
USE `spt_taller`;

SET @OLD_SQL_MODE = @@SQL_MODE;
SET SQL_MODE = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- =============================================================================
-- 1. CATÁLOGOS BASE (sin dependencias)
-- =============================================================================

CREATE TABLE `cliente` (
    `id_cliente` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `nombre` VARCHAR(255) DEFAULT NULL,
    `apellido` VARCHAR(255) DEFAULT NULL,
    `telefono` VARCHAR(255) DEFAULT NULL,
    `whatsapp` VARCHAR(255) DEFAULT NULL,
    `email` VARCHAR(255) DEFAULT NULL,
    `direccion` VARCHAR(255) DEFAULT NULL,
    `activo` TINYINT(1) NOT NULL DEFAULT 1,
    `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_modificacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id_cliente`),
    KEY `ix_cliente_nombre_apellido` (`apellido`, `nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `pieza` (
    `id_pieza` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `nombre` VARCHAR(255) DEFAULT NULL,
    `marca` VARCHAR(255) DEFAULT NULL,
    `medidas` VARCHAR(255) DEFAULT NULL,
    `calidad` VARCHAR(255) DEFAULT NULL,
    `precio_unitario` DECIMAL(14,2) DEFAULT NULL,
    `activo` TINYINT(1) NOT NULL DEFAULT 1,
    `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_modificacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id_pieza`),
    KEY `ix_pieza_busqueda` (`nombre`, `marca`),
    CONSTRAINT `pieza_chk_1` CHECK (`precio_unitario` IS NULL OR `precio_unitario` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `tipo_servicio` (
    `id_servicio` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `nombre` VARCHAR(255) DEFAULT NULL,
    `descripcion` VARCHAR(255) DEFAULT NULL,
    `precio_base` DECIMAL(14,2) DEFAULT NULL,
    `activo` TINYINT(1) NOT NULL DEFAULT 1,
    `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_modificacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id_servicio`),
    UNIQUE KEY `uk_servicio_nombre` (`nombre`),
    CONSTRAINT `tipo_servicio_chk_1` CHECK (`precio_base` IS NULL OR `precio_base` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `empleado` (
    `id_empleado` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `nombre` VARCHAR(255) NOT NULL,
    `apellido` VARCHAR(255) NOT NULL,
    `dni` VARCHAR(32) DEFAULT NULL,
    `telefono` VARCHAR(255) DEFAULT NULL,
    `email` VARCHAR(255) DEFAULT NULL,
    `rol_taller` VARCHAR(64) DEFAULT NULL,
    `sueldo_base` DECIMAL(14,2) DEFAULT NULL,
    `activo` TINYINT(1) NOT NULL DEFAULT 1,
    `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_modificacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id_empleado`),
    UNIQUE KEY `uk_empleado_dni` (`dni`),
    KEY `ix_empleado_nombre_apellido` (`apellido`, `nombre`),
    CONSTRAINT `empleado_chk_sueldo` CHECK (`sueldo_base` IS NULL OR `sueldo_base` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `usuario` (
    `id_usuario` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(255) DEFAULT NULL,
    `password_hash` VARCHAR(255) NOT NULL,
    `rol` ENUM('ADMIN','OPERADOR') NOT NULL DEFAULT 'OPERADOR',
    `activo` TINYINT(1) NOT NULL DEFAULT 1,
    `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_modificacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id_usuario`),
    UNIQUE KEY `uk_usuario_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `configuracion_taller` (
    `id_configuracion` TINYINT UNSIGNED NOT NULL DEFAULT 1,
    `nombre_taller` VARCHAR(255) DEFAULT NULL,
    `telefono` VARCHAR(64) DEFAULT NULL,
    `direccion` VARCHAR(255) DEFAULT NULL,
    `email` VARCHAR(255) DEFAULT NULL,
    `cuit` VARCHAR(32) DEFAULT NULL,
    `logo_path` VARCHAR(500) DEFAULT NULL,
    `plantilla_wp_recordatorio` TEXT,
    `horas_anticipacion_wp` INT NOT NULL DEFAULT 24,
    `wp_habilitado` TINYINT(1) NOT NULL DEFAULT 0,
    `fecha_modificacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id_configuracion`),
    CONSTRAINT `cfg_chk_singleton` CHECK (`id_configuracion` = 1),
    CONSTRAINT `cfg_chk_horas` CHECK (`horas_anticipacion_wp` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =============================================================================
-- 2. TABLAS QUE DEPENDEN DE CLIENTE
-- =============================================================================

CREATE TABLE `vehiculo` (
    `id_vehiculo` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `id_cliente` BIGINT UNSIGNED NOT NULL,
    `patente` VARCHAR(255) DEFAULT NULL,
    `marca` VARCHAR(255) DEFAULT NULL,
    `modelo` VARCHAR(255) DEFAULT NULL,
    `anio` INT DEFAULT NULL,
    `kilometraje` INT DEFAULT NULL,
    `activo` TINYINT(1) NOT NULL DEFAULT 1,
    `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_modificacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id_vehiculo`),
    UNIQUE KEY `uk_vehiculo_patente` (`patente`),
    KEY `ix_vehiculo_cliente` (`id_cliente`),
    CONSTRAINT `fk_vehiculo_cliente` FOREIGN KEY (`id_cliente`)
        REFERENCES `cliente` (`id_cliente`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `cita` (
    `id_cita` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `id_cliente` BIGINT UNSIGNED NOT NULL,
    `id_vehiculo` BIGINT UNSIGNED NOT NULL,
    `fecha_hora` DATETIME NOT NULL,
    `tipo_cita` ENUM('ENTREGA','VERIFICACION','ADQUISICION') NOT NULL,
    `observaciones` VARCHAR(255) DEFAULT NULL,
    `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id_cita`),
    UNIQUE KEY `uk_cita_vehiculo_fechahora` (`id_vehiculo`, `fecha_hora`),
    KEY `ix_cita_fecha` (`fecha_hora`),
    KEY `ix_cita_cliente_fecha` (`id_cliente`, `fecha_hora`),
    CONSTRAINT `fk_cita_cliente` FOREIGN KEY (`id_cliente`)
        REFERENCES `cliente` (`id_cliente`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_cita_vehiculo` FOREIGN KEY (`id_vehiculo`)
        REFERENCES `vehiculo` (`id_vehiculo`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =============================================================================
-- 3. PRESUPUESTO Y DETALLES
-- =============================================================================

CREATE TABLE `presupuesto` (
    `id_presupuesto` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `numero_presupuesto` VARCHAR(255) DEFAULT NULL,
    `id_cliente` BIGINT UNSIGNED NOT NULL,
    `id_vehiculo` BIGINT UNSIGNED NOT NULL,
    `resumen` LONGTEXT,
    `total` DECIMAL(14,2) DEFAULT NULL,
    `estado` ENUM('ACTIVO','ACEPTADO','ARCHIVADO') NOT NULL,
    `ocultar_precios_items` TINYINT(1) NOT NULL DEFAULT 0,
    `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_modificacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id_presupuesto`),
    UNIQUE KEY `uk_presupuesto_numero` (`numero_presupuesto`),
    KEY `ix_presupuesto_cliente_fecha` (`id_cliente`, `fecha_creacion`),
    KEY `ix_presupuesto_vehiculo_fecha` (`id_vehiculo`, `fecha_creacion`),
    CONSTRAINT `fk_presupuesto_cliente` FOREIGN KEY (`id_cliente`)
        REFERENCES `cliente` (`id_cliente`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_presupuesto_vehiculo` FOREIGN KEY (`id_vehiculo`)
        REFERENCES `vehiculo` (`id_vehiculo`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `presupuesto_chk_1` CHECK (`total` IS NULL OR `total` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `presupuesto_detalle` (
    `id_detalle_presupuesto` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `id_presupuesto` BIGINT UNSIGNED NOT NULL,
    `tipo_item` ENUM('PIEZA','SERVICIO') NOT NULL,
    `id_pieza` BIGINT UNSIGNED DEFAULT NULL,
    `id_servicio` BIGINT UNSIGNED DEFAULT NULL,
    `descripcion_item` LONGTEXT,
    `cantidad` INT NOT NULL DEFAULT 1,
    `precio_unitario` DECIMAL(14,2) DEFAULT NULL,
    `subtotal` DECIMAL(14,2) DEFAULT NULL,
    PRIMARY KEY (`id_detalle_presupuesto`),
    KEY `ix_pd_presupuesto` (`id_presupuesto`),
    KEY `fk_pd_pieza` (`id_pieza`),
    KEY `fk_pd_servicio` (`id_servicio`),
    CONSTRAINT `fk_pd_presupuesto` FOREIGN KEY (`id_presupuesto`)
        REFERENCES `presupuesto` (`id_presupuesto`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_pd_pieza` FOREIGN KEY (`id_pieza`)
        REFERENCES `pieza` (`id_pieza`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_pd_servicio` FOREIGN KEY (`id_servicio`)
        REFERENCES `tipo_servicio` (`id_servicio`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `presupuesto_detalle_chk_1` CHECK (`cantidad` > 0),
    CONSTRAINT `presupuesto_detalle_chk_2` CHECK (`precio_unitario` IS NULL OR `precio_unitario` >= 0),
    CONSTRAINT `presupuesto_detalle_chk_3` CHECK (`subtotal` IS NULL OR `subtotal` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =============================================================================
-- 4. ORDEN DE TRABAJO Y DETALLES
-- =============================================================================

CREATE TABLE `orden_trabajo` (
    `id_ot` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `numero_ot` VARCHAR(255) DEFAULT NULL,
    `id_presupuesto` BIGINT UNSIGNED NOT NULL,
    `id_cliente` BIGINT UNSIGNED NOT NULL,
    `id_vehiculo` BIGINT UNSIGNED NOT NULL,
    `resumen_trabajo` LONGTEXT,
    `estado` ENUM('EN_ESPERA','EN_PROCESO','FINALIZADA_COMPLETA','FINALIZADA_INCOMPLETA')
        NOT NULL DEFAULT 'EN_ESPERA',
    `total` DECIMAL(14,2) DEFAULT NULL,
    `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_finalizacion` DATETIME DEFAULT NULL,
    `fecha_modificacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id_ot`),
    UNIQUE KEY `uk_ot_presupuesto` (`id_presupuesto`),
    UNIQUE KEY `uk_ot_numero` (`numero_ot`),
    KEY `ix_ot_cliente_estado_fecha` (`id_cliente`, `estado`, `fecha_creacion`),
    KEY `ix_ot_vehiculo_estado_fecha` (`id_vehiculo`, `estado`, `fecha_creacion`),
    CONSTRAINT `fk_ot_presupuesto` FOREIGN KEY (`id_presupuesto`)
        REFERENCES `presupuesto` (`id_presupuesto`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_ot_cliente` FOREIGN KEY (`id_cliente`)
        REFERENCES `cliente` (`id_cliente`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_ot_vehiculo` FOREIGN KEY (`id_vehiculo`)
        REFERENCES `vehiculo` (`id_vehiculo`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `orden_trabajo_chk_1` CHECK (`total` IS NULL OR `total` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `ot_detalle` (
    `id_detalle_ot` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `id_ot` BIGINT UNSIGNED NOT NULL,
    `tipo_item` ENUM('PIEZA','SERVICIO') NOT NULL,
    `id_pieza` BIGINT UNSIGNED DEFAULT NULL,
    `id_servicio` BIGINT UNSIGNED DEFAULT NULL,
    `descripcion_item` LONGTEXT,
    `cantidad` INT NOT NULL DEFAULT 1,
    `precio_unitario` DECIMAL(14,2) DEFAULT NULL,
    `subtotal` DECIMAL(14,2) DEFAULT NULL,
    PRIMARY KEY (`id_detalle_ot`),
    KEY `ix_otd_ot` (`id_ot`),
    KEY `fk_otd_pieza` (`id_pieza`),
    KEY `fk_otd_servicio` (`id_servicio`),
    CONSTRAINT `fk_otd_ot` FOREIGN KEY (`id_ot`)
        REFERENCES `orden_trabajo` (`id_ot`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_otd_pieza` FOREIGN KEY (`id_pieza`)
        REFERENCES `pieza` (`id_pieza`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_otd_servicio` FOREIGN KEY (`id_servicio`)
        REFERENCES `tipo_servicio` (`id_servicio`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `ot_detalle_chk_1` CHECK (`cantidad` > 0),
    CONSTRAINT `ot_detalle_chk_2` CHECK (`precio_unitario` IS NULL OR `precio_unitario` >= 0),
    CONSTRAINT `ot_detalle_chk_3` CHECK (`subtotal` IS NULL OR `subtotal` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `ot_empleado` (
    `id_ot_empleado` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `id_ot` BIGINT UNSIGNED NOT NULL,
    `id_empleado` BIGINT UNSIGNED NOT NULL,
    `costo_mano_obra` DECIMAL(14,2) NOT NULL DEFAULT 0,
    `horas_trabajadas` DECIMAL(6,2) DEFAULT NULL,
    `observaciones` VARCHAR(255) DEFAULT NULL,
    `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id_ot_empleado`),
    UNIQUE KEY `uk_otemp_ot_empleado` (`id_ot`, `id_empleado`),
    KEY `ix_otemp_empleado` (`id_empleado`),
    CONSTRAINT `fk_otemp_ot` FOREIGN KEY (`id_ot`)
        REFERENCES `orden_trabajo` (`id_ot`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_otemp_empleado` FOREIGN KEY (`id_empleado`)
        REFERENCES `empleado` (`id_empleado`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `otemp_chk_costo` CHECK (`costo_mano_obra` >= 0),
    CONSTRAINT `otemp_chk_horas` CHECK (`horas_trabajadas` IS NULL OR `horas_trabajadas` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =============================================================================
-- 5. PAGOS DE CLIENTES Y DE EMPLEADOS
-- =============================================================================

CREATE TABLE `pago` (
    `id_pago` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `id_ot` BIGINT UNSIGNED NOT NULL,
    `monto` DECIMAL(14,2) NOT NULL,
    `metodo_pago` ENUM('EFECTIVO','TRANSFERENCIA','TARJETA','CHEQUE') NOT NULL,
    `fecha_pago` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `observaciones` VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (`id_pago`),
    KEY `ix_pago_ot_fecha` (`id_ot`, `fecha_pago`),
    CONSTRAINT `fk_pago_ot` FOREIGN KEY (`id_ot`)
        REFERENCES `orden_trabajo` (`id_ot`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `pago_chk_1` CHECK (`monto` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `pago_empleado` (
    `id_pago_empleado` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `id_empleado` BIGINT UNSIGNED NOT NULL,
    `monto` DECIMAL(14,2) NOT NULL,
    `periodo_desde` DATE DEFAULT NULL,
    `periodo_hasta` DATE DEFAULT NULL,
    `fecha_pago` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `metodo_pago` ENUM('EFECTIVO','TRANSFERENCIA','TARJETA','CHEQUE') NOT NULL DEFAULT 'EFECTIVO',
    `observaciones` VARCHAR(255) DEFAULT NULL,
    `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id_pago_empleado`),
    KEY `ix_pago_emp_empleado_fecha` (`id_empleado`, `fecha_pago`),
    KEY `ix_pago_emp_fecha` (`fecha_pago`),
    CONSTRAINT `fk_pago_emp_empleado` FOREIGN KEY (`id_empleado`)
        REFERENCES `empleado` (`id_empleado`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `pago_emp_chk_monto` CHECK (`monto` > 0),
    CONSTRAINT `pago_emp_chk_periodo` CHECK (
        `periodo_desde` IS NULL OR `periodo_hasta` IS NULL OR `periodo_desde` <= `periodo_hasta`
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =============================================================================
-- 6. FACTURA
-- =============================================================================

CREATE TABLE `factura` (
    `id_factura` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `numero_factura` VARCHAR(64) NOT NULL,
    `tipo_factura` ENUM('A','B','C','X','OTRO') NOT NULL DEFAULT 'B',
    `id_cliente` BIGINT UNSIGNED NOT NULL,
    `id_presupuesto` BIGINT UNSIGNED DEFAULT NULL,
    `id_ot` BIGINT UNSIGNED DEFAULT NULL,
    `subtotal` DECIMAL(14,2) NOT NULL DEFAULT 0,
    `impuestos` DECIMAL(14,2) NOT NULL DEFAULT 0,
    `total` DECIMAL(14,2) NOT NULL DEFAULT 0,
    `estado` ENUM('EMITIDA','ANULADA') NOT NULL DEFAULT 'EMITIDA',
    `fecha_emision` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `observaciones` VARCHAR(500) DEFAULT NULL,
    `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_modificacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id_factura`),
    UNIQUE KEY `uk_factura_numero_tipo` (`tipo_factura`, `numero_factura`),
    KEY `ix_factura_cliente_fecha` (`id_cliente`, `fecha_emision`),
    KEY `ix_factura_presupuesto` (`id_presupuesto`),
    KEY `ix_factura_ot` (`id_ot`),
    CONSTRAINT `fk_factura_cliente` FOREIGN KEY (`id_cliente`)
        REFERENCES `cliente` (`id_cliente`)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    -- NOTA: MySQL no permite ON UPDATE CASCADE (ni otras acciones referenciales)
    -- sobre columnas usadas por un CHECK constraint. Las PKs son AUTO_INCREMENT
    -- y nunca se actualizan, así que NO ACTION no cambia nada en la práctica.
    CONSTRAINT `fk_factura_presupuesto` FOREIGN KEY (`id_presupuesto`)
        REFERENCES `presupuesto` (`id_presupuesto`)
        ON DELETE RESTRICT ON UPDATE NO ACTION,
    CONSTRAINT `fk_factura_ot` FOREIGN KEY (`id_ot`)
        REFERENCES `orden_trabajo` (`id_ot`)
        ON DELETE RESTRICT ON UPDATE NO ACTION,
    CONSTRAINT `factura_chk_origen` CHECK (
        `id_presupuesto` IS NOT NULL OR `id_ot` IS NOT NULL
    ),
    CONSTRAINT `factura_chk_subtotal` CHECK (`subtotal` >= 0),
    CONSTRAINT `factura_chk_impuestos` CHECK (`impuestos` >= 0),
    CONSTRAINT `factura_chk_total` CHECK (`total` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =============================================================================
-- 7. MENSAJE WHATSAPP
-- =============================================================================

CREATE TABLE `mensaje_whatsapp` (
    `id_mensaje` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `id_cita` BIGINT UNSIGNED NOT NULL,
    `telefono_destino` VARCHAR(64) NOT NULL,
    `plantilla` VARCHAR(64) DEFAULT NULL,
    `contenido` TEXT NOT NULL,
    `estado` ENUM('PENDIENTE','ENVIADO','FALLIDO','CANCELADO') NOT NULL DEFAULT 'PENDIENTE',
    `intentos` INT NOT NULL DEFAULT 0,
    `fecha_programada` DATETIME DEFAULT NULL,
    `fecha_envio` DATETIME DEFAULT NULL,
    `error_mensaje` VARCHAR(500) DEFAULT NULL,
    `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_modificacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id_mensaje`),
    KEY `ix_mwp_cita` (`id_cita`),
    KEY `ix_mwp_estado_programada` (`estado`, `fecha_programada`),
    CONSTRAINT `fk_mwp_cita` FOREIGN KEY (`id_cita`)
        REFERENCES `cita` (`id_cita`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `mwp_chk_intentos` CHECK (`intentos` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =============================================================================
-- 8. EVENTO (calendario genérico)
-- =============================================================================

CREATE TABLE `evento` (
    `id_evento` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `titulo` VARCHAR(255) NOT NULL,
    `descripcion` TEXT,
    `tipo` ENUM('RECORDATORIO','BLOQUEO','MANTENIMIENTO','FERIADO','OTRO')
        NOT NULL DEFAULT 'OTRO',
    `fecha_inicio` DATETIME NOT NULL,
    `fecha_fin` DATETIME DEFAULT NULL,
    `todo_el_dia` TINYINT(1) NOT NULL DEFAULT 0,
    `color` VARCHAR(16) DEFAULT NULL,
    `id_empleado` BIGINT UNSIGNED DEFAULT NULL,
    `fecha_creacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `fecha_modificacion` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id_evento`),
    KEY `ix_evento_fecha_inicio` (`fecha_inicio`),
    KEY `ix_evento_tipo_fecha` (`tipo`, `fecha_inicio`),
    KEY `ix_evento_empleado` (`id_empleado`),
    CONSTRAINT `fk_evento_empleado` FOREIGN KEY (`id_empleado`)
        REFERENCES `empleado` (`id_empleado`) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `evento_chk_fechas` CHECK (
        `fecha_fin` IS NULL OR `fecha_fin` >= `fecha_inicio`
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =============================================================================
-- 9. VISTAS DE NEGOCIO
-- =============================================================================

CREATE OR REPLACE VIEW `vista_saldo_cliente` AS
SELECT
    c.id_cliente,
    c.nombre,
    c.apellido,
    COALESCE(ot_sum.total_facturado, 0) AS total_facturado,
    COALESCE(pago_sum.total_pagado, 0)  AS total_pagado,
    COALESCE(ot_sum.total_facturado, 0) - COALESCE(pago_sum.total_pagado, 0) AS saldo_pendiente
FROM cliente c
LEFT JOIN (
    SELECT id_cliente, SUM(COALESCE(total, 0)) AS total_facturado
    FROM orden_trabajo
    GROUP BY id_cliente
) ot_sum ON ot_sum.id_cliente = c.id_cliente
LEFT JOIN (
    SELECT ot.id_cliente, SUM(p.monto) AS total_pagado
    FROM pago p
    INNER JOIN orden_trabajo ot ON ot.id_ot = p.id_ot
    GROUP BY ot.id_cliente
) pago_sum ON pago_sum.id_cliente = c.id_cliente;

CREATE OR REPLACE VIEW `vista_saldo_ot` AS
SELECT
    ot.id_ot,
    ot.numero_ot,
    ot.id_cliente,
    ot.id_vehiculo,
    ot.estado,
    COALESCE(ot.total, 0) AS total_ot,
    COALESCE(p.total_pagado, 0) AS total_pagado,
    COALESCE(ot.total, 0) - COALESCE(p.total_pagado, 0) AS saldo_pendiente
FROM orden_trabajo ot
LEFT JOIN (
    SELECT id_ot, SUM(monto) AS total_pagado
    FROM pago
    GROUP BY id_ot
) p ON p.id_ot = ot.id_ot;

CREATE OR REPLACE VIEW `vista_movimiento_caja` AS
SELECT
    fecha_pago AS fecha,
    monto,
    'INGRESO_OT' AS tipo,
    CONCAT('Pago OT #', id_ot) AS concepto
FROM pago
UNION ALL
SELECT
    fecha_pago AS fecha,
    -monto AS monto,
    'EGRESO_SUELDO' AS tipo,
    CONCAT('Sueldo empleado #', id_empleado) AS concepto
FROM pago_empleado;

CREATE OR REPLACE VIEW `vista_ganancias_diarias` AS
SELECT
    fecha,
    SUM(CASE WHEN monto > 0 THEN monto ELSE 0 END)  AS ingresos_brutos,
    SUM(CASE WHEN monto < 0 THEN -monto ELSE 0 END) AS total_sueldos,
    SUM(monto) AS ganancia_neta
FROM (
    SELECT DATE(fecha_pago) AS fecha, monto FROM pago
    UNION ALL
    SELECT DATE(fecha_pago) AS fecha, -monto FROM pago_empleado
) m
GROUP BY fecha;

CREATE OR REPLACE VIEW `vista_ganancias_semanales` AS
SELECT
    YEARWEEK(fecha, 3) AS anio_semana,
    MIN(fecha) AS desde,
    MAX(fecha) AS hasta,
    SUM(CASE WHEN monto > 0 THEN monto ELSE 0 END)  AS ingresos_brutos,
    SUM(CASE WHEN monto < 0 THEN -monto ELSE 0 END) AS total_sueldos,
    SUM(monto) AS ganancia_neta
FROM (
    SELECT DATE(fecha_pago) AS fecha, monto FROM pago
    UNION ALL
    SELECT DATE(fecha_pago) AS fecha, -monto FROM pago_empleado
) m
GROUP BY YEARWEEK(fecha, 3);

CREATE OR REPLACE VIEW `vista_ganancias_mensuales` AS
SELECT
    DATE_FORMAT(fecha, '%Y-%m') AS anio_mes,
    SUM(CASE WHEN monto > 0 THEN monto ELSE 0 END)  AS ingresos_brutos,
    SUM(CASE WHEN monto < 0 THEN -monto ELSE 0 END) AS total_sueldos,
    SUM(monto) AS ganancia_neta
FROM (
    SELECT DATE(fecha_pago) AS fecha, monto FROM pago
    UNION ALL
    SELECT DATE(fecha_pago) AS fecha, -monto FROM pago_empleado
) m
GROUP BY DATE_FORMAT(fecha, '%Y-%m');

CREATE OR REPLACE VIEW `vista_mano_obra_ot` AS
SELECT
    ot.id_ot,
    ot.numero_ot,
    COALESCE(ot.total, 0) AS total_ot,
    COALESCE(SUM(oe.costo_mano_obra), 0) AS costo_mano_obra,
    COALESCE(ot.total, 0) - COALESCE(SUM(oe.costo_mano_obra), 0) AS subtotal_neto_ot
FROM orden_trabajo ot
LEFT JOIN ot_empleado oe ON oe.id_ot = ot.id_ot
GROUP BY ot.id_ot, ot.numero_ot, ot.total;

-- =============================================================================
-- 10. DATOS SEMILLA
-- =============================================================================

INSERT INTO `configuracion_taller`
    (`id_configuracion`, `nombre_taller`, `plantilla_wp_recordatorio`,
     `horas_anticipacion_wp`, `wp_habilitado`)
VALUES
    (1, 'Taller Mecánico',
     'Hola {cliente}, te recordamos tu turno para el vehículo {vehiculo} el {fecha} a las {hora}. ¡Te esperamos!',
     24, 0);

-- Usuario admin por defecto (cambiar el hash en producción).
-- Hash de ejemplo BCrypt para "admin1234".
INSERT INTO `usuario` (`username`, `password_hash`, `rol`, `activo`)
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', 1);

-- =============================================================================
SET SQL_MODE = @OLD_SQL_MODE;
-- FIN
-- =============================================================================
