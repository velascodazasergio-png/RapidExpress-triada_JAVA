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
