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
