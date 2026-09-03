-- =====================================================================
-- Migracion (Persona 1) - 2026-09-02
-- Agrega ON DELETE CASCADE a los FK de asignaciones_conductor_vehiculo.
--
-- Aplicar SOLO en bases que ya ejecutaron la version anterior de
-- 1_schema_ddl_persona1.sql (los FK tenian unicamente ON UPDATE CASCADE,
-- lo que hacia fallar el borrado de un vehiculo/conductor con historial).
-- En una BD creada desde cero con el DDL actualizado NO hace falta.
-- =====================================================================
USE rapidexpress;

ALTER TABLE asignaciones_conductor_vehiculo
    DROP FOREIGN KEY fk_asignacion_conductor;
ALTER TABLE asignaciones_conductor_vehiculo
    DROP FOREIGN KEY fk_asignacion_vehiculo;

ALTER TABLE asignaciones_conductor_vehiculo
    ADD CONSTRAINT fk_asignacion_conductor FOREIGN KEY (id_conductor)
        REFERENCES conductores(id_conductor) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE asignaciones_conductor_vehiculo
    ADD CONSTRAINT fk_asignacion_vehiculo FOREIGN KEY (id_vehiculo)
        REFERENCES vehiculos(id_vehiculo) ON DELETE CASCADE ON UPDATE CASCADE;
