-- ============================================================
-- RapidExpress - INSTALACION COMPLETA (esquema + reportes/auditoria + datos)
-- Generado por concatenacion de:
--   1_schema_ddl.sql  +  3_schema_reportes_auditoria.sql  +  2_data_dml.sql
-- Uso:  mysql -h <host> -u <usuario> -p < 0_instalacion_completa.sql
-- ============================================================

-- =====================================================================
-- RapidExpress - DDL CONSOLIDADO (Persona 1 + Persona 2)
-- Motor: MySQL 8.0+
--
-- Generado a partir de:
--   1_schema_ddl_persona1.sql  (flota de vehiculos, personal, asignaciones)
--   1_schema_ddl_persona2.sql  (clientes, paquetes, rutas, ruta_paquetes)
--
-- Orden: primero las tablas de la Persona 1 porque 'rutas' tiene llaves
-- foraneas hacia vehiculos y conductores.
-- Es el script que referencia README_seccion_instalacion.md.
-- =====================================================================

-- =====================================================================
-- RapidExpress - DDL PARCIAL (Persona 1: Flota de Vehiculos y Personal)
-- Motor: MySQL 8.0+ (usa CHECK y columnas generadas)
-- Este archivo se fusiona luego dentro de database/1_schema_ddl.sql
-- =====================================================================

-- Solo para pruebas locales. En el script consolidado del equipo,
-- la creacion de la BD la define la Persona 3 una sola vez.
CREATE DATABASE IF NOT EXISTS rapidexpress
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE rapidexpress;

-- ---------------------------------------------------------------------
-- Tabla: conductores
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS conductores (
    id_conductor    INT AUTO_INCREMENT PRIMARY KEY,
    identificacion  VARCHAR(20)  NOT NULL,
    nombre_completo VARCHAR(120) NOT NULL,
    tipo_licencia   ENUM('A1','A2','B1','B2','B3','C1','C2','C3') NOT NULL,
    contacto        VARCHAR(30)  NOT NULL,
    estado          ENUM('Activo','De Vacaciones','Inactivo','En Ruta')
                    NOT NULL DEFAULT 'Activo',
    fecha_registro  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_conductor_identificacion UNIQUE (identificacion)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Tabla: vehiculos
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vehiculos (
    id_vehiculo        INT AUTO_INCREMENT PRIMARY KEY,
    placa              VARCHAR(10)  NOT NULL,
    marca              VARCHAR(50)  NOT NULL,
    modelo             VARCHAR(50)  NOT NULL,
    anio_fabricacion   SMALLINT     NOT NULL,
    capacidad_carga_kg DECIMAL(10,2) NOT NULL,
    estado             ENUM('Disponible','En Ruta','En Mantenimiento')
                       NOT NULL DEFAULT 'Disponible',
    CONSTRAINT uk_vehiculo_placa UNIQUE (placa),
    CONSTRAINT chk_vehiculo_anio      CHECK (anio_fabricacion BETWEEN 1950 AND 2100),
    CONSTRAINT chk_vehiculo_capacidad CHECK (capacidad_carga_kg > 0)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Tabla: mantenimientos  (1:N con vehiculos -> historial individual)
-- fecha_fin NULL = mantenimiento aun abierto
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS mantenimientos (
    id_mantenimiento INT AUTO_INCREMENT PRIMARY KEY,
    id_vehiculo      INT NOT NULL,
    tipo             ENUM('Preventivo','Correctivo') NOT NULL,
    descripcion      VARCHAR(255) NOT NULL,
    fecha_inicio     DATE NOT NULL,
    fecha_fin        DATE NULL,
    costo            DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    taller           VARCHAR(100) NULL,
    CONSTRAINT fk_mantenimiento_vehiculo FOREIGN KEY (id_vehiculo)
        REFERENCES vehiculos(id_vehiculo)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_mantenimiento_fechas CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio),
    CONSTRAINT chk_mantenimiento_costo  CHECK (costo >= 0)
) ENGINE=InnoDB;

CREATE INDEX idx_mantenimiento_vehiculo ON mantenimientos(id_vehiculo);

-- ---------------------------------------------------------------------
-- Tabla: asignaciones_conductor_vehiculo
--
-- Se modela como tabla historica (no como campo en vehiculos) para poder
-- consultar quien manejo que vehiculo y cuando.
--
-- Regla "un conductor no puede estar en mas de un vehiculo a la vez":
-- la columna generada 'activa' vale 1 mientras fecha_fin sea NULL y NULL
-- cuando la asignacion ya se cerro. Como MySQL ignora los NULL en indices
-- UNIQUE, la restriccion solo aplica sobre las asignaciones vigentes.
--
-- Los FK usan ON DELETE CASCADE (igual que mantenimientos): al eliminar un
-- vehiculo o conductor se borra tambien su historial de asignaciones. El
-- servicio impide borrar mientras exista una asignacion VIGENTE, asi que el
-- cascade solo alcanza filas ya cerradas.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS asignaciones_conductor_vehiculo (
    id_asignacion INT AUTO_INCREMENT PRIMARY KEY,
    id_conductor  INT NOT NULL,
    id_vehiculo   INT NOT NULL,
    fecha_inicio  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_fin     DATETIME NULL,
    activa        TINYINT GENERATED ALWAYS AS (IF(fecha_fin IS NULL, 1, NULL)) STORED,
    CONSTRAINT fk_asignacion_conductor FOREIGN KEY (id_conductor)
        REFERENCES conductores(id_conductor) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_asignacion_vehiculo FOREIGN KEY (id_vehiculo)
        REFERENCES vehiculos(id_vehiculo) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uk_conductor_asignacion_activa UNIQUE (id_conductor, activa),
    CONSTRAINT uk_vehiculo_asignacion_activa  UNIQUE (id_vehiculo, activa)
) ENGINE=InnoDB;


-- ================ MODULO 2: PAQUETES Y RUTAS ================


-- ---------------------------------------------------------------------
-- Tabla: clientes
--
-- Decision de diseno: UNA sola tabla en vez de "remitentes" y
-- "destinatarios" separadas. La misma persona puede enviar un paquete y
-- recibir otro; con dos tablas habria que duplicar el registro y no
-- habria forma de saber que son la misma persona.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS clientes (
    id_cliente  INT AUTO_INCREMENT PRIMARY KEY,
    documento   VARCHAR(20)  NOT NULL,
    nombre      VARCHAR(120) NOT NULL,
    telefono    VARCHAR(30)  NULL,
    email       VARCHAR(120) NULL,
    direccion   VARCHAR(160) NOT NULL,
    ciudad      VARCHAR(80)  NOT NULL,
    creado_en   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_cliente_documento UNIQUE (documento)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Tabla: paquetes
--
-- codigo_seguimiento es UNIQUE: es la garantia a nivel de BD de que dos
-- terminales no puedan crear el mismo codigo al mismo tiempo, aunque el
-- generador aleatorio coincida.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS paquetes (
    id_paquete         INT AUTO_INCREMENT PRIMARY KEY,
    codigo_seguimiento VARCHAR(24)  NOT NULL,
    descripcion        VARCHAR(200) NOT NULL,
    peso_kg            DECIMAL(8,2) NOT NULL,
    largo_cm           DECIMAL(7,2) NOT NULL DEFAULT 0,
    ancho_cm           DECIMAL(7,2) NOT NULL DEFAULT 0,
    alto_cm            DECIMAL(7,2) NOT NULL DEFAULT 0,
    id_remitente       INT NOT NULL,
    id_destinatario    INT NOT NULL,
    direccion_origen   VARCHAR(160) NOT NULL,
    ciudad_origen      VARCHAR(80)  NOT NULL,
    direccion_destino  VARCHAR(160) NOT NULL,
    ciudad_destino     VARCHAR(80)  NOT NULL,
    estado             ENUM('En Bodega','Asignado a Ruta','En Transito','Entregado','Devuelto')
                       NOT NULL DEFAULT 'En Bodega',
    fecha_registro     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_entrega      DATETIME NULL,
    CONSTRAINT uk_paquete_codigo UNIQUE (codigo_seguimiento),
    -- Sin ON UPDATE CASCADE a proposito: id_cliente es un AUTO_INCREMENT que
    -- nunca cambia, y MySQL 8 no deja usar una columna en un CHECK si su FK
    -- tiene accion referencial distinta de RESTRICT/NO ACTION (error 3823).
    CONSTRAINT fk_paquete_remitente FOREIGN KEY (id_remitente)
        REFERENCES clientes(id_cliente),
    CONSTRAINT fk_paquete_destinatario FOREIGN KEY (id_destinatario)
        REFERENCES clientes(id_cliente),
    CONSTRAINT chk_paquete_peso CHECK (peso_kg > 0),
    CONSTRAINT chk_paquete_partes CHECK (id_remitente <> id_destinatario)
) ENGINE=InnoDB;

CREATE INDEX idx_paquete_estado  ON paquetes(estado);
CREATE INDEX idx_paquete_entrega ON paquetes(fecha_entrega);

-- ---------------------------------------------------------------------
-- Tabla: rutas (hoja de ruta diaria)
--
-- El conductor no se elige a mano: al crear la ruta se toma de la
-- asignacion vigente del vehiculo (modulo de la Persona 1) y se guarda
-- aqui para conservar el historico aunque la asignacion cambie despues.
--
-- Los dos UNIQUE son reglas de negocio empujadas a la BD: un vehiculo no
-- puede tener dos hojas de ruta ACTIVAS el mismo dia, y un conductor tampoco.
-- Solo cuentan las rutas Planificada / En Curso: si una ruta se Cancela o
-- Finaliza, esa pareja vehiculo/fecha (o conductor/fecha) vuelve a quedar
-- libre. Se implementa igual que 'activa' en asignaciones_conductor_vehiculo:
-- una columna generada que vale 1 mientras la ruta este activa y NULL en
-- cuanto pasa a un estado terminal; MySQL ignora los NULL en los UNIQUE.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS rutas (
    id_ruta        INT AUTO_INCREMENT PRIMARY KEY,
    codigo         VARCHAR(24) NOT NULL,
    fecha          DATE NOT NULL,
    id_vehiculo    INT NOT NULL,
    id_conductor   INT NOT NULL,
    estado         ENUM('Planificada','En Curso','Finalizada','Cancelada')
                   NOT NULL DEFAULT 'Planificada',
    observaciones  VARCHAR(255) NULL,
    creada_en      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    iniciada_en    DATETIME NULL,
    finalizada_en  DATETIME NULL,
    activa         TINYINT GENERATED ALWAYS AS
                   (IF(estado IN ('Planificada','En Curso'), 1, NULL)) STORED,
    CONSTRAINT uk_ruta_codigo UNIQUE (codigo),
    CONSTRAINT uk_ruta_vehiculo_activa  UNIQUE (id_vehiculo, fecha, activa),
    CONSTRAINT uk_ruta_conductor_activa UNIQUE (id_conductor, fecha, activa),
    CONSTRAINT fk_ruta_vehiculo FOREIGN KEY (id_vehiculo)
        REFERENCES vehiculos(id_vehiculo) ON UPDATE CASCADE,
    CONSTRAINT fk_ruta_conductor FOREIGN KEY (id_conductor)
        REFERENCES conductores(id_conductor) ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_ruta_estado ON rutas(estado);
CREATE INDEX idx_ruta_fecha  ON rutas(fecha);

-- ---------------------------------------------------------------------
-- Tabla: ruta_paquetes (relacion N:M entre rutas y paquetes)
--
-- La PK compuesta impide agregar el mismo paquete dos veces a una ruta.
-- Que un paquete no este en dos rutas ACTIVAS a la vez se valida en
-- RutaService: un paquete devuelto SI puede reasignarse a una ruta
-- posterior, por eso no hay un UNIQUE global sobre id_paquete.
--
-- ON DELETE CASCADE solo hacia rutas: si se borra una ruta desaparecen
-- sus lineas, pero un paquete nunca se borra si esta en una ruta.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ruta_paquetes (
    id_ruta       INT NOT NULL,
    id_paquete    INT NOT NULL,
    orden_entrega INT NOT NULL DEFAULT 1,
    PRIMARY KEY (id_ruta, id_paquete),
    CONSTRAINT fk_rp_ruta FOREIGN KEY (id_ruta)
        REFERENCES rutas(id_ruta) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_rp_paquete FOREIGN KEY (id_paquete)
        REFERENCES paquetes(id_paquete) ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_rp_paquete ON ruta_paquetes(id_paquete);

-- ===== 3_schema_reportes_auditoria.sql =====
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

-- ===== 2_data_dml.sql =====
-- =====================================================================
-- RapidExpress - DML CONSOLIDADO (Persona 1 + Persona 2)
-- Ejecutar DESPUES de 1_schema_ddl.sql
--
-- Generado a partir de:
--   2_data_dml_persona1.sql  (24 conductores, 24 vehiculos, 24 mantenimientos,
--                             24 asignaciones)
--   2_data_dml_persona2.sql  (26 clientes, 40 paquetes, 20 rutas, 34 lineas)
--
-- Las rutas usan parejas vehiculo-conductor con asignacion vigente en el
-- DML de la Persona 1.
-- =====================================================================

-- =====================================================================
-- RapidExpress - DML PARCIAL (Persona 1)
-- 20+ registros por entidad. Ejecutar DESPUES de 1_schema_ddl_persona1.sql
-- =====================================================================
USE rapidexpress;

-- ---------------------- CONDUCTORES (24) -----------------------------
INSERT INTO conductores (identificacion, nombre_completo, tipo_licencia, contacto, estado) VALUES
('1098234567','Carlos Andres Rueda Pinzon','C2','3145678901','Activo'),
('1098234568','Maria Fernanda Ortiz Leon','C1','3105678902','Activo'),
('1098234569','Jhon Alexander Serrano Diaz','C2','3125678903','Activo'),
('1098234570','Laura Catalina Mendez Rojas','C1','3155678904','Activo'),
('1098234571','Diego Armando Castillo Vera','C3','3175678905','Activo'),
('1098234572','Sandra Milena Beltran Nino','C1','3005678906','De Vacaciones'),
('1098234573','Julian David Quintero Sosa','C2','3185678907','Activo'),
('1098234574','Angela Patricia Gomez Silva','C1','3195678908','Activo'),
('1098234575','Oscar Eduardo Ramirez Cruz','C3','3205678909','Activo'),
('1098234576','Natalia Andrea Vargas Pena','C2','3215678910','Inactivo'),
('1098234577','Andres Felipe Moreno Suarez','C2','3225678911','Activo'),
('1098234578','Paola Alejandra Duran Meza','C1','3235678912','Activo'),
('1098234579','Ricardo Antonio Sanchez Lara','C3','3245678913','Activo'),
('1098234580','Claudia Ximena Herrera Pico','C1','3255678914','De Vacaciones'),
('1098234581','Miguel Angel Toro Bautista','C2','3265678915','Activo'),
('1098234582','Diana Carolina Pardo Acuna','C1','3275678916','Activo'),
('1098234583','Fabian Steven Rincon Alvarez','C2','3285678917','Activo'),
('1098234584','Luisa Fernanda Camacho Vega','C1','3295678918','Activo'),
('1098234585','Hernan Dario Prada Villamizar','C3','3305678919','Activo'),
('1098234586','Yeimy Rocio Delgado Parra','C2','3315678920','Activo'),
('1098234587','Sebastian Camilo Ariza Gomez','C2','3325678921','Activo'),
('1098234588','Monica Liliana Cardenas Ruiz','C1','3335678922','Inactivo'),
('1098234589','Wilson Ferney Barrera Osma','C3','3345678923','Activo'),
('1098234590','Tatiana Isabel Forero Lizcano','C1','3355678924','Activo');

-- ------------------------ VEHICULOS (24) -----------------------------
INSERT INTO vehiculos (placa, marca, modelo, anio_fabricacion, capacidad_carga_kg, estado) VALUES
('WGT123','Chevrolet','NHR Reward',2019,2500.00,'Disponible'),
('WGT124','Hino','Serie 300 616',2020,3500.00,'Disponible'),
('WGT125','Isuzu','ELF 200',2018,2000.00,'Disponible'),
('WGT126','Foton','Aumark S 3.5',2021,3500.00,'Disponible'),
('WGT127','JAC','X200 Cabinado',2020,1800.00,'En Mantenimiento'),
('WGT128','Chevrolet','FRR Forward',2017,5500.00,'Disponible'),
('WGT129','Hyundai','HD65',2019,3000.00,'Disponible'),
('WGT130','Kia','K2700',2016,1200.00,'Disponible'),
('WGT131','Renault','Master Furgon',2022,1500.00,'Disponible'),
('WGT132','Mercedes-Benz','Sprinter 415',2021,1700.00,'Disponible'),
('WGT133','Chevrolet','N300 Max',2018,850.00,'Disponible'),
('WGT134','Volkswagen','Delivery 9.170',2020,8000.00,'Disponible'),
('WGT135','Foton','Aumark C 1039',2019,2200.00,'En Mantenimiento'),
('WGT136','Hino','Serie 500 1726',2021,9500.00,'Disponible'),
('WGT137','Isuzu','NPR 4 Ton',2018,4000.00,'Disponible'),
('WGT138','JMC','Carrying Plus',2020,1300.00,'Disponible'),
('WGT139','Nissan','NP300 Frontier',2019,1000.00,'Disponible'),
('WGT140','Chevrolet','NPR Reward',2022,4300.00,'Disponible'),
('WGT141','Hyundai','HD78',2020,4500.00,'Disponible'),
('WGT142','Fiat','Ducato Maxi Cargo',2021,1900.00,'Disponible'),
('WGT143','Peugeot','Boxer Furgon',2019,1600.00,'Disponible'),
('WGT144','Dongfeng','Captain C 3.5',2022,3500.00,'Disponible'),
('WGT145','Chevrolet','FTR Forward',2016,7500.00,'Disponible'),
('WGT146','Hino','Serie 300 816',2023,4200.00,'Disponible');

-- ---------------------- MANTENIMIENTOS (24) --------------------------
INSERT INTO mantenimientos (id_vehiculo, tipo, descripcion, fecha_inicio, fecha_fin, costo, taller) VALUES
(1,'Preventivo','Cambio de aceite y filtros','2026-01-15','2026-01-15',420000.00,'Serviteca La 33'),
(1,'Correctivo','Reemplazo de pastillas de freno delanteras','2026-03-02','2026-03-03',680000.00,'Taller Diesel BGA'),
(2,'Preventivo','Revision general de 20.000 km','2026-02-10','2026-02-11',950000.00,'Hino Autopista'),
(3,'Correctivo','Reparacion de caja de velocidades','2026-01-20','2026-01-28',3200000.00,'Mecanica Central'),
(4,'Preventivo','Rotacion y alineacion de llantas','2026-02-18','2026-02-18',260000.00,'Llantas del Norte'),
(5,'Correctivo','Cambio de embrague completo','2026-08-20',NULL,0.00,'Taller Diesel BGA'),
(6,'Preventivo','Sincronizacion de motor','2026-03-11','2026-03-12',780000.00,'Serviteca La 33'),
(7,'Preventivo','Cambio de aceite y refrigerante','2026-04-05','2026-04-05',510000.00,'Lubricentro Cabecera'),
(8,'Correctivo','Reparacion de sistema electrico','2026-02-25','2026-02-27',890000.00,'Electroauto Giron'),
(9,'Preventivo','Mantenimiento de frenos traseros','2026-05-09','2026-05-10',640000.00,'Taller Diesel BGA'),
(10,'Preventivo','Cambio de correa de reparticion','2026-04-22','2026-04-23',1150000.00,'Mecanica Central'),
(11,'Correctivo','Cambio de bomba de agua','2026-06-01','2026-06-02',730000.00,'Serviteca La 33'),
(12,'Preventivo','Revision de suspension','2026-05-30','2026-05-31',540000.00,'Suspension Total'),
(13,'Correctivo','Reparacion de furgon y latoneria','2026-08-22',NULL,0.00,'Latoneria El Sol'),
(14,'Preventivo','Cambio de filtros de aire y combustible','2026-06-14','2026-06-14',380000.00,'Lubricentro Cabecera'),
(15,'Preventivo','Revision de 40.000 km','2026-07-02','2026-07-03',1250000.00,'Isuzu Servicio'),
(16,'Correctivo','Cambio de bateria','2026-07-19','2026-07-19',450000.00,'Baterias Willard'),
(17,'Preventivo','Engrase y ajuste general','2026-07-25','2026-07-25',220000.00,'Serviteca La 33'),
(18,'Correctivo','Reparacion de direccion hidraulica','2026-03-28','2026-03-30',1420000.00,'Mecanica Central'),
(19,'Preventivo','Cambio de aceite de motor','2026-08-01','2026-08-01',430000.00,'Lubricentro Cabecera'),
(20,'Preventivo','Revision preoperacional anual','2026-08-05','2026-08-06',700000.00,'Taller Diesel BGA'),
(21,'Correctivo','Cambio de amortiguadores traseros','2026-06-20','2026-06-21',980000.00,'Suspension Total'),
(22,'Preventivo','Cambio de llantas delanteras','2026-08-10','2026-08-10',1600000.00,'Llantas del Norte'),
(23,'Correctivo','Reparacion de sistema de escape','2026-05-15','2026-05-16',520000.00,'Mecanica Central');

-- ------------- ASIGNACIONES CONDUCTOR - VEHICULO (24) ----------------
-- Cerradas (historial)
INSERT INTO asignaciones_conductor_vehiculo (id_conductor, id_vehiculo, fecha_inicio, fecha_fin) VALUES
(1, 3,'2026-01-05 06:00:00','2026-02-28 18:00:00'),
(2, 4,'2026-01-05 06:00:00','2026-02-28 18:00:00'),
(3, 5,'2026-01-08 06:00:00','2026-03-15 18:00:00'),
(4, 6,'2026-01-10 06:00:00','2026-03-20 18:00:00'),
(5, 7,'2026-01-12 06:00:00','2026-04-01 18:00:00'),
(6, 8,'2026-02-01 06:00:00','2026-04-30 18:00:00'),
(7, 9,'2026-02-05 06:00:00','2026-05-10 18:00:00'),
(8,10,'2026-02-10 06:00:00','2026-05-15 18:00:00'),
(9,11,'2026-03-01 06:00:00','2026-06-01 18:00:00'),
(10,12,'2026-03-05 06:00:00','2026-06-10 18:00:00'),
(11,13,'2026-04-01 06:00:00','2026-07-01 18:00:00'),
(12,14,'2026-04-10 06:00:00','2026-07-15 18:00:00');
-- Vigentes (fecha_fin NULL): un conductor por vehiculo y viceversa
INSERT INTO asignaciones_conductor_vehiculo (id_conductor, id_vehiculo, fecha_inicio, fecha_fin) VALUES
(1, 1,'2026-08-01 06:00:00',NULL),
(2, 2,'2026-08-01 06:00:00',NULL),
(3, 3,'2026-08-01 06:00:00',NULL),
(4, 4,'2026-08-01 06:00:00',NULL),
(5, 6,'2026-08-01 06:00:00',NULL),
(7, 7,'2026-08-02 06:00:00',NULL),
(8, 8,'2026-08-02 06:00:00',NULL),
(9, 9,'2026-08-02 06:00:00',NULL),
(11,10,'2026-08-03 06:00:00',NULL),
(12,11,'2026-08-03 06:00:00',NULL),
(13,12,'2026-08-03 06:00:00',NULL),
(15,14,'2026-08-04 06:00:00',NULL);


-- ================ MODULO 2: CLIENTES, PAQUETES Y RUTAS ================


-- ------------------------- CLIENTES (26) -----------------------------
INSERT INTO clientes (documento, nombre, telefono, email, direccion, ciudad) VALUES
('63500137','Andrea Morales Pineda','3178812311','andrea.pineda@correo.com','Transversal 66 # 76-25','Giron'),
('63500274','Carlos Restrepo Ayala','3188981982','carlos.ayala@correo.com','Avenida 102 # 24-13','Barranquilla'),
('63500411','Luisa Fernanda Gomez Rico','3143378927','luisa.gomez@correo.com','Calle 69 # 89-82','Bucaramanga'),
('63500548','Julian Ospina Tavera','3197646461','julian.tavera@correo.com','Transversal 84 # 95-79','Giron'),
('63500685','Maria Camila Diaz Ortega','3191251670','maria.diaz@correo.com','Avenida 9 # 8-5','Piedecuesta'),
('63500822','Jorge Enrique Pardo Lizarazo','3131504702','jorge.pardo@correo.com','Transversal 42 # 57-76','Piedecuesta'),
('63500959','Natalia Quintero Bautista','3184919923','natalia.bautista@correo.com','Diagonal 64 # 1-85','Floridablanca'),
('63501096','Sebastian Rojas Amaya','3175666799','sebastian.amaya@correo.com','Transversal 71 # 11-91','Barrancabermeja'),
('63501233','Paola Andrea Mejia Solano','3154853154','paola.mejia@correo.com','Avenida 37 # 4-9','Cali'),
('63501370','Ricardo Villamil Osorio','3117717594','ricardo.osorio@correo.com','Calle 109 # 38-50','Floridablanca'),
('63501507','Diana Carolina Nieto Prieto','3101009142','diana.nieto@correo.com','Carrera 27 # 7-61','Medellin'),
('63501644','Felipe Arango Bermudez','3168042931','felipe.bermudez@correo.com','Calle 73 # 81-26','Barrancabermeja'),
('63501781','Sandra Milena Cortes Rangel','3152462037','sandra.cortes@correo.com','Diagonal 43 # 2-53','Floridablanca'),
('63501918','Oscar Ivan Zapata Guzman','3125133703','oscar.zapata@correo.com','Calle 2 # 8-60','Barranquilla'),
('63502055','Laura Valentina Nunez Fajardo','3124160583','laura.nunez@correo.com','Transversal 66 # 25-94','Giron'),
('63502192','Mauricio Salazar Reyes','3167438286','mauricio.reyes@correo.com','Calle 51 # 54-28','Bucaramanga'),
('63502329','Claudia Patricia Rueda Silva','3146102708','claudia.rueda@correo.com','Calle 27 # 24-51','Cali'),
('63502466','Esteban Cardenas Molina','3202683304','esteban.molina@correo.com','Calle 19 # 28-57','Barrancabermeja'),
('63502603','Yuliana Marin Estupinan','3106518956','yuliana.estupinan@correo.com','Diagonal 50 # 10-10','Floridablanca'),
('63502740','Hernan Dario Pena Correa','3135077080','hernan.pena@correo.com','Calle 77 # 48-48','Cali'),
('63502877','Gabriela Suarez Contreras','3173134415','gabriela.contreras@correo.com','Avenida 62 # 74-18','Medellin'),
('63503014','Cristian Camilo Lozano Vera','3123585961','cristian.lozano@correo.com','Diagonal 117 # 30-79','Piedecuesta'),
('63503151','Marcela Bermudez Anaya','3133658781','marcela.anaya@correo.com','Avenida 26 # 88-50','Barranquilla'),
('63503288','Alejandro Trujillo Barrera','3192316974','alejandro.barrera@correo.com','Transversal 7 # 14-14','Bucaramanga'),
('63503425','Tatiana Guerrero Pabon','3185281187','tatiana.pabon@correo.com','Carrera 95 # 91-51','Barrancabermeja'),
('63503562','Nicolas Beltran Chacon','3169237604','nicolas.chacon@correo.com','Diagonal 67 # 23-93','Floridablanca');

-- ------------------------- PAQUETES (40) -----------------------------
-- 26 cerrados (Entregado/Devuelto) en las 14 rutas finalizadas,
-- 8 'Asignado a Ruta' en las 6 rutas planificadas y 6 'En Bodega' libres.
INSERT INTO paquetes (codigo_seguimiento, descripcion, peso_kg, largo_cm, ancho_cm, alto_cm, id_remitente, id_destinatario, direccion_origen, ciudad_origen, direccion_destino, ciudad_destino, estado, fecha_registro, fecha_entrega) VALUES
('RE-20260820-PBET4','Documentos legales',78.59,45.7,13.6,14.7,5,8,'Diagonal 68 # 74-17','Barranquilla','Calle 47 # 18-58','Cucuta','Entregado','2026-08-20 08:31:00','2026-08-31 13:13:00'),
('RE-20260820-YVCBE','Repuestos de motor',113.65,48.6,53.8,22.5,22,24,'Calle 10 # 58-70','Cucuta','Diagonal 95 # 6-95','Cali','Entregado','2026-08-20 08:57:00','2026-08-31 09:30:00'),
('RE-20260828-9A22A','Ropa infantil',82.37,58.7,14.8,40.2,23,5,'Diagonal 113 # 54-94','Bogota','Diagonal 50 # 95-89','Floridablanca','Entregado','2026-08-28 08:47:00','2026-08-30 09:55:00'),
('RE-20260829-8BQH9','Equipo medico',90.15,72.4,49.6,31.7,15,18,'Calle 48 # 39-19','Floridablanca','Avenida 26 # 67-22','Cucuta','Entregado','2026-08-29 08:58:00','2026-08-30 18:05:00'),
('RE-20260825-NP2QW','Libros escolares',39.57,26.8,18.1,24.3,11,22,'Calle 92 # 9-36','Barranquilla','Carrera 15 # 58-61','Piedecuesta','Entregado','2026-08-25 08:27:00','2026-08-31 15:51:00'),
('RE-20260824-CTCTY','Herramientas manuales',59.52,34.7,57.5,6.1,7,14,'Carrera 52 # 59-25','Medellin','Calle 99 # 35-31','Cucuta','Entregado','2026-08-24 08:59:00','2026-08-30 14:04:00'),
('RE-20260821-NN8SM','Electrodomestico pequeno',53.85,67.1,55.3,34.4,5,26,'Calle 23 # 29-35','Bucaramanga','Diagonal 70 # 90-67','Floridablanca','Entregado','2026-08-21 08:42:00','2026-08-30 10:43:00'),
('RE-20260826-2J67N','Insumos de oficina',101.93,60.0,64.6,25.7,20,25,'Avenida 84 # 65-44','Giron','Transversal 42 # 84-27','Medellin','Entregado','2026-08-26 08:16:00','2026-08-31 15:59:00'),
('RE-20260823-WSBYF','Alimentos no perecederos',29.45,13.0,30.5,28.2,24,26,'Diagonal 63 # 4-28','Floridablanca','Calle 55 # 5-23','Piedecuesta','Entregado','2026-08-23 08:44:00','2026-08-31 13:34:00'),
('RE-20260821-FQ6UL','Material publicitario',18.27,51.8,60.6,35.8,11,22,'Diagonal 86 # 49-79','Giron','Carrera 39 # 19-70','Barranquilla','Entregado','2026-08-21 08:43:00','2026-08-30 16:37:00'),
('RE-20260828-BC3H9','Componentes electronicos',49.62,17.5,20.0,56.2,9,19,'Transversal 53 # 52-35','Barranquilla','Carrera 61 # 64-17','Piedecuesta','Entregado','2026-08-28 08:31:00','2026-08-31 18:24:00'),
('RE-20260823-BCNKQ','Muestras de laboratorio',13.48,10.9,27.0,44.8,14,16,'Carrera 80 # 64-14','Cucuta','Transversal 94 # 75-16','Bogota','Entregado','2026-08-23 08:42:00','2026-08-31 13:08:00'),
('RE-20260829-LPPPJ','Calzado deportivo',105.51,28.6,54.4,49.6,20,9,'Carrera 41 # 78-41','Piedecuesta','Carrera 28 # 25-13','Cucuta','Entregado','2026-08-29 08:18:00','2026-08-30 15:52:00'),
('RE-20260823-JN3BG','Juguetes didacticos',46.85,26.1,50.9,55.1,8,5,'Diagonal 15 # 91-65','Floridablanca','Diagonal 65 # 88-25','Barrancabermeja','Entregado','2026-08-23 08:14:00','2026-08-30 17:50:00'),
('RE-20260820-MHPML','Cosmeticos',73.8,89.2,65.6,10.2,16,4,'Calle 18 # 88-60','Bucaramanga','Calle 98 # 13-42','Cucuta','Entregado','2026-08-20 08:35:00','2026-08-30 11:12:00'),
('RE-20260820-CNM47','Cableado estructurado',51.89,38.8,32.2,55.7,15,14,'Avenida 22 # 13-66','Cucuta','Calle 42 # 11-91','Bogota','Entregado','2026-08-20 08:50:00','2026-08-30 09:40:00'),
('RE-20260824-HL3JX','Cafe empacado',60.76,80.4,51.7,47.2,13,19,'Carrera 52 # 27-93','Cali','Carrera 10 # 44-39','Piedecuesta','Entregado','2026-08-24 08:40:00','2026-08-30 13:58:00'),
('RE-20260825-3ME29','Filtros industriales',6.42,61.4,52.1,46.9,4,1,'Avenida 3 # 15-79','Bogota','Calle 95 # 31-33','Cali','Entregado','2026-08-25 08:38:00','2026-08-30 14:13:00'),
('RE-20260829-TX4G6','Uniformes corporativos',77.94,74.8,14.6,37.9,13,17,'Calle 50 # 21-50','Bucaramanga','Transversal 22 # 64-70','Piedecuesta','Entregado','2026-08-29 08:49:00','2026-08-31 13:16:00'),
('RE-20260821-ZU8TU','Vidrio templado',50.77,63.1,54.9,6.5,20,2,'Carrera 75 # 6-82','Medellin','Carrera 53 # 97-88','Barranquilla','Entregado','2026-08-21 08:34:00','2026-08-30 18:19:00'),
('RE-20260824-8SRCG','Piezas de plomeria',70.02,85.0,53.4,30.8,2,11,'Carrera 78 # 91-15','Medellin','Calle 80 # 54-59','Bucaramanga','Entregado','2026-08-24 08:17:00','2026-08-31 12:27:00'),
('RE-20260824-47S4Z','Repuestos de bicicleta',18.99,72.2,41.8,59.9,7,2,'Diagonal 94 # 17-31','Bogota','Transversal 15 # 65-40','Cucuta','Entregado','2026-08-24 08:42:00','2026-08-30 11:42:00'),
('RE-20260820-RNRNC','Bateria de respaldo',82.36,60.3,10.6,19.3,20,12,'Calle 80 # 5-14','Barrancabermeja','Carrera 70 # 36-10','Cali','Entregado','2026-08-20 08:58:00','2026-08-31 13:48:00'),
('RE-20260824-88YPX','Papeleria institucional',28.55,37.5,54.7,51.1,3,22,'Calle 103 # 16-58','Giron','Avenida 108 # 99-99','Cucuta','Devuelto','2026-08-24 08:23:00',NULL),
('RE-20260824-KWUM5','Semillas certificadas',97.28,62.3,44.8,30.4,15,14,'Diagonal 11 # 70-11','Barrancabermeja','Transversal 73 # 73-63','Medellin','Devuelto','2026-08-24 08:53:00',NULL),
('RE-20260820-FV9WU','Panel solar portatil',37.56,80.5,23.1,41.1,15,10,'Diagonal 47 # 16-42','Bucaramanga','Transversal 73 # 77-88','Floridablanca','Devuelto','2026-08-20 08:27:00',NULL),
('RE-20260823-RFZZB','Cargador industrial',38.46,39.1,33.7,26.3,15,17,'Carrera 95 # 73-48','Barrancabermeja','Transversal 70 # 20-76','Barranquilla','Asignado a Ruta','2026-08-23 08:46:00',NULL),
('RE-20260826-DLUB5','Sillas plegables',97.52,15.1,42.8,48.3,6,3,'Calle 43 # 11-36','Barranquilla','Calle 116 # 41-11','Barrancabermeja','Asignado a Ruta','2026-08-26 08:11:00',NULL),
('RE-20260826-9X73Y','Kit de aseo',29.83,37.0,68.1,42.0,21,5,'Transversal 63 # 69-93','Floridablanca','Calle 91 # 80-6','Medellin','Asignado a Ruta','2026-08-26 08:36:00',NULL),
('RE-20260828-7BZAH','Impresora de tickets',96.81,43.7,35.2,13.0,25,22,'Carrera 82 # 49-88','Bogota','Carrera 42 # 27-50','Bucaramanga','Asignado a Ruta','2026-08-28 08:37:00',NULL),
('RE-20260828-VKMD7','Router empresarial',56.84,11.4,38.9,49.0,17,10,'Diagonal 14 # 26-24','Barrancabermeja','Diagonal 28 # 76-31','Bucaramanga','Asignado a Ruta','2026-08-28 08:48:00',NULL),
('RE-20260827-T3MV8','Tanque plastico',27.26,78.3,39.1,27.1,23,16,'Diagonal 85 # 71-99','Cali','Calle 62 # 29-52','Cucuta','Asignado a Ruta','2026-08-27 08:13:00',NULL),
('RE-20260825-DRA8Z','Malla de seguridad',95.47,46.0,24.8,28.2,7,5,'Carrera 54 # 21-99','Medellin','Diagonal 19 # 34-67','Cucuta','Asignado a Ruta','2026-08-25 08:18:00',NULL),
('RE-20260829-KMAPK','Extintores recargados',62.15,75.8,14.9,19.8,21,24,'Avenida 13 # 62-89','Cali','Transversal 11 # 96-76','Barrancabermeja','Asignado a Ruta','2026-08-29 08:44:00',NULL),
('RE-20260828-6PC49','Manguera de presion',50.33,22.6,59.9,22.1,17,10,'Diagonal 52 # 10-72','Bucaramanga','Diagonal 60 # 86-92','Piedecuesta','En Bodega','2026-08-28 08:14:00',NULL),
('RE-20260822-H6A28','Set de ollas',80.29,29.0,43.8,30.2,12,3,'Calle 110 # 93-27','Giron','Avenida 20 # 38-81','Floridablanca','En Bodega','2026-08-22 08:34:00',NULL),
('RE-20260821-FSNN2','Pintura acrilica',90.71,68.5,29.8,57.0,18,24,'Avenida 61 # 93-82','Barrancabermeja','Diagonal 58 # 49-12','Bogota','En Bodega','2026-08-21 08:16:00',NULL),
('RE-20260827-MN3C7','Bombillas LED',81.48,57.4,15.4,17.5,5,20,'Carrera 16 # 56-53','Floridablanca','Calle 44 # 55-66','Giron','En Bodega','2026-08-27 08:18:00',NULL),
('RE-20260823-A3WRB','Rodamientos',58.19,30.9,21.3,21.9,21,7,'Transversal 14 # 72-49','Giron','Carrera 66 # 43-38','Medellin','En Bodega','2026-08-23 08:16:00',NULL),
('RE-20260825-QYKNN','Correas transportadoras',92.1,55.4,11.6,41.3,20,23,'Calle 18 # 32-62','Giron','Avenida 54 # 90-43','Barrancabermeja','En Bodega','2026-08-25 08:31:00',NULL);

-- --------------------------- RUTAS (20) ------------------------------
-- 14 Finalizada (historial para los reportes de la Persona 3),
--  6 Planificada (listas para probar 'Iniciar ruta').
-- Restricciones uk_ruta_vehiculo_fecha / uk_ruta_conductor_fecha: por eso
-- cada pareja aparece a lo sumo una vez por fecha.
INSERT INTO rutas (codigo, fecha, id_vehiculo, id_conductor, estado, observaciones, iniciada_en, finalizada_en) VALUES
('RUT-20260828-1001','2026-08-28',1,1,'Finalizada','Reparto zona centro','2026-08-28 07:05:00','2026-08-28 17:55:00'),
('RUT-20260828-1002','2026-08-28',2,2,'Finalizada','Reparto zona norte','2026-08-28 07:17:00','2026-08-28 17:29:00'),
('RUT-20260828-1003','2026-08-28',3,3,'Finalizada','Reparto zona sur','2026-08-28 07:28:00','2026-08-28 17:46:00'),
('RUT-20260828-1004','2026-08-28',4,4,'Finalizada','Reparto zona occidente','2026-08-28 07:15:00','2026-08-28 17:36:00'),
('RUT-20260828-1005','2026-08-28',6,5,'Finalizada','Reparto zona norte','2026-08-28 07:34:00','2026-08-28 17:35:00'),
('RUT-20260828-1006','2026-08-28',7,7,'Finalizada','Reparto zona sur','2026-08-28 07:35:00','2026-08-28 17:16:00'),
('RUT-20260828-1007','2026-08-28',8,8,'Finalizada','Reparto zona sur','2026-08-28 07:21:00','2026-08-28 17:38:00'),
('RUT-20260828-1008','2026-08-28',9,9,'Finalizada','Reparto zona norte','2026-08-28 07:07:00','2026-08-28 17:52:00'),
('RUT-20260828-1009','2026-08-28',10,11,'Finalizada','Reparto zona centro','2026-08-28 07:18:00','2026-08-28 17:43:00'),
('RUT-20260828-1010','2026-08-28',11,12,'Finalizada','Reparto zona occidente','2026-08-28 07:15:00','2026-08-28 17:48:00'),
('RUT-20260828-1011','2026-08-28',12,13,'Finalizada','Reparto zona sur','2026-08-28 07:23:00','2026-08-28 17:53:00'),
('RUT-20260828-1012','2026-08-28',14,15,'Finalizada','Reparto zona occidente','2026-08-28 07:11:00','2026-08-28 17:38:00'),
('RUT-20260830-1013','2026-08-30',1,1,'Finalizada','Reparto zona norte','2026-08-30 07:04:00','2026-08-30 17:29:00'),
('RUT-20260830-1014','2026-08-30',2,2,'Finalizada','Reparto zona cabecera','2026-08-30 07:25:00','2026-08-30 17:11:00'),
('RUT-20260830-1015','2026-08-30',3,3,'Planificada','Reparto zona sur',NULL,NULL),
('RUT-20260830-1016','2026-08-30',4,4,'Planificada','Reparto zona centro',NULL,NULL),
('RUT-20260830-1017','2026-08-30',6,5,'Planificada','Reparto zona cabecera',NULL,NULL),
('RUT-20260830-1018','2026-08-30',7,7,'Planificada','Reparto zona cabecera',NULL,NULL),
('RUT-20260830-1019','2026-08-30',8,8,'Planificada','Reparto zona oriente',NULL,NULL),
('RUT-20260830-1020','2026-08-30',9,9,'Planificada','Reparto zona occidente',NULL,NULL);

-- ------------------- RUTA_PAQUETES (34 relaciones) -------------------
INSERT INTO ruta_paquetes (id_ruta, id_paquete, orden_entrega) VALUES
(1,1,1),
(1,2,2),
(2,3,1),
(2,4,2),
(3,5,1),
(3,6,2),
(4,7,1),
(4,8,2),
(5,9,1),
(5,10,2),
(6,11,1),
(6,12,2),
(7,13,1),
(7,14,2),
(8,15,1),
(8,16,2),
(9,17,1),
(9,18,2),
(10,19,1),
(10,20,2),
(11,21,1),
(11,22,2),
(12,23,1),
(12,24,2),
(13,25,1),
(14,26,1),
(15,27,1),
(15,28,2),
(16,29,1),
(16,30,2),
(17,31,1),
(18,32,1),
(19,33,1),
(20,34,1);

-- Verificacion: ninguna ruta debe exceder la capacidad de su vehiculo.
-- Esta consulta debe devolver 0 filas.
SELECT r.codigo, v.placa, v.capacidad_carga_kg, ROUND(SUM(p.peso_kg),2) AS peso
FROM rutas r JOIN ruta_paquetes rp ON rp.id_ruta = r.id_ruta
JOIN paquetes p ON p.id_paquete = rp.id_paquete
JOIN vehiculos v ON v.id_vehiculo = r.id_vehiculo
GROUP BY r.codigo, v.placa, v.capacidad_carga_kg
HAVING SUM(p.peso_kg) > v.capacidad_carga_kg;
