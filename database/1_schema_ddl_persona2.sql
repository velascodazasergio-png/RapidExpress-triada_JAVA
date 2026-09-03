-- =====================================================================
-- RapidExpress - DDL PARCIAL (Persona 2: Paquetes y Rutas)
-- Motor: MySQL 8.0+
-- Este archivo se fusiona dentro de database/1_schema_ddl.sql
--
-- ORDEN OBLIGATORIO: se ejecuta DESPUES de 1_schema_ddl_persona1.sql,
-- porque "rutas" tiene llaves foraneas hacia vehiculos y conductores.
--
-- Convencion de nombres: se sigue la misma que la Persona 1
-- (PK id_<tabla>, FK con el mismo nombre de la PK referenciada, estados
-- guardados con su etiqueta legible: 'En Bodega', 'En Transito', ...).
-- =====================================================================
USE rapidexpress;

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
