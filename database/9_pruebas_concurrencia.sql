-- =====================================================================
-- Pruebas manuales de concurrencia (Persona 2) - para la sustentacion
--
-- Abrir DOS clientes MySQL y ejecutar en paralelo. Demuestra que el
-- SELECT ... FOR UPDATE bloquea de verdad y que el UPDATE condicional
-- impide que dos terminales pisen el mismo paquete.
-- =====================================================================
USE rapidexpress;

-- ---------------- TERMINAL A ----------------
-- START TRANSACTION;
-- SELECT id_paquete, codigo_seguimiento, estado FROM paquetes WHERE id_paquete = 27 FOR UPDATE;
-- -- dejar la transaccion abierta unos segundos
-- UPDATE paquetes SET estado = 'En Transito' WHERE id_paquete = 27 AND estado = 'Asignado a Ruta';
-- COMMIT;

-- ------- TERMINAL B (mientras A sigue abierta) -------
-- START TRANSACTION;
-- SELECT id_paquete, estado FROM paquetes WHERE id_paquete = 27 FOR UPDATE;  -- queda esperando a A
-- UPDATE paquetes SET estado = 'Devuelto' WHERE id_paquete = 27 AND estado = 'Asignado a Ruta';
-- -- 0 filas afectadas: A ya lo movio. En Java esto lanza
-- -- NegocioException("Otra terminal ya actualizo este paquete").
-- ROLLBACK;

-- --------------- Verificaciones de consistencia ---------------

-- 1) Ninguna ruta puede exceder la capacidad de su vehiculo (debe dar 0 filas)
SELECT r.codigo, v.placa, v.capacidad_carga_kg, ROUND(SUM(p.peso_kg),2) AS peso_asignado
FROM rutas r
JOIN ruta_paquetes rp ON rp.id_ruta = r.id_ruta
JOIN paquetes p       ON p.id_paquete = rp.id_paquete
JOIN vehiculos v      ON v.id_vehiculo = r.id_vehiculo
GROUP BY r.codigo, v.placa, v.capacidad_carga_kg
HAVING SUM(p.peso_kg) > v.capacidad_carga_kg;

-- 2) Ningun paquete puede estar en dos rutas activas (debe dar 0 filas)
SELECT rp.id_paquete, COUNT(*) AS rutas_activas
FROM ruta_paquetes rp
JOIN rutas r ON r.id_ruta = rp.id_ruta
WHERE r.estado IN ('Planificada','En Curso')
GROUP BY rp.id_paquete
HAVING COUNT(*) > 1;

-- 3) Coherencia entre rutas En Curso y el estado de la flota (debe dar 0 filas)
SELECT r.codigo, v.placa, v.estado AS estado_vehiculo, c.estado AS estado_conductor
FROM rutas r
JOIN vehiculos v   ON v.id_vehiculo = r.id_vehiculo
JOIN conductores c ON c.id_conductor = r.id_conductor
WHERE r.estado = 'En Curso' AND (v.estado <> 'En Ruta' OR c.estado <> 'En Ruta');
