-- =====================================================================
-- RapidExpress - DDL de REPORTES y AUDITORIA (Persona 3)
-- Motor: MySQL 8.0+
--
-- Ejecutar DESPUES de 1_schema_ddl.sql (necesita las ocho tablas base).
-- Define:
--   - la tabla transversal 'auditoria'
--   - cinco vistas de reporte  (v_*)
--   - cuatro procedimientos    (sp_*)
--
-- Los nombres de columna coinciden con lo que leen ReporteDAO y AuditoriaDAO.
-- =====================================================================

USE rapidexpress;

-- ---------------------------------------------------------------------
-- Tabla: auditoria  (bitacora de operaciones criticas)
-- La escribe AuditLogger (archivo + esta tabla). id_entidad es VARCHAR
-- porque el identificador llega como texto desde los servicios.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS auditoria (
    id_auditoria    BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha_hora      DATETIME     NOT NULL,
    operacion       VARCHAR(40)  NOT NULL,
    entidad         VARCHAR(20)  NOT NULL,
    id_entidad      VARCHAR(40)  NOT NULL,
    usuario         VARCHAR(60)  NOT NULL DEFAULT 'SISTEMA',
    estado_anterior VARCHAR(40)  NULL,
    estado_nuevo    VARCHAR(40)  NULL,
    detalle         VARCHAR(500) NULL
) ENGINE=InnoDB;

CREATE INDEX idx_auditoria_entidad ON auditoria (entidad, id_entidad);
CREATE INDEX idx_auditoria_fecha   ON auditoria (fecha_hora);
CREATE INDEX idx_auditoria_op      ON auditoria (operacion);

-- =====================================================================
-- VISTAS DE REPORTE
-- =====================================================================

-- Estado general de la flota: un renglon por vehiculo con su conductor
-- vigente y cuantos mantenimientos tiene abiertos.
CREATE OR REPLACE VIEW v_estado_flota AS
SELECT
    v.placa,
    v.marca,
    v.modelo,
    v.anio_fabricacion,
    v.capacidad_carga_kg,
    v.estado                                   AS estado_vehiculo,
    c.nombre_completo                          AS conductor_asignado,
    (SELECT COUNT(*) FROM mantenimientos m
       WHERE m.id_vehiculo = v.id_vehiculo AND m.fecha_fin IS NULL) AS mantenimientos_abiertos
FROM vehiculos v
LEFT JOIN asignaciones_conductor_vehiculo a
       ON a.id_vehiculo = v.id_vehiculo AND a.fecha_fin IS NULL
LEFT JOIN conductores c ON c.id_conductor = a.id_conductor;

-- Desempeno de conductores: rutas finalizadas y paquetes movidos.
CREATE OR REPLACE VIEW v_desempeno_conductores AS
SELECT
    c.nombre_completo                          AS conductor,
    c.estado,
    COUNT(DISTINCT r.id_ruta)                  AS rutas_finalizadas,
    COALESCE(SUM(p.estado = 'Entregado'), 0)   AS paquetes_entregados,
    COALESCE(SUM(p.estado = 'Devuelto'), 0)    AS paquetes_devueltos,
    ROUND(COALESCE(SUM(CASE WHEN p.estado = 'Entregado' THEN p.peso_kg END), 0), 2) AS kg_entregados
FROM conductores c
LEFT JOIN rutas r          ON r.id_conductor = c.id_conductor AND r.estado = 'Finalizada'
LEFT JOIN ruta_paquetes rp ON rp.id_ruta = r.id_ruta
LEFT JOIN paquetes p       ON p.id_paquete = rp.id_paquete
GROUP BY c.id_conductor, c.nombre_completo, c.estado;

-- Ocupacion de rutas: carga transportada frente a la capacidad del vehiculo.
CREATE OR REPLACE VIEW v_ocupacion_rutas AS
SELECT
    r.codigo                                   AS codigo_ruta,
    r.fecha                                    AS fecha_programada,
    r.estado                                   AS estado_ruta,
    v.placa,
    v.capacidad_carga_kg,
    c.nombre_completo                          AS conductor,
    COUNT(rp.id_paquete)                       AS paquetes,
    ROUND(COALESCE(SUM(p.peso_kg), 0), 2)      AS peso_kg,
    ROUND(COALESCE(SUM(p.peso_kg), 0) / v.capacidad_carga_kg * 100, 2) AS ocupacion_pct
FROM rutas r
JOIN vehiculos v   ON v.id_vehiculo  = r.id_vehiculo
JOIN conductores c ON c.id_conductor = r.id_conductor
LEFT JOIN ruta_paquetes rp ON rp.id_ruta = r.id_ruta
LEFT JOIN paquetes p       ON p.id_paquete = rp.id_paquete
GROUP BY r.id_ruta, r.codigo, r.fecha, r.estado, v.placa, v.capacidad_carga_kg, c.nombre_completo;

-- Paquetes agrupados por estado.
CREATE OR REPLACE VIEW v_paquetes_por_estado AS
SELECT
    p.estado,
    COUNT(*)                                   AS cantidad,
    ROUND(COALESCE(SUM(p.peso_kg), 0), 2)      AS peso_total_kg
FROM paquetes p
GROUP BY p.estado;

-- Actividad de la bitacora agrupada por dia y operacion.
CREATE OR REPLACE VIEW v_actividad_auditoria AS
SELECT
    DATE(fecha_hora)                           AS dia,
    operacion,
    entidad,
    COUNT(*)                                   AS eventos,
    COUNT(DISTINCT usuario)                    AS usuarios
FROM auditoria
GROUP BY DATE(fecha_hora), operacion, entidad;

-- =====================================================================
-- PROCEDIMIENTOS ALMACENADOS
-- =====================================================================

DROP PROCEDURE IF EXISTS sp_entregas_por_conductor;
DROP PROCEDURE IF EXISTS sp_historial_rutas_vehiculo;
DROP PROCEDURE IF EXISTS sp_seguimiento_paquete;
DROP PROCEDURE IF EXISTS sp_auditoria_por_periodo;

DELIMITER $$

-- Reporte 1: entregas de un conductor en un rango de fechas (por fecha de ruta).
CREATE PROCEDURE sp_entregas_por_conductor(IN p_id_conductor INT, IN p_desde DATE, IN p_hasta DATE)
BEGIN
    SELECT
        c.nombre_completo    AS conductor,
        r.codigo             AS codigo_ruta,
        r.fecha              AS fecha_programada,
        v.placa              AS placa,
        p.codigo_seguimiento AS codigo_seguimiento,
        p.descripcion        AS descripcion,
        p.peso_kg            AS peso_kg,
        p.direccion_destino  AS direccion_destino,
        p.estado             AS resultado,
        p.fecha_entrega      AS fecha_entrega
    FROM rutas r
    JOIN conductores c    ON c.id_conductor = r.id_conductor
    JOIN vehiculos   v    ON v.id_vehiculo  = r.id_vehiculo
    JOIN ruta_paquetes rp ON rp.id_ruta     = r.id_ruta
    JOIN paquetes p       ON p.id_paquete   = rp.id_paquete
    WHERE r.id_conductor = p_id_conductor
      AND p.estado IN ('Entregado', 'Devuelto')
      AND r.fecha BETWEEN p_desde AND p_hasta
    ORDER BY r.fecha, p.codigo_seguimiento;
END $$

-- Reporte 2: historial de rutas de un vehiculo, con ocupacion y horas.
CREATE PROCEDURE sp_historial_rutas_vehiculo(IN p_id_vehiculo INT)
BEGIN
    SELECT
        v.placa           AS placa,
        r.codigo          AS codigo_ruta,
        r.fecha           AS fecha_programada,
        r.estado          AS estado_ruta,
        c.nombre_completo AS conductor,
        COUNT(rp.id_paquete)              AS paquetes,
        ROUND(COALESCE(SUM(p.peso_kg), 0), 2) AS peso_kg,
        ROUND(COALESCE(SUM(p.peso_kg), 0) / v.capacidad_carga_kg * 100, 2) AS ocupacion_pct,
        r.iniciada_en     AS fecha_inicio,
        r.finalizada_en   AS fecha_fin,
        CASE WHEN r.iniciada_en IS NOT NULL AND r.finalizada_en IS NOT NULL
             THEN TIMESTAMPDIFF(HOUR, r.iniciada_en, r.finalizada_en)
             ELSE NULL END AS horas_ruta
    FROM rutas r
    JOIN vehiculos   v ON v.id_vehiculo  = r.id_vehiculo
    JOIN conductores c ON c.id_conductor = r.id_conductor
    LEFT JOIN ruta_paquetes rp ON rp.id_ruta   = r.id_ruta
    LEFT JOIN paquetes p       ON p.id_paquete = rp.id_paquete
    WHERE r.id_vehiculo = p_id_vehiculo
    GROUP BY r.id_ruta, v.placa, r.codigo, r.fecha, r.estado, c.nombre_completo,
             v.capacidad_carga_kg, r.iniciada_en, r.finalizada_en
    ORDER BY r.fecha DESC, r.id_ruta DESC;
END $$

-- Trazabilidad de un paquete por su codigo de seguimiento.
CREATE PROCEDURE sp_seguimiento_paquete(IN p_codigo VARCHAR(24))
BEGIN
    SELECT
        p.codigo_seguimiento,
        p.estado,
        p.descripcion,
        p.peso_kg,
        rem.nombre                                          AS remitente,
        des.nombre                                          AS destinatario,
        CONCAT(p.direccion_origen,  ', ', p.ciudad_origen)  AS origen,
        CONCAT(p.direccion_destino, ', ', p.ciudad_destino) AS destino,
        p.fecha_registro,
        p.fecha_entrega,
        r.codigo          AS ruta,
        r.estado          AS estado_ruta,
        r.fecha           AS fecha_ruta,
        c.nombre_completo AS conductor,
        v.placa           AS vehiculo
    FROM paquetes p
    JOIN clientes rem ON rem.id_cliente = p.id_remitente
    JOIN clientes des ON des.id_cliente = p.id_destinatario
    LEFT JOIN ruta_paquetes rp ON rp.id_paquete   = p.id_paquete
    LEFT JOIN rutas r          ON r.id_ruta       = rp.id_ruta
    LEFT JOIN conductores c    ON c.id_conductor  = r.id_conductor
    LEFT JOIN vehiculos v      ON v.id_vehiculo   = r.id_vehiculo
    WHERE p.codigo_seguimiento = p_codigo;
END $$

-- Bitacora filtrada por periodo y, opcionalmente, por operacion.
CREATE PROCEDURE sp_auditoria_por_periodo(IN p_desde DATETIME, IN p_hasta DATETIME, IN p_operacion VARCHAR(40))
BEGIN
    SELECT id_auditoria, fecha_hora, operacion, entidad, id_entidad, usuario,
           estado_anterior, estado_nuevo, detalle
    FROM auditoria
    WHERE fecha_hora BETWEEN p_desde AND p_hasta
      AND (p_operacion IS NULL OR p_operacion = '' OR operacion = p_operacion)
    ORDER BY fecha_hora;
END $$

DELIMITER ;
