-- =====================================================================
--  RapidExpress · 1_schema_ddl.sql
--  Script DDL consolidado — Sistema de Gestión de Flotas y Rutas
-- ---------------------------------------------------------------------
--  Consolida el modelo de los tres módulos:
--    Bloque 1 · Flota y personal        (vehículos, conductores, mantenimientos)
--    Bloque 2 · Paquetería y rutas      (personas, paquetes, rutas, ruta_paquetes)
--    Bloque 3 · Auditoría y reportes    (auditoria, vistas, procedimientos)
--
--  Motor: MySQL 8.0+ / InnoDB / utf8mb4
--  Ejecución:  mysql -h <host> -u <usuario> -p < 1_schema_ddl.sql
-- =====================================================================

DROP DATABASE IF EXISTS rapidexpress;
CREATE DATABASE rapidexpress
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
USE rapidexpress;

SET SQL_MODE = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION';


-- =====================================================================
--  BLOQUE 1 · FLOTA Y PERSONAL
-- =====================================================================

-- ---------------------------------------------------------------------
--  conductores
-- ---------------------------------------------------------------------
CREATE TABLE conductores (
    id_conductor    INT AUTO_INCREMENT PRIMARY KEY,
    identificacion  VARCHAR(20)  NOT NULL,
    nombre_completo VARCHAR(120) NOT NULL,
    tipo_licencia   ENUM('B1','B2','B3','C1','C2','C3') NOT NULL,
    telefono        VARCHAR(20)  NOT NULL,
    -- El enunciado lista tres estados (Activo, De Vacaciones, Inactivo) pero
    -- más adelante exige que "al iniciar una ruta el estado del conductor
    -- cambie a En Ruta". Se añade EN_RUTA como cuarto valor para que ambos
    -- requisitos puedan cumplirse sin inferir la disponibilidad desde otra
    -- tabla en cada consulta.
    estado          ENUM('ACTIVO','EN_RUTA','DE_VACACIONES','INACTIVO')
                    NOT NULL DEFAULT 'ACTIVO',
    fecha_registro  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_conductor_identificacion UNIQUE (identificacion),
    INDEX idx_conductor_estado (estado)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
--  vehiculos
--  Regla de negocio: "un conductor no puede estar asignado a más de un
--  vehículo simultáneamente". Se resuelve con UNIQUE sobre id_conductor:
--  MySQL admite múltiples NULL en un índice único, así que varios
--  vehículos pueden quedar sin conductor, pero ningún conductor puede
--  aparecer dos veces. La restricción queda en el motor, no en el código.
-- ---------------------------------------------------------------------
CREATE TABLE vehiculos (
    id_vehiculo   INT AUTO_INCREMENT PRIMARY KEY,
    placa         VARCHAR(10)   NOT NULL,
    marca         VARCHAR(50)   NOT NULL,
    modelo        VARCHAR(50)   NOT NULL,
    anio          SMALLINT      NOT NULL,
    capacidad_kg  DECIMAL(10,2) NOT NULL,
    estado        ENUM('DISPONIBLE','EN_RUTA','EN_MANTENIMIENTO')
                  NOT NULL DEFAULT 'DISPONIBLE',
    id_conductor  INT           NULL,

    CONSTRAINT uq_vehiculo_placa     UNIQUE (placa),
    CONSTRAINT uq_vehiculo_conductor UNIQUE (id_conductor),
    CONSTRAINT ck_vehiculo_anio      CHECK (anio BETWEEN 1990 AND 2100),
    CONSTRAINT ck_vehiculo_capacidad CHECK (capacidad_kg > 0),
    CONSTRAINT fk_vehiculo_conductor FOREIGN KEY (id_conductor)
        REFERENCES conductores (id_conductor)
        ON DELETE SET NULL ON UPDATE CASCADE,

    INDEX idx_vehiculo_estado (estado)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
--  mantenimientos
--  Historial individual por vehículo. ON DELETE CASCADE: si el vehículo
--  sale del parque automotor, su historial de taller deja de tener uso.
-- ---------------------------------------------------------------------
CREATE TABLE mantenimientos (
    id_mantenimiento  INT AUTO_INCREMENT PRIMARY KEY,
    id_vehiculo       INT           NOT NULL,
    tipo              ENUM('PREVENTIVO','CORRECTIVO') NOT NULL,
    descripcion       VARCHAR(255)  NOT NULL,
    fecha_programada  DATE          NOT NULL,
    fecha_realizacion DATE          NULL,
    costo             DECIMAL(12,2) NULL,
    taller            VARCHAR(120)  NULL,

    CONSTRAINT fk_mantenimiento_vehiculo FOREIGN KEY (id_vehiculo)
        REFERENCES vehiculos (id_vehiculo)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT ck_mantenimiento_costo CHECK (costo IS NULL OR costo >= 0),

    INDEX idx_mantenimiento_vehiculo (id_vehiculo, fecha_programada)
) ENGINE=InnoDB;


-- =====================================================================
--  BLOQUE 2 · PAQUETERÍA Y RUTAS
-- =====================================================================

-- ---------------------------------------------------------------------
--  personas
--  Remitentes y destinatarios en una sola entidad: la misma persona
--  puede enviar un paquete hoy y recibir otro mañana. Duplicarla en dos
--  tablas rompería la 3FN y obligaría a mantener sus datos por partida
--  doble. El rol no es un atributo de la persona, sino de la relación
--  con el paquete.
-- ---------------------------------------------------------------------
CREATE TABLE personas (
    id_persona      INT AUTO_INCREMENT PRIMARY KEY,
    identificacion  VARCHAR(20)  NOT NULL,
    nombre_completo VARCHAR(120) NOT NULL,
    telefono        VARCHAR(20)  NOT NULL,
    correo          VARCHAR(120) NULL,
    ciudad          VARCHAR(80)  NOT NULL,

    CONSTRAINT uq_persona_identificacion UNIQUE (identificacion)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
--  paquetes
-- ---------------------------------------------------------------------
CREATE TABLE paquetes (
    id_paquete         INT AUTO_INCREMENT PRIMARY KEY,
    codigo_seguimiento VARCHAR(20)   NOT NULL,
    descripcion        VARCHAR(255)  NOT NULL,
    peso_kg            DECIMAL(8,2)  NOT NULL,
    alto_cm            DECIMAL(6,2)  NOT NULL,
    ancho_cm           DECIMAL(6,2)  NOT NULL,
    largo_cm           DECIMAL(6,2)  NOT NULL,
    direccion_origen   VARCHAR(200)  NOT NULL,
    direccion_destino  VARCHAR(200)  NOT NULL,
    id_remitente       INT           NOT NULL,
    id_destinatario    INT           NOT NULL,
    estado             ENUM('EN_BODEGA','ASIGNADO_A_RUTA','EN_TRANSITO',
                            'ENTREGADO','DEVUELTO')
                       NOT NULL DEFAULT 'EN_BODEGA',
    fecha_registro     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_paquete_codigo UNIQUE (codigo_seguimiento),
    CONSTRAINT ck_paquete_peso   CHECK (peso_kg > 0),
    CONSTRAINT ck_paquete_dims   CHECK (alto_cm > 0 AND ancho_cm > 0 AND largo_cm > 0),
    CONSTRAINT ck_paquete_partes CHECK (id_remitente <> id_destinatario),
    CONSTRAINT fk_paquete_remitente FOREIGN KEY (id_remitente)
        REFERENCES personas (id_persona)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_paquete_destinatario FOREIGN KEY (id_destinatario)
        REFERENCES personas (id_persona)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    INDEX idx_paquete_estado (estado)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
--  rutas  (hoja de ruta diaria)
--  ON DELETE RESTRICT sobre vehículo y conductor: una ruta ejecutada es
--  un hecho histórico y no puede quedar huérfana.
-- ---------------------------------------------------------------------
CREATE TABLE rutas (
    id_ruta          INT AUTO_INCREMENT PRIMARY KEY,
    codigo_ruta      VARCHAR(20) NOT NULL,
    fecha_programada DATE        NOT NULL,
    id_vehiculo      INT         NOT NULL,
    id_conductor     INT         NOT NULL,
    estado           ENUM('PLANIFICADA','EN_CURSO','FINALIZADA','CANCELADA')
                     NOT NULL DEFAULT 'PLANIFICADA',
    fecha_inicio     DATETIME    NULL,
    fecha_fin        DATETIME    NULL,
    observaciones    VARCHAR(255) NULL,

    CONSTRAINT uq_ruta_codigo UNIQUE (codigo_ruta),
    CONSTRAINT ck_ruta_fechas CHECK (fecha_fin IS NULL OR fecha_inicio IS NULL
                                     OR fecha_fin >= fecha_inicio),
    CONSTRAINT fk_ruta_vehiculo FOREIGN KEY (id_vehiculo)
        REFERENCES vehiculos (id_vehiculo)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_ruta_conductor FOREIGN KEY (id_conductor)
        REFERENCES conductores (id_conductor)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    INDEX idx_ruta_fecha     (fecha_programada),
    INDEX idx_ruta_estado    (estado),
    INDEX idx_ruta_conductor (id_conductor, fecha_programada)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
--  ruta_paquetes  (tabla puente N:M)
--  Resuelve la relación muchos a muchos entre rutas y paquetes, y añade
--  los atributos propios de la asignación: el orden de entrega, cuándo
--  se entregó y con qué resultado. Un paquete devuelto puede reasignarse
--  a una ruta posterior, por eso la PK es compuesta y no hay UNIQUE
--  sobre id_paquete.
-- ---------------------------------------------------------------------
CREATE TABLE ruta_paquetes (
    id_ruta       INT      NOT NULL,
    id_paquete    INT      NOT NULL,
    orden_entrega SMALLINT NOT NULL DEFAULT 1,
    fecha_entrega DATETIME NULL,
    resultado     ENUM('PENDIENTE','ENTREGADO','DEVUELTO')
                  NOT NULL DEFAULT 'PENDIENTE',

    PRIMARY KEY (id_ruta, id_paquete),
    CONSTRAINT fk_rp_ruta FOREIGN KEY (id_ruta)
        REFERENCES rutas (id_ruta)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_rp_paquete FOREIGN KEY (id_paquete)
        REFERENCES paquetes (id_paquete)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    INDEX idx_rp_paquete   (id_paquete),
    INDEX idx_rp_resultado (resultado)
) ENGINE=InnoDB;


-- =====================================================================
--  BLOQUE 3 · AUDITORÍA Y REPORTES
-- =====================================================================

-- ---------------------------------------------------------------------
--  auditoria
--  Bitácora de operaciones críticas. Deliberadamente SIN claves foráneas:
--  debe sobrevivir al borrado de la fila que registró. La referencia a la
--  entidad es lógica, mediante el par (entidad, id_entidad).
--
--  La escribe la capa de aplicación, no triggers de base de datos, porque
--  el requisito exige registrar operaciones de negocio ("inicio de ruta",
--  "creación de paquete"), no sentencias sueltas. Iniciar una ruta toca
--  cuatro tablas y debe quedar como UN evento, no como diez INSERT/UPDATE
--  sin relación entre sí. La misma llamada escribe además en el archivo
--  de texto centralizado logs/auditoria.log.
-- ---------------------------------------------------------------------
CREATE TABLE auditoria (
    id_auditoria BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha_hora   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    operacion    VARCHAR(40)  NOT NULL,
    entidad      VARCHAR(40)  NOT NULL,
    id_entidad   VARCHAR(40)  NOT NULL,
    usuario      VARCHAR(60)  NOT NULL DEFAULT 'SISTEMA',
    estado_anterior VARCHAR(40) NULL,
    estado_nuevo    VARCHAR(40) NULL,
    detalle      VARCHAR(500) NULL,

    INDEX idx_auditoria_fecha     (fecha_hora),
    INDEX idx_auditoria_operacion (operacion),
    INDEX idx_auditoria_entidad   (entidad, id_entidad)
) ENGINE=InnoDB;


-- ---------------------------------------------------------------------
--  VISTAS DE REPORTES
-- ---------------------------------------------------------------------

--  Estado general de la flota: qué vehículo tiene quién, en qué estado
--  está y cuánto ha rodado.
CREATE OR REPLACE VIEW v_estado_flota AS
SELECT
    v.id_vehiculo,
    v.placa,
    CONCAT(v.marca, ' ', v.modelo, ' (', v.anio, ')') AS vehiculo,
    v.capacidad_kg,
    v.estado                                          AS estado_vehiculo,
    COALESCE(c.nombre_completo, 'SIN ASIGNAR')        AS conductor,
    COALESCE(c.estado, '-')                           AS estado_conductor,
    COUNT(DISTINCT r.id_ruta)                         AS rutas_totales,
    COUNT(DISTINCT CASE WHEN m.fecha_realizacion IS NULL
                        THEN m.id_mantenimiento END)  AS mantenimientos_pendientes
FROM vehiculos v
LEFT JOIN conductores    c ON c.id_conductor = v.id_conductor
LEFT JOIN rutas          r ON r.id_vehiculo  = v.id_vehiculo
LEFT JOIN mantenimientos m ON m.id_vehiculo  = v.id_vehiculo
GROUP BY v.id_vehiculo, v.placa, v.marca, v.modelo, v.anio,
         v.capacidad_kg, v.estado, c.nombre_completo, c.estado;

--  Desempeño acumulado por conductor.
CREATE OR REPLACE VIEW v_desempeno_conductores AS
SELECT
    c.id_conductor,
    c.identificacion,
    c.nombre_completo,
    c.tipo_licencia,
    c.estado,
    COUNT(DISTINCT r.id_ruta)                                        AS rutas_asignadas,
    COUNT(DISTINCT CASE WHEN r.estado = 'FINALIZADA'
                        THEN r.id_ruta END)                          AS rutas_finalizadas,
    COUNT(CASE WHEN rp.resultado = 'ENTREGADO' THEN 1 END)           AS paquetes_entregados,
    COUNT(CASE WHEN rp.resultado = 'DEVUELTO'  THEN 1 END)           AS paquetes_devueltos,
    ROUND(
        COUNT(CASE WHEN rp.resultado = 'ENTREGADO' THEN 1 END) * 100.0
        / NULLIF(COUNT(rp.id_paquete), 0)
    , 2)                                                             AS efectividad_pct
FROM conductores c
LEFT JOIN rutas         r  ON r.id_conductor = c.id_conductor
LEFT JOIN ruta_paquetes rp ON rp.id_ruta     = r.id_ruta
GROUP BY c.id_conductor, c.identificacion, c.nombre_completo,
         c.tipo_licencia, c.estado;

--  Ocupación de cada hoja de ruta frente a la capacidad del vehículo.
CREATE OR REPLACE VIEW v_ocupacion_rutas AS
SELECT
    r.id_ruta,
    r.codigo_ruta,
    r.fecha_programada,
    r.estado                                       AS estado_ruta,
    v.placa,
    v.capacidad_kg,
    c.nombre_completo                              AS conductor,
    COUNT(rp.id_paquete)                           AS paquetes,
    COALESCE(SUM(p.peso_kg), 0)                    AS peso_asignado_kg,
    ROUND(COALESCE(SUM(p.peso_kg), 0) / v.capacidad_kg * 100, 2)
                                                   AS ocupacion_pct,
    COUNT(CASE WHEN rp.resultado = 'ENTREGADO' THEN 1 END) AS entregados,
    COUNT(CASE WHEN rp.resultado = 'PENDIENTE'  THEN 1 END) AS pendientes
FROM rutas r
JOIN vehiculos    v  ON v.id_vehiculo  = r.id_vehiculo
JOIN conductores  c  ON c.id_conductor = r.id_conductor
LEFT JOIN ruta_paquetes rp ON rp.id_ruta    = r.id_ruta
LEFT JOIN paquetes      p  ON p.id_paquete  = rp.id_paquete
GROUP BY r.id_ruta, r.codigo_ruta, r.fecha_programada, r.estado,
         v.placa, v.capacidad_kg, c.nombre_completo;

--  Distribución de la paquetería por estado del ciclo de vida.
CREATE OR REPLACE VIEW v_paquetes_por_estado AS
SELECT
    p.estado,
    COUNT(*)                 AS cantidad,
    ROUND(SUM(p.peso_kg), 2) AS peso_total_kg,
    ROUND(AVG(p.peso_kg), 2) AS peso_promedio_kg,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM paquetes), 2) AS porcentaje
FROM paquetes p
GROUP BY p.estado;

--  Actividad registrada en la bitácora, agrupada por día y operación.
CREATE OR REPLACE VIEW v_actividad_auditoria AS
SELECT
    DATE(a.fecha_hora) AS dia,
    a.operacion,
    a.entidad,
    a.usuario,
    COUNT(*)           AS eventos
FROM auditoria a
GROUP BY DATE(a.fecha_hora), a.operacion, a.entidad, a.usuario;


-- ---------------------------------------------------------------------
--  PROCEDIMIENTOS ALMACENADOS
--  Los dos reportes que el enunciado pide de forma explícita, más el
--  seguimiento de un paquete y la consulta de la bitácora.
-- ---------------------------------------------------------------------
DELIMITER $$

--  Reporte 1 · Entregas de un conductor en un rango de fechas.
DROP PROCEDURE IF EXISTS sp_entregas_por_conductor$$
CREATE PROCEDURE sp_entregas_por_conductor(
    IN p_id_conductor INT,
    IN p_desde        DATE,
    IN p_hasta        DATE
)
BEGIN
    SELECT
        c.nombre_completo                AS conductor,
        r.codigo_ruta,
        r.fecha_programada,
        v.placa,
        p.codigo_seguimiento,
        p.descripcion,
        p.peso_kg,
        p.direccion_destino,
        rp.resultado,
        rp.fecha_entrega
    FROM ruta_paquetes rp
    JOIN rutas       r ON r.id_ruta      = rp.id_ruta
    JOIN conductores c ON c.id_conductor = r.id_conductor
    JOIN vehiculos   v ON v.id_vehiculo  = r.id_vehiculo
    JOIN paquetes    p ON p.id_paquete   = rp.id_paquete
    WHERE r.id_conductor     = p_id_conductor
      AND r.fecha_programada BETWEEN p_desde AND p_hasta
      AND rp.resultado       = 'ENTREGADO'
    ORDER BY r.fecha_programada, rp.orden_entrega;
END$$

--  Reporte 2 · Historial de rutas de un vehículo.
DROP PROCEDURE IF EXISTS sp_historial_rutas_vehiculo$$
CREATE PROCEDURE sp_historial_rutas_vehiculo(
    IN p_id_vehiculo INT
)
BEGIN
    SELECT
        v.placa,
        r.codigo_ruta,
        r.fecha_programada,
        r.estado                                   AS estado_ruta,
        c.nombre_completo                          AS conductor,
        COUNT(rp.id_paquete)                       AS paquetes,
        COALESCE(SUM(p.peso_kg), 0)                AS peso_kg,
        ROUND(COALESCE(SUM(p.peso_kg),0) / v.capacidad_kg * 100, 2) AS ocupacion_pct,
        r.fecha_inicio,
        r.fecha_fin,
        TIMESTAMPDIFF(HOUR, r.fecha_inicio, r.fecha_fin) AS horas_ruta
    FROM rutas r
    JOIN vehiculos   v ON v.id_vehiculo  = r.id_vehiculo
    JOIN conductores c ON c.id_conductor = r.id_conductor
    LEFT JOIN ruta_paquetes rp ON rp.id_ruta   = r.id_ruta
    LEFT JOIN paquetes      p  ON p.id_paquete = rp.id_paquete
    WHERE r.id_vehiculo = p_id_vehiculo
    GROUP BY r.id_ruta, v.placa, r.codigo_ruta, r.fecha_programada,
             r.estado, c.nombre_completo, v.capacidad_kg,
             r.fecha_inicio, r.fecha_fin
    ORDER BY r.fecha_programada DESC;
END$$

--  Reporte 3 · Trazabilidad de un paquete por su código de seguimiento.
DROP PROCEDURE IF EXISTS sp_seguimiento_paquete$$
CREATE PROCEDURE sp_seguimiento_paquete(
    IN p_codigo VARCHAR(20)
)
BEGIN
    SELECT
        a.fecha_hora,
        a.operacion,
        a.estado_anterior,
        a.estado_nuevo,
        a.usuario,
        a.detalle
    FROM auditoria a
    JOIN paquetes p ON p.id_paquete = CAST(a.id_entidad AS UNSIGNED)
    WHERE a.entidad = 'PAQUETE'
      AND p.codigo_seguimiento = p_codigo
    ORDER BY a.fecha_hora;
END$$

--  Reporte 4 · Bitácora de auditoría filtrada por periodo y operación.
DROP PROCEDURE IF EXISTS sp_auditoria_por_periodo$$
CREATE PROCEDURE sp_auditoria_por_periodo(
    IN p_desde     DATETIME,
    IN p_hasta     DATETIME,
    IN p_operacion VARCHAR(40)   -- NULL o '' para traer todas
)
BEGIN
    SELECT id_auditoria, fecha_hora, operacion, entidad, id_entidad,
           usuario, estado_anterior, estado_nuevo, detalle
    FROM auditoria
    WHERE fecha_hora BETWEEN p_desde AND p_hasta
      AND (p_operacion IS NULL OR p_operacion = '' OR operacion = p_operacion)
    ORDER BY fecha_hora DESC;
END$$

DELIMITER ;


-- =====================================================================
--  VERIFICACIÓN DEL DESPLIEGUE
-- =====================================================================
SELECT 'Tablas'          AS objeto, COUNT(*) AS total FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'rapidexpress' AND TABLE_TYPE = 'BASE TABLE'
UNION ALL
SELECT 'Vistas',          COUNT(*) FROM information_schema.VIEWS
WHERE TABLE_SCHEMA = 'rapidexpress'
UNION ALL
SELECT 'Procedimientos',  COUNT(*) FROM information_schema.ROUTINES
WHERE ROUTINE_SCHEMA = 'rapidexpress' AND ROUTINE_TYPE = 'PROCEDURE';
