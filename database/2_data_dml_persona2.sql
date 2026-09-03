-- =====================================================================
-- RapidExpress - DML PARCIAL (Persona 2: Paquetes y Rutas)
-- 20+ registros por entidad. Ejecutar DESPUES de:
--   1_schema_ddl_persona1.sql, 2_data_dml_persona1.sql, 1_schema_ddl_persona2.sql
-- Las rutas usan parejas vehiculo-conductor que YA tienen asignacion
-- vigente en el DML de la Persona 1.
-- =====================================================================
USE rapidexpress;

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
